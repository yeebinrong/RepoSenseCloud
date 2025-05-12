package com.hamburger.job.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import lombok.Getter;
import lombok.Setter;

@DynamoDbBean
@Getter
@Setter
public class Job {

    private String jobId; //set on create
    private String jobName;
    private String repoLink;
    private String sinceDate;
    private String untilDate;
    private String period;
    private Float originalityThreshold;
    private String timeZone;
    private boolean authorship;
    private boolean prevAuthors;
    private boolean shallowClone;
    private boolean ignoreFileSizeLimit; 
    private boolean addLastMod;
    private List<String> formatChipValues; //this is exclude file type list
    private String jobType;
    private String frequency;
    private String startMinute;
    private String startHour;
    private String startDate;
    private String endDate;
    private String owner;
    private String status;
    private Map<String,String> lastUpdated;
    private Map<String,String> nextScheduled;
    private Map<String,String> settingsUpdatedAt;

    public Job () {
        if(this.jobId == null){
            this.jobId = UUID.randomUUID().toString();
        }
        if(this.status == null){
            this.status = "Pending";
        }
    }

    @DynamoDbSortKey
    public String getJobId() {
        return jobId;
    }

    @DynamoDbPartitionKey
    public String getOwner() {
        return owner;
    }
}
