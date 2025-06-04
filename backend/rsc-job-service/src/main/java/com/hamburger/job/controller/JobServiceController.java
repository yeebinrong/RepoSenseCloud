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
import com.hamburger.job.util.JobUserAuth;
import com.hamburger.job.util.JwtHelper;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/jobs")
public class JobServiceController {
    private final JobService jobService;
    private final JobUserAuth jobUserAuth;
    private final JwtHelper jwtHelper;
    private final S3Service s3Service;
    private static final String env = "prod"; // change to prod to use auth

    @Autowired
    public JobServiceController(JobService jobService, JobUserAuth jobUserAuth, JwtHelper jwtHelper, S3Service s3Service) {
        this.jobService = jobService;
        this.jobUserAuth = jobUserAuth;
        this.jwtHelper = jwtHelper;
        this.s3Service = s3Service;
    }


    @GetMapping(value = "/", params = {"page", "limit"})
    public ResponseEntity<List<Job>> getJobsByPage( @RequestParam int page, @RequestParam int limit, HttpServletRequest request) {
        System.out.println("retrieving " + page + " of jobs with " + limit + " jobs per page");
        try {
            String jwtToken;
            jwtToken = jwtHelper.extractJwtFromRequest(request);
            String owner = env.equals("dev") ? "*" : jobUserAuth.authorizeAction(jwtToken).getBody();
            if(owner == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.status(200).body(jobService.getJobsByPage(owner, page, limit));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{jobId}")
    public  ResponseEntity<Optional<Job>> getJobById( @PathVariable("jobId") String jobId, HttpServletRequest request) {
        System.out.println("retrieving job with id " + jobId);
        try {
            String jwtToken;
            jwtToken = jwtHelper.extractJwtFromRequest(request);
            String owner = env.equals("dev") ? "*" : jobUserAuth.authorizeAction(jwtToken).getBody();
            if(owner == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.status(200).body(jobService.getJobsById(owner, jobId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/")
    public ResponseEntity<Optional<List<Job>>> getAllJobs(HttpServletRequest request) {
        System.out.println("retrieving jobs");
        try {
            String jwtToken;
            jwtToken = jwtHelper.extractJwtFromRequest(request);
            String owner = env.equals("dev") ? "*" : jobUserAuth.authorizeAction(jwtToken).getBody();
            if(owner == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.status(200).body(jobService.getAllJobs(owner));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search/{keyword}")
    public  ResponseEntity<Optional<List<Job>>> getJobsByKeyword( @PathVariable("keyword") String keyword, HttpServletRequest request) {
        System.out.println("retrieving jobs with keyword " + keyword);
        try {
            String jwtToken;
            jwtToken = jwtHelper.extractJwtFromRequest(request);
            String owner = env.equals("dev") ? "*" : jobUserAuth.authorizeAction(jwtToken).getBody();
            if(owner == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.ok(jobService.getJobsByKeyword(owner, keyword));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // BR: we can remove this endpoint coz the the s3 link i just open to public and generate based on owner + job id
    // @GetMapping("/report/{jobId}") //TODO: returns s3 link
    // public ResponseEntity<String> getReport(@PathVariable("jobId") String jobId, HttpServletRequest request) {
    //     System.out.println("retrieving report");
    //     try {
    //         String jwtToken;
    //         jwtToken = jwtHelper.extractJwtFromRequest(request);
    //         String owner = env.equals("dev") ? "*" : jobUserAuth.authorizeAction(jwtToken).getBody();
    //         if(owner == null){
    //             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    //         }
    //         return ResponseEntity.status(200).body(jobService.getReport(jobId));
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    //     }
    // }

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
    public ResponseEntity<Void> createJob(@RequestBody Job job, HttpServletRequest request) {
        System.out.println("creating job");
        try {
            String jwtToken;
            jwtToken = jwtHelper.extractJwtFromRequest(request);
            String owner = env.equals("dev") ? "*" : jobUserAuth.authorizeAction(jwtToken).getBody();
            if(owner == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            job.setOwner(owner);
            jobService.createJob(job);
            return ResponseEntity.status(201).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/start/{jobId}")
    public ResponseEntity<Void> startJob( @PathVariable("jobId") String jobId, HttpServletRequest request) {
        System.out.println("starting job with id " + jobId);
        try {
            String jwtToken;
            jwtToken = jwtHelper.extractJwtFromRequest(request);
            String owner = env.equals("dev") ? "*" : jobUserAuth.authorizeAction(jwtToken).getBody();
            if(owner == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            jobService.startJob(owner, jobId);
            return ResponseEntity.status(202).build();
        } catch (StartJobException e) {
            System.out.println("start job exception at job controller e: "+ e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            System.out.println("general exception at job controller e: "+ e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/edit/{jobId}")
    public ResponseEntity<Void> updateJob(@RequestBody Job job, HttpServletRequest request) {
        System.out.println("updating job with id " + job.getJobId());
        try {
            String jwtToken;
            jwtToken = jwtHelper.extractJwtFromRequest(request);
            String owner = env.equals("dev") ? "*" : jobUserAuth.authorizeAction(jwtToken).getBody();
            if(owner == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            job.setOwner(owner);
            jobService.editJob(job);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete/{jobId}")
    public ResponseEntity<Void> deleteJob( @PathVariable("jobId") String jobId, HttpServletRequest request) {
        System.out.println("deleting job with id " + jobId);
        try {
            String jwtToken;
            jwtToken = jwtHelper.extractJwtFromRequest(request);
            String owner = env.equals("dev") ? "*" : jobUserAuth.authorizeAction(jwtToken).getBody();
            if(owner == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            jobService.deleteJob(owner, jobId);
            return ResponseEntity.status(200).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/s3-presigned-url")
    public ResponseEntity<String> getS3PresignedUrl(@RequestParam String jobId, HttpServletRequest request) {
        try {
            String jwtToken = jwtHelper.extractJwtFromRequest(request);
            String owner = env.equals("dev") ? "*" : jobUserAuth.authorizeAction(jwtToken).getBody();
            if (owner == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String url = s3Service.generateS3PresignedUrl(owner, jobId, 15); // 15 minutes expiry
            if (url == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Folder or report not found");
            }
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

//     @DeleteMapping("/delete-all")
//     public ResponseEntity<Void> deleteAllJobs() {
//         String owner = env.equals("dev") ? "*" : username;
//         System.out.println("deleting all jobs");
//         try {
//             jobService.deleteAllJob(owner);
//             return ResponseEntity.ok().build();
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//         }
//     }

//     @DeleteMapping("/delete-all-scheduled")
//     public ResponseEntity<Void> deleteAllScheduledJobs() {
//         String owner = env.equals("dev") ? "*" : username;
//         System.out.println("deleting all upcoming scheduled jobs");
//         try {
//             jobService.deleteAllScheduledJobs(owner);
//             return ResponseEntity.ok().build();
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//         }
//     }

//     @DeleteMapping("/delete-all-completed")
//     public ResponseEntity<Void> deleteAllCompletedJobs() {
//         String owner = env.equals("dev") ? "*" : username;
//         System.out.println("deleting all completed jobs");
//         try {
//             jobService.deleteAllCompletedJobs(owner);
//             return ResponseEntity.ok().build();
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//         }
//     }
}
