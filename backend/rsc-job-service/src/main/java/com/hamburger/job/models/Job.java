package com.hamburger.job.models;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.boot.origin.SystemEnvironmentOrigin;

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
    private List<String> formatChipValues;
    private String jobType;
    private String frequency;
    private String startMinute;
    private String startHour;
    private String startDate;
    private String endDate;
    private String owner;
    private String status;
    private String prevStatus;
    private Map<String,String> lastUpdated;
    private Map<String,String> settingsUpdatedAt;

    public Job () {
        if(this.status == null){
            this.status = "Pending";
            this.prevStatus = "Pending";
        }
        // if(this.lastUpdated == null){
        //     this.lastUpdated = new HashMap<>();
        //     ZonedDateTime now = ZonedDateTime.now();
        //     lastUpdated.put("date", now.format(DateTimeFormatter.ISO_LOCAL_DATE));
        //     lastUpdated.put("time", now.format(DateTimeFormatter.ofPattern("HH:mm:ssX")));                                                   
        // }
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
