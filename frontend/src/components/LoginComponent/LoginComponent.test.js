import React from "react";
import "@testing-library/jest-dom";
import { render, fireEvent, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import LoginPage from "../../pages/login-page";
import axios from "axios";

const TEST_USERNAME = "name";
const TEST_PASSWORD = "Password!1"; // NOSONAR
const TEST_INVALID_PASSWORD = "Password!2"; // NOSONAR
const TEST_EMAIL = "name@email.com";

jest.mock("axios");

const renderWithRoute = ({
  route = "/login",
  isRegisterPage = false,
  extraRoutes = null,
  props = {},
} = {}) => {
  const mainProps = { navigate: jest.fn(), ...props };
  return render(
    <MemoryRouter initialEntries={[route]}>
      <Routes>
        <Route path="/login" element={<LoginPage {...mainProps} />} />
        <Route
          path="/register"
          element={<LoginPage {...mainProps} isRegisterPage />}
        />
        {extraRoutes}
      </Routes>
    </MemoryRouter>
  );
};

const fillLoginForm = (username, password) => {
  fireEvent.change(screen.getByLabelText(/Username/i), {
    target: { value: username },
  });
  fireEvent.change(screen.getAllByLabelText(/Password/i)[0], {
    target: { value: password },
  });
};

const submitLogin = () => {
  fireEvent.click(screen.getByRole("button", { name: "Log in to continue" }));
};

const fillRegisterForm = (username, email, password, retypePassword) => {
  fireEvent.change(screen.getByLabelText(/Username/i), {
    target: { value: username },
  });
  fireEvent.change(screen.getByLabelText(/Email/i), {
    target: { value: email },
  });
  fireEvent.change(screen.getAllByLabelText(/Password/i)[0], {
    target: { value: password },
  });
  fireEvent.change(screen.getByLabelText(/Retype password/i), {
    target: { value: retypePassword },
  });
};

const submitRegister = () => {
  fireEvent.click(screen.getByRole("button", { name: "Sign up" }));
};

describe("LoginComponent", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders Login Page Title", () => {
    renderWithRoute();
    expect(screen.getByText("Login to your account")).toBeInTheDocument();
  });

  it("navigates to Register Page", () => {
    renderWithRoute();
    const registerButton = screen.getByRole("button", {
      name: "Sign up",
    });
    fireEvent.click(registerButton);
    expect(screen.getByText("Create your account")).toBeInTheDocument();
  });

  it("shows error on empty login field", () => {
    renderWithRoute();
    submitLogin();
    const usernameInput = screen.getByLabelText(/Username/i);
    expect(usernameInput).toBeInvalid();
  });

  it("submits login form with valid username", () => {
    renderWithRoute();
    const usernameInput = screen.getByLabelText(/Username/i);
    fireEvent.change(usernameInput, { target: { value: TEST_USERNAME } });
    expect(usernameInput.value).toBe(TEST_USERNAME);
    submitLogin();
  });

  it("handles valid credentails from login API", async () => {
    const token = "mocked_token";
    axios.post.mockResolvedValueOnce({ data: { token } });
    renderWithRoute();
    fillLoginForm(TEST_USERNAME, TEST_PASSWORD);
    submitLogin();
    await waitFor(() => {
      expect(axios.post).toHaveBeenCalledWith(
        `${process.env.REACT_APP_USER_SERVICE_URL}/login`,
        { userName: TEST_USERNAME, password: TEST_PASSWORD },
        expect.objectContaining({ headers: expect.any(Object) })
      );
    });
  });

  it("handles invalid credentails from login API", async () => {
    axios.post.mockRejectedValueOnce({ response: { status: 400 } });
    renderWithRoute();
    fillLoginForm(TEST_USERNAME, TEST_INVALID_PASSWORD);
    submitLogin();
    await waitFor(() => {
      expect(
        screen.getByText("Invalid username or password")
      ).toBeInTheDocument();
    });
  });

  it("renders Register Page Title", () => {
    renderWithRoute({ route: "/register", isRegisterPage: true });
    expect(screen.getByText("Create your account")).toBeInTheDocument();
  });

  it("navigates to Login Page", () => {
    renderWithRoute({ route: "/register", isRegisterPage: true });
    const loginButton = screen.getByRole("button", {
      name: "Log in",
    });
    fireEvent.click(loginButton);
    expect(screen.getByText("Login to your account")).toBeInTheDocument();
  });

  it("shows error on empty register field", () => {
    renderWithRoute({ route: "/register", isRegisterPage: true });
    submitRegister();
    const usernameInput = screen.getByLabelText(/Username/i);
    expect(usernameInput).toBeInvalid();
  });

  it("submits register form with valid username", () => {
    renderWithRoute({ route: "/register", isRegisterPage: true });
    const usernameInput = screen.getByLabelText(/Username/i);
    fireEvent.change(usernameInput, { target: { value: TEST_USERNAME } });
    expect(usernameInput.value).toBe(TEST_USERNAME);
    submitRegister();
  });

  it("toggles password visibility", () => {
    renderWithRoute({ route: "/register", isRegisterPage: true });
    const button = screen.getAllByLabelText("toggle password visibility")[0];
    fireEvent.click(button);
    expect(button).toBeInTheDocument();
  });

  it("handles success from register API", async () => {
    const token = "mocked_token";
    axios.post.mockResolvedValueOnce({ data: { token } });
    renderWithRoute({ route: "/register", isRegisterPage: true });
    fillRegisterForm(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD);
    submitRegister();
    await waitFor(() => {
      expect(axios.post).toHaveBeenCalledWith(
        `${process.env.REACT_APP_USER_SERVICE_URL}/register`,
        { userName: TEST_USERNAME, email: TEST_EMAIL, password: TEST_PASSWORD },
        expect.objectContaining({ headers: expect.any(Object) })
      );
    });
  });

  it("handles user already exists from register API", async () => {
    axios.post.mockRejectedValueOnce({ response: { status: 409 } });
    renderWithRoute({ route: "/register", isRegisterPage: true });
    fillRegisterForm(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD);
    submitRegister();
    await waitFor(() => {
      expect(screen.getByText("User already exists")).toBeInTheDocument();
    });
  });
});
