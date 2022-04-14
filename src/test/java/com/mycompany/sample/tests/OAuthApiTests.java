package com.mycompany.sample.tests;

import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Suite
public class OAuthApiTests {

    private static final Logger logger = LoggerFactory.getLogger(OAuthApiTests.class);

    @BeforeAll
    public static void init() {
        logger.info("Setting it up!");
    }

    @AfterAll
    public static void teardown() {
        logger.info("Tearing it down");
    }

    @Test
    public void GetUserClaims_ReturnsSingleRegion_ForStandardUser() {
        Assertions.assertEquals("hello", "hello");
        logger.info("GetUserClaims_ReturnsSingleRegion_ForStandardUser");
    }

    @Test
    public void GetUserClaims_ReturnsAllRegions_ForAdminUser() {
        Assertions.assertEquals("goodbye", "goodbye");
        logger.info("GetUserClaims_ReturnsAllRegions_ForAdminUser");
    }
}
