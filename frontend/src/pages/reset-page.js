import React from "react";
import PropTypes from "prop-types";
import ResetComponent from "../components/ResetComponent/ResetComponent";
import BasePage from "./base-page";

const ResetComponentWrapper = (props) => <ResetComponent {...props} />;

class ResetPage extends React.Component {
  render() {
    return (
      <BasePage
        navigate={this.props.navigate}
        isLoginPage
        component={ResetComponentWrapper}
      />
    );
  }
}

ResetPage.propTypes = {
  navigate: PropTypes.func.isRequired,
};

export default ResetPage;
