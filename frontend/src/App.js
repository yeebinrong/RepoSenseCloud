import React from "react";
import "./App.scss";
import BasePage from "./pages/base-page";

if (typeof setImmediate === "undefined") {
  window.setImmediate = function (fn) {
    return setTimeout(fn, 0);
  };
}

const App = () => {
  return <BasePage />;
};

export default App;
