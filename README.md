# Final Java Spring Boot API

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/599ddc4dabcc4810b6ac9af8ddc8bc20)](https://www.codacy.com/gh/gary-archer/oauth.apisample.javaspringboot/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=gary-archer/oauth.apisample.javaspringboot&amp;utm_campaign=Badge_Grade)

[![Known Vulnerabilities](https://snyk.io/test/github/gary-archer/oauth.apisample.javaspringboot/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/gary-archer/oauth.apisample.javaspringboot/badge.svg?targetFile=pom.xml)

### Overview 

The final OAuth secured Java API code sample, referenced in my blog at https://authguidance.com:

- The API takes finer control over OAuth domain specific claims and uses a certified JOSE library
- The API also implements other [Non Functional Behaviour](https://authguidance.com/2017/10/08/corporate-code-sample-core-behavior/), for good technical quality

### Quick Start

Ensure that Java 17 and maven are installed, then run the start script to begin listening over HTTPS.\
You need to run the script at least once in order to download development SSL certificates.

```bash
./start.sh
```

### Details

* See the [Overview Page](https://authguidance.com/2019/03/24/java-spring-boot-api-overview/) for further details on running the API
* See the [OAuth Integration Page](https://authguidance.com/2019/03/24/java-spring-boot-api-coding-key-points/) for key implementation details

### Programming Languages

* Java 17 and Spring Boot 2.6 are used to implement the REST API

### Middleware Used

* The Tomcat web server hosts the API over SSL port 443
* AWS Cognito is used as the default Authorization Server
* The [Jose4j Library](https://bitbucket.org/b_c/jose4j/wiki/Home) library is used to manage in memory validation of JWTs
* API logs can be aggregated to [Elasticsearch](https://authguidance.com/2019/07/19/log-aggregation-setup/) to support [Query Use Cases](https://authguidance.com/2019/08/02/intelligent-api-platform-analysis/)
