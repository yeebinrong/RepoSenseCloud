import React from "react";
import "./HomeComponent.scss";
import { styled, Paper, Button, Grid2 } from "@mui/material";

export const Item = styled(Paper)(({ theme }) => ({
  backgroundColor: "#fff",
  ...theme.typography.body2,
  color: theme.palette.text.secondary,
}));

class HomeComponent extends React.Component {
  scrollToTop = function () {
    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  };

  render() {
    return (
      <div style={{ fontSize: "30px", margin: "32px 184px 0px 184px" }}>
        <Grid2 xs style={{ position: "relative" }}>
          <Item style={{ padding: "16px" }}>
            <div style={{ textAlign: "center" }}>Work in progress...</div>
          </Item>
        </Grid2>
        <div style={{ display: "flex", justifyContent: "center" }}>
          <Button
            style={{ margin: "200px 0" }}
            onClick={() => {
              this.scrollToTop();
            }}
            variant="outlined"
          >
            Back to Top
          </Button>
        </div>
      </div>
    );
  }
}

export default HomeComponent;
