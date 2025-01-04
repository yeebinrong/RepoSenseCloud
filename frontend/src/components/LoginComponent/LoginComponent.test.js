import React from "react";
import "@testing-library/jest-dom";
import { render } from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import LoginPage from "../../pages/login-page";

describe("LoginComponent", () => {
  const navigate = jest.fn();
  const mainProps = {
    navigate: navigate,
  };

  it("renders Login Page Title", () => {
    const { getByText } = render(
      <MemoryRouter initialEntries={["/login"]}>
        <Routes>
          <Route path="/login" element={<LoginPage {...mainProps} />} />
        </Routes>
      </MemoryRouter>
    );

    expect(getByText("Login to your account")).toBeInTheDocument();
  });

  it("renders Register Page Title", () => {
    const { getByText } = render(
      <MemoryRouter initialEntries={["/register"]}>
        <Routes>
          <Route
            path="/register"
            element={<LoginPage {...mainProps} isRegisterPage />}
          />
        </Routes>
      </MemoryRouter>
    );

    expect(getByText("Create your account")).toBeInTheDocument();
  });
});
