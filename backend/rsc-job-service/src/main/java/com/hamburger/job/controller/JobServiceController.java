package com.hamburger.job.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hamburger.job.models.Job;
import com.hamburger.job.models.exceptions.StartJobException;
import com.hamburger.job.service.JobService;
import com.hamburger.job.service.S3Service;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/jobs")
public class JobServiceController {
    private final JobService jobService;
    private final S3Service s3Service;

    @Autowired
    public JobServiceController(JobService jobService, S3Service s3Service) {
        this.jobService = jobService;
        this.s3Service = s3Service;
    }

    @GetMapping(value = "", params = {"page", "limit"})
    public ResponseEntity<List<Job>> getJobsByPage(@RequestParam int page, @RequestParam int limit, HttpServletRequest request) {
        try {
            String owner = (String) request.getAttribute("owner");
            return ResponseEntity.ok(jobService.getJobsByPage(owner, page, limit));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<Optional<Job>> getJobById(@PathVariable("jobId") String jobId, HttpServletRequest request) {
        try {
            String owner = (String) request.getAttribute("owner");
            return ResponseEntity.ok(jobService.getJobsById(owner, jobId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("")
    public ResponseEntity<Optional<List<Job>>> getAllJobs(HttpServletRequest request) {
        try {
            String owner = (String) request.getAttribute("owner");
            return ResponseEntity.ok(jobService.getAllJobs(owner));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search/{keyword}")
    public ResponseEntity<Optional<List<Job>>> getJobsByKeyword(@PathVariable("keyword") String keyword, HttpServletRequest request) {
        try {
            String owner = (String) request.getAttribute("owner");
            return ResponseEntity.ok(jobService.getJobsByKeyword(owner, keyword));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/save")
    public ResponseEntity<Void> saveJob() {
        try {
            jobService.saveJob();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createJob(@RequestBody Job job, HttpServletRequest request) {
        try {
            String owner = (String) request.getAttribute("owner");
            job.setOwner(owner);
            jobService.createJob(job);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/start/{jobId}")
    public ResponseEntity<Void> startJob(@PathVariable("jobId") String jobId, HttpServletRequest request) {
        try {
            String owner = (String) request.getAttribute("owner");
            jobService.startJob(owner, jobId);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (StartJobException e) {
            System.out.println("StartJobException: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/edit/{jobId}")
    public ResponseEntity<Void> updateJob(@RequestBody Job job, HttpServletRequest request) {
        try {
            String owner = (String) request.getAttribute("owner");
            job.setOwner(owner);
            jobService.editJob(job);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete/{jobId}")
    public ResponseEntity<Void> deleteJob(@PathVariable("jobId") String jobId, HttpServletRequest request) {
        try {
            String owner = (String) request.getAttribute("owner");
            jobService.deleteJob(owner, jobId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/s3-presigned-url")
    public ResponseEntity<String> getS3PresignedUrl(@RequestParam String jobId, HttpServletRequest request) {
        try {
            String owner = (String) request.getAttribute("owner");
            String url = s3Service.generateS3PresignedUrl(owner, jobId, 15);
            if (url == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Folder or report not found");
            }
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
