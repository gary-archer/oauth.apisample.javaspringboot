# authguidance.websample.springbootapi

### Overview

* An SPA sample using OAuth 2.0 and Open Id Connect, referenced in my blog at http://authguidance.com
* **This sample implements our [API Architecture](http://authguidance.com/2017/10/03/api-tokens-claims) in Java using Spring Boot**

### Details

* See the **Overview Page** for how to run the API
* See the **Coding Key Points** for integration and reliability details

### Programming Languages

* TypeScript is used for the SPA
* Java 11 and Spring Boot 2 are used for the API

### Middleware Used

* The [Oidc-Client Library](https://github.com/IdentityModel/oidc-client-js) is used to implement the Implicit Flow
* The [MitreId Open Id Connect Client](https://github.com/mitreid-connect/OpenID-Connect-Java-Spring-Server/tree/master/openid-connect-client) is used to introspect tokens and to cache API claims in memory
* Tomcat is used to host both the API and the SPA's static web content
* Okta is used for the Authorization Server
* OpenSSL is used for SSL certificate handling
