# Final Java Spring Boot API

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/599ddc4dabcc4810b6ac9af8ddc8bc20)](https://www.codacy.com/gh/gary-archer/oauth.apisample.javaspringboot/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=gary-archer/oauth.apisample.javaspringboot&amp;utm_campaign=Badge_Grade)

[![Known Vulnerabilities](https://snyk.io/test/github/gary-archer/oauth.apisample.javaspringboot/badge.svg?targetFile=build.gradle)](https://snyk.io/test/github/gary-archer/oauth.apisample.javaspringboot?targetFile=build.gradle)

## Overview 

The final OAuth secured Java API code sample, referenced in my blog at https://authguidance.com:

* The API takes finer control over OAuth domain specific claims and uses a certified JOSE library
* The API uses JSON request logging and Elasticsearch log aggregation, for measurability
* The API uses non-blocking code with request scoped ClaimsPrincipal / LogEntry objects

## API integrates with UI Clients

The API can run as part of an OAuth end-to-end setup, to server my blog's UI code samples.\
Running the API in this manner forces it to be consumer focused to its clients:

![SPA and API](./doc/spa-and-api.png)

## API can be Productively Tested

The API's clients are UIs, which get user level access tokens by running an OpenID Connect code flow.\
For productive test driven development, the API instead mocks the Authorization Server:

![Test Driven Development](./doc/tests.png)

## API can be Load Tested

A basic load test uses Completable Futures to fire 5 parallel requests at a time at the API.\
This ensures no concurrency problems, and error rehearsal is used to verify that the API is supportable:

![Load Test](./doc/loadtest.png)

## Further Details

* See the [Overview Page](https://authguidance.com/2019/03/24/java-spring-boot-api-overview/) for instructions on how to run the API
* See the [OAuth Integration Page](https://authguidance.com/2019/03/24/java-spring-boot-api-coding-key-points/) for the security implementation
* See the [Non Functional Behaviour](https://authguidance.com/2017/10/08/corporate-code-sample-core-behavior/) page for a summary of overall qualities

## Programming Languages

* Java 17 and Spring Boot 2.6 are used to implement the REST API

## Middleware Used

* The Tomcat web server hosts the API over SSL port 443
* AWS Cognito is used as the default Authorization Server
* The [Jose4j Library](https://bitbucket.org/b_c/jose4j/wiki/Home) library is used to manage in memory validation of JWTs
* API logs can be aggregated to [Elasticsearch](https://authguidance.com/2019/07/19/log-aggregation-setup/) to support [Query Use Cases](https://authguidance.com/2019/08/02/intelligent-api-platform-analysis/)
* The API is designed for cloud native deployment to Kubernetes