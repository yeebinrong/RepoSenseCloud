import {
  Alert,
  Button,
  FormControl,
  IconButton,
  InputAdornment,
  TextField,
  Typography,
} from "@mui/material";
import React from "react";
import "./LoginComponent.scss";
import NavigateButton from "../NavigateButton";
import VisibilityOffIcon from "@mui/icons-material/VisibilityOff";
import VisibilityIcon from "@mui/icons-material/Visibility";
import PersonIcon from "@mui/icons-material/Person";
import EmailIcon from "@mui/icons-material/Email";
import LockIcon from "@mui/icons-material/Lock";
import { initialLoginPageState } from "../../constants/constants";
import { showSuccessBar } from "../../constants/snack-bar";

class LoginComponent extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      ...initialLoginPageState,
      isRegisterPage: this.props.isRegisterPage,
    };
  }

  static getDerivedStateFromProps(nextProps, prevState) {
    if (prevState.isRegisterPage !== nextProps.isRegisterPage) {
      return {
        ...initialLoginPageState,
        isRegisterPage: nextProps.isRegisterPage,
      };
    }
    // Return null to indicate no change to state.
    return null;
  }

  validateEmail = (email) => {
    const regex = /[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!regex.test(email) && email.length > 0) {
      return "Please enter a valid email address";
    }
    return null;
  };

  validatePassword = (password) => {
    const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]*$/;
    if (password.length > 0) {
      if (password.length < 8) {
        return "Must contain at least 8 or more characters";
      }
      if (!regex.test(password)) {
        return "Must contain a mix of letters and numbers";
      }
    }
    return null;
  };

  validateConfirmPassword = (password, confirmPassword) => {
    if (password !== confirmPassword && confirmPassword.length > 0) {
      return "Passwords did not match";
    }
    return null;
  };

  handleSubmit = async (e) => {
    e.preventDefault();
    this.setState({
      errorMessage: "",
      emailErrorMessage: "",
      passwordErrorMessage: "",
      confirmPasswordErrorMessage: "",
      isButtonClicked: true,
    });

    const { username, email, password, confirmPassword, isRegisterPage } =
      this.state;

    const emailErrorMessage = this.validateEmail(email);
    const passwordErrorMessage = this.validatePassword(password);
    const confirmPasswordErrorMessage = this.validateConfirmPassword(
      password,
      confirmPassword
    );

    if (
      emailErrorMessage ||
      passwordErrorMessage ||
      confirmPasswordErrorMessage
    ) {
      this.setState({
        emailErrorMessage: emailErrorMessage,
        passwordErrorMessage: passwordErrorMessage,
        confirmPasswordErrorMessage: confirmPasswordErrorMessage,
        isButtonClicked: false,
      });
      return;
    }

    const url = isRegisterPage
      ? "http://localhost:3001/api/user/register"
      : "http://localhost:3001/api/user/login";
    const body = isRegisterPage
      ? JSON.stringify({ userName: username, email: email, password: password })
      : JSON.stringify({ userName: username, password: password });

    try {
      const response = await fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: body,
        credentials: "include",
      });

      if (!response.ok) {
        const errorMessage =
          response.status === 409
            ? "User already exists"
            : "Invalid username or password";
        this.setState({
          errorMessage: errorMessage,
          isButtonClicked: false,
        });
        return;
      }

      if (!isRegisterPage) {
        showSuccessBar("Welcome " + username + "!");
        this.props.navigate("/home");
      } else {
        showSuccessBar("User Registered Successfully!");
        this.props.navigate("/login");
      }
    } catch (error) {
      this.setState({
        errorMessage: "An error occurred. Please try again.",
        isButtonClicked: false,
      });
    }
  };

  updateState = (target, value) => {
    this.setState({
      [target]: value,
    });
  };

  inverseState = (target) => {
    this.setState((prevState) => {
      return {
        ...prevState,
        [target]: !prevState[target],
      };
    });
  };

  renderShowPasswordIcon = (target) => {
    return (
      <InputAdornment position="end">
        <IconButton
          disableRipple
          aria-label="toggle password visibility"
          onClick={() => this.inverseState(target)}
          onMouseDown={(e) => e.preventDefault}
          edge="end"
        >
          {this.state[target] ? <VisibilityOffIcon /> : <VisibilityIcon />}
        </IconButton>
      </InputAdornment>
    );
  };

  renderTextField = (
    isStandard,
    target,
    value,
    label,
    errorMessage,
    showPassword,
    showPassTarget
  ) => {
    return (
      <TextField
        size="medium"
        className={
          isStandard ? "standard-panel-text-field" : "helper-text-field"
        }
        onChange={(e) => this.updateState(target, e.target.value)}
        value={value}
        label={label}
        type={showPassTarget && !showPassword ? "password" : "text"}
        error={!!errorMessage}
        helperText={errorMessage}
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              {target === "username" && <PersonIcon />}
              {target === "email" && <EmailIcon />}
              {(target === "password" || target === "confirmPassword") && (
                <LockIcon />
              )}
            </InputAdornment>
          ),
          endAdornment: showPassTarget
            ? this.renderShowPasswordIcon(showPassTarget)
            : "",
        }}
        required
      />
    );
  };

  renderLoginForm = () => {
    return (
      <>
        {this.renderTextField(
          true,
          "username",
          this.state.username,
          "Username"
        )}
        {this.renderTextField(
          true,
          "password",
          this.state.password,
          "Password",
          this.state.passwordErrorMessage,
          this.state.showPassword,
          "showPassword"
        )}
        <div
          style={{
            display: "flex",
            flexDirection: "column",
          }}
        >
          <NavigateButton
            url="/forgot"
            disableRipple
            style={{
              backgroundColor: "transparent",
              color: "#F7A81B",
              fontWeight: "bold",
              width: "145px",
              marginLeft: "240px",
              justifyContent: "end",
            }}
            text="Forgot password?"
          />
          <div
            style={{
              marginTop: "12px",
              display: "flex",
              width: "100%",
              justifyContent: "center",
            }}
          >
            <Button
              type="submit"
              size="large"
              disabled={this.state.isButtonClicked}
              className={"login-panel-main-button"}
              variant="contained"
            >
              Log in to continue
            </Button>
          </div>
          <inline
            style={{
              display: "flex",
              justifyContent: "center",
              alignItems: "baseline",
              marginTop: "12px",
              color: "#7F5305",
            }}
          >
            Don't have an account?
            <NavigateButton
              url="/register"
              disableRipple
              style={{
                backgroundColor: "transparent",
              }}
              text={
                <inline
                  style={{
                    color: "#F7A81B",
                    fontWeight: "bold",
                  }}
                >
                  Sign up
                </inline>
              }
            />
          </inline>
        </div>
      </>
    );
  };

  renderRegisterForm = () => {
    return (
      <>
        {this.renderTextField(
          true,
          "username",
          this.state.username,
          "Username",
          null
        )}
        {this.renderTextField(
          true,
          "email",
          this.state.email,
          "Email Address",
          this.state.emailErrorMessage
        )}
        {this.renderTextField(
          true,
          "password",
          this.state.password,
          "Password",
          this.state.passwordErrorMessage,
          this.state.showPassword,
          "showPassword"
        )}
        {this.renderTextField(
          true,
          "confirmPassword",
          this.state.confirmPassword,
          "Retype password",
          this.state.confirmPasswordErrorMessage,
          this.state.showConfirmPassword,
          "showConfirmPassword"
        )}
        <div
          style={{
            display: "flex",
            flexDirection: "column",
          }}
        >
          <div
            style={{
              marginTop: "12px",
              display: "flex",
              width: "100%",
              justifyContent: "center",
            }}
          >
            <Button
              type="submit"
              size="large"
              disabled={this.state.isButtonClicked}
              className={"login-panel-main-button"}
              variant="contained"
            >
              Sign up
            </Button>
          </div>
          <inline
            style={{
              display: "flex",
              justifyContent: "center",
              alignItems: "baseline",
              marginTop: "12px",
              color: "#7F5305",
            }}
          >
            Already have an account?
            <NavigateButton
              url="/login"
              disableRipple
              style={{
                backgroundColor: "transparent",
              }}
              text={
                <inline
                  style={{
                    color: "#F7A81B",
                    fontWeight: "bold",
                  }}
                >
                  Log in
                </inline>
              }
            />
          </inline>
        </div>
      </>
    );
  };

  render() {
    return (
      <div className="login-container">
        <form onSubmit={this.handleSubmit} className={"from-login-panel"}>
          <FormControl className={"form-login-control"}>
            <img
              draggable={false}
              src="/static/hamburger_logo.png"
              className="main-logo"
              alt=""
            />
            <Typography className={"login-panel-main-title"}>
              {this.props.isRegisterPage
                ? "Create your account"
                : "Login to your account"}
            </Typography>
            <Typography className={"login-panel-sub-title"}>
              {this.props.isRegisterPage
                ? "Let's create an account and start a wonderful journey"
                : "Enter to continue and explore within your grasp"}
            </Typography>
            {this.state.errorMessage !== "" && (
              <Alert className="main-panel-error" severity="error">
                {this.state.errorMessage}
              </Alert>
            )}
            {this.props.isRegisterPage
              ? this.renderRegisterForm()
              : this.renderLoginForm()}
          </FormControl>
        </form>
      </div>
    );
  }
}

export default LoginComponent;
