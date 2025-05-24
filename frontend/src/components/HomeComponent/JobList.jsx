import React, { useEffect, useState } from 'react';
import styles from './JobList.module.css';
import JobItem from './JobItem';
import axios from 'axios';

function JobList({ refreshKey, searchKeyword }) {
    const [jobData, setJobData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        function UpdateJobData() {
            const token = localStorage.getItem('token');
            let url = `${process.env.REACT_APP_JOB_SERVICE_URL}/`;
            if (searchKeyword && searchKeyword.trim() !== "") {
                url = `${process.env.REACT_APP_JOB_SERVICE_URL}/search/${searchKeyword.trim()}`;
            }
            axios.get(url, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                }
            })
                .then(res => {
                    setJobData(res.data);
                    setLoading(false);
                })
                .catch(err => {
                    console.error('Error fetching jobs:', err);
                    setError(err);
                    setLoading(false);
                });
        }

        UpdateJobData();

        window.addEventListener('updateJobData', UpdateJobData);

        return () => {
            window.removeEventListener('updateJobData', UpdateJobData);
        };
    }, [refreshKey, searchKeyword]);

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
