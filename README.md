# authguidance.apisample.javaspringboot

[![Known Vulnerabilities](https://snyk.io/test/github/gary-archer/authguidance.apisample.javaspringboot/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/gary-archer/authguidance.apisample.javaspringboot?targetFile=pom.xml)

### Overview 

* The final API code sample using OAuth 2.0 and Open Id Connect, referenced in my blog at https://authguidance.com
* **The goal of this sample is to implement this blog's** [API Architecture](https://authguidance.com/2019/03/24/api-platform-design/) **in Java using Spring Boot**

### Details

* See the [Overview Page](https://authguidance.com/2019/03/24/java-spring-boot-api-overview/) for what the API does and how to run it
* See the [OAuth Integration Page](https://authguidance.com/2019/03/24/java-spring-boot-api-coding-key-points/) for notes on Custom Claims Handling

### Programming Languages

* Java 13 and Spring Boot 2.3 are used to implement the REST API

### Middleware Used

* The [Connect2Id SDK](https://connect2id.com/products/nimbus-oauth-openid-connect-sdk) is used for API OAuth operations
* [Cache2K](https://cache2k.org) is used to cache API claims in memory
* The Tomcat web server is used to host both the API and the SPA's static web content
* AWS Cognito is used for the Authorization Server
* OpenSSL is used for SSL certificate handling
* API logs can be aggregated to [Elastic Search](https://authguidance.com/2019/07/19/log-aggregation-setup/) to support common [Query Use Cases](https://authguidance.com/2019/08/02/intelligent-api-platform-analysis/)
