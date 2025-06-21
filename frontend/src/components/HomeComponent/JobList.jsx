import React, { useEffect, useState } from 'react';
import styles from './JobList.module.css';
import JobItem from './JobItem';
import axios from 'axios';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';
import {Skeleton} from "@mui/material";

function JobList({ refreshKey, searchKeyword }) {
    const [jobData, setJobData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [sortColumn, setSortColumn] = useState('lastUpdated');
    const [sortOrder, setSortOrder] = useState('desc');

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

    const parseJobDateTime = (job, column) => {
        const item = job[column];
        if (!item || !item.date || !item.time) return new Date(0);
        const dateStr = item.date;
        const [timeStr, tzStr] = item.time.split(' ');
        const [hour, minute] = timeStr.split(':');
        const timezone = tzStr ? tzStr : '+00:00';
        const isoStr = `${dateStr}T${hour.padStart(2, '0')}:${minute.padStart(2, '0')}:00${timezone.replace('UTC', '')}`;
        return new Date(isoStr);
    };

    const sortByColumn = {
        jobName: job => job.jobName || '',
        status: job => job.status || '',
        lastUpdated: job => parseJobDateTime(job, 'lastUpdated'),
        nextScheduledJob: job => parseJobDateTime(job, 'nextScheduledJob'),
        settingsUpdatedAt: job => parseJobDateTime(job, 'settingsUpdatedAt'),
    };

    const sortedJobData = [...jobData].sort((a, b) => {
        const c = sortByColumn[sortColumn];
        if (!c) return 0;
        const x = c(a);
        const y = c(b);
        if (typeof x === 'string' && typeof y === 'string') {
            return sortOrder === 'asc' ? x.localeCompare(y) : y.localeCompare(x);
        } else if (x instanceof Date && y instanceof Date) {
            return sortOrder === 'asc' ? x - y : y - x;
        }
        return 0;
    });

    const handleSort = (column) => {
        if (sortColumn === column) {
            setSortOrder((prev) => (prev === 'asc' ? 'desc' : 'asc'));
        } else {
            setSortColumn(column);
            setSortOrder('asc');
        }
    };

    if (error) return <p style={{ fontFamily: "DM Sans" }}>Error loading jobs: {error.message}</p>;

    return (
        <section className={styles.jobListSection}>
            <table className={styles.jobListContainer}>
                <thead>
                    <tr className={styles.jobListHeader}>
                        <th>
                            <input type="checkbox" className={styles.checkBox} />
                            <span className={styles.headerText} style={{ marginLeft: "15px" }}>Job Name</span>
                            <button
                                type="button"
                                className={styles.sortButton}
                                onClick={() => handleSort('jobName')}
                                aria-label={`Sort by job name (${sortOrder === 'asc' && sortColumn === 'jobName' ? 'ascending' : 'descending'})`}
                                style={{ background: 'none', border: 'none', padding: 0, marginLeft: 4, cursor: 'pointer' }}
                            >
                                <ArrowUpwardIcon
                                    fontSize="small"
                                    style={{ color: sortColumn === 'jobName' && sortOrder === 'asc' ? '#444' : '#B3B3B3', verticalAlign: 'middle' }}
                                />
                                <ArrowDownwardIcon
                                    fontSize="small"
                                    style={{ color: sortColumn === 'jobName' && sortOrder === 'desc' ? '#444' : '#B3B3B3', verticalAlign: 'middle' }}
                                />
                            </button>
                        </th>
                        <th>
                            <span className={styles.headerText}>Status</span>
                            <button
                                type="button"
                                className={styles.sortButton}
                                onClick={() => handleSort('status')}
                                aria-label={`Sort by status (${sortOrder === 'asc' && sortColumn === 'status' ? 'ascending' : 'descending'})`}
                                style={{ background: 'none', border: 'none', padding: 0, marginLeft: 4, cursor: 'pointer' }}
                            >
                                <ArrowUpwardIcon
                                    fontSize="small"
                                    style={{ color: sortColumn === 'status' && sortOrder === 'asc' ? '#444' : '#B3B3B3', verticalAlign: 'middle' }}
                                />
                                <ArrowDownwardIcon
                                    fontSize="small"
                                    style={{ color: sortColumn === 'status' && sortOrder === 'desc' ? '#444' : '#B3B3B3', verticalAlign: 'middle' }}
                                />
                            </button>
                        </th>
                        <th>
                            <span className={styles.headerText}>Last Updated</span>
                            <button
                                type="button"
                                className={styles.sortButton}
                                onClick={() => handleSort('lastUpdated')}
                                aria-label={`Sort by last updated (${sortOrder === 'asc' && sortColumn === 'lastUpdated' ? 'ascending' : 'descending'})`}
                                style={{ background: 'none', border: 'none', padding: 0, marginLeft: 4, cursor: 'pointer' }}
                            >
                                <ArrowUpwardIcon
                                    fontSize="small"
                                    style={{ color: sortColumn === 'lastUpdated' && sortOrder === 'asc' ? '#444' : '#B3B3B3', verticalAlign: 'middle' }}
                                />
                                <ArrowDownwardIcon
                                    fontSize="small"
                                    style={{ color: sortColumn === 'lastUpdated' && sortOrder === 'desc' ? '#444' : '#B3B3B3', verticalAlign: 'middle' }}
                                />
                            </button>
                        </th>
                        <th>
                            <span className={styles.headerText}>Next Scheduled Job</span>
                            <button
                                type="button"
                                className={styles.sortButton}
                                onClick={() => handleSort('nextScheduledJob')}
                                aria-label={`Sort by next scheduled job (${sortOrder === 'asc' && sortColumn === 'nextScheduledJob' ? 'ascending' : 'descending'})`}
                                style={{ background: 'none', border: 'none', padding: 0, marginLeft: 4, cursor: 'pointer' }}
                            >
                                <ArrowUpwardIcon
                                    fontSize="small"
                                    style={{ color: sortColumn === 'nextScheduledJob' && sortOrder === 'asc' ? '#444' : '#B3B3B3', verticalAlign: 'middle' }}
                                />
                                <ArrowDownwardIcon
                                    fontSize="small"
                                    style={{ color: sortColumn === 'nextScheduledJob' && sortOrder === 'desc' ? '#444' : '#B3B3B3', verticalAlign: 'middle' }}
                                />
                            </button>
                        </th>
                        <th>
                            <span className={styles.headerText}>Settings Updated At</span>
                            <button
                                type="button"
                                className={styles.sortButton}
                                onClick={() => handleSort('settingsUpdatedAt')}
                                aria-label={`Sort by settings updated at (${sortOrder === 'asc' && sortColumn === 'settingsUpdatedAt' ? 'ascending' : 'descending'})`}
                                style={{ background: 'none', border: 'none', padding: 0, marginLeft: 4, cursor: 'pointer' }}
                            >
                                <ArrowUpwardIcon
                                    fontSize="small"
                                    style={{ color: sortColumn === 'settingsUpdatedAt' && sortOrder === 'asc' ? '#444' : '#B3B3B3', verticalAlign: 'middle' }}
                                />
                                <ArrowDownwardIcon
                                    fontSize="small"
                                    style={{ color: sortColumn === 'settingsUpdatedAt' && sortOrder === 'desc' ? '#444' : '#B3B3B3', verticalAlign: 'middle' }}
                                />
                            </button>
                        </th>
                        <th>
                            <span className={styles.actionText}>View</span>
                            <span className={styles.actionText}>Edit</span>
                            <span className={styles.actionText}>Run</span>
                            <span className={styles.actionText}></span>
                        </th>
                    </tr>
                </thead>
                <tbody>
                    {sortedJobData?.map((job, index) => (
                        <JobItem key={index} {...job} />
                    ))}
                    {loading &&
                        <tr>
                            <td><Skeleton /></td>
                            <td><Skeleton /></td>
                            <td><Skeleton /></td>
                            <td><Skeleton /></td>
                            <td><Skeleton /></td>
                            <td><Skeleton /></td>
                        </tr>
                    }
                </tbody>
            </table>
        </section>
    );
}

export default JobList;
