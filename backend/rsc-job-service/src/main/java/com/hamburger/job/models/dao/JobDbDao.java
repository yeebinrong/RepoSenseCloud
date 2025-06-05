package com.hamburger.job.models.dao;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hamburger.job.models.Job;
import com.hamburger.job.models.exceptions.StartJobException;
import com.hamburger.job.service.SqsService;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Repository
public class JobDbDao {

    private final DynamoDbTable<Job> jobTable;
    private final DynamoDbClient dynamoDbClient;
    private final SqsService sqsService;

    @Autowired
    public JobDbDao(DynamoDbClient dynamoDbClient, DynamoDbEnhancedClient enhancedDynamoDbClient, SqsService sqsService) {
        this.jobTable = enhancedDynamoDbClient.table("rsc-localhost-job-data", TableSchema.fromBean(Job.class));
        this.dynamoDbClient = dynamoDbClient;
        this.sqsService = sqsService;
    }

    public Optional<List<Job>> getAllJobs(String owner) {
        List<Job> jobs = new ArrayList<>();
        Map<String, AttributeValue> lastEvaluatedKey = null;

        try {
            QueryConditional queryConditional = QueryConditional.keyEqualTo(
                    Key.builder().partitionValue(owner).build());

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
            return Optional.empty();
        }
    }

    public Optional<List<Job>> getJobsByPage(String owner, int pageNum, int limit) {
        try{
            Optional<List<Job>> jobs = getAllJobs(owner);
            if (jobs.isPresent()) {
                List<Job> jobList = jobs.get();
                int start = (pageNum - 1) * limit;
                int end = Math.min(start + limit, jobList.size());
                return Optional.of(jobList.subList(start, end));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            System.err.println("Error retrieving jobs: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }

    }

    public Optional<Job> getJobsById(String owner, String jobId) {
        try {
            Job job = jobTable.getItem(Key.builder().partitionValue(owner).sortValue(jobId).build());
            return Optional.ofNullable(job);
        } catch (Exception e) {
            System.err.println("Error retrieving job: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<List<Job>> getJobsByKeyword(String owner, String keyword) {
        try {
            // Build the filter expression
            Expression filterExpression = Expression.builder()
                    .expression("contains(jobName, :keyword) AND #owner = :owner")
                    .putExpressionValue(":keyword", AttributeValue.builder().s(keyword).build())
                    .putExpressionName("#owner", "owner")
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
            return Optional.empty();
        }
    }

    public String getReport(String jobId) {

        return null; // return s3 presigned url
    }

    public void saveJob() {

    }

    public void createJob(Job job){
        try {
            System.out.println("Creating job: " + job);
            jobTable.putItem(job);
        } catch (Exception e) {
            System.err.println("Error creating job (DAO): " + e.getMessage());
        }
    }

    public void startJob(String owner, String jobId) {
        String newStatus = "Running";

        try {
            Job job;
            // Retrieve the existing job
            try{ job = jobTable.getItem(Key.builder()
                    .partitionValue(owner)
                    .sortValue(jobId)
                    .build());
            } catch (Exception e) {
                System.err.println("Unable to find job");
                throw new StartJobException("Job cannot be started because job doesn't exist.");
            }

            // Job should allow to run if pending / completed / failed
            if (job != null && (
                    "Pending".equals(job.getStatus()) ||
                    "Completed".equals(job.getStatus()) ||
                    "Failed".equals(job.getStatus())
                )
            ) {
                // Update the status
                job.setStatus(newStatus);

                // Update last updated
                String timeZone = job.getTimeZone(); // e.g., "UTC+03"
                ZoneId zoneId = ZoneId.of(timeZone.replace("UTC", "GMT")); // "UTC+03" -> "GMT+03"
                ZonedDateTime now = ZonedDateTime.now(zoneId);
                Map<String, String> latestDateTime = new HashMap<>();
                latestDateTime.put("date", now.format(DateTimeFormatter.ISO_LOCAL_DATE));

                // Format offset as "+0800" or "-0845"
                String offset = now.getOffset().getId().replace(":", "");
                // Compose the time string in "HH:mm UTC+0800" format
                latestDateTime.put("time", now.format(DateTimeFormatter.ofPattern("HH:mm")) + " UTC" + offset);
                job.setLastUpdated(latestDateTime);

                jobTable.updateItem(job);
                String messageBody = "{"
                    + "\"owner\": \"" + job.getOwner() + "\","
                    + "\"id\": \"" + job.getJobId() + "\","
                    + "\"repos\": \"" + job.getRepoLink() + "\","
                    + "\"since\": \"" + job.getSinceDate() + "\","
                    + "\"until\": \"" + job.getUntilDate() + "\","
                    + "\"formats\": \"" + String.join(" ", job.getFormatChipValues()) + "\","
                    + "\"period\": \"" + job.getPeriod() + "\","
                    + "\"originalityThreshold\": " + job.getOriginalityThreshold() + ","
                    + "\"timezone\": \"" + job.getTimeZone() + "\","
                    + "\"ignoreFileSizeLimit\": " + job.isIgnoreFileSizeLimit() + ","
                    + "\"shallowClone\": " + job.isShallowClone() + ","
                    + "\"lastModDate\": " + job.isAddLastMod() + ","
                    + "\"findPrevAuthors\": " + job.isPrevAuthors() + ","
                    + "\"analyzeAuthorship\": " + job.isAuthorship()
                    + "}";

                sqsService.sendMessage(jobId, messageBody);

                System.out.println("Job: " + job.toString());
                System.out.println("Job started successfully.");
            } else {
                throw new StartJobException("Job cannot be started because it is not in the Pending state or doesn't exist");
            }
        } catch (StartJobException e) {
            System.err.println("start job exception" + e.getMessage());
            throw new StartJobException("StartJobException: "+ e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Error starting job: " + e.getMessage());
            throw new RuntimeException("Error starting job", e);
        }
    }

    public void editJob(Job jobReplacement) {
        try {
            // Retrieve the existing job
            Job jobTarget = jobTable.getItem(Key.builder()
                    .partitionValue(jobReplacement.getOwner())
                    .sortValue(jobReplacement.getJobId())
                    .build());

            if (jobTarget != null && "Pending".equals(jobTarget.getStatus())) {
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
        // TODO: test this
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

}