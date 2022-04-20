package com.mycompany.sample.tests;

import com.mycompany.sample.tests.utils.TokenIssuer;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.Suite;

/*
 * A basic load test to ensure that the API behaves correctly when there are concurrent requests
 */
@Suite
public class LoadTest {

    @Test
    public void Run() throws Throwable {

        var issuer = new TokenIssuer();
        var accessToken = issuer.issueAccessToken("");
        System.out.println(accessToken);
    }
}
