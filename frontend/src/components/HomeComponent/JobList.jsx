import React from 'react';
import styles from './JobList.module.css';
import JobItem from './JobItem';

const jobData = [
    {
        name: 'Peerprep Job',
        status: 'Completed',
        lastUpdated: {time: '10.45PM', date: '13th Sep 2024'},
        nextScheduled: {time: '12.00PM', date: '4th Sep 2024'},
    },
    {
        name: 'SPA Job',
        status: 'Running',
        lastUpdated: {time: '10.45PM', date: '13th Sep 2024'},
        nextScheduled: {time: '12.00PM', date: '4th Sep 2024'},
    },
    {
        name: 'Bitcoin Job',
        status: 'Failed',
        lastUpdated: {time: '10.45PM', date: '13th Sep 2024'},
        nextScheduled: {time: '12.00PM', date: '4th Sep 2024'},
    },
    {
        name: 'A Job',
        status: 'Completed',
        lastUpdated: {time: '10.45PM', date: '13th Sep 2024'},
        nextScheduled: {time: '12.00PM', date: '4th Sep 2024'},
    },
    {
        name: 'B Job',
        status: 'Running',
        lastUpdated: {time: '10.45PM', date: '13th Sep 2024'},
        nextScheduled: {time: '12.00PM', date: '4th Sep 2024'},
    },
    {
        name: 'C Job',
        status: 'Failed',
        lastUpdated: {time: '10.45PM', date: '13th Sep 2024'},
        nextScheduled: {time: '12.00PM', date: '4th Sep 2024'},
    },
    {
        name: 'D Job',
        status: 'Completed',
        lastUpdated: {time: '10.45PM', date: '13th Sep 2024'},
        nextScheduled: {time: '12.00PM', date: '4th Sep 2024'},
    },
    {
        name: 'E Job',
        status: 'Completed',
        lastUpdated: {time: '10.45PM', date: '13th Sep 2024'},
        nextScheduled: {time: '12.00PM', date: '4th Sep 2024'},
    }
];

function JobList() {
    return (
        <table className={styles.jobListContainer}>
            <tr className={styles.jobListHeader}>
                <th >
                    <input type="checkbox" className={styles.checkBox}/>
                    <span className={styles.headerText}>Job Name</span>
                    <img src="vector.svg" alt="" className={styles.sortIcon}/>
                </th>
                <th>
                    <span className={styles.headerText}>Status</span>
                    <img src="vector.svg" alt="" className={styles.sortIcon}/>
                </th>
                <th>
                    <span className={styles.headerText}>Last Updated</span>
                    <img src="vector.svg" alt="" className={styles.sortIcon}/>
                </th>
                <th>
                    <span className={styles.headerText}>Next Scheduled Job</span>
                    <img src="vector.svg" alt="" className={styles.sortIcon}/>
                </th>
                <th>
                    <span className={styles.actionText}>View</span>
                    <span className={styles.actionText}>Edit</span>
                    <span className={styles.actionText}>Run</span>
                </th>
            </tr>

                <hr/>

            {jobData.map((job, index) => (
                <JobItem key={index} {...job}/>
            ))}

        </table>
    );
}

export default JobList;