import React, { useEffect } from "react";
import "./App.scss";
import HomePage from "./pages/home-page";
import ErrorPage from "./pages/error-page";
import CreateJobPage from "./pages/create-job-page";
import { Navigate, Route, Routes, useNavigate } from "react-router-dom";
import { showSuccessBar } from "./constants/snack-bar";

if (typeof setImmediate === "undefined") {
  window.setImmediate = function (fn) {
    return setTimeout(fn, 0);
  };
}

const App = () => {
  let navigate = useNavigate();

  useEffect(() => {
    const isHomePage = window.location.pathname === "/home";
    const isCreateJobPage = window.location.pathname === "/create-job";

    if (isHomePage || isCreateJobPage) {
      showSuccessBar("Welcome to ReposenseCloud.");
    } else {
      navigate("/error");
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const mainProps = {
    navigate: navigate,
  };

  return (
    <Routes>
      <Route path="" exact element={<Navigate replace to="/home" />} />
      <Route path="/home" exact element={<HomePage {...mainProps} />} />
      {/* Below is a temp route to test create job TODO: remove this */}
      <Route path="/create-job" element={<CreateJobPage {...mainProps} />} />
      {<Route path="*" element={<ErrorPage {...mainProps} />} />}
    </Routes>
  );
};

export default App;
