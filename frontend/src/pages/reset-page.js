import React from "react";
import ResetComponent from "../components/ResetComponent/ResetComponent";
import BasePage from "./base-page";

class ResetPage extends React.Component {
  render() {
    return (
      <BasePage
        navigate={this.props.navigate}
        isLoginPage
        component={(props) => {
          return <ResetComponent {...props} />;
        }}
      />
    );
  }
}

export default ResetPage;
