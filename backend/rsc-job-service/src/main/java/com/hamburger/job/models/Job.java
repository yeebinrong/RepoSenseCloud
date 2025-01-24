package com.hamburger.job.models;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class Job {

    private String id; //set on create
    private String jobName;
    private String repoLink;
    private String sinceDate;
    private String untilDate;
    private String period;
    private String originalityThreshold;
    private String timeZone;
    private boolean authorship;
    private boolean prevAuthors;
    private boolean shallowClone;
    private boolean ignoreSizeLimit; 
    private boolean addLastMod;
    private List<String> formatChipValues; //this is exclude file type list
    private String jobType;
    private String frequency;
    private String startMinute;
    private String startHour;
    private String startDate;
    private String endDate;

    public Job () {
        this.id = UUID.randomUUID().toString();
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getRepoLink() {
        return repoLink;
    }

    public void setRepoLink(String repoLink) {
        this.repoLink = repoLink;
    }

    public String getSinceDate() {
        return sinceDate;
    }

    public void setSinceDate(String sinceDate) {
        this.sinceDate = sinceDate;
    }

    public String getUntilDate() {
        return untilDate;
    }

    public void setUntilDate(String untilDate) {
        this.untilDate = untilDate;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getOriginalityThreshold() {
        return originalityThreshold;
    }

    public void setOriginalityThreshold(String originalityThreshold) {
        this.originalityThreshold = originalityThreshold;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public boolean isAuthorship() {
        return authorship;
    }

    public void setAuthorship(boolean authorship) {
        this.authorship = authorship;
    }

    public boolean isPrevAuthors() {
        return prevAuthors;
    }

    public void setPrevAuthors(boolean prevAuthors) {
        this.prevAuthors = prevAuthors;
    }

    public boolean isShallowClone() {
        return shallowClone;
    }

    public void setShallowClone(boolean shallowClone) {
        this.shallowClone = shallowClone;
    }

    public boolean isIgnoreSizeLimit() {
        return ignoreSizeLimit;
    }

    public void setIgnoreSizeLimit(boolean ignoreSizeLimit) {
        this.ignoreSizeLimit = ignoreSizeLimit;
    }

    public boolean isAddLastMod() {
        return addLastMod;
    }

    public void setAddLastMod(boolean addLastMod) {
        this.addLastMod = addLastMod;
    }

    public List<String> getFormatChipValues() {
        return formatChipValues;
    }

    public void setFormatChipValues(List<String> formatChipValues) {
        this.formatChipValues = formatChipValues;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(String startMinute) {
        this.startMinute = startMinute;
    }

    public String getStartHour() {
        return startHour;
    }

    public void setStartHour(String startHour) {
        this.startHour = startHour;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
