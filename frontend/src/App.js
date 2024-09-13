import React, { useEffect } from "react";
import "./App.scss";
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
    const isHomePage = window.location.pathname === "/home";

    if (isHomePage) {
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
      <Route path="*" element={<ErrorPage {...mainProps} />} />
    </Routes>
  );
};

export default App;
