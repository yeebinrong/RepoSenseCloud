import React, { useState } from 'react';
import styles from './JobList.module.css';
import { showSuccessBar, showErrorBar } from "../../constants/snack-bar";
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import Divider from '@mui/material/Divider';
import Tooltip from '@mui/material/Tooltip';
import axios from 'axios';
import CreateJobComponent from '../CreateJobComponent/CreateJobComponent';

function getJobFlavorTextA({ jobType, frequency, startHour, startMinute }) {
    console.log("getJobFlavorText called with:", { jobType, frequency, startHour, startMinute });
    if (jobType === 'manual') {
        return 'Manual';
    }

    if (jobType !== 'scheduled') {
        return '-';
    }

    switch (frequency) {
        case 'minutely':
            return `Run every 5 minutes`;

        case 'hourly':
            if (startMinute === '--' || isNaN(parseInt(startMinute))) {
                return 'Runs hourly';
            }
            return `Runs hourly at HH:${startMinute.padStart(2, '0')}`;

        case 'daily':
            if (startHour === '--' || startMinute === '--' || isNaN(parseInt(startHour)) || isNaN(parseInt(startMinute))) {
                return 'Runs daily';
            }
            return `Runs daily at ${startHour.padStart(2, '0')}:${startMinute.padStart(2, '0')}`;

        default:
            return 'Unknown frequency setting.';
    }
}

function getJobFlavorTextB({ jobType, startDate, endDate }) {
    if (jobType === 'manual') {
        return '';
    }

    if (jobType !== 'scheduled') {
        return '-';
    }

    return `${startDate || '--'} to ${endDate || '--'}`;
}


function JobItem(props) {
    const {owner, jobName, jobId, status, prevStatus, lastUpdated, jobType, startHour, startMinute, frequency, settingsUpdatedAt, icon, view, edit, run, ...jobProps} = props;
    const statusClass = status.toLowerCase();

    const handleViewReport = () => {
        window.open(`${process.env.REACT_APP_REPORT_BUCKET_URL}/${owner}/${jobId}/reposense-report/index.html`, '_blank');
    }

    const handleRun = async () => {
        try {
            const response = await fetch(`${process.env.REACT_APP_JOB_SERVICE_URL}/start/${jobId}`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                },
                credentials: 'include',
            });
            if (!response.ok) {
                throw new Error(`Job run error! status: ${response.status}`);
            }
            console.log('Job run successfully:', response.data);
            window.dispatchEvent(new Event('updateJobData'));
            showSuccessBar("Job Started Successfully");
            // Optionally, trigger a reload or UI update here
        } catch (error) {
            showErrorBar("Failed to run job");
            console.error('Failed to run job:', error);
        }
    };

    const handleDownloadReport = async () => {
        handleOptionsClose();
        try {
            const response = await axios.get(`${process.env.REACT_APP_JOB_SERVICE_URL}/s3-presigned-url`, {
                params: { jobId },
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                },
                withCredentials: true,
            });
            const presignedUrl = response.data;
            window.open(presignedUrl, '_blank');
            showSuccessBar('Download started');
        } catch (error) {
            showErrorBar('Failed to get download link');
            console.error('Failed to get presigned url:', error);
        }
    };

    const handleCopyiframe = () => {
        handleOptionsClose();
        const iframeCode = `<iframe src="${process.env.REACT_APP_REPORT_BUCKET_URL}/${owner}/${jobId}/reposense-report/index.html" frameBorder="0" width="800px" height="616px"></iframe>`;
        navigator.clipboard.writeText(iframeCode)
            .then(() => {
                showSuccessBar('Copied iframe to clipboard');
            })
            .catch(err => {
                showErrorBar('Failed to copy iframe');
                console.error('Failed to copy iframe:', err);
            });
    }

    const handleDelete = async () => {
        handleOptionsClose();
        try {
            const response = await axios.delete(`${process.env.REACT_APP_JOB_SERVICE_URL}/delete/${jobId}`, {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                },
                withCredentials: true,
            });
            if (response.status === 200) {
                showSuccessBar('Job deleted successfully');
                window.dispatchEvent(new Event('updateJobData'));
            } else {
                showErrorBar('Failed to delete job');
            }
        } catch (error) {
            showErrorBar('Failed to delete job');
            console.error('Failed to delete job:', error);
        }
    };

    const [menuAnchorEl, setMenuAnchorEl] = useState(null);
    const menuOpen = Boolean(menuAnchorEl);
    const [editModalOpen, setEditModalOpen] = useState(false);

    const handleOptionsOpen = (event) => {
        setMenuAnchorEl(event.currentTarget);
    };
    const handleOptionsClose = () => {
        setMenuAnchorEl(null);
    };

    const jobData = {
        ...props,
        jobId,
        jobName,
        status,
        prevStatus,
    };

    return (
        <>
        <tr className={styles.jobItem}>
            <td className={styles.jobInfo}>
                {/* <input type="checkbox" className={styles.checkBox}/> */}
                <span className={styles.jobName} style={{ marginLeft: "30px" }}>{jobName}</span>
            </td>
            <td className={`${styles.jobStatus} ${styles[statusClass]}`}>
                <div className={styles.statusIndicator}/>
                <span>{status}</span>
            </td>
            <td className={styles.jobTiming} style={{ width: '15%' }}>
                <div className={styles.timeInfo}>
                    <span className={styles.time}>{lastUpdated.time}</span>
                    <span className={styles.date}>{lastUpdated.date}</span>
                </div>
            </td>
            <td className={styles.jobTiming} style={{ width: '16%' }}>
                <div className={styles.timeInfo}>
                    <span className={styles.time}>{getJobFlavorTextA(jobData)}</span>
                    <span className={styles.date}>{getJobFlavorTextB(jobData)}</span>
                </div>
            </td>
            <td className={styles.jobTiming} style={{ width: '16%' }}>
                <div className={styles.timeInfo}>
                    <span className={styles.time}>{settingsUpdatedAt?.time}</span>
                    <span className={styles.date}>{settingsUpdatedAt?.date}</span>
                </div>
            </td>
            <td className={styles.jobActions}>
                <button className={styles.iconButton} disabled = {status !== "Completed" && !(status === "Running" && prevStatus === "Completed")} onClick={() => handleViewReport()}>
                    <img src="view.svg" alt="View" className={`${styles.actionIcon} ${status !== "Completed" && !(status === "Running" && prevStatus === "Completed") ? styles.actionIconDisabled : ''}`}/>
                </button>
                <button className={styles.iconButton} onClick={() => setEditModalOpen(true)}>
                    <img src="edit.svg" alt="Edit" className={styles.actionIcon}/>
                </button>
                <button className={styles.iconButton} disabled = { status === "Running" } onClick={() => handleRun()}>
                    <img src="rerun.svg" alt="Run" className={styles.actionIcon} />
                </button>
            </td>
            <td style={{ width: "5%"}}>
                <div style={{ position: 'relative', display: 'inline-block' }}>
                    <Tooltip title="More options" arrow>
                        <button
                        className={`${styles.moreOptionsButton}`}
                        onClick={handleOptionsOpen}
                        >
                        <span className={styles.moreOptionsText}>. . .</span>
                        </button>
                    </Tooltip>
                    <Menu
                        anchorEl={menuAnchorEl}
                        open={menuOpen}
                        onClose={handleOptionsClose}
                        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
                        transformOrigin={{ vertical: 'top', horizontal: 'right' }}
                    >
                        <MenuItem onClick={handleDownloadReport} disabled= {status !== "Completed" && !(status === "Running" && prevStatus === "Completed")} style={{ fontFamily: "DM Sans" }}>Download Report</MenuItem>
                        <Divider />
                        <MenuItem onClick={handleCopyiframe} disabled= {status !== "Completed" && !(status === "Running" && prevStatus === "Completed")} style={{ fontFamily: "DM Sans" }}>Copy iframe</MenuItem>
                        <Divider />
                        <MenuItem onClick={handleDelete} style={{ fontFamily: "DM Sans" }}>Delete</MenuItem>
                    </Menu>
                </div>
            </td>
        </tr>
        {editModalOpen && (
            <CreateJobComponent
                mode="edit"
                jobData={jobData}
                open={editModalOpen}
                onClose={() => setEditModalOpen(false)}
            />
        )}
        </>
    );
}

export default JobItem;