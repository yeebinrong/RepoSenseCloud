import React from "react";
import "./CreateJobComponent.scss";
import { styled, Paper, Button, Grid2 } from "@mui/material";
import PageIcon from "../../assets/icons/page-icon.svg";

class CreateJobComponent extends React.Component {
  

  render() {
    return (
        <div className="create-job-container">
            <div className="create-job-header">
                <h1>Create a Job</h1>
                <h4>Fill In Job Detail To Queue Or Run A New ReposenseCloud Job</h4>
                <span className="create-job-page-status">
                    <img src = {PageIcon} alt="Page Icon" className="page-icon1"/>
                    <div className = "dotted-line"/>
                    <img src = {PageIcon} alt="Page Icon" className="page-icon2"/>
                </span>
            </div>

                
        </div>
    );
  }
}

export default CreateJobComponent;