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
import "./ResetComponent.scss";
import NavigateButton from "../NavigateButton";
import VisibilityOffIcon from "@mui/icons-material/VisibilityOff";
import VisibilityIcon from "@mui/icons-material/Visibility";
import LockIcon from "@mui/icons-material/Lock";
import { showSuccessBar } from "../../constants/snack-bar";
import axios from "axios";

class ResetComponent extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      errorMessage: "",
      passwordErrorMessage: "",
      confirmPasswordErrorMessage: "",
      email: "",
      token: "",
    };
  }

  componentDidMount() {
    // Extract email and token from URL query params
    const params = new URLSearchParams(window.location.search);
    const email = params.get("email") || "";
    const token = params.get("token") || "";
    this.setState({ email, token });
  }

  validatePassword = (password) => {
    const regexUppercase = /[A-Z]/;
    const regexLowercase = /[a-z]/;
    const regexDigit = /\d/;
    const regexSpecialChar = /[~!@#$%^&*()]/;
    const validationErrors = [];

    if (password.length > 0) {
      if (password.length < 8) {
        validationErrors.push("Must be a minimum of 8 characters in length");
      }
      if (!regexUppercase.test(password)) {
        validationErrors.push("Must contain at least 1 uppercase letter");
      }
      if (!regexLowercase.test(password)) {
        validationErrors.push("Must contain at least 1 lowercase letter");
      }
      if (!regexDigit.test(password)) {
        validationErrors.push("Must contain at least 1 digit");
      }
      if (!regexSpecialChar.test(password)) {
        validationErrors.push(
          "Must contain at least 1 special character ~!@#$%^&*()"
        );
      }

      if (validationErrors.length > 0) {
        return validationErrors;
      }
    }
    return null;
  };

  validateConfirmPassword = (password, confirmPassword) => {
    if (password !== confirmPassword && confirmPassword.length > 0) {
      return ["Passwords did not match"];
    }
    return null;
  };

  handleSubmit = async (e) => {
    e.preventDefault();
    this.setState({
      errorMessage: "",
      passwordErrorMessage: "",
      confirmPasswordErrorMessage: "",
      isButtonClicked: true,
    });

    const { email, token, password, confirmPassword } = this.state;

    const passwordErrorMessage = this.validatePassword(password);
    const confirmPasswordErrorMessage = this.validateConfirmPassword(
      password,
      confirmPassword
    );

    if (passwordErrorMessage || confirmPasswordErrorMessage) {
      this.setState({
        passwordErrorMessage: passwordErrorMessage,
        confirmPasswordErrorMessage: confirmPasswordErrorMessage,
        isButtonClicked: false,
      });
      return;
    }

    try {
      const response = await axios.post(
        `${process.env.REACT_APP_USER_SERVICE_URL}/reset-password`,
        {
          email: email,
          token: token,
          newPassword: password,
        }
      );
      if (response.status === 200) {
        showSuccessBar(response.data.message);
        this.setState({ errorMessage: "", isButtonClicked: false });
        this.props.navigate("/login");
      }
    } catch (error) {
      this.setState({
        errorMessage:
          error.response?.data.error || "An error occurred. Please try again.",
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

  renderShowPasswordIcon = (target) => (
    <InputAdornment position="end">
      <IconButton
        disableRipple
        aria-label="toggle password visibility"
        onClick={() => this.inverseState(target)}
        onMouseDown={(e) => e.preventDefault()}
        edge="end"
      >
        {this.state[target] ? <VisibilityOffIcon /> : <VisibilityIcon />}
      </IconButton>
    </InputAdornment>
  );

  renderTextField = ({
    target,
    value,
    label,
    errorMessage,
    showPassword,
    showPassTarget,
  }) => (
    <>
      <TextField
        size="medium"
        className="reset-panel-text-field"
        onChange={(e) => this.updateState(target, e.target.value)}
        value={value}
        label={label}
        type={showPassTarget && !showPassword ? "password" : "text"}
        error={!!errorMessage}
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              {(target === "password" || target === "confirmPassword") && <LockIcon />}
            </InputAdornment>
          ),
          endAdornment: showPassTarget ? this.renderShowPasswordIcon(showPassTarget) : "",
        }}
        required
      />
      {Array.isArray(errorMessage) &&
        errorMessage.map((message) => (
          <Alert key={message} className="reset-input-error-message" severity="error">
            {message}
          </Alert>
        ))}
    </>
  );

  renderResetForm = () => {
    const passwordFieldProps = {
      target: "password",
      value: this.state.password,
      label: "Password",
      errorMessage: this.state.passwordErrorMessage,
      showPassword: this.state.showPassword,
      showPassTarget: "showPassword",
    };
    const confirmPasswordFieldProps = {
      target: "confirmPassword",
      value: this.state.confirmPassword,
      label: "Retype password",
      errorMessage: this.state.confirmPasswordErrorMessage,
      showPassword: this.state.showConfirmPassword,
      showPassTarget: "showConfirmPassword",
    };
    return (
      <>
        {this.renderTextField(passwordFieldProps)}
        {this.renderTextField(confirmPasswordFieldProps)}
        <div className="reset-flex-column">
          <div className="reset-center-button">
            <Button
              type="submit"
              size="large"
              disabled={this.state.isButtonClicked}
              className={"reset-panel-main-button"}
              variant="contained"
            >
              Reset Password
            </Button>
          </div>
          <span className="reset-inline-text">
            Already have an account?
            <NavigateButton
              url="/login"
              disableRipple
              style={{ backgroundColor: "transparent" }}
              text={<span className="reset-login-text">Log in</span>}
            />
          </span>
        </div>
      </>
    );
  };

  render() {
    return (
      <div className="reset-container">
        <form onSubmit={this.handleSubmit} className={"from-reset-panel"}>
          <FormControl className={"form-reset-control"}>
            <img
              draggable={false}
              src="/static/hamburger_logo.png"
              className="main-logo"
              alt=""
            />
            <Typography className={"reset-panel-main-title"}>
              Reset Password
            </Typography>
            <Typography className={"reset-panel-sub-title"}>
              Enter your new secure password
            </Typography>
            {this.state.errorMessage !== "" && (
              <Alert className="reset-panel-error" severity="error">
                {this.state.errorMessage}
              </Alert>
            )}
            {this.renderResetForm()}
          </FormControl>
        </form>
      </div>
    );
  }
}

export default ResetComponent;
