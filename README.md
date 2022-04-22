# Final Java Spring Boot API

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/599ddc4dabcc4810b6ac9af8ddc8bc20)](https://www.codacy.com/gh/gary-archer/oauth.apisample.javaspringboot/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=gary-archer/oauth.apisample.javaspringboot&amp;utm_campaign=Badge_Grade)

[![Known Vulnerabilities](https://snyk.io/test/github/gary-archer/oauth.apisample.javaspringboot/badge.svg?targetFile=build.gradle)](https://snyk.io/test/github/gary-archer/oauth.apisample.javaspringboot?targetFile=build.gradle)

## Overview 

The final OAuth secured Java API code sample, referenced in my blog at https://authguidance.com:

- The API takes finer control over OAuth domain specific claims and uses a certified JOSE library
- The API also implements other [Non Functional Behaviour](https://authguidance.com/2017/10/08/corporate-code-sample-core-behavior/), for good technical quality

## Build and Run the API

Ensure that Java 17 is installed, then run the start script to build the API and start listening over HTTPS.\
The script downloads development SSL certificates associated to test domains.

```bash
./start.sh
```

## Integration Test Setup

Add host names for the API and Authorization Server to your hosts file:

```text
127.0.0.1     localhost api.authsamples-dev.com login.mycompany.com
::1           localhost
```

Also trust the development root certificate by running this command from a terminal in the repo's root folder:

```bash
sudo "$JAVA_HOME/bin/keytool" -import -alias authsamples.ca -cacerts -file ./certs/authsamples-dev.ca.pem -storepass changeit -noprompt
```

Stop the API if it is listening, then run this command to run the API with a test configuration:

```bash
./testsetup.sh
```

Later, when finished testing, revoke trust via this command:

```bash
sudo "$JAVA_HOME/bin/keytool" -delete -alias authsamples.ca -cacerts -storepass changeit -noprompt
```

## Run Integration Tests

Next run integration tests that call the running API's HTTPS endpoints, via this command:

```bash
./gradlew test --rerun-tasks
```

```text
com.mycompany.sample.tests.IntegrationTests

  Test GetUserClaims_ReturnsSingleRegion_ForStandardUser() PASSED
  Test GetUserClaims_ReturnsAllRegions_ForAdminUser() PASSED
  Test GetCompanies_ReturnsTwoItems_ForStandardUser() PASSED
  Test GetCompanies_ReturnsAllItems_ForAdminUser() PASSED
  Test GetCompanies_Returns401_ForMaliciousJwt() PASSED
  Test GetTransactions_ReturnsAllowedItems_ForCompaniesMatchingTheRegionClaim() PASSED
  Test GetTransactions_ReturnsNotFoundForUser_ForCompaniesNotMatchingTheRegionClaim() PASSED
  Test FailedApiCall_ReturnsSupportable500Error_ForErrorRehearsalRequest() PASSED
```

## Run a Basic Load Test

Or run a basic load test that calls the API's HTTPS endpoints, via this command:

```bash
./gradlew loadtest --rerun-tasks
```

This sends parallel requests to the API to verify that the code has no concurrency problems.\
The API then reports metrics to enable early visualization of errors and slowness:

```text
OPERATION                CORRELATION-ID                        START-TIME                  MILLISECONDS-TAKEN   STATUS-CODE   ERROR-CODE              ERROR-ID    
getUserInfoClaims        920387a0-4196-24af-acf3-55e61769da9e  2022-04-10T21:00:14.081Z    72                   200
getCompanyList           dec2dca9-0dbb-4bf0-c16b-50b8e515022c  2022-04-10T21:00:14.091Z    69                   200
getCompanyList           fae39ffe-4d9a-20e3-c240-f1b4ba069152  2022-04-10T21:00:14.093Z    68                   200
getCompanyTransactions   cfa99c2c-67e4-5353-6521-fa33c5a194ac  2022-04-10T21:00:14.087Z    75                   200
getCompanyTransactions   02379802-2680-22d3-23a6-b41086efdb71  2022-04-10T21:00:14.089Z    75                   200
getUserInfoClaims        7a42b19a-c028-f1bb-f57a-c9f94b911507  2022-04-10T21:00:14.164Z    38                   200
getCompanyList           66eb315f-f4a4-334f-63dc-fa7ff9628117  2022-04-10T21:00:14.173Z    40                   200
getCompanyList           69f5ffb8-6b9a-b806-1ccc-3a1607d56deb  2022-04-10T21:00:14.171Z    44                   200
getCompanyTransactions   0a93d119-5385-257f-c4f3-668ba5a0f6dd  2022-04-10T21:00:14.167Z    50                   200
getCompanyTransactions   65b2f261-056c-d0af-87aa-b4c25b65039a  2022-04-10T21:00:14.169Z    49                   200
getCompanyList           54c9bbe8-dcaa-bded-cacc-23ebd3b23cbc  2022-04-10T21:00:14.224Z    33                   500           exception_simulation    24398
getUserInfoClaims        8f4d1228-cad8-9d1a-72e0-0ccbbeefa663  2022-04-10T21:00:14.218Z    39                   401           unauthorized
getCompanyList           348d8dd6-e8ee-e811-553e-6815c58e6e80  2022-04-10T21:00:14.226Z    38                   200
getCompanyTransactions   65dc3711-d5ef-178b-2d2b-728a991a18e8  2022-04-10T21:00:14.222Z    43                   200
getCompanyTransactions   822ef51e-6219-de6e-3e13-909df036c49a  2022-04-10T21:00:14.220Z    46                   200
getUserInfoClaims        f244f6e0-5ccc-e1f7-8c1c-d15e3ae8ca06  2022-04-10T21:00:14.266Z    27                   200
getCompanyList           bc0c89c6-9e34-8284-060b-5fb5322aee95  2022-04-10T21:00:14.269Z    27                   200
getCompanyTransactions   26457ca5-7ca8-99fe-19f6-56dc4572d81f  2022-04-10T21:00:14.268Z    29                   200
getCompanyTransactions   14a60e83-5f0a-4dbd-ac26-5e1b0252b16d  2022-04-10T21:00:14.267Z    30                   200
getCompanyList           055c6711-59a6-e6b4-0369-5517fdab2cdb  2022-04-10T21:00:14.269Z    29                   200
getUserInfoClaims        ab5257a0-3a4a-e1b3-5b31-bfbf506edf75  2022-04-10T21:00:14.299Z    19                   200
```

## Further Details

* See the [Overview Page](https://authguidance.com/2019/03/24/java-spring-boot-api-overview/) for further details on the API behaviour
* See the [OAuth Integration Page](https://authguidance.com/2019/03/24/java-spring-boot-api-coding-key-points/) for key implementation details

## Programming Languages

* Java 17 and Spring Boot 2.6 are used to implement the REST API

## Middleware Used

* The Tomcat web server hosts the API over SSL port 443
* AWS Cognito is used as the default Authorization Server
* The [Jose4j Library](https://bitbucket.org/b_c/jose4j/wiki/Home) library is used to manage in memory validation of JWTs
* API logs can be aggregated to [Elasticsearch](https://authguidance.com/2019/07/19/log-aggregation-setup/) to support [Query Use Cases](https://authguidance.com/2019/08/02/intelligent-api-platform-analysis/)
