package com.hamburger.job.models.dao;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hamburger.job.models.Job;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
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

    } catch (Exception e) {
        System.err.println("Error retrieving jobs: " + e.getMessage());
        e.printStackTrace();
    }

    return jobs; // Return all retrieved jobs
}
    // public List<Job> getJobsByPage(String owner, int page, int limit) {
        
    //     return null;
    // }

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