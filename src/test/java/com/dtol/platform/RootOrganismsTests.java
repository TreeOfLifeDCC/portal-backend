package com.dtol.platform;

import com.dtol.platform.es.mapping.RootOrganism;
import com.dtol.platform.es.mapping.RootSample;
import com.dtol.platform.es.service.RootSampleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RootOrganismsTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    RootSampleService rootSampleService;

    @Test
    void findAllRootSamples() throws Exception {
        List<RootOrganism> emptySampleList = new ArrayList<RootOrganism>();
        emptySampleList.add(new RootOrganism());

        Optional<String> sortColumn = Optional.of("accession");
        Optional<String> sortOrder = Optional.of("asc");

        when((rootSampleService.findAllOrganisms(0,10, sortColumn, sortOrder))).thenReturn(emptySampleList);

        this.mockMvc.perform(get("/data_portal_test")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("{\"rootSamples\":[],\"count\":0}")));
    }

    @Test
    void getOrganismDetails() throws Exception {
        RootOrganism organism = new RootOrganism();
        Optional<String> sortColumn = Optional.of("accession");
        Optional<String> sortOrder = Optional.of("asc");

        when((rootSampleService.findRootSampleByOrganism(""))).thenReturn(organism);

        this.mockMvc.perform(get("/data_portal_test")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("{\"rootSamples\":[],\"count\":0}")));
    }

}
