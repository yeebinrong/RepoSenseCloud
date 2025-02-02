package com.hamburger.job.models.dao;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hamburger.job.models.Job;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

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

    public List<Job> getAllJobs(String owner) {
        try {
            System.out.println("Getting all jobs for owner: " + owner);
            // Create a query condition to find all items with the specified owner
            QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder()
                    .partitionValue(owner)
                    .build());

            System.out.println("Query Conditional: " + queryConditional);
            return jobTable.query(r -> r.queryConditional(queryConditional))
                    .stream()
                    .flatMap(page -> page.items().stream())
                    .toList();

        } catch (Exception e) {
            System.out.println("Error getting all jobs: " + e.getMessage());
        }

        return null;
    }

    public List<Job> getJobsByPage(int page, int limit) {
        
        return null;
    }

    public Job getJobsById(int id) {
        
        return null;
    }

    public List<Job> getJobsByKeyword(String keyword) {
        
        return null;
    }

    public String getReport (int jobId) {
        
        return null; //return s3 presigned url
    }

    public void saveJob() {
        
    }

    public void createJob(Job job) {
        
    }

    public void startJob(int jobId) {
        
    }

    public void editJob(int jobId, Job job) {
        
    }

    public void deleteJob(int jobId) {
        
    }

    public void deleteAllJob() {
        
    }

    public void deleteAllScheduledJobs() {
        
    }

    public void deleteAllCompletedJobs() {
        
    }


}