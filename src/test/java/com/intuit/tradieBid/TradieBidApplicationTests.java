package com.intuit.tradieBid;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.tradieBid.entity.Bid;
import com.intuit.tradieBid.entity.Customer;
import com.intuit.tradieBid.entity.Job;
import com.intuit.tradieBid.entity.Tradie;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;

import static com.intuit.tradieBid.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@RunWith(SpringRunner.class)
@EnableTransactionManagement
@Transactional
class TradieBidApplicationTests {

    @Container
    public static PostgreSQLContainer postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("tradieBid")
            .withUsername("intuit")
            .withPassword("intuit")
            .withInitScript("db/init_without_data.sql");
    @Autowired
    private JdbcTemplate jdbcTemplate;
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

    @Test
    public void testCreateCustomer_and_store_in_database() throws Exception {

        Integer customerId = objectMapper.treeToValue(objectMapper.readTree(mockMvc.perform(MockMvcRequestBuilders.post("/customer").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString()), Customer.class).getCustomerId();

        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/customer/%o", customerId)).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

        assertThat(
                JdbcTestUtils.countRowsInTableWhere(
                        jdbcTemplate,
                        "customers",
                        String.format("email='%s'", customer.getEmail())
                )
        ).isEqualTo(1);
    }


    @Test
    public void testCreateCustomer_but_missing_data_should_be_rejected() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/customer").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lastName\": \"Wang\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().string("\"first_name\" is required."));
    }

    @Test
    public void testCreateTradie_and_store_in_database() throws Exception {

        Integer tradieId = objectMapper.treeToValue(objectMapper.readTree(mockMvc.perform(MockMvcRequestBuilders.post("/tradie").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tradieOne))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString()), Tradie.class).getTradieId();
        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/tradie/%o", tradieId)).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tradieOne))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

        assertThat(
                JdbcTestUtils.countRowsInTableWhere(
                        jdbcTemplate,
                        "tradies",
                        String.format("email='%s'", tradieOne.getEmail())
                )
        ).isEqualTo(1);
    }

    @Test
    public void testCreateJob_and_store_in_database() throws Exception {

        final Job localJob = new Job(jobOne);
        localJob.setCustomerId(objectMapper.treeToValue(objectMapper.readTree(mockMvc.perform(MockMvcRequestBuilders.post("/customer").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString()), Customer.class).getCustomerId());
        Integer jobId = objectMapper.treeToValue(objectMapper.readTree(mockMvc.perform(MockMvcRequestBuilders.post("/job").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(localJob)))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString()), Job.class).getJobId();

        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/job/%o", jobId)).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(localJob))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());


        assertThat(
                JdbcTestUtils.countRowsInTableWhere(
                        jdbcTemplate,
                        "jobs",
                        String.format("customer_id=%o", localJob.getCustomerId())
                )
        ).isEqualTo(1);
    }

    @Test
    public void testCreateBid_and_store_in_database() throws Exception {
        final Job localJob = new Job(jobOne);
        final Bid localBid = new Bid(bidOne);
        localJob.setCustomerId(objectMapper.treeToValue(objectMapper.readTree(mockMvc.perform(MockMvcRequestBuilders.post("/customer").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString()), Customer.class).getCustomerId());

        localBid.setTradieId(objectMapper.treeToValue(objectMapper.readTree(mockMvc.perform(MockMvcRequestBuilders.post("/tradie").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tradieOne))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString()), Tradie.class).getTradieId());

        localBid.setJobId(objectMapper.treeToValue(objectMapper.readTree(mockMvc.perform(MockMvcRequestBuilders.post("/job").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(localJob))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString()), Job.class).getJobId());

        Integer bidId = objectMapper.treeToValue(objectMapper.readTree(mockMvc.perform(MockMvcRequestBuilders.post("/bid").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(localBid))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString()), Bid.class).getBidId();

        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/bid/%o", bidId)).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(localBid))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
        
        assertThat(
                JdbcTestUtils.countRowsInTableWhere(
                        jdbcTemplate,
                        "bids",
                        String.format("tradie_id=%o", localBid.getTradieId())
                )
        ).isEqualTo(1);
    }

    @Test
    public void testCreateBid_for_job_in_the_past_should_not_be_allowed() throws Exception {
        final Job localJob = new Job(jobOne);
        final Bid localBid = new Bid(bidOne);
        localJob.setToBeBiddenBy(OffsetDateTime.now().minusHours(1));
        localJob.setCustomerId(objectMapper.treeToValue(objectMapper.readTree(mockMvc.perform(MockMvcRequestBuilders.post("/customer").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString()), Customer.class).getCustomerId());

        localBid.setTradieId(objectMapper.treeToValue(objectMapper.readTree(mockMvc.perform(MockMvcRequestBuilders.post("/tradie").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tradieOne))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString()), Tradie.class).getTradieId());

        localBid.setJobId(objectMapper.treeToValue(objectMapper.readTree(mockMvc.perform(MockMvcRequestBuilders.post("/job").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(localJob))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString()), Job.class).getJobId());

        mockMvc.perform(MockMvcRequestBuilders.post("/bid").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(localBid))).andExpect(MockMvcResultMatchers.status().isBadRequest());

        assertThat(
                JdbcTestUtils.countRowsInTableWhere(
                        jdbcTemplate,
                        "bids",
                        String.format("tradie_id=%o", localBid.getTradieId())
                )
        ).isEqualTo(0);
    }

    @Test
    public void testCreateBid_for_with_both_fix_price_and_hourly_rate_should_not_be_allowed() throws Exception {
        final Job localJob = new Job(jobOne);
        final Bid localBid = new Bid(bidOne);
        localJob.setCustomerId(objectMapper.treeToValue(objectMapper.readTree(mockMvc.perform(MockMvcRequestBuilders.post("/customer").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString()), Customer.class).getCustomerId());

        localBid.setTradieId(objectMapper.treeToValue(objectMapper.readTree(mockMvc.perform(MockMvcRequestBuilders.post("/tradie").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tradieOne))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString()), Tradie.class).getTradieId());

        localBid.setJobId(objectMapper.treeToValue(objectMapper.readTree(mockMvc.perform(MockMvcRequestBuilders.post("/job").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(localJob))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString()), Job.class).getJobId());

        localBid.setFixedPrice(50);
        localBid.setHourlyRate(20);

        mockMvc.perform(MockMvcRequestBuilders.post("/bid").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(localBid))).andExpect(MockMvcResultMatchers.status().isBadRequest());

        assertThat(
                JdbcTestUtils.countRowsInTableWhere(
                        jdbcTemplate,
                        "bids",
                        String.format("tradie_id=%o", localBid.getTradieId())
                )
        ).isEqualTo(0);
    }

    @Test
    public void testCreateBid_for_job_not_exist_should_not_be_allowed() throws Exception {
        final Bid localBid = new Bid(bidOne);
        localBid.setTradieId(objectMapper.treeToValue(objectMapper.readTree(mockMvc.perform(MockMvcRequestBuilders.post("/tradie").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tradieOne))).andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString()), Tradie.class).getTradieId());

        mockMvc.perform(MockMvcRequestBuilders.post("/bid").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(localBid))).andExpect(MockMvcResultMatchers.status().isNotFound());

        assertThat(
                JdbcTestUtils.countRowsInTableWhere(
                        jdbcTemplate,
                        "bids",
                        String.format("tradie_id=%o", localBid.getTradieId())
                )
        ).isEqualTo(0);
    }


    @Test
    public void testGetWinningBid_for_job_not_exist_should_return_not_found() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/job/101/winning-bid").contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

}
