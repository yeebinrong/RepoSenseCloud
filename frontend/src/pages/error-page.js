import { Button, Grid2, Paper, styled } from "@mui/material";
import React from "react";
import BasePage from "./base-page";

export const Item = styled(Paper)(({ theme }) => ({
  backgroundColor: "#fff",
  ...theme.typography.body2,
  color: theme.palette.text.secondary,
}));

class ErrorPage extends React.Component {
  render() {
    return (
      <BasePage
        navigate={this.props.navigate}
        component={() => {
          return (
            <div style={{ fontSize: "30px", margin: "32px 184px 0px 184px" }}>
              <Grid2 xs style={{ position: "relative" }}>
                <Item style={{ padding: "16px" }}>
                  <div className={"app-error-container"}>
                    An error has occured.
                    <img
                      draggable={false}
                      src=""
                      className={"app-error"}
                      alt=""
                    />
                    <Button
                      className={"app-error-button"}
                      onClick={() => {
                        this.props.navigate("/home");
                      }}
                      variant="outlined"
                    >
                      Back to Home Page
                    </Button>
                  </div>
                </Item>
              </Grid2>
            </div>
          );
        }}
      />
    );
  }
}

export default ErrorPage;
