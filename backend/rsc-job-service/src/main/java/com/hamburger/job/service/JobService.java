package com.hamburger.job.service;
import com.hamburger.job.models.Job;
import com.hamburger.job.models.dao.JobDbDao;
import com.hamburger.job.models.exceptions.StartJobException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Collections;

@Service
public class JobService {
    private final JobDbDao jobDbDao;

    @Autowired
    public JobService(JobDbDao jobDbDao) {
        this.jobDbDao = jobDbDao;
    }

    public Optional<List<Job>> getAllJobs(String owner) {
        return jobDbDao.getAllJobs(owner);
    }

    public List<Job> getJobsByPage(String owner, int page, int limit) {
        //TODO: test this
        List<Job> allJobs = (jobDbDao.getAllJobs(owner) == null) ? Collections.emptyList() : jobDbDao.getAllJobs(owner).get();
        return allJobs.subList((page - 1) * limit, Math.min(page * limit, allJobs.size()));
    }

    public Optional<Job> getJobsById(String owner, String jobId) {
        return jobDbDao.getJobsById(owner, jobId);
    }

    public Optional<List<Job>> getJobsByKeyword(String owner, String keyword) {
        return jobDbDao.getJobsByKeyword(owner, keyword);
    }

    public String getReport (String jobId) {
        return jobDbDao.getReport(jobId);
        //return s3 presigned url
    }

    public void saveJob() {
        jobDbDao.saveJob();
    }

    public void createJob(Job job){
        jobDbDao.createJob(job);
    }

    public void startJob(String owner, String jobId) {
        jobDbDao.startJob(owner, jobId);
    }

    public void editJob(Job job) {
        jobDbDao.editJob(job);
    }

    public void deleteJob(String owner, String jobId) {
        jobDbDao.deleteJob(owner, jobId);
    }

    // public void deleteAllJob(String owner) {
    //     jobDbDao.deleteAllJob(owner);
    // }

    // public void deleteAllScheduledJobs(String owner) {
    //     jobDbDao.deleteAllScheduledJobs(owner);
    // }

    // public void deleteAllCompletedJobs(String owner) {
    //     jobDbDao.deleteAllCompletedJobs(owner);
    // }
}