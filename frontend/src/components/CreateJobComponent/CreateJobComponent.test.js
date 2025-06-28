/**
 * @jest-environment jsdom
 */
import "@testing-library/jest-dom";
import React from "react";
import { render, fireEvent, waitFor, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import CreateJobComponent from "./CreateJobComponent";
import axios from "axios";

// Mock dependencies
jest.mock("axios");
jest.mock("../../constants/snack-bar", () => ({
    showSuccessBar: jest.fn(),
    showErrorBar: jest.fn(),
}));
jest.mock("moment-timezone", () => {
    const moment = jest.requireActual("moment");
    moment.tz = () => moment();
    moment.tz.setDefault = () => {};
    return moment;
});

// Mock localStorage
beforeAll(() => {
    Object.defineProperty(window, "localStorage", {
        value: {
            getItem: jest.fn(() => "test-token"),
            setItem: jest.fn(),
            removeItem: jest.fn(),
            clear: jest.fn(),
        },
        writable: true,
    });
});

// Mock window.confirm
beforeEach(() => {
    window.confirm = jest.fn(() => true);
    jest.clearAllMocks();
});

describe("CreateJobComponent", () => {
    const defaultProps = {};
//ok
    it("renders and opens modal in create mode", () => {
        render(<CreateJobComponent {...defaultProps} />);
        expect(screen.getByText("Create Job")).toBeInTheDocument();
        fireEvent.click(screen.getByText("Create Job"));
        expect(screen.getByText("Create a Job")).toBeInTheDocument();
        expect(screen.getByText("Job Name")).toBeInTheDocument();
        expect(screen.getByText("Target Repository")).toBeInTheDocument();
    });
//ok
    it("disables add/delete repo link in edit mode", async () => {
        const jobData = {
            jobId: "123",
            jobName: "Edit Job",
            repoLink: "https://github.com/test/repo",
            period: "",
            sinceDate: "01/01/2023",
            untilDate: "31/01/2023",
            originalityThreshold: 0.5,
            timeZone: "UTC+08",
            authorship: true,
            prevAuthors: false,
            shallowClone: false,
            ignoreFileSizeLimit: false,
            addLastMod: false,
            formatChipValues: ["js"],
            jobType: "manual",
            frequency: "",
            startMinute: "--",
            startHour: "--",
            startDate: "",
            endDate: "",
            status: "Idle",
        };
        render(<CreateJobComponent mode="edit" jobData={jobData} open={true} />);
        const addBtn = screen.getByTestId("add-repo-button");
        expect(addBtn).toBeDisabled();
        expect(screen.queryByText("✕")).not.toBeInTheDocument();
    });
//ok
    it("toggles checkboxes and updates state", async () => {
        render(<CreateJobComponent />);
        fireEvent.click(screen.getByText("Create Job"));
        const authorshipCheckbox = screen.getByTestId("authorship-checkbox");
        fireEvent.click(authorshipCheckbox);
        expect(authorshipCheckbox).toBeChecked();
    });
//ok
    it("shows error if time zone is not selected", async () => {
        const user = userEvent.setup();
        render(<CreateJobComponent />);
        
        fireEvent.click(screen.getByText("Create Job"));
        
        // For MUI TextField, use within() to scope queries to the container
        const jobNameContainer = screen.getByTestId("job-name-input");
        const jobNameInput = within(jobNameContainer).getByRole('textbox');
        await user.clear(jobNameInput);
        await user.type(jobNameInput, "Test Job");
        
        const repoContainer = screen.getByTestId("repo-link-input-0");
        const repoInput = within(repoContainer).getByRole('textbox');
        await user.clear(repoInput);
        await user.type(repoInput, "https://github.com/test/repo");
        
        fireEvent.change(screen.getByTestId("timezone-dropdown"), { target: { value: "" } });
        fireEvent.click(screen.getByText("Next"));
        expect(await screen.findByText("Please select a time zone")).toBeInTheDocument();
    });
//ok
    it("shows error if start hour/minute not selected for scheduled job", async () => {
        const user = userEvent.setup();
        render(<CreateJobComponent />);
        
        fireEvent.click(screen.getByText("Create Job"));
        
        const jobNameInput = within(screen.getByTestId("job-name-input")).getByRole('textbox');
        await user.clear(jobNameInput);
        await user.type(jobNameInput, "Test Job");
        
        const repoInput = within(screen.getByTestId("repo-link-input-0")).getByRole('textbox');
        await user.clear(repoInput);
        await user.type(repoInput, "https://github.com/test/repo");
        
        fireEvent.click(screen.getByText("Next"));
        await screen.findByTestId("job-type-select");
        
        fireEvent.mouseDown(screen.getByLabelText("Job Type"));
        fireEvent.click(screen.getByText("Scheduled"));
        
        fireEvent.click(screen.getByText("Save"));
        await waitFor(() => {
            const startHourDropdown = screen.getByTestId("start-hour-dropdown");
            expect(startHourDropdown).toHaveClass("error");
        });
    });

    it("shows error for invalid date range", async () => {
        const user = userEvent.setup();
        render(<CreateJobComponent {...defaultProps} />);
        
        fireEvent.click(screen.getByText("Create Job"));
        
        const jobNameInput = within(screen.getByTestId("job-name-input")).getByRole('textbox');
        await user.clear(jobNameInput);
        await user.type(jobNameInput, "Test Job");
        
        const repoInput = within(screen.getByTestId("repo-link-input-0")).getByRole('textbox');
        await user.clear(repoInput);
        await user.type(repoInput, "https://github.com/test/repo");
        
        // For MUI date inputs, use userEvent with the actual input element
        const sinceDateInput = within(screen.getByTestId("since-date-input")).getByRole('textbox');
        await user.clear(sinceDateInput);
        await user.type(sinceDateInput, "2023-12-31");
        
        const untilDateInput = within(screen.getByTestId("until-date-input")).getByRole('textbox');
        await user.clear(untilDateInput);
        await user.type(untilDateInput, "2023-01-01");
        
        fireEvent.click(screen.getByText("Next"));
        expect(await screen.findByText("Improper Date Range")).toBeInTheDocument();
    });

    it("shows error for originality threshold out of bounds", async () => {
        const user = userEvent.setup();
        render(<CreateJobComponent {...defaultProps} />);
        
        fireEvent.click(screen.getByText("Create Job"));
        
        const jobNameInput = within(screen.getByTestId("job-name-input")).getByRole('textbox');
        await user.clear(jobNameInput);
        await user.type(jobNameInput, "Test Job");
        
        const repoInput = within(screen.getByTestId("repo-link-input-0")).getByRole('textbox');
        await user.clear(repoInput);
        await user.type(repoInput, "https://github.com/test/repo");
        
        const thresholdInput = within(screen.getByTestId("originality-threshold-input")).getByRole('textbox');
        await user.clear(thresholdInput);
        await user.type(thresholdInput, "1.5");
        
        fireEvent.click(screen.getByText("Next"));
        expect(await screen.findByText("Input between 0.0 to 1.0")).toBeInTheDocument();
    });

    it("submits form successfully in create mode", async () => {
        const user = userEvent.setup();
        axios.post.mockResolvedValue({ status: 201 });
        render(<CreateJobComponent {...defaultProps} />);
        
        fireEvent.click(screen.getByText("Create Job"));
        
        const jobNameInput = within(screen.getByTestId("job-name-input")).getByRole('textbox');
        await user.clear(jobNameInput);
        await user.type(jobNameInput, "Test Job");
        
        const repoInput = within(screen.getByTestId("repo-link-input-0")).getByRole('textbox');
        await user.clear(repoInput);
        await user.type(repoInput, "https://github.com/test/repo");
        
        fireEvent.click(screen.getByText("Next"));
        await screen.findByText("Job Type");
        fireEvent.click(screen.getByText("Save"));
        
        await waitFor(() => {
            expect(require("../../constants/snack-bar").showSuccessBar).toHaveBeenCalled();
        });
    });

    it("shows error bar on failed submission", async () => {
        const user = userEvent.setup();
        axios.post.mockRejectedValue(new Error("Failed"));
        render(<CreateJobComponent {...defaultProps} />);
        
        fireEvent.click(screen.getByText("Create Job"));
        
        const jobNameInput = within(screen.getByTestId("job-name-input")).getByRole('textbox');
        await user.clear(jobNameInput);
        await user.type(jobNameInput, "Test Job");
        
        const repoInput = within(screen.getByTestId("repo-link-input-0")).getByRole('textbox');
        await user.clear(repoInput);
        await user.type(repoInput, "https://github.com/test/repo");
        
        fireEvent.click(screen.getByText("Next"));
        await screen.findByText("Job Type");
        fireEvent.click(screen.getByText("Save"));
        
        await waitFor(() => {
            expect(require("../../constants/snack-bar").showErrorBar).toHaveBeenCalled();
        });
    });

    it("renders in edit mode with jobData and submits update", async () => {
        axios.patch.mockResolvedValue({ status: 200 });
        const jobData = {
            jobId: "123",
            jobName: "Edit Job",
            repoLink: "https://github.com/test/repo",
            period: "",
            sinceDate: "01/01/2023",
            untilDate: "31/01/2023",
            originalityThreshold: 0.5,
            timeZone: "UTC+08",
            authorship: true,
            prevAuthors: false,
            shallowClone: false,
            ignoreFileSizeLimit: false,
            addLastMod: false,
            formatChipValues: ["js"],
            jobType: "manual",
            frequency: "",
            startMinute: "--",
            startHour: "--",
            startDate: "",
            endDate: "",
            status: "Idle",
        };
        render(<CreateJobComponent mode="edit" jobData={jobData} open={true} />);
        
        // For MUI TextField with value, you can check the container
        const jobNameInput = within(screen.getByTestId("job-name-input")).getByDisplayValue("Edit Job");
        expect(jobNameInput).toBeInTheDocument();
        
        fireEvent.click(screen.getByText("Next"));
        await screen.findByText("Job Type");
        fireEvent.click(screen.getByText("Update"));
        await waitFor(() => {
            expect(require("../../constants/snack-bar").showSuccessBar).toHaveBeenCalled();
        });
    });

    it("handles scheduled job fields", async () => {
        const user = userEvent.setup();
        render(<CreateJobComponent {...defaultProps} />);
        
        fireEvent.click(screen.getByText("Create Job"));
        
        const jobNameInput = within(screen.getByTestId("job-name-input")).getByRole('textbox');
        await user.clear(jobNameInput);
        await user.type(jobNameInput, "Test Job");
        
        const repoInput = within(screen.getByTestId("repo-link-input-0")).getByRole('textbox');
        await user.clear(repoInput);
        await user.type(repoInput, "https://github.com/test/repo");
        
        fireEvent.click(screen.getByText("Next"));
        await screen.findByText("Job Type");
        fireEvent.mouseDown(screen.getByLabelText("Job Type"));
        fireEvent.click(screen.getByText("Scheduled"));
        expect(screen.getByText("Frequency:")).toBeInTheDocument();
        
        // Native dropdowns work with fireEvent.change
        fireEvent.change(screen.getByTestId("start-hour-dropdown"), { target: { value: "01" } });
        fireEvent.change(screen.getByTestId("start-minute-dropdown"), { target: { value: "05" } });
        
        // For MUI date inputs, use userEvent with the actual input element
        const startDateInput = within(screen.getByTestId("start-date-input")).getByRole('textbox');
        await user.clear(startDateInput);
        await user.type(startDateInput, "2023-01-01");
        
        const endDateInput = within(screen.getByTestId("end-date-input")).getByRole('textbox');
        await user.clear(endDateInput);
        await user.type(endDateInput, "2023-01-31");
    });
});