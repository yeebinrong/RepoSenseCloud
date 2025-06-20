import {
  Button,
  FormControl,
  InputAdornment,
  TextField,
  Typography,
} from "@mui/material";
import React from "react";
import "./ForgotComponent.scss";
import NavigateButton from "../NavigateButton";
import EmailIcon from "@mui/icons-material/Email";

class ForgotComponent extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      email: "",
      isButtonClicked: false,
    };
  }

  render() {
    return (
      <div className="forgot-container">
        <form onSubmit={this.handleSubmit} className={"from-forgot-panel"}>
          <FormControl className={"form-forgot-control"}>
            <img
              draggable={false}
              src="/static/hamburger_logo.png"
              className="main-logo"
              alt=""
            />
            <Typography className={"forgot-panel-main-title"}>
              Forgot Password
            </Typography>
            <Typography className={"forgot-panel-sub-title"}>
              Enter your email to reset your password
            </Typography>
            <TextField
              size="medium"
              className="forgot-panel-text-field"
              onChange={(e) => this.setState({ email: e.target.value })}
              value={this.state.email}
              label={"Email Address"}
              type={"text"}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <EmailIcon />
                  </InputAdornment>
                ),
              }}
              required
            />
            <div
              style={{
                display: "flex",
                flexDirection: "column",
                margin: "0 30px",
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
                  className={"forgot-panel-main-button"}
                  variant="contained"
                >
                  Reset Password
                </Button>
              </div>
              <inline
                style={{
                  display: "flex",
                  justifyContent: "center",
                  alignItems: "baseline",
                  marginTop: "12px",
                  color: "#7F5305",
                  fontfamily: "DM Sans",
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
                        fontfamily: "DM Sans",
                        fontWeight: "bold",
                      }}
                    >
                      Sign up
                    </inline>
                  }
                />
              </inline>
            </div>
          </FormControl>
        </form>
      </div>
    );
  }
}

export default ForgotComponent;
