import React, { useEffect, useState } from 'react';
import styles from './JobList.module.css';
import JobItem from './JobItem';

function JobList() {
    const [jobData, setJobData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const token = localStorage.getItem('token');
        fetch(`${process.env.REACT_APP_JOB_SERVICE_URL}/`, {
            headers: {
                'Authorization': `Bearer ${token}`,
            }
        })
            .then(res => {
                if (!res.ok) throw new Error('Network response was not ok');
                return res.json();
            })
            .then(data => {
                setJobData(data);
                setLoading(false);
            })
            .catch(err => {
                console.error('Error fetching jobs:', err);
                setError(err);
                setLoading(false);
            });
    }, []);

    if (loading) return <p>Loading jobs...</p>;
    if (error) return <p>Error loading jobs: {error.message}</p>;

    return (
        <table className={styles.jobListContainer}>
            <thead>
                <tr className={styles.jobListHeader}>
                    <th>
                        <input type="checkbox" className={styles.checkBox} />
                        <span className={styles.headerText}>Job Name</span>
                        <img src="vector.svg" alt="" className={styles.sortIcon} />
                    </th>
                    <th>
                        <span className={styles.headerText}>Status</span>
                        <img src="vector.svg" alt="" className={styles.sortIcon} />
                    </th>
                    <th>
                        <span className={styles.headerText}>Last Updated</span>
                        <img src="vector.svg" alt="" className={styles.sortIcon} />
                    </th>
                    <th>
                        <span className={styles.headerText}>Next Scheduled Job</span>
                        <img src="vector.svg" alt="" className={styles.sortIcon} />
                    </th>
                    <th>
                        <span className={styles.actionText}>View</span>
                        <span className={styles.actionText}>Edit</span>
                        <span className={styles.actionText}>Run</span>
                    </th>
                </tr>
            </thead>
            <tbody>
                {jobData?.map((job, index) => (
                    <JobItem key={index} {...job} />
                ))}
            </tbody>
        </table>
    );
}

export default JobList;
