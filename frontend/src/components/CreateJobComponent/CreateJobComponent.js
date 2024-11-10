import React, { useState } from "react";
import { Autocomplete, TextField, Grid2, Chip, Modal, Box, 
    Button, Select, FormControl, InputLabel, MenuItem, Stack, } from "@mui/material";
import { makeStyles } from "@mui/styles";
import PageIcon from "../../assets/icons/page-icon.svg";
import "./CreateJobComponent.scss";
import { showSuccessBar } from "../../constants/snack-bar";

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
        borderRadius: '10px',
    },
    modal: {
        position: 'relative',
        top: '50%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        width: '1115px',
        height: '788px',
        overflowY: 'auto', 
        backgroundColor: 'white',
        padding: '16px',
        borderRadius: '16px',
    },
}));



const CreateJobComponent = () => {

    const classes = useStyles();
    const [currentPage, setCurrentPage] = useState(1);
    const [open, setOpen] = useState(false);
    const handleModalOpen = () => setOpen(true);
    const handleModalClose = () => { console.log("close"); setOpen(false);}

    //Page 1 States
    const [repoLink, setRepoLink] = useState([{ id: Date.now(), value: "" }]);
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
    const [chipValues, setChipValues] = useState([]);

    //Pagw 2 States
    const [jobName, setJobName] = useState("");
    const [format, setFormat] = useState([]);
    const [frequency, setFrequency] = useState('monthly');
    const [jobType, setJobType] = useState("");
    const [startMinute, setStartMinute] = useState('00');
    const [startHour, setStartHour] = useState('00');



    //State Change Functions
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

    const handleFrequencyChange = (event) => {
        setFrequency(event.target.value);
    };

    //Rendering Functions
    const renderJobFormHeader = () => {
        return (
            <div className="create-job-header">
                <h1 >Create a Job</h1>
                <h4>Fill In Job Detail To Queue Or Run A New ReposenseCloud Job</h4>
                <span className="create-job-page-status">
                <img src={PageIcon} alt="Page Icon" />
                    <div className="dotted-line" />
                    <img src={PageIcon} alt="Page Icon" className= {currentPage === 1? "page-icon2" : "page-icon1"} />
                </span>
            </div>
        )
    }

    const renderJobFormPage1 = () => {
        return (
            <div className="create-job-page-1">
                <div className="create-job-container">
                    <span className="create-job-input-container">
                        <div className="create-job-input-left">
                            <div className="job-name-container">
                                <text className="job-name-label">Job Name</text>
                                <input type="text" className="job-name-textbox" placeholder="Enter Job Name" />
                            </div>
                            <div className="target-repo-container">
                                <text className="target-repo-label">Target Repository</text>
                                {repoLink.map((link) => (
                                    <span>
                                        <input type="text" className="target-repo-textbox" placeholder="Paste Repo URL here" value={link.value} onChange={(e) => handleRepoLinkChange(link.id, e.target.value)} />
                                        <button className="delete-repo-link-button" onClick={() => deleteRepoLink(link.id)}>✕</button>
                                    </span>
                                ))}
                                <button className="add-repo-link-button" onClick={addRepoLink}> + Add repository</button>
                            </div>
                        </div>
                        <div className="dotted-line-down" />
                        <div className="create-job-input-right">
                            <div className="settings-container">
                                <text className="settings-header-label">Settings</text>
                                <div className="settings-input-container">
                                    <Grid2 container spacing={1}>
                                        <Grid2 size={6} container alignItems="center">
                                            <text className="since-label">Since:</text>
                                        </Grid2>
                                        <Grid2 size={6}>
                                            <input type="date" className="since-date-input" onChange={(e) => setSinceDate(e.target.value)} placeholder="DD/MM/YYYY" />
                                        </Grid2>
                                        <Grid2 size={6} container alignItems="center">
                                            <text className="until-label">Until:</text>
                                        </Grid2>
                                        <Grid2 size={6}>
                                            <input type="date" className="until-date-input" onChange={(e) => setUntilDate(e.target.value)}placeholder="DD/MM/YYYY" />
                                        </Grid2>
                                        <Grid2 size={6} container alignItems="center">
                                            <text className="period-label">Period:</text>
                                        </Grid2>
                                        <Grid2 size={6}>
                                            <select className="period-range-dropdown" onChange={(e) => setPeriod(e.target.value)}>
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
                                            <input type="text" className="originality-input" onChange={(e) => setOriginalityThreshold(e.target.value)} placeholder="0 to 1" />
                                        </Grid2>
                                        <Grid2 size={6} container alignItems="center">
                                            <text className="timezone-label">Time Zone:</text>
                                        </Grid2>
                                        <Grid2 size={6}>
                                            <input type="text" className="timezone-input" onChange={(e) => setTimeZone(e.target.value)} placeholder="e.g UTC+8" />
                                        </Grid2>
                                        <Grid2 size={6} marginTop={2} className="left-checklist-container">
                                            <Grid2 container spacing={3} justifyContent="space-between">
                                                <Grid2 size={6}>
                                                    <text className="authorship-label">Analyse authorship:</text>
                                                </Grid2>
                                                <Grid2 size={6}>
                                                    <input type="checkbox" className="authorship-checkbox" onChange={(e) => setAuthorship(e.target.value)} />
                                                </Grid2>
                                                <Grid2 size={6}>
                                                    <text className="prev-author-label">Find previous authors:</text>
                                                </Grid2>
                                                <Grid2 size={6}>
                                                    <input type="checkbox" className="prev-author-checkbox" onChange={(e) => setPrevAuthors(e.target.value)} />
                                                </Grid2>
                                                <Grid2 size={6}>
                                                    <text className="shallow-clone-label">Shallow cloning:</text>
                                                </Grid2>
                                                <Grid2 size={6} >
                                                    <input type="checkbox" className="shallow-clone-checkbox" onChange={(e) => setShallowClone(e.target.value)}/>
                                                </Grid2>
                                            </Grid2>
                                        </Grid2>
                                        <Grid2 size={6} marginTop={2} paddingLeft={6} className="right-checklist-container">
                                            <Grid2 container spacing={3} justifyContent="space-between">
                                                <Grid2 size={6} >
                                                    <text className="ignore-size-limit-label">Ignore file size limit:</text>
                                                </Grid2>
                                                <Grid2 size={6} >
                                                    <input type="checkbox" className="ignore-size-limit-checkbox" onChange={(e) => setIgnoreSizeLimit(e.target.value)} />
                                                </Grid2>
                                                <Grid2 size={6}>
                                                    <text className="Add-last-mod-label">Add last modified date:</text>
                                                </Grid2>
                                                <Grid2 size={6} >
                                                    <input type="checkbox" className="add-last-mod-checkbox" onChange={(e) => setAddLastMod(e.target.value)} />
                                                </Grid2>

                                            </Grid2>
                                        </Grid2>
                                        <Grid2 size={2} marginTop={2}>
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
    };

    const renderJobFormPage2 = () => {
        return (
            <Stack spacing={2} className="create-job-page-2">
                <Box className="job-type-box-container">
                    <Box className="job-type-box" >
                        <FormControl fullWidth>
                            <InputLabel id="demo-simple-select-label">Job Type</InputLabel>
                            <Select
                                labelId="demo-simple-select-label"
                                id="demo-simple-select"
                                value={jobType}
                                label="Job Type"
                                onChange={(e) => setJobType(e.target.value)}
                            >
                                <MenuItem value={"manual"}>Manual</MenuItem>
                                <MenuItem value={"scheduled"}>Scheduled</MenuItem>
                            </Select>
                        </FormControl>
                    </Box>
                </Box>
                <Box>
                    {jobType === "scheduled" ? renderScheduledSettings() : null}
                </Box>
            </Stack>
        );
    };

    const renderScheduledSettings = () => {
        const hours = [
            "00", "01","02", "03", "04", "05", "06", "07", "08", "09", "10", "11",
           "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"
        ];
        const minutes = Array.from({ length: 60 }, (_, i) => i.toString().padStart(2, '0'));

        return (
            <Grid2 container spacing={2} style={{ width: "580px" }}>
                <Grid2 item size={4}>
                    <text className="schedule-settings-labels">Frequency:</text>
                </Grid2>
                <Grid2 item size={8}>
                    <Select labelId="demo-simple-select-label"
                        id="demo-simple-select"
                        value={frequency}
                        label="Frequency"
                        defaultValue="monthly"
                        onChange={handleFrequencyChange}>
                        <MenuItem value={"weekly"}>Weekly</MenuItem>
                        <MenuItem value={"monthly"}>Monthly</MenuItem>
                        <MenuItem value={"quarterly"}>Quarterly</MenuItem>
                    </Select>
                </Grid2>
                <Grid2 item size={4}>
                    <text className="schedule-settings-labels" >Start Time:</text>
                </Grid2>
                <Grid2 item size={2}>
                    <select
                        value={startHour}
                        onChange={(e) => setStartHour(e.target.value)}
                        className="time-dropdown"
                    >
                        {hours.map(hour => (
                            <option value={hour}>
                                {hour}
                            </option>
                        ))}
                    </select>
                </Grid2>
                <Grid2 item size={1}>
                    <text className="time-colon">:</text>
                </Grid2>
                <Grid2 item size={2}>
                    <select
                            value={startMinute}
                            onChange={(e) => setStartMinute(e.target.value)}
                            className="time-dropdown"
                        >
                            {minutes.map(minute => (
                                <option value={minute}>
                                    {minute}
                                </option>
                            ))}
                    </select>
                </Grid2>
                <Grid2 item size={4} container alignItems="center">
                    <text className="start-date-label">Start Date:</text>
                </Grid2>
                <Grid2 item size={6}>
                    <input type="date" className="start-date-input" placeholder="DD/MM/YYYY" />
                </Grid2>
                <Grid2 item size={4} container alignItems="center">
                    <text className="end-date-label">End Date:</text>
                </Grid2>
                <Grid2 item size={6}>
                    <input type="date" className="end-date-input" placeholder="DD/MM/YYYY" />
                </Grid2>
                
            </Grid2>
        )
    }

    const renderNavigationButtons = () => {

        return (
            <div className="navigation-buttons">
                <Button variant="contained" sx={{backgroundColor:'#FFFFFF', color:"#ADA7A7", width:"125px", marginRight: "50px"}} onClick={ () => currentPage === 2? setCurrentPage(1) : handleModalClose() }>
                    {currentPage === 1 ? "Cancel" : "Back"}
                </Button>
                <Button variant="contained" sx={{backgroundColor:'#F7A81B', width:"125px"}} onClick={() => currentPage === 1? setCurrentPage(2) : submitJobForm()}>
                    {currentPage === 2 ? "Create" : "Next"}
                </Button>
            </div>
        )
    }

    //Submit Job Form
    const submitJobForm = async () => {
        handleModalClose();
        showSuccessBar("Job Created Successfully");
        console.log("Submitting job form for", localStorage.getItem("JWT"));
        const formData = {
            jobName,
            repoLink,
            sinceDate,
            untilDate,
            period,
            originalityThreshold,
            timeZone,
            authorship,
            prevAuthors,
            shallowClone,
            ignoreSizeLimit,
            addLastMod,
            chipValues,
            jobType,
            frequency,
            startHour,
            startMinute,
        };
        try {
            const response = await fetch ('https://this-is-a-fake-url.com', {
                method: "POST",
                headers:{
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem("JWT")}`,
                },
                body: JSON.stringify(formData),
            });
        } catch (error) {
            console.error("Error submitting job form:", error);
        }

    }


    //Main Render
    return (
        <div>
            <Button variant="contained" color="primary" onClick={handleModalOpen} style={{ justifySelf: "center" }}>
                Create Job
            </Button>
            <Modal open={open} onClose={handleModalClose} aria-labelledby="modal-title" aria-describedby="modal-description">
                <Box className= {classes.modal}>
                            {renderJobFormHeader()}
                            {currentPage === 1 ? renderJobFormPage1() : renderJobFormPage2()}
                            <div className = "navigation-buttons-container">
                                {renderNavigationButtons()}
                            </div>
                </Box>                
            </Modal>

        </div>
    );
}

export default CreateJobComponent;