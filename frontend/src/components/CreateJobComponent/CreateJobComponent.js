import React from "react";
import "./CreateJobComponent.scss";
import { Stack, Grid2 } from "@mui/material";
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
                <span className= "create-job-input-container">
                    <div className="create-job-input-left">
                        <div className="job-name-container">
                            <text className = "job-name-label">Job Name</text>
                            <input type="text" className = "job-name-textbox" placeholder="Enter Job Name"/>
                        </div>
                        <div className="target-repo-container">
                            <text className = "target-repo-label">Target Repository</text>
                            <span>
                                <input type="text" className = "target-repo-textbox" placeholder="Paste Repo URL here"/>
                                <button className="delete-repo-link-button">✕</button>
                                <button className="add-repo-link-button">+ Add repository</button>
                            </span>
                        </div>
                    </div>
                    <div className = "dotted-line-down"/>
                    <div className="create-job-input-right">
                        <h3>test3</h3>
                    </div>                   
                </span>
            </div>

                
        </div>
    );
  }
}

export default CreateJobComponent;