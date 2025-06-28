package com.hamburger.batch.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RepoSenseJobRequestTest {

    @Test
    void isValid_returnsTrue_whenReposAndIdArePresent() {
        RepoSenseJobRequest req = new RepoSenseJobRequest();
        req.setRepos("https://github.com/reposense/RepoSense.git");
        req.setId("1234567890");
        assertTrue(req.isValid());
    }

    @Test
    void isValid_returnsFalse_whenReposIsNullOrEmpty() {
        RepoSenseJobRequest req = new RepoSenseJobRequest();
        req.setId("1234567890");
        req.setRepos(null);
        assertFalse(req.isValid());

        req.setRepos("");
        assertFalse(req.isValid());

        req.setRepos("   ");
        assertFalse(req.isValid());
    }

    @Test
    void isValid_returnsFalse_whenIdIsNullOrEmpty() {
        RepoSenseJobRequest req = new RepoSenseJobRequest();
        req.setRepos("https://github.com/reposense/RepoSense.git");
        req.setId(null);
        assertFalse(req.isValid());

        req.setId("");
        assertFalse(req.isValid());

        req.setId("   ");
        assertFalse(req.isValid());
    }
}