import React, { useState } from 'react';
import styles from './JobManagement.module.css';
import JobList from './JobList';
import {Autocomplete, TextField} from "@mui/material";
import CreateJobComponent from "../CreateJobComponent/CreateJobComponent";
import RefreshIcon from '@mui/icons-material/Refresh';

function JobManagement() {
    const [jobListRefreshKey, setJobListRefreshKey] = useState(0);
    const [searchKeyword, setSearchKeyword] = useState("");

    return (
        <main className={styles.homePage}>
            <section className={styles.contentSection}>
                <div className={styles.titleSection}>
                    <h2 className={styles.title}>RepoSense Cloud</h2>
                    <p className={styles.description}>
                        {/* Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text. */}
                    </p>
                </div>
                <div className={styles.actionButtons} style={{ justifyContent: 'end' }}>
                    {/* <button className={styles.downloadButton}>
                        <img src="download.svg" alt="" className={styles.buttonIcon} />
                        Download All
                    </button> */}
                    {/* <button className={styles.createJobButton}>Create Job</button> */}
                    <CreateJobComponent/>
                </div>
            </section>
            <section className={styles.filterSection}>
                {/* <button className={styles.filterButton}>
                    <img src="filter.svg" alt="" className={styles.filterIcon}/>
                    Filters
                </button> */}
                <button className={styles.filterButton} onClick={() => setJobListRefreshKey(k => k + 1)}>
                    Refresh Job List
                    <RefreshIcon className={styles.refreshIcon} />
                </button>
                <div className={styles.searchContainer}>
                    <img src="search.svg" alt="" className={styles.searchIcon} />
                    <input
                        type="text"
                        placeholder="Search"
                        className={styles.searchInput}
                        aria-label="Search jobs"
                        value={searchKeyword}
                        onChange={e => setSearchKeyword(e.target.value)}
                        onKeyDown={e => {
                            if (e.key === 'Enter') setJobListRefreshKey(k => k + 1);
                        }}
                    />
                </div>
            </section>
            <JobList refreshKey={jobListRefreshKey} searchKeyword={searchKeyword}/>
        </main>
    );
}

export default JobManagement;