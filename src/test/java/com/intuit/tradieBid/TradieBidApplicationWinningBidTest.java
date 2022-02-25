package com.intuit.tradieBid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.tradieBid.entity.Bid;
import com.intuit.tradieBid.entity.Customer;
import com.intuit.tradieBid.entity.Job;
import com.intuit.tradieBid.entity.Tradie;
import com.intuit.tradieBid.model.WinningBid;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.intuit.tradieBid.TestData.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@RunWith(SpringRunner.class)
@Transactional
public class TradieBidApplicationWinningBidTest {

    @Container
    public static PostgreSQLContainer postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("tradieBid")
            .withUsername("intuit")
            .withPassword("intuit")
            .withInitScript("db/init_with_data.sql");
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> postgres.getJdbcUrl());
        registry.add("spring.datasource.username", () -> postgres.getUsername());
        registry.add("spring.datasource.password", () -> postgres.getPassword());
    }

    @Nested
    class testAppRestarts {
        @Test
        public void testStartApp_should_schedule_computing_for_all_uncompleted_jobs() throws Exception {
            // wait until the winning bid is calculated
            final CountDownLatch waiter = new CountDownLatch(1);
            waiter.await(3, TimeUnit.SECONDS);

            mockMvc.perform(MockMvcRequestBuilders.get("/job/1004/winning-bid").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andExpect(content().json(objectMapper.writeValueAsString(winningBidResult)));

            // no winning bid available
            mockMvc.perform(MockMvcRequestBuilders.get("/job/1007/winning-bid").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                    .andExpect(content().string("No Winning bid available"));
        }
    }

    @Nested
    class testDifferentBidding {

        public void setupData(Job job, Bid bidOne, Bid bidTwo) throws Exception {
            job.setToBeBiddenBy(OffsetDateTime.now().plusSeconds(2));
            job.setCustomerId(objectMapper.treeToValue(objectMapper.readTree(mockMvc.perform(MockMvcRequestBuilders.post("/customer").contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customer))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andReturn().getResponse().getContentAsString()), Customer.class).getCustomerId());

            bidOne.setTradieId(objectMapper.treeToValue(objectMapper.readTree(mockMvc.perform(MockMvcRequestBuilders.post("/tradie").contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tradieOne))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andReturn().getResponse().getContentAsString()), Tradie.class).getTradieId());

            bidTwo.setTradieId(objectMapper.treeToValue(objectMapper.readTree(mockMvc.perform(MockMvcRequestBuilders.post("/tradie").contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tradieTwo))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andReturn().getResponse().getContentAsString()), Tradie.class).getTradieId());

            job.setJobId(objectMapper.treeToValue(objectMapper.readTree(mockMvc.perform(MockMvcRequestBuilders.post("/job").contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(job))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andReturn().getResponse().getContentAsString()), Job.class).getJobId());
            bidOne.setJobId(job.getJobId());
            bidTwo.setJobId(job.getJobId());

        }

        @Test
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        public void testGetWinningBid_for_fixprice_vs_hourly() throws Exception {

            final Job localJob = new Job(jobOne);
            final Bid localBidOne = new Bid(bidOne);
            final Bid localBidTwo = new Bid(bidTwo);
            setupData(localJob, localBidOne, localBidTwo);
            localBidOne.setFixedPrice(500);
            localBidOne.setHourlyRate(null);
            localBidTwo.setFixedPrice(null);
            localBidTwo.setHourlyRate(50);

            mockMvc.perform(MockMvcRequestBuilders.post("/bid").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(localBidOne))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
            mockMvc.perform(MockMvcRequestBuilders.post("/bid").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(localBidTwo))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

            // get winning bid before expire is not available
            mockMvc.perform(MockMvcRequestBuilders.get(String.format("/job/%o/winning-bid", localJob.getJobId())).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                    .andExpect(content().string("No Winning bid available"));

            final CountDownLatch waiter = new CountDownLatch(1);
            waiter.await(3, TimeUnit.SECONDS);

            final WinningBid result = new WinningBid(
                    localJob.getJobId(),
                    localJob.getDescription(),
                    100,
                    customer,
                    tradieTwo);


            mockMvc.perform(MockMvcRequestBuilders.get(String.format("/job/%o/winning-bid", localJob.getJobId())).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andExpect(content().json(objectMapper.writeValueAsString(result)));
        }

        @Test
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        public void testGetWinningBid_for_fixprice_vs_fixprice() throws Exception {

            final Job localJob = new Job(jobOne);
            final Bid localBidOne = new Bid(bidOne);
            final Bid localBidTwo = new Bid(bidTwo);
            setupData(localJob, localBidOne, localBidTwo);
            localBidOne.setFixedPrice(500);
            localBidOne.setHourlyRate(null);
            localBidTwo.setFixedPrice(400);
            localBidTwo.setHourlyRate(null);

            mockMvc.perform(MockMvcRequestBuilders.post("/bid").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(localBidOne))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
            mockMvc.perform(MockMvcRequestBuilders.post("/bid").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(localBidTwo))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

            // get winning bid before expire is not available
            mockMvc.perform(MockMvcRequestBuilders.get(String.format("/job/%o/winning-bid", localJob.getJobId())).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                    .andExpect(content().string("No Winning bid available"));

            final CountDownLatch waiter = new CountDownLatch(1);
            waiter.await(3, TimeUnit.SECONDS);

            final WinningBid result = new WinningBid(
                    localJob.getJobId(),
                    localJob.getDescription(),
                    400,
                    customer,
                    tradieTwo);


            mockMvc.perform(MockMvcRequestBuilders.get(String.format("/job/%o/winning-bid", localJob.getJobId())).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andExpect(content().json(objectMapper.writeValueAsString(result)));
        }

        @Test
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        public void testGetWinningBid_for_hourly_vs_hourly() throws Exception {

            final Job localJob = new Job(jobOne);
            final Bid localBidOne = new Bid(bidOne);
            final Bid localBidTwo = new Bid(bidTwo);
            setupData(localJob, localBidOne, localBidTwo);
            localBidOne.setFixedPrice(null);
            localBidOne.setHourlyRate(100);
            localBidTwo.setFixedPrice(null);
            localBidTwo.setHourlyRate(200);

            mockMvc.perform(MockMvcRequestBuilders.post("/bid").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(localBidOne))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
            mockMvc.perform(MockMvcRequestBuilders.post("/bid").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(localBidTwo))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

            // get winning bid before expire is not available
            mockMvc.perform(MockMvcRequestBuilders.get(String.format("/job/%o/winning-bid", localJob.getJobId())).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                    .andExpect(content().string("No Winning bid available"));

            final CountDownLatch waiter = new CountDownLatch(1);
            waiter.await(3, TimeUnit.SECONDS);

            final WinningBid result = new WinningBid(
                    localJob.getJobId(),
                    localJob.getDescription(),
                    200,
                    customer,
                    tradieOne);

            mockMvc.perform(MockMvcRequestBuilders.get(String.format("/job/%o/winning-bid", localJob.getJobId())).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andExpect(content().json(objectMapper.writeValueAsString(result)));
        }
    }
}
