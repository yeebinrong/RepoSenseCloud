import React, { useEffect, useRef } from "react";
import "./App.scss";
import LoginPage from "./pages/login-page";
import ForgotPage from "./pages/forgot-page";
import ResetPage from "./pages/reset-page";
import HomePage from "./pages/home-page";
import ErrorPage from "./pages/error-page";
import CreateJobPage from "./pages/create-job-page";
import { Navigate, Route, Routes, useNavigate } from "react-router-dom";
import axios from "axios";

if (typeof setImmediate === "undefined") {
  window.setImmediate = function (fn) {
    return setTimeout(fn, 0);
  };
}

const App = () => {
  let navigate = useNavigate();
  const usernameRef = useRef();

  useEffect(() => {
    const token = localStorage.getItem("token");
    const publicRoutes = ["/login", "/register", "/forgot", "/reset"];

    if (!token && !publicRoutes.includes(window.location.pathname)) {
      navigate("/login");
    } else if (token) {
      const validateUser = async () => {
        try {
          const { data } = await axios.post(
            `${process.env.REACT_APP_USER_SERVICE_URL}/auth`,
            { token }
          );
          if (data.username) {
            usernameRef.current = data.username;
            navigate("/home");
          }
        } catch (error) {
          console.error(
            "Token validation error:",
            error.response?.status === 401
              ? "Invalid or expired token"
              : "An error has occurred"
          );
          localStorage.removeItem("token");
          navigate("/login");
        }
      };

      validateUser();
    }
  }, [navigate]);

  const mainProps = {
    navigate: navigate,
    username: usernameRef.current,
  };

  return (
    <Routes>
      <Route path="" exact element={<Navigate replace to="/login" />} />
      <Route path="/login" exact element={<LoginPage {...mainProps} />} />
      <Route
        path="/register"
        exact
        element={<LoginPage {...mainProps} isRegisterPage />}
      />
      <Route path="/forgot" exact element={<ForgotPage {...mainProps} />} />
      <Route path="/reset" exact element={<ResetPage {...mainProps} />} />
      <Route path="/home" exact element={<HomePage {...mainProps} />} />
      {/* Below is a temp route to test create job TODO: remove this */}
      {/* <Route path="/create-job" element={<CreateJobPage {...mainProps} />} /> */}
      {<Route path="*" element={<ErrorPage {...mainProps} />} />}
    </Routes>
  );
};

export default App;
