import React from "react";
import "@testing-library/jest-dom";
import { render, fireEvent, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import ResetPage from "../../pages/reset-page";
import axios from "axios";

jest.mock("axios");

describe("ResetComponent", () => {
  const navigate = jest.fn();
  const mainProps = {
    navigate: navigate,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  const renderResetPage = () =>
    render(
      <MemoryRouter initialEntries={["/reset"]}>
        <Routes>
          <Route path="/reset" element={<ResetPage {...mainProps} />} />
        </Routes>
      </MemoryRouter>
    );

  it("renders Reset Password Button", () => {
    renderResetPage();
    expect(
      screen.getByRole("button", { name: "Reset Password" })
    ).toBeInTheDocument();
  });

  it("toggles password visibility", () => {
    renderResetPage();
    const button = screen.getAllByLabelText("toggle password visibility")[0];
    fireEvent.click(button);
    expect(button).toBeInTheDocument();
  });

  it("updates confirm password input state", () => {
    renderResetPage();
    const confirmPasswordInput = screen.getByLabelText(/Retype password/i);
    fireEvent.change(confirmPasswordInput, { target: { value: "Passw0rd!1" } });
    expect(confirmPasswordInput.value).toBe("Passw0rd!1");
  });

  it("renders toggle password visibility", () => {
    renderResetPage();
    const toggleButton = screen.getAllByLabelText(
      /toggle password visibility/i
    )[0];
    fireEvent.click(toggleButton);
    expect(toggleButton).toBeInTheDocument();
  });

  it("renders retype password input field", () => {
    renderResetPage();
    expect(screen.getByLabelText(/Retype password/i)).toBeInTheDocument();
  });

  it("handles success from reset-password API", async () => {
    axios.post.mockResolvedValueOnce({ data: { success: true } });
    renderResetPage();
    const passwordInput = screen.getAllByLabelText(/Password/i)[0];
    const confirmPasswordInput = screen.getByLabelText(/Retype password/i);
    fireEvent.change(passwordInput, { target: { value: "Passw0rd!" } });
    fireEvent.change(confirmPasswordInput, { target: { value: "Passw0rd!" } });
    fireEvent.click(screen.getByRole("button", { name: "Reset Password" }));
    await waitFor(() => {
      expect(axios.post).toHaveBeenCalled();
    });
  });

  it("shows error message from reset-password API", async () => {
    axios.post.mockRejectedValueOnce({
      response: { data: { error: "Invalid url or token expired" } },
    });
    renderResetPage();
    const passwordInput = screen.getAllByLabelText(/Password/i)[0];
    const confirmPasswordInput = screen.getByLabelText(/Retype password/i);
    fireEvent.change(passwordInput, { target: { value: "Passw0rd!" } });
    fireEvent.change(confirmPasswordInput, { target: { value: "Passw0rd!" } });
    fireEvent.click(screen.getByRole("button", { name: "Reset Password" }));
    await waitFor(() => {
      expect(
        screen.getByText("Invalid url or token expired")
      ).toBeInTheDocument();
    });
  });
});
