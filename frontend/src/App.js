import React from "react";
import "./App.scss";
import HomePage from "./pages/home-page";
import ErrorPage from "./pages/error-page";
import { Navigate, Route, Routes, useNavigate } from "react-router-dom";

if (typeof setImmediate === "undefined") {
  window.setImmediate = function (fn) {
    return setTimeout(fn, 0);
  };
}

const App = () => {
  let navigate = useNavigate();

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
