package com.hamburger.job.controller;
import com.hamburger.job.objects.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobServiceRoutes {

    @GetMapping("/jobs")
    public getJobs() {
        System.out.println("retrieving jobs");
        return;
    }

    @GetMapping("/jobs?page={page}&limit={limit}")
    public getJobsByPage(@PathVariable int page, @PathVariable int limit) {
        System.out.println("retrieving " + page + " of jobs with " + limit + " jobs per page");
        return;
    }

    @GetMapping("/jobs/{id}")
    public getJobById(@PathVariable int id) {
        System.out.println("retrieving job with id " + id);
        return;
    }

    @GetMapping("/jobs/search/{keyword}")
    public getJobsByKeyword(@PathVariable String keyword) {
        System.out.println("retrieving jobs with keyword " + keyword);
        return;
    }

    @PostMapping("/jobs/save") //double check use case
    public saveJob() {
        System.out.println("saving all job");
        return;
    }

    @PostMapping("/jobs/create")
    public createJob(@RequestBody Job job) {
        System.out.println("creating job");
        return;
    }

    @PostMapping("/jobs/edit/{jobId}")
    public editJob(@PathVariable int jobId, @RequestBody Job job) {
        System.out.println("editing job with id " + job-id);
        return;
    }

    @PostMapping("/jobs/start/{jobId}")
    public startJob(@PathVariable int jobId) {
        System.out.println("starting job with id " + jobId);
        return;
    }

    @PatchMapping("/jobs/edit/{jobId}")
    public updateJob(@PathVariable int jobId, @RequestBody Job job) {
        System.out.println("updating job with id " + jobId);
        return;
    }

    @DeleteMapping("/jobs/delete/{jobId}")
    public deleteJob(@PathVariable int jobId) {
        System.out.println("deleting job with id " + jobId);
        return;
    }

    @DeleteMapping("/jobs/delete-all")
    public deleteAllJobs() {
        System.out.println("deleting all jobs");
        return;
    }

    @DeleteMapping("/jobs/delete-all-scheduled")
    public deleteAllScheduledJobs() {
        System.out.println("deleting all upcoming scheduled jobs");
        return;
    }

    @DeleteMapping("/jobs/delete-all-completed")
    public deleteAllCompletedJobs() {
        System.out.println("deleting all completed jobs");
        return;
    }
}
