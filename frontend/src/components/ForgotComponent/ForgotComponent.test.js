import React from "react";
import "@testing-library/jest-dom";
import { render } from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import ForgotPage from "../../pages/forgot-page";

describe("ForgotComponent", () => {
  const navigate = jest.fn();
  const mainProps = {
    navigate: navigate,
  };

  it("renders Forgot Page Title", () => {
    const { getByText } = render(
      <MemoryRouter initialEntries={["/forgot"]}>
        <Routes>
          <Route path="/forgot" element={<ForgotPage {...mainProps} />} />
        </Routes>
      </MemoryRouter>
    );

    expect(getByText("Forgot Password")).toBeInTheDocument();
  });
});
