import {
  Alert,
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
import { showSuccessBar } from "../../constants/snack-bar";
import axios from "axios";

class ForgotComponent extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      email: "",
      isButtonClicked: false,
      errorMessage: "",
    };
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  async handleSubmit(e) {
    e.preventDefault();
    this.setState({ isButtonClicked: true, errorMessage: "" });
    try {
      const response = await axios.post(
        `${process.env.REACT_APP_USER_SERVICE_URL}/forgot-password`,
        { email: this.state.email }
      );

      if (response.status === 200) {
        showSuccessBar(response.data.message);
        this.setState({ email: "", isButtonClicked: false });
      }
    } catch (error) {
      this.setState({
        errorMessage:
          error.response?.data.error || "An error occurred. Please try again.",
        isButtonClicked: false,
      });
    }
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
            {this.state.errorMessage !== "" && (
              <Alert className="main-panel-error" severity="error">
                {this.state.errorMessage}
              </Alert>
            )}
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
                  Send Link to Email
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
