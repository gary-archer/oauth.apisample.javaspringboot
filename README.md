# oauth.apisample.javaspringboot

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/599ddc4dabcc4810b6ac9af8ddc8bc20)](https://www.codacy.com/gh/gary-archer/oauth.apisample.javaspringboot/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=gary-archer/oauth.apisample.javaspringboot&amp;utm_campaign=Badge_Grade)

[![Known Vulnerabilities](https://snyk.io/test/github/gary-archer/oauth.apisample.javaspringboot/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/gary-archer/oauth.apisample.javaspringboot?targetFile=pom.xml&x=2)

### Overview 

The Final Java API code sample using OAuth and Open Id Connect, from my blog at https://authguidance.com:

- The API takes finer control over OAuth processing via a filter, implemented with a certified library
- The API also implements other [Non Functional Behaviour](https://authguidance.com/2017/10/08/corporate-code-sample-core-behavior/), to enable productivity and quality

### Quick Start
The setup script downloads SSL certificates, after which run the standard commands:

- ./setup.sh
- mvn clean install
- java -jar target/sampleapi-0.0.1-SNAPSHOT.jar

### Details

* See the [Overview Page](https://authguidance.com/2019/03/24/java-spring-boot-api-overview/) for further details on running the API
* See the [OAuth Integration Page](https://authguidance.com/2019/03/24/java-spring-boot-api-coding-key-points/) for key implementation details

### Programming Languages

* Java 13 and Spring Boot 2.5 are used to implement the REST API

### Middleware Used

* The Tomcat web server hosts the API over SSL
* AWS Cognito is used as the default Authorization Server
* The [Nimbus SDK](https://connect2id.com/products/nimbus-oauth-openid-connect-sdk) is used to implement the OAuth custom filter
* [EA Async](https://github.com/electronicarts/ea-async) is used to implement a Non Blocking API with simple code
* API logs can be aggregated to [Elastic Search](https://authguidance.com/2019/07/19/log-aggregation-setup/) to support [Query Use Cases](https://authguidance.com/2019/08/02/intelligent-api-platform-analysis/)
