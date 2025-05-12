import React from "react";
import BasePage from "./base-page";
import JobManagement from "../components/HomeComponent/JobManagement";

class HomePage extends React.Component {
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
