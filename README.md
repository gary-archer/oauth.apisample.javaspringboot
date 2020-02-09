# authguidance.apisample.javaspringboot

### Overview

* The final API code sample using OAuth 2.0 and Open Id Connect, referenced in my blog at https://authguidance.com
* **The goal of this sample is to implement our** [API Platform Architecture](https://authguidance.com/2019/03/24/api-platform-design/) **in Java using Spring Boot**

### Details

* See the [Overview Page](https://authguidance.com/2019/03/24/java-spring-boot-api-overview/) for what the API does and how to run it
* See the [Coding Key Points](https://authguidance.com/2019/03/24/java-spring-boot-api-coding-key-points/) for OAuth technical implementation details

### Programming Languages

* Java 13 and Spring Boot 2.2 are used to implement the REST API

### Middleware Used

* The [Connect2Id SDK](https://connect2id.com/products/nimbus-oauth-openid-connect-sdk) is used for API OAuth operations
* [Cache2K](https://cache2k.org) is used to cache API claims in memory
* Tomcat is used to host both the API and the SPA's static web content
* Okta is used for the Authorization Server
* OpenSSL is used for SSL certificate handling
