import React from 'react';
import styles from './JobList.module.css';

function JobItem({name, status, lastUpdated, nextScheduled, icon, view, edit, run}) {
    const statusClass = status.toLowerCase();

    return (
        <table className={styles.jobTable}>
            <tr className={styles.jobItem}>
                <td className={styles.jobInfo}>
                    <input type="checkbox" className={styles.checkBox}/>
                    <span className={styles.jobName}>{name}</span>
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
                    <img src="view.svg" alt="View" className={styles.actionIcon}/>
                    <img src="edit.svg" alt="Edit" className={styles.actionIcon}/>
                    <img src="rerun.svg" alt="Run" className={styles.actionIcon}/>
                </td>
                <td>

                    <button className={styles.moreOptionsButton}>
                        <text className={styles.moreOptionsText}>. . .</text>
                    </button>
                </td>
            </tr>
            <hr/>
        </table>
    );
}

export default JobItem;