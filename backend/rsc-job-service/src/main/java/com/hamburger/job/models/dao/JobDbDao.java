package com.hamburger.job.models.dao;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hamburger.job.models.Job;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

@Repository
public class JobDbDao {

    private final DynamoDbTable<Job> jobTable;
    private final DynamoDbClient dynamoDbClient;

    @Autowired
    public JobDbDao(DynamoDbClient dynamoDbClient, DynamoDbEnhancedClient enhancedDynamoDbClient) {
            this.jobTable = enhancedDynamoDbClient.table("rsc-localhost-job-data", TableSchema.fromBean(Job.class));
            this.dynamoDbClient = DynamoDbClient.builder()
                .endpointOverride(URI.create("http://localhost:4566"))
                .build();
    }

    public Optional<List<Job>> getAllJobs(String owner) {
        List<Job> jobs = new ArrayList<>();
        Map<String, AttributeValue> lastEvaluatedKey = null;
    
    try {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
            Key.builder().partitionValue(owner).build()
        );

        do {
            QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                    .queryConditional(queryConditional);

            // Apply pagination only if there's a lastEvaluatedKey from the previous query
            if (lastEvaluatedKey != null) {
                requestBuilder.exclusiveStartKey(lastEvaluatedKey);
            }

            PageIterable<Job> queryResult = jobTable.query(requestBuilder.build());

            for (Page<Job> page : queryResult) {
                jobs.addAll(page.items()); // Collect jobs
                lastEvaluatedKey = page.lastEvaluatedKey(); // Store lastEvaluatedKey for next iteration
            }
        } while (lastEvaluatedKey != null); // Keep paginating until no more results
        return Optional.ofNullable(jobs);
    } catch (Exception e) {
        System.err.println("Error retrieving jobs: " + e.getMessage());
        e.printStackTrace();
        return Optional.empty(); // Return an empty Optional on failure
    }
}
    // public List<Job> getJobsByPage(String owner, int page, int limit) {
        
    //     return null;
    // }

    public Optional<Job> getJobsById(String owner, String jobId) {
        //TODO: test this
        try{
            Job job = jobTable.getItem(Key.builder().partitionValue(owner).sortValue(jobId).build());
            return Optional.ofNullable(job);
        } catch (Exception e) {
            System.err.println("Error retrieving job: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty(); // Return an empty Optional on failure   
        }
    }

    public Optional<List<Job>> getJobsByKeyword(String owner, String keyword) {
        //TODO: test this
        try {
            // Build the filter expression
            Expression filterExpression = Expression.builder()
                    .expression("contains(jobName, :keyword) AND #owner = :owner")
                    .putExpressionValue(":keyword", AttributeValue.builder().s(keyword).build())
                    .putExpressionName("#owner","owner")
                    .putExpressionValue(":owner", AttributeValue.builder().s(owner).build())
                    .build();
            System.out.println("filterExpression: " + filterExpression);
            // Build the scan request
            ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
                    .filterExpression(filterExpression)
                    .build();

            // Execute the scan and return the results
            List<Job> jobs = jobTable.scan(scanRequest).items().stream().collect(Collectors.toList());
            System.out.println("jobs: " + jobs);
            return Optional.of(jobs);
        } catch (Exception e) {
            System.err.println("Error retrieving jobs: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty(); // Return an empty Optional on failure
        }
    }

    public String getReport (String jobId) {
        
        return null; //return s3 presigned url
    }

    public void saveJob() {
        
    }

    public void createJob(Job job) {
        try{
            jobTable.putItem(job);
        }catch (Exception e) {
            System.err.println("Error creating job: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void startJob(String owner, String jobId) {
        //TODO: test this
        String newStatus = "RUNNING";

        try {
            // Retrieve the existing job
            Job job = jobTable.getItem(Key.builder()
                    .partitionValue(owner)
                    .sortValue(jobId)
                    .build());
    
            if (job != null && "PENDING".equals(job.getStatus())) {
                // Update the status
                job.setStatus(newStatus);
                jobTable.updateItem(job);
                System.out.println("Job started successfully.");
            } else {
                System.err.println("Job cannot be started because it is not in the PENDING state or Job doesn't exist.");
            }
        } catch (Exception e) {
            System.err.println("Error starting job: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    public void editJob(Job jobReplacement) {
        //TODO: test this
        try {
            // Retrieve the existing job
            Job jobTarget = jobTable.getItem(Key.builder()
                    .partitionValue(jobReplacement.getOwner())
                    .sortValue(jobReplacement.getJobId())
                    .build());
    
            if (jobTarget != null && "COMPLETED".equals(jobTarget.getStatus())) {
                // Update the status
                jobTable.updateItem(jobReplacement);
                System.out.println("Job updated  successfully.");
            } else {
                System.err.println("Job cannot be updated because it is completed or Job doesn't exist.");
            }
        } catch (Exception e) {
            System.err.println("Error starting job: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteJob(String owner, String jobId) {
        //TODO: test this
        try {
            // Retrieve the existing job
            Job jobTarget = jobTable.getItem(Key.builder()
                    .partitionValue(owner)
                    .sortValue(jobId)
                    .build());
            
            if (jobTarget != null) {
                jobTable.deleteItem(jobTarget);
                System.out.println("Job deleted successfully.");
            } else {
                System.err.println("Job doesn't exist.");
            }

        } catch (Exception e) {
            System.err.println("Error deleting job: " + e.getMessage());
            e.printStackTrace();

        }
    }

    public void deleteAllJob(String owner) {
        //TODO: test this
        try {
            // Retrieve the existing jobs
            List<Job> jobs = getJobsByOwner(owner);
            
            if (jobs != null) {
                for (Job job : jobs) {
                    jobTable.deleteItem(job);
                }
                System.out.println("All jobs deleted successfully.");
            } else {
                System.err.println("No jobs found.");
            }
        } catch (Exception e) {
            System.err.println("Error deleting all jobs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteAllScheduledJobs(String owner) {
        //TODO: test this
        try {
            // Retrieve jobs of owner and scheduled status
            List<Job> jobs = getJobsByOwnerAndStatus(owner,"SCHEDULED");
            
            if (jobs != null) {
                for (Job job : jobs) {
                    jobTable.deleteItem(job);
                }
                System.out.println("All scheduled jobs deleted successfully.");
            } else {
                System.err.println("No jobs found.");
            }
        } catch (Exception e) {
            System.err.println("Error deleting all jobs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteAllCompletedJobs(String owner) {
                //TODO: test this
                try {
                    // Retrieve jobs of owner and completed status
                    List<Job> jobs = getJobsByOwnerAndStatus(owner,"COMPLETED");
                    
                    if (jobs != null) {
                        for (Job job : jobs) {
                            jobTable.deleteItem(job);
                        }
                        System.out.println("All scheduled jobs deleted successfully.");
                    } else {
                        System.err.println("No jobs found.");
                    }
                } catch (Exception e) {
                    System.err.println("Error deleting all jobs: " + e.getMessage());
                    e.printStackTrace();
                }
    }

    public List<Job> getJobsByOwner(String owner) {
        // Build the filter expression
        Expression filterExpression = Expression.builder()
                .expression("#owner = :owner")
                .putExpressionName("#owner", "owner")
                .putExpressionValue(":owner", AttributeValue.builder().s(owner).build())
                .build();

        // Build the scan request
        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
                .filterExpression(filterExpression)
                .build();

        // Execute the scan and return the results
        return jobTable.scan(scanRequest).items().stream().collect(Collectors.toList());
    }

    public List<Job> getJobsByOwnerAndStatus(String owner, String status) {
        // Build the filter expression
        Expression filterExpression = Expression.builder()
                .expression("#owner = :owner and #status = :status")
                .putExpressionName("#owner", "owner")
                .putExpressionValue(":owner", AttributeValue.builder().s(owner).build())
                .putExpressionName("#status", "status")
                .putExpressionValue(":status", AttributeValue.builder().s(status).build())
                .build();

        // Build the scan request
        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
                .filterExpression(filterExpression)
                .build();

        // Execute the scan and return the results
        return jobTable.scan(scanRequest).items().stream().collect(Collectors.toList());
    }


}