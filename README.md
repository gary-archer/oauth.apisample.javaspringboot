# authguidance.apisample.javaspringboot

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/599ddc4dabcc4810b6ac9af8ddc8bc20)](https://www.codacy.com/gh/gary-archer/authguidance.apisample.javaspringboot/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=gary-archer/authguidance.apisample.javaspringboot&amp;utm_campaign=Badge_Grade)

[![Known Vulnerabilities](https://snyk.io/test/github/gary-archer/authguidance.apisample.javaspringboot/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/gary-archer/authguidance.apisample.javaspringboot?targetFile=pom.xml&x=2)

### Overview 

* The Final Java API code sample using OAuth 2.x and Open Id Connect, from my blog at https://authguidance.com

### Details

* See the [Overview Page](https://authguidance.com/2019/03/24/java-spring-boot-api-overview/) for what the API does and how to run it
* See the [OAuth Integration Page](https://authguidance.com/2019/03/24/java-spring-boot-api-coding-key-points/) for details on OAuth Integration and Custom Claims handling

### Programming Languages

* Java 13 and Spring Boot 2.3 are used to implement the REST API

### Middleware Used

* The Tomcat web server hosts the API over SSL, using OpenSSL self signed certificates 
* AWS Cognito is used as the Cloud Authorization Server
* The [Connect2Id SDK](https://connect2id.com/products/nimbus-oauth-openid-connect-sdk) is used for API OAuth handling
* [Cache2K](https://cache2k.org) is used to cache API claims in memory
* API logs can be aggregated to [Elastic Search](https://authguidance.com/2019/07/19/log-aggregation-setup/) to support [Query Use Cases](https://authguidance.com/2019/08/02/intelligent-api-platform-analysis/)
* [EA Async](https://github.com/electronicarts/ea-async) is used to implement a Non Blocking API with simple code
