import React from 'react';
import styles from './JobList.module.css';

function JobItem({owner, jobName, jobId, status, lastUpdated, nextScheduled, icon, view, edit, run}) {
    const statusClass = status.toLowerCase();

    const handleViewReport = () => {
        window.open(`https://rsc-reports-dev.s3.ap-southeast-1.amazonaws.com/${owner}/${jobId}/reposense-report/index.html`, '_blank');
    }

    const handleRun = async () => {
        try {
            const response = await fetch(`http://localhost:3002/api/jobs/start/${jobId}`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
            });
            console.log('Job run successfully:', response.data);
            // Optionally, trigger a reload or UI update here
        } catch (error) {
            console.error('Failed to run job:', error);
        }
    };

    return (

        <tr className={styles.jobItem}>
            <td className={styles.jobInfo}>
                <input type="checkbox" className={styles.checkBox}/>
                <span className={styles.jobName}>{jobName}</span>
            </td>
            <td className={`${styles.jobStatus} ${styles[statusClass]}`}>
                <div className={styles.statusIndicator}/>
                <span>{status}</span>
            </td>
            <td className={styles.jobTiming}>
                <div className={styles.timeInfo}>
                    <span className={styles.time}>{lastUpdated.time}</span>
                    <span className={styles.date}>{lastUpdated.date}</span>
                </div>
            </td>
            <td className={styles.jobTiming}>
                <div className={styles.timeInfo}>
                    <span className={styles.time}>{nextScheduled.time}</span>
                    <span className={styles.date}>{nextScheduled.date}</span>
                </div>
            </td>
            <td className={styles.jobActions}>
                <button className={styles.iconButton} onClick={() => handleViewReport()}>
                    <img src="view.svg" alt="View" className={styles.actionIcon}/>
                </button>
                <img src="edit.svg" alt="Edit" className={styles.actionIcon}/>
                <button className={styles.iconButton} onClick={() => handleRun()}>
                    <img src="rerun.svg" alt="Run" className={styles.actionIcon} />
                </button>
            </td>
            <td>

                <button className={styles.moreOptionsButton}>
                    <text className={styles.moreOptionsText}>. . .</text>
                </button>
            </td>
        </tr>
    );
}

export default JobItem;