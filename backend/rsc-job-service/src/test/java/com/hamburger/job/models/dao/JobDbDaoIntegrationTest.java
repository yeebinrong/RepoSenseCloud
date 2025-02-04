package com.hamburger.job.models.dao;

import com.hamburger.job.models.Job;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class JobDbDaoIntegrationTest {

    private DynamoDbClient dynamoDbClient;
    private DynamoDbEnhancedClient enhancedClient;
    private DynamoDbTable<Job> jobTable;
    private JobDbDao jobDbDao;

    @BeforeEach
    public void setUp() {
        dynamoDbClient = DynamoDbClient.builder()
                .endpointOverride(URI.create("http://localhost:4566"))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("dummy-access-key", "dummy-secret-key")))
                .build();

        enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();

        jobTable = enhancedClient.table("rsc-localhost-job-data", TableSchema.fromBean(Job.class));

        // Create table if it doesn't exist
        if (!dynamoDbClient.listTables().tableNames().contains("rsc-localhost-job-data")) {
            CreateTableEnhancedRequest createTableRequest = CreateTableEnhancedRequest.builder()
                    .provisionedThroughput(ProvisionedThroughput.builder()
                            .readCapacityUnits(5L)
                            .writeCapacityUnits(5L)
                            .build())
                    .build();
            jobTable.createTable(createTableRequest);
        }

        jobDbDao = new JobDbDao(dynamoDbClient,enhancedClient);
    }

    @AfterEach
    public void tearDown() {
        dynamoDbClient.deleteTable(DeleteTableRequest.builder().tableName("rsc-localhost-job-data").build());
        dynamoDbClient.close();
    }

    @Test
    public void testTableCreation() {
        ListTablesResponse listTablesResponse = dynamoDbClient.listTables();
        assertTrue(listTablesResponse.tableNames().contains("rsc-localhost-job-data"));
    }

    @Test
    public void testGetAllJobs() {
        // Insert a job
        Job job = new Job();
        job.setJobId("job1");
        job.setOwner("owner1");
        jobTable.putItem(job);

        // Retrieve jobs
        List<Job> jobs = jobDbDao.getAllJobs("owner1")
            .orElseThrow(() -> new RuntimeException("No jobs found"));

        assertEquals(1, jobs.size());
        assertEquals("owner1", jobs.get(0).getOwner());
        assertEquals("job1", jobs.get(0).getJobId());
    }

    @Test
    public void testGetJobsById() {
        // Insert a job
        Job job = new Job();
        job.setJobId("job1");
        job.setOwner("owner1");
        jobTable.putItem(job);

        // Retrieve job by ID
        Optional<Job> retrievedJob = jobDbDao.getJobsById("owner1", "job1");

        assertTrue(retrievedJob.isPresent());
        assertEquals("owner1", retrievedJob.get().getOwner());
        assertEquals("job1", retrievedJob.get().getJobId());
    }

    @Test
    public void testGetJobsByKeyword() {
        // Insert a job
        Job job = new Job();
        job.setJobId("job1");
        job.setOwner("owner1");
        job.setJobName("test");
        jobTable.putItem(job);

        // Retrieve jobs by keyword
        Optional<List<Job>> jobs = jobDbDao.getJobsByKeyword("owner1", "test");

        assertTrue(jobs.isPresent());
        assertEquals(1, jobs.get().size());
        assertEquals("owner1", jobs.get().get(0).getOwner());
        assertEquals("job1", jobs.get().get(0).getJobId());
    }

    @Test
    public void testCreateJob() {
        // Create a job
        Job job = new Job();
        job.setJobId("job1");
        job.setOwner("owner1");
        jobDbDao.createJob(job);

        // Retrieve job by ID
        Optional<Job> retrievedJob = jobDbDao.getJobsById("owner1", "job1");

        assertTrue(retrievedJob.isPresent());
        assertEquals("owner1", retrievedJob.get().getOwner());
        assertEquals("job1", retrievedJob.get().getJobId());
    }

    @Test
    public void testStartJob() {
        // Insert a job
        Job job = new Job();
        job.setJobId("job1");
        job.setOwner("owner1");
        job.setStatus("PENDING");
        jobTable.putItem(job);

        // Start the job
        jobDbDao.startJob("owner1", "job1");

        // Retrieve job by ID
        Optional<Job> retrievedJob = jobDbDao.getJobsById("owner1", "job1");

        assertTrue(retrievedJob.isPresent());
        assertEquals("RUNNING", retrievedJob.get().getStatus());
    }

    @Test
    public void testEditJob() {
        // Insert a job
        Job job = new Job();
        job.setJobId("job1");
        job.setOwner("owner1");
        job.setStatus("COMPLETED");
        jobTable.putItem(job);

        // Edit the job
        Job jobReplacement = new Job();
        jobReplacement.setJobId("job1");
        jobReplacement.setOwner("owner1");
        jobReplacement.setStatus("UPDATED");
        jobDbDao.editJob(jobReplacement);

        // Retrieve job by ID
        Optional<Job> retrievedJob = jobDbDao.getJobsById("owner1", "job1");

        assertTrue(retrievedJob.isPresent());
        assertEquals("UPDATED", retrievedJob.get().getStatus());
    }

    @Test
    public void testDeleteJob() {
        // Insert a job
        Job job = new Job();
        job.setJobId("job1");
        job.setOwner("owner1");
        jobTable.putItem(job);

        // Delete the job
        jobDbDao.deleteJob("owner1", "job1");

        // Retrieve job by ID
        Optional<Job> retrievedJob = jobDbDao.getJobsById("owner1", "job1");

        assertFalse(retrievedJob.isPresent());
    }

    @Test
    public void testGetJobsByOwner() {
        // Insert a job
        Job job = new Job();
        job.setJobId("job1");
        job.setOwner("owner1");
        jobTable.putItem(job);

        // Retrieve jobs by owner
        List<Job> jobs = jobDbDao.getJobsByOwner("owner1");

        assertEquals(1, jobs.size());
        assertEquals("owner1", jobs.get(0).getOwner());
        assertEquals("job1", jobs.get(0).getJobId());
    }

    @Test
    public void testGetJobsByOwnerAndStatus() {
        // Insert a job
        Job job = new Job();
        job.setJobId("job1");
        job.setOwner("owner1");
        job.setStatus("SCHEDULED");
        jobTable.putItem(job);

        // Retrieve jobs by owner and status
        List<Job> jobs = jobDbDao.getJobsByOwnerAndStatus("owner1", "SCHEDULED");

        assertEquals(1, jobs.size());
        assertEquals("owner1", jobs.get(0).getOwner());
        assertEquals("job1", jobs.get(0).getJobId());
        assertEquals("SCHEDULED", jobs.get(0).getStatus());
    }
}