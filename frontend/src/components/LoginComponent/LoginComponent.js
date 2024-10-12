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
import LockIcon from "@mui/icons-material/Lock";
import { initialLoginPageState } from "../../constants/constants";

class LoginComponent extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      ...initialLoginPageState,
    };
  }

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
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              {target === "email" ? <PersonIcon /> : <LockIcon />}
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
        <img
          draggable={false}
          src="/static/hamburger_logo.png"
          className="main-logo"
          alt=""
        />
        <Typography className={"login-panel-main-title"}>
          Login to your account
        </Typography>
        <Typography className={"login-panel-sub-title"}>
          Enter to continue and explore within your grasp
        </Typography>
        {this.state.errorMessage !== "" && (
          <Alert className="main-panel-error" severity="error">
            {this.state.errorMessage}
          </Alert>
        )}
        {this.renderTextField(true, "email", this.state.email, "Email Address")}
        {this.renderTextField(
          true,
          "password",
          this.state.password,
          "Password",
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
              marginLeft: "225px",
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

  render() {
    return (
      <div className="login-container">
        <form onSubmit={this.handleSubmit} className={"from-login-panel"}>
          <FormControl className={"form-login-control"}>
            {this.renderLoginForm()}
          </FormControl>
        </form>
      </div>
    );
  }
}

export default LoginComponent;
