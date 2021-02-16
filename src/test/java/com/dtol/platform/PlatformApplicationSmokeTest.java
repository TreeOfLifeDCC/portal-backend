package com.dtol.platform;

import com.dtol.platform.controller.OrganismController;
import com.dtol.platform.controller.OrganismStatusTrackingController;
import com.dtol.platform.controller.RootOrganismController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class PlatformApplicationSmokeTest {

    @Autowired
    OrganismStatusTrackingController organismStatusTrackingController;

    @Autowired
    OrganismController organismController;

    @Autowired
    RootOrganismController rootOrganismController;

    @Test
    void contextLoads() throws Exception {
        assertThat(organismStatusTrackingController).isNotNull();
        assertThat(organismController).isNotNull();
        assertThat(rootOrganismController).isNotNull();
    }
}
