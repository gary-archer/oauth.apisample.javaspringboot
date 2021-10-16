# oauth.apisample.javaspringboot

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/599ddc4dabcc4810b6ac9af8ddc8bc20)](https://www.codacy.com/gh/gary-archer/oauth.apisample.javaspringboot/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=gary-archer/oauth.apisample.javaspringboot&amp;utm_campaign=Badge_Grade)

[![Known Vulnerabilities](https://snyk.io/test/github/gary-archer/oauth.apisample.javaspringboot/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/gary-archer/oauth.apisample.javaspringboot?targetFile=pom.xml&x=2)

### Overview 

The Final OAuth secured Java API code sample, referenced in my blog at https://authguidance.com:

- The API takes finer control over OAuth processing via a filter, implemented with a certified JOSE library
- The API provides two mechanisms for authorizing via domain specific claims
- The API also implements other [Non Functional Behaviour](https://authguidance.com/2017/10/08/corporate-code-sample-core-behavior/), for good technical quality

### Quick Start

Ensure that Java 13 and maven are installed, then run the start script to begin listening over SSL:

- ./start.sh

### Details

* See the [Overview Page](https://authguidance.com/2019/03/24/java-spring-boot-api-overview/) for further details on running the API
* See the [OAuth Integration Page](https://authguidance.com/2019/03/24/java-spring-boot-api-coding-key-points/) for key implementation details

### Programming Languages

* Java 13 and Spring Boot 2.5 are used to implement the REST API

### Middleware Used

* The Tomcat web server hosts the API over SSL port 443
* AWS Cognito is used as the default Authorization Server
* The [Nimbus JOSE JWT](https://connect2id.com/products/nimbus-jose-jwt) library is used to manage in memory validation of JWTs
* [EA Async](https://github.com/electronicarts/ea-async) is used to implement a Non Blocking API with simple code
* API logs can be aggregated to [Elastic Search](https://authguidance.com/2019/07/19/log-aggregation-setup/) to support [Query Use Cases](https://authguidance.com/2019/08/02/intelligent-api-platform-analysis/)
