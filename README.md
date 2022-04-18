# Final Java Spring Boot API

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/599ddc4dabcc4810b6ac9af8ddc8bc20)](https://www.codacy.com/gh/gary-archer/oauth.apisample.javaspringboot/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=gary-archer/oauth.apisample.javaspringboot&amp;utm_campaign=Badge_Grade)

[![Known Vulnerabilities](https://snyk.io/test/github/gary-archer/oauth.apisample.javaspringboot/badge.svg?targetFile=build.gradle)](https://snyk.io/test/github/gary-archer/oauth.apisample.javaspringboot?targetFile=build.gradle)

### Overview 

The final OAuth secured Java API code sample, referenced in my blog at https://authguidance.com:

- The API takes finer control over OAuth domain specific claims and uses a certified JOSE library
- The API also implements other [Non Functional Behaviour](https://authguidance.com/2017/10/08/corporate-code-sample-core-behavior/), for good technical quality

### Build the API

Ensure that Java 17 and maven are installed, then run the start script to build the API and start listening over HTTPS.\
Development SSL certificates must be downloaded before `npm start` will work.

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

Revoke trust when required via this command:

```bash
sudo "$JAVA_HOME/bin/keytool" -delete -alias authsamples.ca -cacerts -storepass changeit -noprompt
```

Then run the following command, to run the API with a test configuration:

```bash
./testsetup.sh
```

## Run Integration Tests

To run integration tests that call the API's HTTPS endpoints, run this command:

```bash
./gradlew test --rerun-tasks
```

```text
com.mycompany.sample.tests.IntegrationTests

  Test GetCompanies_Returns401_ForMaliciousJwt() PASSED
  Test GetTransactions_ReturnsNotFoundForUser_ForCompaniesNotMatchingTheRegionClaim() PASSED
  Test GetTransactions_ReturnsAllowedItems_ForCompaniesMatchingTheRegionClaim() PASSED
  Test GetCompanies_ReturnsAllItems_ForAdminUser() PASSED
  Test GetCompanies_ReturnsTwoItems_ForStandardUser() PASSED
  Test FailedApiCall_ReturnsSupportable500Error_ForErrorRehearsalRequest() PASSED
  Test GetUserClaims_ReturnsSingleRegion_ForStandardUser() PASSED
  Test GetUserClaims_ReturnsAllRegions_ForAdminUser() PASSED


INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.mycompany.sample.tests.IntegrationTests
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
```

### Further Details

* See the [Overview Page](https://authguidance.com/2019/03/24/java-spring-boot-api-overview/) for further details on running the API
* See the [OAuth Integration Page](https://authguidance.com/2019/03/24/java-spring-boot-api-coding-key-points/) for key implementation details

### Programming Languages

* Java 17 and Spring Boot 2.6 are used to implement the REST API

### Middleware Used

* The Tomcat web server hosts the API over SSL port 443
* AWS Cognito is used as the default Authorization Server
* The [Jose4j Library](https://bitbucket.org/b_c/jose4j/wiki/Home) library is used to manage in memory validation of JWTs
* API logs can be aggregated to [Elasticsearch](https://authguidance.com/2019/07/19/log-aggregation-setup/) to support [Query Use Cases](https://authguidance.com/2019/08/02/intelligent-api-platform-analysis/)
