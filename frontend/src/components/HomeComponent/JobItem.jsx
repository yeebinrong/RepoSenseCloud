import React, { useState } from 'react';
import styles from './JobList.module.css';
import { showSuccessBar, showErrorBar } from "../../constants/snack-bar";
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import Divider from '@mui/material/Divider';
import Tooltip from '@mui/material/Tooltip';
import axios from 'axios';
import CreateJobComponent from '../CreateJobComponent/CreateJobComponent';

function JobItem({owner, jobName, jobId, status, lastUpdated, nextScheduled, settingsUpdatedAt, icon, view, edit, run, ...jobProps}) {
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
        jobId,
        jobName,
        ...jobProps
    };

    return (

        <tr className={styles.jobItem}>
            <td className={styles.jobInfo}>
                <input type="checkbox" className={styles.checkBox}/>
                <span className={styles.jobName} style={{ marginLeft: "15px" }}>{jobName}</span>
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
                    <span className={styles.time}>{nextScheduled.time}</span>
                    <span className={styles.date}>{nextScheduled.date}</span>
                </div>
            </td>
            <td className={styles.jobTiming} style={{ width: '16%' }}>
                <div className={styles.timeInfo}>
                    <span className={styles.time}>{settingsUpdatedAt.time}</span>
                    <span className={styles.date}>{settingsUpdatedAt.date}</span>
                </div>
            </td>
            <td className={styles.jobActions}>
                <button className={styles.iconButton} onClick={() => handleViewReport()}>
                    <img src="view.svg" alt="View" className={styles.actionIcon}/>
                </button>
                <button className={styles.iconButton} onClick={() => setEditModalOpen(true)}>
                    <img src="edit.svg" alt="Edit" className={styles.actionIcon}/>
                </button>
                {editModalOpen && (
                    <CreateJobComponent
                        mode="edit"
                        jobData={jobData}
                        open={editModalOpen}
                        onClose={() => setEditModalOpen(false)}
                    />
                )}
                <button className={styles.iconButton} onClick={() => handleRun()}>
                    <img src="rerun.svg" alt="Run" className={styles.actionIcon} />
                </button>
            </td>
            <td style={{ width: "5%"}}>
                <div style={{ position: 'relative', display: 'inline-block' }}>
                    <Tooltip title="More options" arrow>
                        <button className={styles.moreOptionsButton} onClick={handleOptionsOpen}>
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
                        <MenuItem onClick={handleDownloadReport} style={{ fontFamily: "DM Sans" }}>Download Report</MenuItem>
                        <Divider />
                        <MenuItem onClick={handleCopyiframe} style={{ fontFamily: "DM Sans" }}>Copy iframe</MenuItem>
                        <Divider />
                        <MenuItem onClick={handleDelete} style={{ fontFamily: "DM Sans" }}>Delete</MenuItem>
                    </Menu>
                </div>
            </td>
        </tr>
    );
}

export default JobItem;