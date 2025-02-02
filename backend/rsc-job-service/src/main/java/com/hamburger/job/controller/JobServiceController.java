package com.hamburger.job.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hamburger.job.models.Job;
import com.hamburger.job.service.JobService;

@RestController
@RequestMapping("/api/jobs")
public class JobServiceController {
    private final JobService jobService;

    @Autowired
    public JobServiceController (JobService jobService){
        this.jobService = jobService;
    }

    @GetMapping("/")
    public ResponseEntity<List<Job>> getAllJobs(@AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("retrieving jobs");
        //IMPORTANT TODO: change this! should only return specific user's jobs 
        //String owner = userDetails.getUsername();
        String owner = "*";
        try {
            return ResponseEntity.ok(jobService.getAllJobs(owner));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(params = {"page", "limit"})
    public ResponseEntity<List<Job>> getJobsByPage(@RequestParam int page, @RequestParam int limit) {
        System.out.println("retrieving " + page + " of jobs with " + limit + " jobs per page");
        try {
            return ResponseEntity.ok(jobService.getJobsByPage(page, limit));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public  ResponseEntity<Job> getJobById(@PathVariable int jobId) {
        System.out.println("retrieving job with id " + jobId);
        try {
            return ResponseEntity.ok(jobService.getJobsById(jobId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search/{keyword}")
    public  ResponseEntity<List<Job>> getJobsByKeyword(@PathVariable String keyword) {
        System.out.println("retrieving jobs with keyword " + keyword);
        try {
            return ResponseEntity.ok(jobService.getJobsByKeyword(keyword));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/report/{jobId}")
    public ResponseEntity<String> getReport(@PathVariable int jobId) {
        System.out.println("retrieving report");
        try {
            return ResponseEntity.ok(jobService.getReport(jobId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/save") //double check use case
    public ResponseEntity<Void> saveJob() {
        System.out.println("saving all job");
        try {
            jobService.saveJob();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createJob(@RequestBody Job job) {
        System.out.println("creating job");
        try {
            jobService.createJob(job);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/start/{jobId}")
    public ResponseEntity<Void> startJob(@PathVariable int jobId) {
        System.out.println("starting job with id " + jobId);
        try {
            jobService.startJob(jobId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/edit/{jobId}")
    public ResponseEntity<Void> updateJob(@PathVariable int jobId, @RequestBody Job job) {
        System.out.println("updating job with id " + jobId);
        try {
            jobService.editJob(jobId,job);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete/{jobId}")
    public ResponseEntity<Void> deleteJob(@PathVariable int jobId) {
        System.out.println("deleting job with id " + jobId);
        try {
            jobService.deleteJob(jobId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<Void> deleteAllJobs() {
        System.out.println("deleting all jobs");
        try {
            jobService.deleteAllJob();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete-all-scheduled")
    public ResponseEntity<Void> deleteAllScheduledJobs() {
        System.out.println("deleting all upcoming scheduled jobs");
        try {
            jobService.deleteAllScheduledJobs();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete-all-completed")
    public ResponseEntity<Void> deleteAllCompletedJobs() {
        System.out.println("deleting all completed jobs");
        try {
            jobService.deleteAllCompletedJobs();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
