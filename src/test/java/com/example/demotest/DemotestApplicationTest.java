package com.example.demotest;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DemotestApplicationTest {

    @Autowired
    protected MockMvc mockMvc;

    @RepeatedTest(3)
    public void whenGetBackendThenTwoCalls200AndThreeCalls502(RepetitionInfo repetitionInfo) throws Exception {
        ResultMatcher result = repetitionInfo.getCurrentRepetition() <= 2 ? status().isOk() : status().isBadGateway();
        mockMvc.perform(MockMvcRequestBuilders.get("/get"))
                .andExpect(result);
    }
}