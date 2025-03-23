import React from "react";
import BasePage from "./base-page";
import HomeComponent from "../components/HomeComponent/HomeComponent";
import JobManagement from "../components/HomeComponent/JobManagement";

class HomePage extends React.Component {
  render() {
    return (
      <BasePage
        navigate={this.props.navigate}
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
