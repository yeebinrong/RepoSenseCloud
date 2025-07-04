import React, { useEffect, useState } from 'react';
import styles from './JobList.module.css';
import JobItem from './JobItem';
import axios from 'axios';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';
import PropTypes from 'prop-types';

function SortButton({ column, sortColumn, sortOrder, onSort, label }) {
    return (
        <button
            type="button"
            onClick={() => onSort(column)}
            aria-label={`Sort by ${label} (${sortOrder === 'asc' && sortColumn === column ? 'ascending' : 'descending'})`}
            style={{ background: 'none', border: 'none', padding: 0, marginLeft: 4, cursor: 'pointer' }}
        >
            <ArrowUpwardIcon
                className={styles.sortButton}
                fontSize="small"
                style={{ color: sortColumn === column && sortOrder === 'asc' ? '#444' : '#B3B3B3', verticalAlign: 'middle' }}
            />
            <ArrowDownwardIcon
                className={styles.sortButton}
                fontSize="small"
                style={{ color: sortColumn === column && sortOrder === 'desc' ? '#444' : '#B3B3B3', verticalAlign: 'middle' }}
            />
        </button>
    );
}

SortButton.propTypes = {
    column: PropTypes.string.isRequired,
    sortColumn: PropTypes.string.isRequired,
    sortOrder: PropTypes.string.isRequired,
    onSort: PropTypes.func.isRequired,
    label: PropTypes.string.isRequired,
};

function JobList({ refreshKey, searchKeyword }) {
    const [jobData, setJobData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [sortColumn, setSortColumn] = useState('lastUpdated');
    const [sortOrder, setSortOrder] = useState('desc');

    useEffect(() => {
        function UpdateJobData() {
            setLoading(true);
            const token = localStorage.getItem('token');
            let url = `${process.env.REACT_APP_JOB_SERVICE_URL}`;
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
                    setTimeout(() => {
                        setLoading(false);
                    }, 500);
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
        nextScheduledJob: job => job.frequency || '',
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
                        <th style={{ width: '18%' }}>
                            {/* <input type="checkbox" className={styles.checkBox} /> */}
                            <span className={styles.headerText} style={{ marginLeft: "30px" }}>Job Name</span>
                            <SortButton
                                column="jobName"
                                sortColumn={sortColumn}
                                sortOrder={sortOrder}
                                onSort={handleSort}
                                label="job name"
                            />
                        </th>
                        <th style={{ width: '15%' }}>
                            <span className={styles.headerText}>Status</span>
                            <SortButton
                                column="status"
                                sortColumn={sortColumn}
                                sortOrder={sortOrder}
                                onSort={handleSort}
                                label="status"
                            />
                        </th>
                        <th style={{ width: '15%' }}>
                            <span className={styles.headerText}>Last Updated</span>
                            <SortButton
                                column="lastUpdated"
                                sortColumn={sortColumn}
                                sortOrder={sortOrder}
                                onSort={handleSort}
                                label="last updated"
                            />
                        </th>
                        <th style={{ width: '16%' }}>
                            <span className={styles.headerText}>Next Scheduled Job</span>
                            <SortButton
                                column="nextScheduledJob"
                                sortColumn={sortColumn}
                                sortOrder={sortOrder}
                                onSort={handleSort}
                                label="next scheduled job"
                            />
                        </th>
                        <th style={{ width: '16%' }}>
                            <span className={styles.headerText}>Settings Updated At</span>
                            <SortButton
                                column="settingsUpdatedAt"
                                sortColumn={sortColumn}
                                sortOrder={sortOrder}
                                onSort={handleSort}
                                label="settings updated at"
                            />
                        </th>
                        <th style={{ width: '20%' }}>
                            <span className={styles.actionText}>View</span>
                            <span className={styles.actionText}>Edit</span>
                            <span className={styles.actionText}>Run</span>
                            <span className={styles.actionText}></span>
                        </th>
                    </tr>
                </thead>
                <tbody>
                    {!loading && sortedJobData.length === 0 && (
                        <tr>
                            <td style={{ padding: "40px 0", textAlign: "center" }}>
                            <div style={{ marginTop: "-150px", color: "#888", fontFamily: "DM Sans", fontSize: "16px" }}>
                                <strong>No jobs found</strong>
                                <div style={{ fontSize: "14px", color: "#aaa" }}>
                                Try adjusting your search term or create a new job.
                                </div>
                            </div>
                            </td>
                        </tr>
                    )}
                    {!loading && sortedJobData?.map((job, index) => (
                        <JobItem key={index} {...job} />
                    ))}
                    {loading  &&
                        Array(6).fill(0).map((_, i) => (
                        <tr key={i} className={styles.jobItem}>
                            {/* Job Name */}
                            <td className={styles.jobInfo}>
                            <div
                                className={styles.skeletonBox}
                                style={{ marginLeft: "30px", width: "140px", animationDuration: "1.4s" }}
                            />
                            </td>

                            {/* Status */}
                            <td className={styles.jobStatus}>
                            <div
                                className={styles.skeletonCircle}
                                style={{ animationDuration: "1.6s" }}
                            />
                            <div
                                className={styles.skeletonBox}
                                style={{ width: "60px", animationDuration: "1.6s" }}
                            />
                            </td>

                            {/* Last Updated */}
                            <td className={styles.jobTiming} style={{ width: '15%' }}>
                            <div className={styles.timeInfo}>
                                <div
                                className={styles.skeletonBox}
                                style={{ width: "120px", animationDuration: "1.5s" }}
                                />
                                <div
                                className={styles.skeletonBox}
                                style={{ width: "80px", animationDuration: "1.7s" }}
                                />
                            </div>
                            </td>

                            {/* Flavor Text */}
                            <td className={styles.jobTiming} style={{ width: '16%' }}>
                            <div className={styles.timeInfo}>
                                <div
                                className={styles.skeletonBox}
                                style={{ width: "120px", animationDuration: "1.6s" }}
                                />
                                <div
                                className={styles.skeletonBox}
                                style={{ width: "80px", animationDuration: "1.8s" }}
                                />
                            </div>
                            </td>

                            {/* Settings Updated */}
                            <td className={styles.jobTiming} style={{ width: '16%' }}>
                            <div className={styles.timeInfo}>
                                <div
                                className={styles.skeletonBox}
                                style={{ width: "120px", animationDuration: "1.5s" }}
                                />
                                <div
                                className={styles.skeletonBox}
                                style={{ width: "80px", animationDuration: "1.7s" }}
                                />
                            </div>
                            </td>

                            {/* Action Icons */}
                            <td className={styles.jobActions}>
                            <div
                                className={styles.skeletonIcon}
                                style={{ animationDuration: "1.3s" }}
                            />
                            <div
                                className={styles.skeletonIcon}
                                style={{ animationDuration: "1.4s" }}
                            />
                            <div
                                className={styles.skeletonIcon}
                                style={{ animationDuration: "1.5s" }}
                            />
                            </td>

                            {/* More Options */}
                            <td style={{ width: "5%", justifyItems: "center" }}>
                            <div
                                className={styles.skeletonCircle}
                                style={{ animationDuration: "1.6s" }}
                            />
                            </td>
                        </tr>
                        ))}
                </tbody>
            </table>
        </section>
    );
}

export default JobList;
