package com.dtol.platform;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dtol.platform.es.mapping.Organism;
import com.dtol.platform.es.mapping.RootSample;
import com.dtol.platform.es.service.OrganismService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
public class OrganismTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    OrganismService organismService;

    @Test
    void getOrganismDetails() throws Exception {
        Organism organism = new Organism();
        Optional<String> sortColumn = Optional.of("accession");
        Optional<String> sortOrder = Optional.of("asc");

        when((organismService.findBioSampleByAccession(""))).thenReturn(organism);

        this.mockMvc.perform(get("/organisms")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("{\"count\":0,\"biosamples\":[]}")));
    }
}
