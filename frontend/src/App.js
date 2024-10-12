import React, { useEffect } from "react";
import "./App.scss";
import LoginPage from "./pages/login-page";
import ForgotPage from "./pages/forgot-page";
import HomePage from "./pages/home-page";
import ErrorPage from "./pages/error-page";
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
    const isLoginPage = window.location.pathname === "/login";

    if (isLoginPage) {
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
      <Route path="" exact element={<Navigate replace to="/login" />} />
      <Route path="/login" exact element={<LoginPage {...mainProps} />} />
      <Route path="/forgot" exact element={<ForgotPage {...mainProps} />} />
      <Route path="/home" exact element={<HomePage {...mainProps} />} />
      <Route path="*" element={<ErrorPage {...mainProps} />} />
    </Routes>
  );
};

export default App;
