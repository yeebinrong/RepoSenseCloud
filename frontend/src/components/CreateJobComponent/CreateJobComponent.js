import React, { useState } from "react";
import "./CreateJobComponent.scss";
import { Autocomplete, TextField, Grid2, Chip} from "@mui/material";
import { makeStyles } from "@mui/styles";
import PageIcon from "../../assets/icons/page-icon.svg";

const useStyles = makeStyles(() => ({
    autocomplete: {
        width: '100%',
        marginTop: '16px',
    },
    chip: {
        margin: '4px',
        backgroundColor: '#e0f7fa',
        color: '#00695c',
    },
    textField: {
        backgroundColor: '#00000',
        borderRadius: '10px', // Set the border radius for rounded corners
    },
}));



const CreateJobComponent = () => {

    const classes = useStyles();
    const [currentPage, setCurrentPage] = useState(1);
    const [repoLink, setRepoLink] = useState([{ id: Date.now(), value: "" }]);
    const [jobName, setJobName] = useState("");
    const [sinceDate, setSinceDate] = useState("");
    const [untilDate, setUntilDate] = useState("");
    const [period, setPeriod] = useState("1-week");
    const [originalityThreshold, setOriginalityThreshold] = useState("");
    const [timeZone, setTimeZone] = useState("");
    const [authorship, setAuthorship] = useState(false);
    const [prevAuthors, setPrevAuthors] = useState(false);
    const [shallowClone, setShallowClone] = useState(false);
    const [ignoreSizeLimit, setIgnoreSizeLimit] = useState(false);
    const [addLastMod, setAddLastMod] = useState(false);
    const [jobType, setJobType] = useState("");
    const [chipValues, setChipValues] = useState([]);

    const addRepoLink = () => {
        console.log("Adding repo link");
        setRepoLink([...repoLink, { id: Date.now(), value: "" }]);
    }

    const deleteRepoLink = (id) => {
        console.log("Deleting repo link with id", id);
        setRepoLink(repoLink.filter((link) => link.id !== id));
    }

    const handleRepoLinkChange = (id, value) => {
        console.log("Updating repo link with id", id, "to value", value);
        setRepoLink(repoLink.map(link => link.id === id ? { ...link, value } : link)); // Update the value of the repo link with the given id
    }

    const handleChipChange = (event, value) => {
        setChipValues(value);
    };

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
                            {repoLink.map((link) => (
                                <span>
                                    <input type="text" className = "target-repo-textbox" placeholder="Paste Repo URL here" value = {link.value} onChange={(e) => handleRepoLinkChange(link.id, e.target.value)}/>
                                    <button className="delete-repo-link-button" onClick={() => deleteRepoLink(link.id)}>✕</button>
                                </span>
                            ))}
                            <button className="add-repo-link-button" onClick={addRepoLink}> + Add repository</button>
                        </div>
                    </div>
                    <div className = "dotted-line-down"/>
                    <div className="create-job-input-right">
                        <div className="settings-container">
                            <text className = "settings-header-label">Settings</text>
                            <div className = "settings-input-container">
                                <Grid2 container spacing={1}>
                                    <Grid2 size={6} container alignItems="center">
                                        <text className = "since-label">Since:</text>
                                    </Grid2>
                                    <Grid2 size={6}>
                                        <input type="date" className = "since-date-input" placeholder="DD/MM/YYYY"/>
                                    </Grid2>
                                    <Grid2 size={6} container alignItems="center">
                                        <text className = "until-label">Until:</text>
                                    </Grid2>
                                    <Grid2 size={6}>
                                        <input type="date" className = "until-date-input" placeholder="DD/MM/YYYY"/>
                                    </Grid2>
                                    <Grid2 size={6} container alignItems="center">
                                        <text className = "period-label">Period:</text>
                                    </Grid2>
                                    <Grid2 size={6}>
                                        <select className="period-range-dropdown">
                                            <option value="1-week">1 Week</option>
                                            <option value="4-week">4 Week</option>
                                            <option value="3-month">3 Months</option>
                                            <option value="6-month">6 Months</option>
                                        </select>
                                    </Grid2>
                                    <Grid2 size={6} container alignItems="center">
                                        <text className="originality-label">Originality Threshold:</text>
                                    </Grid2>
                                    <Grid2 size={6}>
                                        <input type="number" className = "originality-input" placeholder="0 to 1"/>
                                    </Grid2>
                                    <Grid2 size={6} container alignItems="center">
                                        <text className="timezone-label">Time Zone:</text>
                                    </Grid2>
                                    <Grid2 size={6}>
                                        <input type="text" className = "timezone-input" placeholder="e.g UTC+8"/>
                                    </Grid2>
                                    <Grid2 size={6} marginTop = {2} className = "left-checklist-container">
                                        <Grid2 container spacing = {3} justifyContent="space-between">
                                            <Grid2 size={6}>
                                                <text className="authorship-label">Analyse authorship:</text>
                                            </Grid2>
                                            <Grid2 size={6}>
                                                <input type="checkbox" className = "authorship-checkbox"/>
                                            </Grid2>
                                            <Grid2 size={6}>
                                                <text className="prev-author-label">Find previous authors:</text>
                                            </Grid2>
                                            <Grid2 size={6}>
                                                <input type="checkbox" className = "prev-author-checkbox"/>
                                            </Grid2>
                                            <Grid2 size={6}>
                                                <text className="shallow-clone-label">Shallow cloning:</text>
                                            </Grid2>
                                            <Grid2 size={6} >
                                                <input type="checkbox" className = "shallow-clone-checkbox"/>
                                            </Grid2>
                                        </Grid2>
                                    </Grid2>
                                    <Grid2 size={6} marginTop = {2} paddingLeft ={6} className= "right-checklist-container">
                                        <Grid2 container spacing = {3} justifyContent="space-between">
                                            <Grid2 size={6} >
                                                <text className="ignore-size-limit-label">Ignore file size limit:</text>
                                            </Grid2>
                                            <Grid2 size={6} >
                                                <input type="checkbox" className="ignore-size-limit-checkbox" />
                                            </Grid2>
                                            <Grid2 size={6}>
                                                <text className="Add-last-mod-label">Add last modified date:</text>
                                            </Grid2>
                                            <Grid2 size={6} >
                                                <input type="checkbox" className="add-last-mod-checkbox" />
                                            </Grid2>
                                            
                                        </Grid2>
                                    </Grid2>
                                    <Grid2 size={2} marginTop = {2}>
                                        <text className="format-label">Format:</text>
                                    </Grid2>
                                    <Grid2 size={8}>
                                        <Autocomplete
                                            multiple
                                            freeSolo
                                            id="tags-filled"
                                            options={[]}
                                            value={chipValues}
                                            onChange={handleChipChange}
                                            renderTags={(value, getTagProps) =>
                                                value.map((option, index) => (
                                                    <Chip
                                                        variant="outlined"
                                                        label={option}
                                                        {...getTagProps({ index })}
                                                        className={classes.chip}
                                                    />
                                                ))
                                            }
                                            renderInput={(params) => (
                                                <TextField
                                                    {...params}
                                                    variant="filled"
                                                    label="Enter File Format(s) To Scan"
                                                    placeholder="e.g. Java, Python"
                                                    className={classes.textField}
                                                />
                                            )}
                                            className={classes.autocomplete}
                                        />
                                    </Grid2>
                                </Grid2>
                            </div>
                        </div>
                    </div>                   
                </span>
            </div>

                
        </div>
    );
}

export default CreateJobComponent;