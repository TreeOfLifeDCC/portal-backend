package com.dtol.platform;

import com.dtol.platform.controller.SecondaryOrganismsController;
import com.dtol.platform.controller.StatusTrackingController;
import com.dtol.platform.controller.RootOrganismController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class PlatformApplicationSmokeTest {

    @Autowired
    StatusTrackingController statusTrackingController;

    @Autowired
    SecondaryOrganismsController organismController;

    @Autowired
    RootOrganismController rootOrganismController;

    @Test
    void contextLoads() throws Exception {
        assertThat(statusTrackingController).isNotNull();
        assertThat(organismController).isNotNull();
        assertThat(rootOrganismController).isNotNull();
    }
}
