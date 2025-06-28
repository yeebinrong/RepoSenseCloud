import { initialLoginPageState, withParams } from "./constants";
import React from "react";

describe("loginPageInitialState", () => {
  afterEach(() => {
    jest.resetModules();
    jest.clearAllMocks();
  });

  it("returns correct default values", () => {
    expect(initialLoginPageState).toEqual({
      username: "",
      email: "",
      password: "",
      confirmPassword: "",
      errorMessage: "",
      showPassword: false,
      showConfirmPassword: false,
      isButtonClicked: false,
    });
  });

  it("returns correct number of properties", () => {
    const keys = [
      "username",
      "email",
      "password",
      "confirmPassword",
      "errorMessage",
      "showPassword",
      "showConfirmPassword",
      "isButtonClicked",
    ];
    keys.forEach((key) => {
      expect(initialLoginPageState).toHaveProperty(key);
    });
  });

  it("injects params prop into wrapped component", () => {
    const params = { id: "user" };
    jest.mock("react-router-dom", () => ({
      ...jest.requireActual("react-router-dom"),
      useParams: () => params,
    }));
    const component = (props) => <div>{props.params.id}</div>;
    const Wrapped = withParams(component);
    expect(typeof Wrapped).toBe("function");
  });
});
