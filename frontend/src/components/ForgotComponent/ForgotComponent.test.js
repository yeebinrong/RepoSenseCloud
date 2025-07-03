import React from "react";
import "@testing-library/jest-dom";
import { render, fireEvent, screen } from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import ForgotPage from "../../pages/forgot-page";
import axios from "axios";
import * as snackBar from "../../constants/snack-bar";

describe("ForgotComponent", () => {
  const navigate = jest.fn();
  const mainProps = {
    navigate: navigate,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders Forgot Page Title", () => {
    render(
      <MemoryRouter initialEntries={["/forgot"]}>
        <Routes>
          <Route path="/forgot" element={<ForgotPage {...mainProps} />} />
        </Routes>
      </MemoryRouter>
    );
    const forgotTitle = screen.getByText("Forgot Password");
    expect(forgotTitle).toBeInTheDocument();
  });

  it("renders email input field", () => {
    render(
      <MemoryRouter initialEntries={["/forgot"]}>
        <Routes>
          <Route path="/forgot" element={<ForgotPage {...mainProps} />} />
        </Routes>
      </MemoryRouter>
    );
    const emailInput = screen.getByLabelText(/Email Address/i);
    expect(emailInput).toBeInTheDocument();
  });

  it("renders form submit button", () => {
    render(
      <MemoryRouter initialEntries={["/forgot"]}>
        <Routes>
          <Route path="/forgot" element={<ForgotPage {...mainProps} />} />
        </Routes>
      </MemoryRouter>
    );
    const submitButton = screen.getByRole("button", {
      name: "Send Link to Email",
    });
    expect(submitButton).toBeInTheDocument();
  });

  it("submit form with valid email", () => {
    render(
      <MemoryRouter initialEntries={["/forgot"]}>
        <Routes>
          <Route path="/forgot" element={<ForgotPage {...mainProps} />} />
        </Routes>
      </MemoryRouter>
    );
    const emailInput = screen.getByLabelText(/Email Address/i);
    const submitButton = screen.getByRole("button", {
      name: "Send Link to Email",
    });
    fireEvent.change(emailInput, { target: { value: "name@email.com" } });
    expect(emailInput.value).toBe("name@email.com");
    fireEvent.click(submitButton);
  });

  it("shows error message from forgot-password API", async () => {
    jest.spyOn(axios, "post").mockRejectedValueOnce({
      response: { data: { error: "Invalid email format" } },
    });
    render(
      <MemoryRouter initialEntries={["/forgot"]}>
        <Routes>
          <Route path="/forgot" element={<ForgotPage {...mainProps} />} />
        </Routes>
      </MemoryRouter>
    );
    const emailInput = screen.getByLabelText(/Email Address/i);
    const submitButton = screen.getByRole("button", {
      name: "Send Link to Email",
    });
    fireEvent.change(emailInput, { target: { value: "name@email" } });
    fireEvent.click(submitButton);
    const error = await screen.findByText("Invalid email format");
    expect(error).toBeInTheDocument();
  });

  it("shows success message from forgot-password API", async () => {
    jest.spyOn(axios, "post").mockResolvedValueOnce({
      status: 200,
      data: { message: "Reset password email is sent" },
    });
    const showSuccessBarSpy = jest.spyOn(snackBar, "showSuccessBar");
    render(
      <MemoryRouter initialEntries={["/forgot"]}>
        <Routes>
          <Route path="/forgot" element={<ForgotPage {...mainProps} />} />
        </Routes>
      </MemoryRouter>
    );
    const emailInput = screen.getByLabelText(/Email Address/i);
    const submitButton = screen.getByRole("button", {
      name: "Send Link to Email",
    });
    fireEvent.change(emailInput, { target: { value: "name@email.com" } });
    fireEvent.click(submitButton);
    await screen.findByLabelText(/Email Address/i);
    expect(showSuccessBarSpy).toHaveBeenCalledWith(
      "Reset password email is sent"
    );
  });

  it("shows error if email input is empty", async () => {
    render(
      <MemoryRouter initialEntries={["/forgot"]}>
        <Routes>
          <Route path="/forgot" element={<ForgotPage {...mainProps} />} />
        </Routes>
      </MemoryRouter>
    );
    const submitButton = screen.getByRole("button", {
      name: "Send Link to Email",
    });
    fireEvent.click(submitButton);
    const emailInput = screen.getByLabelText(/Email Address/i);
    expect(emailInput).toBeInvalid();
  });
});
