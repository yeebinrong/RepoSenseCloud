import React from "react";
import "@testing-library/jest-dom";
import { render, fireEvent, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import LoginPage from "../../pages/login-page";
import axios from "axios";

jest.mock("axios");

describe("LoginComponent", () => {
  const navigate = jest.fn();
  const mainProps = {
    navigate: navigate,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders Login Page Title", () => {
    render(
      <MemoryRouter initialEntries={["/login"]}>
        <Routes>
          <Route path="/login" element={<LoginPage {...mainProps} />} />
        </Routes>
      </MemoryRouter>
    );
    expect(screen.getByText("Login to your account")).toBeInTheDocument();
  });

  it("navigates to Register Page", () => {
    render(
      <MemoryRouter initialEntries={["/login"]}>
        <Routes>
          <Route path="/login" element={<LoginPage {...mainProps} />} />
          <Route
            path="/register"
            element={<LoginPage {...mainProps} isRegisterPage />}
          />
        </Routes>
      </MemoryRouter>
    );
    const registerButton = screen.getByRole("button", {
      name: "Sign up",
    });
    fireEvent.click(registerButton);
    expect(screen.getByText("Create your account")).toBeInTheDocument();
  });

  it("shows error on empty login field", () => {
    render(
      <MemoryRouter initialEntries={["/login"]}>
        <Routes>
          <Route path="/login" element={<LoginPage {...mainProps} />} />
        </Routes>
      </MemoryRouter>
    );
    const submitButton = screen.getByRole("button", {
      name: "Log in to continue",
    });
    fireEvent.click(submitButton);
    const usernameInput = screen.getByLabelText(/Username/i);
    expect(usernameInput).toBeInvalid();
  });

  it("submits login form with valid username", () => {
    render(
      <MemoryRouter initialEntries={["/login"]}>
        <Routes>
          <Route path="/login" element={<LoginPage {...mainProps} />} />
        </Routes>
      </MemoryRouter>
    );
    const usernameInput = screen.getByLabelText(/Username/i);
    const submitButton = screen.getByRole("button", {
      name: "Log in to continue",
    });
    fireEvent.change(usernameInput, { target: { value: "name" } });
    expect(usernameInput.value).toBe("name");
    fireEvent.click(submitButton);
  });

  it("handles valid credentails from login API", async () => {
    const token = "mocked_token";
    axios.post.mockResolvedValueOnce({ data: { token } });
    render(
      <MemoryRouter initialEntries={["/login"]}>
        <Routes>
          <Route path="/login" element={<LoginPage {...mainProps} />} />
        </Routes>
      </MemoryRouter>
    );
    fireEvent.change(screen.getByLabelText(/Username/i), {
      target: { value: "name" },
    });
    fireEvent.change(screen.getAllByLabelText(/Password/i)[0], {
      target: { value: "Password1!" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Log in to continue" }));
    await waitFor(() => {
      expect(axios.post).toHaveBeenCalledWith(
        `${process.env.REACT_APP_USER_SERVICE_URL}/login`,
        { userName: "name", password: "Password1!" },
        expect.objectContaining({ headers: expect.any(Object) })
      );
    });
  });

  it("handles invalid credentails from login API", async () => {
    axios.post.mockRejectedValueOnce({ response: { status: 400 } });
    render(
      <MemoryRouter initialEntries={["/login"]}>
        <Routes>
          <Route path="/login" element={<LoginPage {...mainProps} />} />
        </Routes>
      </MemoryRouter>
    );
    fireEvent.change(screen.getByLabelText(/Username/i), {
      target: { value: "name" },
    });
    fireEvent.change(screen.getAllByLabelText(/Password/i)[0], {
      target: { value: "Password2!" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Log in to continue" }));
    await waitFor(() => {
      expect(
        screen.getByText("Invalid username or password")
      ).toBeInTheDocument();
    });
  });

  it("renders Register Page Title", () => {
    render(
      <MemoryRouter initialEntries={["/register"]}>
        <Routes>
          <Route
            path="/register"
            element={<LoginPage {...mainProps} isRegisterPage />}
          />
        </Routes>
      </MemoryRouter>
    );
    expect(screen.getByText("Create your account")).toBeInTheDocument();
  });

  it("navigates to Login Page", () => {
    render(
      <MemoryRouter initialEntries={["/register"]}>
        <Routes>
          <Route
            path="/register"
            element={<LoginPage {...mainProps} isRegisterPage />}
          />
          <Route path="/login" element={<LoginPage {...mainProps} />} />
        </Routes>
      </MemoryRouter>
    );
    const loginButton = screen.getByRole("button", {
      name: "Log in",
    });
    fireEvent.click(loginButton);
    expect(screen.getByText("Login to your account")).toBeInTheDocument();
  });

  it("shows error on empty register field", () => {
    render(
      <MemoryRouter initialEntries={["/register"]}>
        <Routes>
          <Route
            path="/register"
            element={<LoginPage {...mainProps} isRegisterPage />}
          />
        </Routes>
      </MemoryRouter>
    );
    const submitButton = screen.getByRole("button", { name: "Sign up" });
    fireEvent.click(submitButton);
    const usernameInput = screen.getByLabelText(/Username/i);
    expect(usernameInput).toBeInvalid();
  });

  it("submits register form with valid username", () => {
    render(
      <MemoryRouter initialEntries={["/register"]}>
        <Routes>
          <Route
            path="/register"
            element={<LoginPage {...mainProps} isRegisterPage />}
          />
        </Routes>
      </MemoryRouter>
    );
    const usernameInput = screen.getByLabelText(/Username/i);
    const submitButton = screen.getByRole("button", {
      name: "Sign up",
    });
    fireEvent.change(usernameInput, { target: { value: "name" } });
    expect(usernameInput.value).toBe("name");
    fireEvent.click(submitButton);
  });

  it("toggles password visibility", () => {
    render(
      <MemoryRouter initialEntries={["/register"]}>
        <Routes>
          <Route
            path="/register"
            element={<LoginPage {...mainProps} isRegisterPage />}
          />
        </Routes>
      </MemoryRouter>
    );
    const button = screen.getAllByLabelText("toggle password visibility")[0];
    fireEvent.click(button);
    expect(button).toBeInTheDocument();
  });

  it("handles success from register API", async () => {
    const token = "mocked_token";
    axios.post.mockResolvedValueOnce({ data: { token } });
    render(
      <MemoryRouter initialEntries={["/register"]}>
        <Routes>
          <Route
            path="/register"
            element={<LoginPage {...mainProps} isRegisterPage />}
          />
        </Routes>
      </MemoryRouter>
    );
    fireEvent.change(screen.getByLabelText(/Username/i), {
      target: { value: "name" },
    });
    fireEvent.change(screen.getByLabelText(/Email/i), {
      target: { value: "name@email.com" },
    });
    fireEvent.change(screen.getAllByLabelText(/Password/i)[0], {
      target: { value: "Password1!" },
    });
    fireEvent.change(screen.getByLabelText(/Retype password/i), {
      target: { value: "Password1!" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Sign up" }));
    await waitFor(() => {
      expect(axios.post).toHaveBeenCalledWith(
        `${process.env.REACT_APP_USER_SERVICE_URL}/register`,
        { userName: "name", email: "name@email.com", password: "Password1!" },
        expect.objectContaining({ headers: expect.any(Object) })
      );
    });
  });

  it("handles user already exists from register API", async () => {
    axios.post.mockRejectedValueOnce({ response: { status: 409 } });
    render(
      <MemoryRouter initialEntries={["/register"]}>
        <Routes>
          <Route
            path="/register"
            element={<LoginPage {...mainProps} isRegisterPage />}
          />
        </Routes>
      </MemoryRouter>
    );
    fireEvent.change(screen.getByLabelText(/Username/i), {
      target: { value: "name" },
    });
    fireEvent.change(screen.getByLabelText(/Email/i), {
      target: { value: "name@email.com" },
    });
    fireEvent.change(screen.getAllByLabelText(/Password/i)[0], {
      target: { value: "Password1!" },
    });
    fireEvent.change(screen.getByLabelText(/Retype password/i), {
      target: { value: "Password1!" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Sign up" }));
    await waitFor(() => {
      expect(screen.getByText("User already exists")).toBeInTheDocument();
    });
  });
});
