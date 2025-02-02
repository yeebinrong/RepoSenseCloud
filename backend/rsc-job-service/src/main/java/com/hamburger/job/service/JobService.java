package com.hamburger.job.service;
import com.hamburger.job.models.Job;
import com.hamburger.job.models.dao.JobDbDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {
    private final JobDbDao jobDbDao;

    @Autowired
    public JobService(JobDbDao jobDbDao) {
        this.jobDbDao = jobDbDao;
    }

    public List<Job> getAllJobs(String owner) {
        return jobDbDao.getAllJobs(owner);
    }

    public List<Job> getJobsByPage(int page, int limit) {
        return jobDbDao.getJobsByPage(page, limit);
    }

    public Job getJobsById(int jobId) {
        return jobDbDao.getJobsById(jobId);
    }

    public List<Job> getJobsByKeyword(String keyword) {
        return jobDbDao.getJobsByKeyword(keyword);
    }

    public String getReport (int jobId) {
        return jobDbDao.getReport(jobId);
        //return s3 presigned url
    }

    public void saveJob() {
        jobDbDao.saveJob();
    }

    public void createJob(Job job) {
        jobDbDao.createJob(job);
    }

    public void startJob(int jobId) {
        jobDbDao.startJob(jobId);
    }

    public void editJob(int jobId, Job job) {
        jobDbDao.editJob(jobId, job);
    }

    public void deleteJob(int jobId) {
        jobDbDao.deleteJob(jobId);
    }

    public void deleteAllJob() {
        jobDbDao.deleteAllJob();
    }

    public void deleteAllScheduledJobs() {
        jobDbDao.deleteAllScheduledJobs();
    }

    public void deleteAllCompletedJobs() {
        jobDbDao.deleteAllCompletedJobs();
    }
}