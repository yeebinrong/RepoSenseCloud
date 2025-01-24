package com.hamburger.job.models.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hamburger.job.models.Job;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Repository
public class JobDbDao {

    private final DynamoDbTable<Job> jobTable;
    private final DynamoDbClient dynamoDbClient;

    @Autowired
    public JobDbDao(DynamoDbClient dynamoDbClient, DynamoDbEnhancedClient enhancedDynamoDbClient) {
            this.jobTable = enhancedDynamoDbClient.table("rsc-localhost-job-data", TableSchema.fromBean(Job.class));
            this.dynamoDbClient = dynamoDbClient;
    }

    //TODO: should only return specific user's jobs
    public List<Job> getAllJobs() {
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