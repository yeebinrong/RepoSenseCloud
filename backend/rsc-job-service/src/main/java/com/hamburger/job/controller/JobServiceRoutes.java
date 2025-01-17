package com.hamburger.job.controller;
import com.hamburger.job.objects.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobServiceRoutes {

    @GetMapping("/")
    public getJobs() {
        System.out.println("retrieving jobs");
        return;
    }

    @GetMapping(params = {"page", "limit"})
    public getJobsByPage(@RequestParam int page, @RequestParam int limit) {
        System.out.println("retrieving " + page + " of jobs with " + limit + " jobs per page");
        return;
    }

    @GetMapping("/{id}")
    public getJobById(@PathVariable int id) {
        System.out.println("retrieving job with id " + id);
        return;
    }

    @GetMapping("/search/{keyword}")
    public getJobsByKeyword(@PathVariable String keyword) {
        System.out.println("retrieving jobs with keyword " + keyword);
        return;
    }

    @PostMapping("/save") //double check use case
    public saveJob() {
        System.out.println("saving all job");
        return;
    }

    @PostMapping("/create")
    public createJob(@RequestBody Job job) {
        System.out.println("creating job");
        return;
    }

    @PostMapping("/edit/{jobId}")
    public editJob(@PathVariable int jobId, @RequestBody Job job) {
        System.out.println("editing job with id " + job-id);
        return;
    }

    @PostMapping("/start/{jobId}")
    public startJob(@PathVariable int jobId) {
        System.out.println("starting job with id " + jobId);
        return;
    }

    @PatchMapping("/edit/{jobId}")
    public updateJob(@PathVariable int jobId, @RequestBody Job job) {
        System.out.println("updating job with id " + jobId);
        return;
    }

    @DeleteMapping("/delete/{jobId}")
    public deleteJob(@PathVariable int jobId) {
        System.out.println("deleting job with id " + jobId);
        return;
    }

    @DeleteMapping("/delete-all")
    public deleteAllJobs() {
        System.out.println("deleting all jobs");
        return;
    }

    @DeleteMapping("/delete-all-scheduled")
    public deleteAllScheduledJobs() {
        System.out.println("deleting all upcoming scheduled jobs");
        return;
    }

    @DeleteMapping("/delete-all-completed")
    public deleteAllCompletedJobs() {
        System.out.println("deleting all completed jobs");
        return;
    }
}
