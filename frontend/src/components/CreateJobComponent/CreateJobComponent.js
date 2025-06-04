import React, { useState, useEffect, use } from "react";
import moment from "moment-timezone";
import axios from "axios";
import { Autocomplete, TextField, Grid2, Chip, Modal, Box, 
    Button, Select, FormControl, InputLabel, MenuItem, Stack, CircularProgress } from "@mui/material";
import { makeStyles } from "@mui/styles";
import PageIcon from "../../assets/icons/page-icon.svg";
import "./CreateJobComponent.scss";
import { showSuccessBar, showErrorBar } from "../../constants/snack-bar";
import { v4 as uuidv4 } from 'uuid';

const useStyles = makeStyles(() => ({
    autocomplete: {
        width: '100%',
    },
    chip: {
        margin: '4px',
        backgroundColor: '#e0f7fa',
        color: '#00695c',
    },
    textField: {
        fontFamily: 'DM Sans',
    },
    modal: {
        position: 'relative',
        top: '50%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        width: '1115px',
        height: '95vh',
        //overflowY: 'auto', 
        backgroundColor: 'white',
        padding: '16px',
        borderRadius: '16px',
        display: 'flex',
        flexDirection: 'column',
        flex: '1',
    },
}));

const CreateJobComponent = ({
  mode = "create",
  jobData = null,
  open: controlledOpen = undefined,
  onClose: controlledOnClose = undefined,
}) => {
    const token = localStorage.getItem("token");

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
    const isControlled = controlledOpen !== undefined;
    const modalOpen = isControlled ? controlledOpen : open;
    const handleModalOpen = () => {
        if (isControlled) return;
        setOpen(true);
    };
    const handleModalClose = () => {
        if (controlledOnClose) controlledOnClose();
        if (!isControlled) setOpen(false);
    };

    //Page 1 States
    const [jobName, setJobName] = useState("");
    const [repoLink, setRepoLink] = useState([{ id: Date.now(), value: "" }]);
    const [periodMode, setPeriodMode] = useState("Specific Date Range");
    const [sinceDate, setSinceDate] = useState("");
    const [untilDate, setUntilDate] = useState("");
    const [period, setPeriod] = useState("");
    const [periodModifier, setPeriodModifier] = useState("latest");
    const [originalityThreshold, setOriginalityThreshold] = useState(0.5);
    const [timeZone, setTimeZone] = useState("UTC+08");
    const [authorship, setAuthorship] = useState(false);
    const [prevAuthors, setPrevAuthors] = useState(false);
    const [shallowClone, setShallowClone] = useState(false);
    const [ignoreFileSizeLimit, setIgnoreFileSizeLimit] = useState(false);
    const [addLastMod, setAddLastMod] = useState(false);
    const [formatChipValues, setFormatChipValues] = useState([]);

    //Page 2 States
    const [jobType, setJobType] = useState("manual");
    const [frequency, setFrequency] = useState('');
    const [startMinute, setStartMinute] = useState('--');
    const [startHour, setStartHour] = useState('--');
    const [startDate, setStartDate] = useState("");
    const [endDate, setEndDate] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    //Form Validation States
    const [page1Error, setPage1Error] = useState(false);
    const [page2Error, setPage2Error] = useState(false);
    const [jobNameError, setJobNameError] = useState(true);
    const [repoLinkError, setRepoLinkError] = useState(true);
    const [sinceUntilDateError, setSinceUntilDateError] = useState(false);
    const [originalityThresholdError, setOriginalityThresholdError] = useState(false);
    const [timeZoneError, setTimeZoneError] = useState(false);
    const [startHourError, setStartHourError] = useState(true);
    const [startMinuteError, setStartMinuteError] = useState(true);
    const [dateError, setDateError] = useState(false);

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
            setOriginalityThreshold(0.5);
            setTimeZone("UTC+08");
            setAuthorship(false);
            setPrevAuthors(false);
            setShallowClone(false);
            setIgnoreFileSizeLimit(false);
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
            setPage1Error(false);
            setPage2Error(false);
            setJobNameError(true);
            setRepoLinkError(true);
            setSinceUntilDateError(false);
            setOriginalityThresholdError(false);
            setTimeZoneError(false);
            setStartHourError(true);
            setStartMinuteError(true);
            setDateError(false);
        }
    }, [open]);

    // Set state when editing modal opens
    useEffect(() => {
        if (mode === "edit" && jobData && modalOpen) {
            setCurrentPage(1);
            // page 1 states
            setJobName(jobData.jobName || "");
            setRepoLink(
                jobData.repoLink
                ? jobData.repoLink.split(" ").map((value, idx) => ({ id: Date.now() + idx, value }))
                : [{ id: Date.now(), value: "" }]
            );
            if (jobData.period) {
                setPeriodMode("By Days/Weeks");
                setPeriod(jobData.period);
            } else {
                setPeriodMode("Specific Date Range");
                setPeriod("");
            }
            setPeriodModifier(checkEditPeriodModifier() || "latest");
            setSinceDate(jobData.sinceDate || "");
            setUntilDate(jobData.untilDate || "");
            setOriginalityThreshold(
                typeof jobData.originalityThreshold === "number" ? jobData.originalityThreshold : 0.5
            );
            setTimeZone(jobData.timeZone || "UTC+08");
            setAuthorship(!!jobData.authorship);
            setPrevAuthors(!!jobData.prevAuthors);
            setShallowClone(!!jobData.shallowClone);
            setIgnoreFileSizeLimit(!!jobData.ignoreFileSizeLimit);
            setAddLastMod(!!jobData.addLastMod);
            setFormatChipValues(jobData.formatChipValues || []);
            // page 2 states
            setJobType(jobData.jobType || "manual");
            setFrequency(jobData.frequency || "");
            setStartMinute(jobData.startMinute || "--");
            setStartHour(jobData.startHour || "--");
            setStartDate(jobData.startDate || "");
            setEndDate(jobData.endDate || "");
            // form validation states
            setPage1Error(false);
            setPage2Error(false);
            setSinceUntilDateError(false);
            setOriginalityThresholdError(false);
            setTimeZoneError(false);
            setStartHourError(true);
            setStartMinuteError(true);
            setDateError(false);
        }
    }, [mode, jobData, modalOpen]);

    useEffect(() => {
        validateJobName();
    }, [jobName]);

    useEffect(() => {
        validateRepoLink(repoLink[repoLink.length - 1].id);
    }, [repoLink]);

    //Reset period states when period mode changes
    useEffect(() => {
        if (mode !== "edit" && periodMode !== "Specific Date Range" ){
            setPeriod("7d");
            setSinceDate("");
            setUntilDate("");
        } else if (mode !== "edit" && periodMode === "Specific Date Range") {
            setPeriod("");
            setPeriodModifier("latest");
            setSinceDate("");
            setUntilDate("");
        }
    }, [mode, periodMode]);

    //Reset period states when period mode changes for edit mode
    useEffect(() => {
        if (mode === "edit" && periodMode === "By Days/Weeks") {
            setPeriod(jobData.period || "7d");
            switch (periodModifier) {
                case "latest": {
                    setSinceDate("");
                    setUntilDate("");
                    break;
                }
                case "before": {
                    setSinceDate("");
                    setUntilDate(jobData.untilDate || "");
                    break;
                }
                default: {
                    setSinceDate(jobData.sinceDate || "");
                    setUntilDate("");
                }
            }

        } else if (mode === "edit" && periodMode === "Specific Date Range") {
            setPeriod("");
            setPeriodModifier("latest");
            jobData ? setSinceDate(jobData.sinceDate) : setSinceDate("");
            jobData ? setUntilDate(jobData.untilDate) : setUntilDate("");
        }
    }, [mode, periodMode,periodModifier, jobData]);

    //Reset scheduled job states when job type changes
    useEffect(() => {
        if (mode !== "edit" && jobType !== "scheduled") {
            setFrequency("");
            setStartHour("--");
            setStartMinute("--"); 
            setStartDate("");
            setEndDate("");
        } else if (mode !== "edit" && jobType === "scheduled") {
            setFrequency("weekly");
        }
    }, [jobType]);

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
                <h1>{mode === "edit" ? "Edit Job" : "Create a Job"}</h1>
                <h4>
                {mode === "edit"
                    ? "Modify Job Details"
                    : "Fill In Job Detail To Queue Or Run A New ReposenseCloud Job"}
                </h4>
                <span className="create-job-page-status">
                <img src={PageIcon} alt="Page Icon" />
                <div className="dotted-line" />
                <img
                    src={PageIcon}
                    alt="Page Icon"
                    className={currentPage === 1 ? "page-icon2" : "page-icon1"}
                />
                </span>
            </div>
        );
    };

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
        if (link.value === "" ) {
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
        setPage1Error(hasError);
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
                                    //onChange={(e)=> { validateJobName(); setJobName(e.target.value)}} 
                                    onInput={(e)=> {setJobName(e.target.value)}}
                                    onPaste={(e)=> {setJobName(e.target.value)}}
                                    autoComplete="off"

                                    error={(jobNameError && page1Error)}        
                                    helperText={(jobNameError && page1Error) ? "Please Enter Job Name" : ""}
                                    disabled={mode === "edit"}
                                />
                            </div>
                            <div className="target-repo-container">
                                <text className="target-repo-label">Target Repository</text>
                                {repoLink.map((link, index) => (
                                    <span key={link.id}>
                                        <TextField className="target-repo-textbox" placeholder="Paste Repo URL here" value={link.value}
                                            //onChange={(e) => { validateRepoLink(link.id); handleRepoLinkChange(link.id, e.target.value) }}
                                            onInput={(e) => {handleRepoLinkChange(link.id, e.target.value) }}
                                            onPaste={(e) => {handleRepoLinkChange(link.id, e.target.value) }}
                                            autoComplete="off"
                                            error={repoLinkError && page1Error}
                                            helperText={repoLinkError && page1Error ? "Please Paste Repository URL" : ""}
                                            disabled={mode === "edit"}
                                        />
                                        {index > 0 && (<button className="delete-repo-link-button" onClick={() => deleteRepoLink(link.id)} disabled={mode === "edit"}>✕</button>)}
                                    </span>
                                ))}
                                <button className="add-repo-link-button" onClick={addRepoLink} disabled={mode === "edit"}> + Add repository</button>
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
                                                <select className="period-mode-dropdown" value = {periodMode} 
                                                    onChange={(e) => setPeriodMode(e.target.value)}>
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
                                                className={`timezone-dropdown ${timeZoneError && page1Error ? 'error' : ''}`}
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
                                            {timeZoneError && page1Error && <span className="error-message">Please select a time zone</span>}
                                        </Grid2>
                                        <Grid2 size={6} marginTop={2} className="left-checklist-container">
                                            <Grid2 container spacing={1} justifyContent="space-between">
                                                <Grid2 size={10}>
                                                    <text className="authorship-label">Analyse authorship:</text>
                                                </Grid2>
                                                <Grid2 size={2}>
                                                    <input type="checkbox" className="authorship-checkbox" checked={authorship} onChange={(e) => setAuthorship(e.target.checked)} />
                                                </Grid2>
                                                <Grid2 size={10}>
                                                    <text className="prev-author-label">Find previous authors:</text>
                                                </Grid2>
                                                <Grid2 size={2}>
                                                    <input type="checkbox" className="prev-author-checkbox" checked={prevAuthors} onChange={(e) => setPrevAuthors(e.target.checked)} />
                                                </Grid2>
                                                <Grid2 size={10}>
                                                    <text className="shallow-clone-label">Shallow cloning:</text>
                                                </Grid2>
                                                <Grid2 size={2} >
                                                    <input type="checkbox" className="shallow-clone-checkbox" checked={shallowClone} onChange={(e) => setShallowClone(e.target.checked)}/>
                                                </Grid2>
                                            </Grid2>
                                        </Grid2>
                                        <Grid2 size={6} marginTop={2} paddingLeft={6} className="right-checklist-container">
                                            <Grid2 container spacing={1} justifyContent="space-between">
                                                <Grid2 size={10} >
                                                    <text className="ignore-size-limit-label">Ignore file size limit:</text>
                                                </Grid2>
                                                <Grid2 size={2} >
                                                    <input type="checkbox" className="ignore-size-limit-checkbox" checked={ignoreFileSizeLimit} onChange={(e) => setIgnoreFileSizeLimit(e.target.checked)} />
                                                </Grid2>
                                                <Grid2 size={10}>
                                                    <text className="Add-last-mod-label">Add last modified date:</text>
                                                </Grid2>
                                                <Grid2 size={2} >
                                                    <input type="checkbox" className="add-last-mod-checkbox" checked={addLastMod} onChange={(e) => setAddLastMod(e.target.checked)} />
                                                </Grid2>

                                            </Grid2>
                                        </Grid2>
                                        <Grid2 size={2} marginTop={2}>
                                            <text className="format-label">Format:</text>
                                        </Grid2>
                                        <Grid2 size={10}>
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
        let mod = periodModifier;
        switch (mod) {
            case "before":
                return <TextField type="date" className="until-date-input2" value={untilDate} onChange={(e) => setUntilDate(e.target.value)} placeholder="DD/MM/YYYY" />

            case "after":
                return <TextField type="date" className="since-date-input2" value={sinceDate} onChange={(e) => setSinceDate(e.target.value)} placeholder="DD/MM/YYYY" />

            default:
                return <text style={{ fontFamily: "DM Sans" }}> **{period} from date of job run</text>
        }
    }

    const renderPeriodForPeriod = () => {
        return (
            <Grid2 container spacing={1}>
                <Grid2 size={6} container alignItems="center">
                    <text className="period-label">Period:</text>
                </Grid2>
                <Grid2 size={6}>
                    <select className="period-range-dropdown" value = {period} 
                        onChange={(e) => setPeriod(e.target.value)}>
                        <option value="7d">7 days</option>
                        <option value="30d">30 days</option>
                        <option value="3-month">3 Months</option>
                        <option value="6-month">6 Months</option>
                    </select>
                </Grid2>
                <Grid2 size={3} container alignItems="center">
                    
                    <select className="period-modifier-dropdown" value = {periodModifier} 
                        onChange={(e) => setPeriodModifier(e.target.value)}>
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

    const checkEditPeriodModifier = () => {
        if(jobData.sinceDate === "" && jobData.untilDate === "") {
            return "latest";
        } else if (jobData.sinceDate !== ""){
            return "after";
        } else {
            return "before";
        }
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
                        <option value={"daily"}>Daily</option>
                        <option value={"hourly"}>Hourly</option>
                        <option value={"minutely"}>Every 5 Mins</option>
                    </select>
                </Grid2>
                <Grid2 item size={4}>
                    <text className="schedule-settings-labels" >Start Time:</text>
                </Grid2>
                <Grid2 item size={2}>
                    <select
                        className={`time-dropdown  ${startHourError && page2Error ? 'error' : ''}`}
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
                            className={`time-dropdown ${startMinuteError && page2Error ? 'error' : ''}`}
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
                        error = {dateError && page2Error } placeholder="DD/MM/YYYY" />
                </Grid2>
                <Grid2 item size={4} container alignItems="center">
                    <text className="end-date-label">End Date:</text>
                </Grid2>
                <Grid2 item size={6}>
                    <TextField type="date" className="end-date-input" value = {endDate} 
                        onChange={(e) => { setEndDate(e.target.value); validateDate(startDate, e.target.value)}} placeholder="DD/MM/YYYY" 
                        onInput={(e) => {setEndDate(e.target.value); validateDate(startDate, e.target.value) }} 
                        error = {dateError && page2Error}
                        helperText={(endDate!==""&& dateError )? "Improper Date Range":""}/>
                </Grid2>
                
            </Grid2>
        )
    }

    /// Navigation Button For Both Pages
    const renderNavigationButtons = () => {
        return (
            <div className="navigation-buttons">
                <Button
                variant="contained"
                sx={{
                    backgroundColor: "#FFFFFF",
                    color: "#ADA7A7",
                    width: "125px",
                    marginRight: "50px",
                }}
                onClick={() =>
                    currentPage === 2 ? setCurrentPage(1) : handleModalClose()
                }
                >
                {currentPage === 1 ? "Cancel" : "Back"}
                </Button>
                <Button
                variant="contained"
                sx={{ backgroundColor: "#F7A81B", width: "125px" }}
                onClick={() => {
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
                }}
                disabled={isLoading}
                >
                {isLoading ? (
                    <CircularProgress size={24} />
                ) : currentPage === 2 ? (mode === "edit" ? "Update" : "Save") : "Next"}
                </Button>
            </div>
        );
    };

    const validateForm = () => {
        return new Promise((resolve, reject) => {
            setPage2Error(true);
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
            setPage2Error(false);
            return resolve();
        });
    }

    // Helper function to get time in "HH:mm UTC+0830" format
    function getTimeWithUtcOffset(timeZone) {
        if (!timeZone) return moment().format("HH:mm") + " UTC+0000";
        // Convert timeZone to a valid moment timezone string
        const tz = timeZone.replace("UTC", "Etc/GMT").replace("+", "-").replace("-", "+");
        const m = moment().tz(tz);
        // Get offset and remove colon
        const offset = m.format("Z").replace(":", ""); // e.g. "+0830"
        return `${m.format("HH:mm")} UTC${offset}`;
    }

    //Submit Job Form
    const jobServiceUrl = process.env.REACT_APP_JOB_SERVICE_URL;

    const submitJobForm = async () => {
        let formData = {};
        try {
            setIsLoading(true);
            await validateForm();
            startHour === "--" ? formData.startHour = "" : formData.startHour = startHour;
            startMinute === "--" ? formData.startMinute = "" : formData.startMinute = startMinute;
            formData = {
                jobId: mode === "edit" && jobData ? jobData.jobId : uuidv4(),
                jobName,
                repoLink: repoLink.map(link => link.value).join(" "),
                sinceDate,
                untilDate,
                period,
                originalityThreshold,
                timeZone,
                authorship,
                prevAuthors,
                shallowClone,
                ignoreFileSizeLimit,
                addLastMod,
                formatChipValues,
                jobType,
                frequency,
                startHour,
                startMinute,
                startDate,
                endDate,
                lastUpdated: {
                    time: timeZone
                        ? getTimeWithUtcOffset(timeZone)
                        : moment().format("HH:mm") + " UTC+0000",
                    date: timeZone
                        ? moment().tz(timeZone.replace("UTC", "Etc/GMT").replace("+", "-").replace("-", "+")).format("YYYY-MM-DD")
                        : moment().format("YYYY-MM-DD")
                },
                nextScheduled: {
                    time: "",
                    date: ""
                },
                settingsUpdatedAt: {
                    time: timeZone
                        ? getTimeWithUtcOffset(timeZone)
                        : moment().format("HH:mm") + " UTC+0000",
                    date: timeZone
                        ? moment().tz(timeZone.replace("UTC", "Etc/GMT").replace("+", "-").replace("-", "+")).format("YYYY-MM-DD")
                        : moment().format("YYYY-MM-DD")
                },
            };
            console.log(JSON.stringify(formData));
            let response;
            if (mode === "edit" && jobData) {
                response = await axios.patch(
                `${jobServiceUrl}/edit/${jobData.jobId}`,
                formData,
                {
                    headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                    },
                    withCredentials: true,
                }
                );
            } else {
                response = await axios.post(
                `${jobServiceUrl}/create`,
                formData,
                {
                    headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                    },
                    withCredentials: true,
                }
                );
            }
            if (response.status === 201 || response.status === 200) {
                showSuccessBar(
                mode === "edit" ? "Job Updated Successfully" : "Job Created Successfully"
                );
                window.dispatchEvent(new Event("updateJobData"));
                handleModalClose();
            } else {
                showErrorBar(mode === "edit" ? "Error Updating Job" : "Error Creating Job");
            }
        } catch (error) {
            showErrorBar(error.message);
        } finally {
            setIsLoading(false);
        }
    }

    //Main Render
    return (
        <div>
        {!isControlled && (
            <button className="create-job-button" onClick={handleModalOpen}>
            {mode === "edit" ? "Edit Job" : "Create Job"}
            </button>
        )}
        <Modal
            open={modalOpen}
            onClose={handleModalClose}
            aria-labelledby="modal-title"
            aria-describedby="modal-description"
        >
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