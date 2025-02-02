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
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        // Create table
        CreateTableRequest request = CreateTableRequest.builder()
                .tableName("rsc-localhost-job-data")
                .keySchema(KeySchemaElement.builder()
                        .attributeName("owner")
                        .keyType(KeyType.HASH)
                        .build(),
                        KeySchemaElement.builder()
                        .attributeName("jobId")
                        .keyType(KeyType.RANGE)
                        .build())
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName("owner")
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                        AttributeDefinition.builder()
                        .attributeName("jobId")
                        .attributeType(ScalarAttributeType.S)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();

        if (!dynamoDbClient.listTables().tableNames().contains("rsc-localhost-job-data")) {
            dynamoDbClient.createTable(request);
        }


        jobDbDao = new JobDbDao(dynamoDbClient, enhancedClient);
    }

    // @AfterEach
    // public void tearDown() {
    //     dynamoDbClient.deleteTable(DeleteTableRequest.builder().tableName("rsc-localhost-job-data").build());
    //     dynamoDbClient.close();
    // }

    @Test
    public void testTableCreation() {
        ListTablesResponse listTablesResponse = dynamoDbClient.listTables();
        assertTrue(listTablesResponse.tableNames().contains("rsc-localhost-job-data"));
    }

    @Test
    public void testGetAllJobs() {
        // Insert a job
        Job job = new Job();
        job.setJobId("job1"); // Ensure jobId is set
        job.setOwner("owner1");
        jobTable.putItem(job);

        // Retrieve jobs
        List<Job> jobs = jobDbDao.getAllJobs("owner1");

        assertEquals(1, jobs.size());
        assertEquals("owner1", jobs.get(0).getOwner());
        assertEquals("job1", jobs.get(0).getJobId());
    }
}