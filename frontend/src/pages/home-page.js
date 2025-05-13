import React from "react";
import PropTypes from "prop-types";
import BasePage from "./base-page";
import JobManagement from "../components/HomeComponent/JobManagement";

class HomePage extends React.Component {
  static propTypes = {
    navigate: PropTypes.func.isRequired,
    username: PropTypes.string.isRequired,
  };

  render() {
    return (
      <BasePage
        navigate={this.props.navigate}
        username={this.props.username}
        component={(props) => {
          // return <HomeComponent {...props} />;
          // return <HomePageContent {...props} />;
          return <JobManagement {...props} />;
        }}
      />
    );
  }
}

export default HomePage;
