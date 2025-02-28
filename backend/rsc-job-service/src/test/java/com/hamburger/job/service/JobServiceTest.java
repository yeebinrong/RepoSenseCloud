// // filepath: /c:/Users/Gwee/Documents/ReposenseCloudGit/backend/rsc-job-service/src/test/java/com/hamburger/job/service/JobServiceTest.java
// package com.hamburger.job.service;

// import com.hamburger.job.models.Job;
// import com.hamburger.job.models.dao.JobDbDao;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import java.util.Collections;
// import java.util.List;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.*;

// public class JobServiceTest {

//     @Mock
//     private JobDbDao jobDbDao;

//     @InjectMocks
//     private JobService jobService;

//     @BeforeEach
//     public void setUp() {
//         MockitoAnnotations.openMocks(this);
//     }

//     @Test
//     public void testGetAllJobs() {
//         Job job = new Job();
//         job.setOwner("owner1");
//         job.setJobId("job1");

//         when(jobDbDao.getAllJobs(anyString())).thenReturn(Collections.singletonList(job));

//         List<Job> jobs = jobService.getAllJobs("owner1");

//         assertEquals(1, jobs.size());
//         assertEquals("owner1", jobs.get(0).getOwner());
//         assertEquals("job1", jobs.get(0).getJobId());

//         verify(jobDbDao, times(1)).getAllJobs(anyString());
//     }

//     // @Test
//     // public void testGetJobsByPage() {
//     //     Job job = new Job();
//     //     job.setOwner("owner1");
//     //     job.setId("job1");

//     //     when(jobDbDao.getJobsByPage(anyInt(), anyInt())).thenReturn(Collections.singletonList(job));

//     //     List<Job> jobs = jobService.getJobsByPage(1, 10);

//     //     assertEquals(1, jobs.size());
//     //     assertEquals("owner1", jobs.get(0).getOwner());
//     //     assertEquals("job1", jobs.get(0).getId());

//     //     verify(jobDbDao, times(1)).getJobsByPage(anyInt(), anyInt());
//     // }

//     // @Test
//     // public void testGetJobsById() {
//     //     Job job = new Job();
//     //     job.setOwner("owner1");
//     //     job.setId("job1");

//     //     when(jobDbDao.getJobsById(anyInt())).thenReturn(job);

//     //     Job result = jobService.getJobsById(1);

//     //     assertEquals("owner1", result.getOwner());
//     //     assertEquals("job1", result.getId());

//     //     verify(jobDbDao, times(1)).getJobsById(anyInt());
//     // }

//     // @Test
//     // public void testGetJobsByKeyword() {
//     //     Job job = new Job();
//     //     job.setOwner("owner1");
//     //     job.setId("job1");

//     //     when(jobDbDao.getJobsByKeyword(anyString())).thenReturn(Collections.singletonList(job));

//     //     List<Job> jobs = jobService.getJobsByKeyword("keyword");

//     //     assertEquals(1, jobs.size());
//     //     assertEquals("owner1", jobs.get(0).getOwner());
//     //     assertEquals("job1", jobs.get(0).getId());

//     //     verify(jobDbDao, times(1)).getJobsByKeyword(anyString());
//     // }

//     // @Test
//     // public void testGetReport() {
//     //     when(jobDbDao.getReport(anyInt())).thenReturn("reportUrl");

//     //     String report = jobService.getReport(1);

//     //     assertEquals("reportUrl", report);

//     //     verify(jobDbDao, times(1)).getReport(anyInt());
//     // }
// }