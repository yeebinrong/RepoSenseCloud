package com.hamburger.batch.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepoSenseJobRequest {

    // Required
    private String owner;
    private String id;
    private String repos;

    // Optional
    private String since;
    private String until;
    private String formats;
    private String timezone;
    private Float originalityThreshold;
    private String period;

    // Boolean flags
    private Boolean ignoreFileSizeLimit = true;
    private Boolean lastModDate = false;
    private Boolean findPrevAuthors = false;
    private Boolean analyzeAuthorship = true;
    private Boolean shallowClone = false;

    // Getters and setters omitted for brevity

    public boolean isValid() {
        return repos != null && !repos.trim().isEmpty()
                && id != null && !id.trim().isEmpty();
    }
}

// sample argument to send to SQS directly

// {
//     "owner": "binrong",
//     "id": "1234567890123",
//     "repos": "https://github.com/reposense/RepoSense.git",
//     "since": "31/1/2017",
//     "until": "31/12/2018",
//     "formats": "java adoc xml",
//     "ignoreConfig": true,
//     "lastModDate": true,
//     "timezone": "UTC+08",
//     "findPrevAuthors": true,
//     "analyzeAuthorship": true,
//     "originalityThreshold": 0.66
//   }