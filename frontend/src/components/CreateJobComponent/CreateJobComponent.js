import React, { useState, useEffect } from "react";
import moment from "moment";
import { Autocomplete, TextField, Grid2, Chip, Modal, Box, 
    Button, Select, FormControl, InputLabel, MenuItem, Stack } from "@mui/material";
import { makeStyles } from "@mui/styles";
import PageIcon from "../../assets/icons/page-icon.svg";
import "./CreateJobComponent.scss";
import { showSuccessBar, showErrorBar } from "../../constants/snack-bar";

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
        font: "DM Sans",
    },
    modal: {
        position: 'relative',
        top: '50%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        width: '1115px',
        height: '90vh',
        overflowY: 'auto', 
        backgroundColor: 'white',
        padding: '16px',
        borderRadius: '16px',
        display: 'flex',
        flexDirection: 'column',
        flex: '1',
    },
}));


const CreateJobComponent = (jobId) => {

    const classes = useStyles();
    const timezoneList = [
        "UTC-12", "UTC-11", "UTC-10", "UTC-0930", "UTC-09", "UTC-08",
        "UTC-07", "UTC-06", "UTC-05", "UTC-04", "UTC-0330", "UTC-03",
        "UTC-02", "UTC-01", "UTC+00", "UTC+01", "UTC+02", "UTC+03",
        "UTC+0330", "UTC+04", "UTC+0430", "UTC+05", "UTC+0530", "UTC+0545",
        "UTC+06", "UTC+0630", "UTC+07", "UTC+08", "UTC+0845", "UTC+09",
        "UTC+0930", "UTC+10", "UTC+1030", "UTC+11", "UTC+12", "UTC+1245",
        "UTC+13", "UTC+14"
    ];

    //Modal States
    const [currentPage, setCurrentPage] = useState(1);
    const [open, setOpen] = useState(false);
    const handleModalOpen = () => setOpen(true);
    const handleModalClose = () => setOpen(false);

    //Page 1 States
    const [jobName, setJobName] = useState("");
    const [repoLink, setRepoLink] = useState([{ id: Date.now(), value: "" }]);
    const [periodMode, setPeriodMode] = useState("Specific Date Range");
    const [sinceDate, setSinceDate] = useState("");
    const [untilDate, setUntilDate] = useState("");
    const [period, setPeriod] = useState("");
    const [periodModifier, setPeriodModifier] = useState("latest");
    const [originalityThreshold, setOriginalityThreshold] = useState(0.5);
    const [timeZone, setTimeZone] = useState("");
    const [authorship, setAuthorship] = useState(false);
    const [prevAuthors, setPrevAuthors] = useState(false);
    const [shallowClone, setShallowClone] = useState(false);
    const [ignoreSizeLimit, setIgnoreSizeLimit] = useState(false);
    const [addLastMod, setAddLastMod] = useState(false);
    const [formatChipValues, setFormatChipValues] = useState([]);

    //Page 2 States
    const [jobType, setJobType] = useState("manual");
    const [frequency, setFrequency] = useState('');
    const [startMinute, setStartMinute] = useState('--');
    const [startHour, setStartHour] = useState('--');
    const [startDate, setStartDate] = useState("");
    const [endDate, setEndDate] = useState("");

    //Form Validation States
    const [jobNameError, setJobNameError] = useState(true);
    const [repoLinkError, setRepoLinkError] = useState(true);
    const [sinceUntilDateError, setSinceUntilDateError] = useState(false);
    const [originalityThresholdError, setOriginalityThresholdError] = useState(false);
    const [timeZoneError, setTimeZoneError] = useState(true);
    const [startHourError, setStartHourError] = useState(true);
    const [startMinuteError, setStartMinuteError] = useState(true);
    const [dateError, setDateError] = useState(true);


    //Retrieve Job Details for Editing
    useEffect(() => {
        if (jobId) {
            // Retrieve job details from API
            // Set the states with the retrieved job details
            fetch(`/api/job/${jobId}`)
                .then((response) => response.json())
                .then(data => {
                    setJobName(data.jobName);
                    setRepoLink(data.repoLink);
                    setPeriodMode(data.periodMode);
                    setSinceDate(data.sinceDate);
                    setUntilDate(data.untilDate);
                    setOriginalityThreshold(data.originalityThreshold);
                    setTimeZone(data.timeZone);
                    setAuthorship(data.authorship);
                    setPrevAuthors(data.prevAuthors);
                    setShallowClone(data.shallowClone);
                    setIgnoreSizeLimit(data.ignoreSizeLimit);
                    setAddLastMod(data.addLastMod);
                    setFormatChipValues(data.formatChipValues);
                    setJobType(data.jobType);
                    setFrequency(data.frequency);
                    setStartHour(data.startHour);
                    setStartMinute(data.startMinute);
                    setStartDate(data.startDate);
                    setEndDate(data.endDate);
                })
                .catch((error) => {
                    console.error("Error fetching job details:", error);
                });
        }
    }, [jobId]);

    
    // Reset state when modal closes
    useEffect(() => {
        if (!open) {
            setCurrentPage(1)
            // page 1 states
            setJobName("");
            setRepoLink([{ id: Date.now(), value: "" }]);
            setPeriodMode("Specific Date Range");
            setSinceDate("");
            setUntilDate("");
            setPeriod("");
            setOriginalityThreshold("");
            setTimeZone("");
            setAuthorship(false);
            setPrevAuthors(false);
            setShallowClone(false);
            setIgnoreSizeLimit(false);
            setAddLastMod(false);
            setFormatChipValues([]);
            // page 2 states
            setJobType("manual");
            setFrequency("");
            setStartMinute("--");
            setStartHour("--");
            setStartDate("")
            setEndDate("");
            // form validation states  
            setJobNameError(true);
            setRepoLinkError(true);
            setSinceUntilDateError(false);
            setOriginalityThresholdError(false);
            setTimeZoneError(true);
            setStartHourError(true);
            setStartMinuteError(true);
            setDateError(true);
        }
    }, [open]);

    //Reset period states when period mode changes
    useEffect(() => {
        if (periodMode !== "Specific Date Range" ){
            setPeriod("7d");
            setSinceDate("");
            setUntilDate("");
        } else {
            setPeriod("");
            setPeriodModifier("latest");
            setSinceDate("");
            setUntilDate("");
        }
    }, [periodMode]);

    //State Change Functions
    ///Repo Link Input
    const addRepoLink = () => {
        setRepoLink([...repoLink, { id: Date.now(), value: "" }]);
    }

    const deleteRepoLink = (id) => {
        setRepoLink(repoLink.filter((link) => link.id !== id));
    }

    const handleRepoLinkChange = (id, value) => {
        setRepoLink(repoLink.map(link => link.id === id ? { ...link, value } : link)); // Update the value of the repo link with the given id
    }

    ///Format Chip Input
    const handleChipChange = (event, value) => {
        setFormatChipValues(value);
    };

    //Sub-Component Rendering Functions
    /// Header Render for both pages
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

    //Form Validation Rules
    const validateJobName = () => {
        if (jobName === "") {
            setJobNameError(true);
        } else {
            setJobNameError(false);
        }
    }

    const validateRepoLink = (id) => {
        const link = repoLink.find(link => link.id === id);
        if (link.value === "") {
            setRepoLinkError(true);
        } else {
            setRepoLinkError(false);
        }
    }

    const validateSinceUntilDate = () => {
        if (sinceDate !== "" && untilDate !== "" && moment(sinceDate).isAfter(moment(untilDate))) {
            setSinceUntilDateError(true);
        } else {
            setSinceUntilDateError(false);
        }
    }

    const validateOriginalityThreshold = () => {
        if (originalityThreshold < 0 || originalityThreshold > 1) {
            setOriginalityThresholdError(true);
        } else {
            setOriginalityThresholdError(false);
        }
    }

    const validateTimeZone = (tz) => {
        if (tz === "" || tz === "Select a time zone") {
            setTimeZoneError(true);
        } else {
            setTimeZoneError(false);
        }
    }

    const validatePage1 = (callback) => {
        let hasError = false;
    
        if (jobNameError || repoLinkError || sinceUntilDateError || originalityThresholdError || timeZoneError) {
            hasError = true;
        }
    
        if (callback) {
            callback(hasError);
        }
    };

    const validateStartHour = (sHour) => {
        if (sHour === "--") {
            setStartHourError(true);
        } else {
            setStartHourError(false);
        }
    }

    const validateStartMinute = (sMin) => {
        if (sMin === "--") {
            setStartMinuteError(true);
        } else {
            setStartMinuteError(false);
        }
    }

    const validateDate = (sDate, eDate) => {
        if (sDate === "" || eDate === "" || moment(sDate).isAfter(moment(eDate))) {
            setDateError(true);
        } else {
            setDateError(false);
        }
    }



    /// Page 1 Render
    const renderJobFormPage1 = () => {
        return (
            <div className="create-job-page-1">
                <div className="create-job-container">
                    <span className="create-job-input-container">
                        <div className="create-job-input-left">
                            <div className="job-name-container">
                                <text className="job-name-label">Job Name</text>
                                <TextField className="job-name-textbox" placeholder="Enter Job Name" value = {jobName} 
                                    onChange={(e)=> { validateJobName(); setJobName(e.target.value)}} error={jobNameError}     
                                    helperText={jobNameError ? "Please Enter Job Name" : ""}/>
                            </div>
                            <div className="target-repo-container">
                                <text className="target-repo-label">Target Repository</text>
                                {repoLink.map((link, index) => (
                                    <span key={link.id}>
                                        <TextField className="target-repo-textbox" placeholder="Paste Repo URL here" value={link.value}
                                            onChange={(e) => { validateRepoLink(link.id); handleRepoLinkChange(link.id, e.target.value) }}
                                            error={repoLinkError}
                                            helperText={repoLinkError ? "Please Paste Repository URL" : ""} />
                                        {index > 0 && (<button className="delete-repo-link-button" onClick={() => deleteRepoLink(link.id)}>✕</button>)}
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
                                            <text className="period-mode-label">Period Mode:</text>
                                        </Grid2>
                                        <Grid2 size={6}>
                                            <Grid2 size={6}>
                                                <select className="period-mode-dropdown" onChange={(e) => setPeriodMode(e.target.value)}>
                                                    <option value="Specific Date Range">Specific Date Range</option>
                                                    <option value="By Days/Weeks">By Days/Weeks</option>
                                                </select>
                                            </Grid2>
                                        </Grid2>
                                        {periodSwitchCase(periodMode)}
                                        <Grid2 size={6} container alignItems="center">
                                            <text className="originality-label">Originality Threshold:</text>
                                        </Grid2>
                                        <Grid2 size={6}>
                                            <TextField type="text" className="originality-input" value = {originalityThreshold} 
                                            onChange={(e) => {validateOriginalityThreshold(); setOriginalityThreshold(e.target.value)}} placeholder="0.5" 
                                                helperText="Input between 0.0 to 1.0" />
                                        </Grid2>
                                        <Grid2 size={6} container alignItems="center">
                                            <text className="timezone-label">Time Zone:</text>
                                        </Grid2>
                                        <Grid2 size={6}>
                                            <select
                                                className={`timezone-dropdown ${timeZoneError ? 'error' : ''}`}
                                                value={timeZone}
                                                onChange={(e) => {
                                                    setTimeZone(e.target.value);
                                                    validateTimeZone(e.target.value);
                                                }}
                                            >
                                                <option value="">Select a time zone</option>
                                                {timezoneList.map((timezone) => (
                                                    <option key={timezone} value={timezone}>{timezone}</option>
                                                ))}
                                            </select>
                                            {timeZoneError && <span className="error-message">Please select a time zone</span>}
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
                                                options={["js", "java", "python", "c", "cpp", "html", "css"]}
                                                value={formatChipValues}
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
                                                        placeholder="e.g. js, py"
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

    /// Page 1 - Period Modes Render
    const renderPeriodForSinceUntil = () => {
        return (
            <Grid2 container spacing={1}>
                <Grid2 size={6} container alignItems="center">
                    <text className="since-label">Since:</text>
                </Grid2>
                <Grid2 size={6}>
                    <TextField type="date" className="since-date-input" value={sinceDate} 
                    onChange={(e) => setSinceDate(e.target.value)} 
                    onBlur={(e) => { validateSinceUntilDate(); setSinceDate(e.target.value); }}
                    placeholder="DD/MM/YYYY" />
                    
                </Grid2>
                <Grid2 size={6} container alignItems="center">
                    <text className="until-label">Until:</text>
                </Grid2>
                <Grid2 size={6}>
                    <TextField type="date" className="until-date-input" value={untilDate} 
                        onChange={(e) => {validateSinceUntilDate(); setUntilDate(e.target.value)}} 
                        onBlur={(e) => { validateSinceUntilDate(); setUntilDate(e.target.value); }}
                        placeholder="DD/MM/YYYY" 
                        error = {sinceUntilDateError} helperText={sinceUntilDateError? "Improper Date Range" : "Default: last 30 days from date of job"}/>
                </Grid2>
            </Grid2>
        )
    }

    const renderPeriodModifierInput = () => {
        switch (periodModifier) {
            case "before":
                return <TextField type="date" className="until-date-input2" value={untilDate} onChange={(e) => setUntilDate(e.target.value)} placeholder="DD/MM/YYYY" />

            case "after":
                return <TextField type="date" className="since-date-input2" value={sinceDate} onChange={(e) => setSinceDate(e.target.value)} placeholder="DD/MM/YYYY" />

            default:
                return <text> **{period} from date of job run</text>
        }
    }

    const renderPeriodForPeriod = () => {
        return (
            <Grid2 container spacing={1}>
                <Grid2 size={6} container alignItems="center">
                    <text className="period-label">Period:</text>
                </Grid2>
                <Grid2 size={6}>
                    <select className="period-range-dropdown" onChange={(e) => setPeriod(e.target.value)}>
                        <option value="7d">7 days</option>
                        <option value="30d">30 days</option>
                        <option value="3-month">3 Months</option>
                        <option value="6-month">6 Months</option>
                    </select>
                </Grid2>
                <Grid2 size={3} container alignItems="center">
                    <select className="period-modifier-dropdown" onChange={(e) => setPeriodModifier(e.target.value)}>
                        <option value="latest">Latest</option>
                        <option value="before">Before Date:</option>
                        <option value="after">After Date:</option>
                    </select>
                </Grid2>
                <Grid2 size={3}>
                    <div>

                    </div>
                </Grid2>
                <Grid2 size={6}>
                    {renderPeriodModifierInput()}
                </Grid2>
            </Grid2>
        )
    }

    const periodSwitchCase = (periodMode) => {
        switch (periodMode) {
            case "Specific Date Range":
                return renderPeriodForSinceUntil();
            case "By Days/Weeks":
                return renderPeriodForPeriod();
            default:
                return renderPeriodForSinceUntil();
        }
    }


    /// Page 2 Render
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

    /// Page 2 - Scheduled Settings Render
    const renderScheduledSettings = () => {
        const hours = ["--", ...Array.from({ length: 24 }, (_, i) => i.toString().padStart(2, '0'))];
        const minutes = ["--", ...Array.from({ length: 60 }, (_, i) => i.toString().padStart(2, '0'))];

        return (
            <Grid2 container spacing={2} style={{ width: "580px" }}>
                <Grid2 item size={4}>
                    <text className="schedule-settings-labels">Frequency:</text>
                </Grid2>
                <Grid2 item size={8}>
                    <select className="frequency-dropdown"
                        onChange={(e) => setFrequency(e.target.value)}>
                        <option value={"weekly"}>Weekly</option>
                        <option value={"monthly"}>Monthly</option>
                        <option value={"quarterly"}>Quarterly</option>
                    </select>
                </Grid2>
                <Grid2 item size={4}>
                    <text className="schedule-settings-labels" >Start Time:</text>
                </Grid2>
                <Grid2 item size={2}>
                    <select
                        className={`time-dropdown  ${startHourError ? 'error' : ''}`}
                        value={startHour}
                        onChange={(e) => { setStartHour(e.target.value); validateStartHour(e.target.value);}}
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
                            className={`time-dropdown ${startMinuteError ? 'error' : ''}`}
                            value={startMinute}
                            onChange={(e) => { setStartMinute(e.target.value); validateStartMinute(e.target.value)}}
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
                    <TextField type="date" className="start-date-input" value = {startDate}
                        onChange={(e) => {setStartDate(e.target.value); validateDate(e.target.value, endDate)}} 
                        onInput={(e) => {setStartDate(e.target.value); validateDate(e.target.value, endDate)}} 
                        error = {dateError} placeholder="DD/MM/YYYY" />
                </Grid2>
                <Grid2 item size={4} container alignItems="center">
                    <text className="end-date-label">End Date:</text>
                </Grid2>
                <Grid2 item size={6}>
                    <TextField type="date" className="end-date-input" value = {endDate} 
                        onChange={(e) => { setEndDate(e.target.value); validateDate(startDate, e.target.value)}} placeholder="DD/MM/YYYY" 
                        onInput={(e) => {setEndDate(e.target.value); validateDate(startDate, e.target.value) }} 
                        error = {dateError}
                        helperText={(endDate!==""&& dateError)? "Improper Date Range":""}/>
                </Grid2>
                
            </Grid2>
        )
    }

    /// Navigation Button For Both Pages
    const renderNavigationButtons = () => {
        return (
            <div className="navigation-buttons">
                <Button variant="contained" sx={{backgroundColor:'#FFFFFF', color:"#ADA7A7", width:"125px", marginRight: "50px"}} onClick={ () => currentPage === 2? setCurrentPage(1) : handleModalClose() }>
                    {currentPage === 1 ? "Cancel" : "Back"}
                </Button>
                <Button variant="contained" sx={{ backgroundColor: '#F7A81B', width: "125px" }} onClick={() => {

                    if (currentPage === 1) {
                        validatePage1((hasError) => {
                            if (hasError) {
                                showErrorBar("incomplete form");
                            } else {
                                setCurrentPage(2);
                            }
                        });
                    } else {
                        submitJobForm();
                    }
                }}>
                    {currentPage === 2 ? "Save" : "Next"}
                </Button>
            </div>
        )
    }

    const validateForm = () => {
        return new Promise((resolve, reject) => {
            let isValid = true; //mock test

            if (jobName === "") {
                return reject(new Error("Job name cannot be blank."));
            }

            if (repoLink[0].value === "") {
                return reject(new Error("Repository link cannot be blank"));
            }

            if (sinceDate !== "" && untilDate !== "" && moment(sinceDate).isAfter(moment(untilDate))) {
                return reject(new Error('"Since Date" should be earlier than "Until Date".'));
            }

            if (originalityThreshold < 0 || originalityThreshold > 1) {
                return reject(new Error("Originality Threshold should be between 0 - 1."));
            }

            if (timeZone === ""){
                return reject(new Error("Time Zone Not Selected."));
            }

            if (jobType === "scheduled"){
                if (startHour === "--" || startMinute === "--") {
                    return reject(new Error("Start Time is incomplete."))
                }
    
                if (startDate === "" || endDate === "") {
                    return reject(new Error("Start and End Date of schedule job is incomplete."))
                }
    
                if (startDate !== "" && endDate !== "" && moment(startDate).isAfter(moment(endDate))) {
                    return reject(new Error('"Start Date" should be earlier than "End Date".'))
                }    
            }

            return resolve();
        });
    }

    //Submit Job Form
    const submitJobForm = async () => {
        let formData = {};
        validateForm().then(() => {
            handleModalClose();
            showSuccessBar("Job Created Successfully");
            console.log("Submitting job form for", localStorage.getItem("JWT"));
            formData = {
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
                formatChipValues,
                jobType,
                frequency,
                startHour,
                startMinute,
            };
        }).catch((error) => {
            console.error("Form Input Failed Validation", error);
            showErrorBar(error);
        });

        try {
            const response = await fetch('https://this-is-a-fake-url.com', {
                method: "POST",
                headers: {
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
                <Box className={classes.modal}>
                    {renderJobFormHeader()}
                    {currentPage === 1 ? renderJobFormPage1() : renderJobFormPage2()}
                    <div className="navigation-buttons-container">
                        {renderNavigationButtons()}
                    </div>
                </Box>
            </Modal>

        </div>
    );
}

export default CreateJobComponent;