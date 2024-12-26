import React from "react";
import BasePage from "./base-page";
import CreateJobComponent from "../components/CreateJobComponent/CreateJobComponent";


class CreateJobPage extends React.Component {
  render() {
    return (
        <BasePage
        navigate={this.props.navigate}
        component={(props) => {
          return <CreateJobComponent {...props} />;
        }}
      />
    );
  }
}

export default CreateJobPage;
