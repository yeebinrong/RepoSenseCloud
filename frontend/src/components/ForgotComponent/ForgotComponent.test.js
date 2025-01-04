import React from "react";
import "@testing-library/jest-dom";
import { render } from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import ForgotPage from "../../pages/forgot-page";

describe("LoginComponent", () => {
  const navigate = jest.fn();
  const mainProps = {
    navigate: navigate,
  };

  it("renders Login Page Title", () => {
    const { getByText } = render(
      <MemoryRouter initialEntries={["/login"]}>
        <Routes>
          <Route path="/login" element={<ForgotPage {...mainProps} />} />
        </Routes>
      </MemoryRouter>
    );

    expect(getByText("Forgot Password")).toBeInTheDocument();
  });
});
