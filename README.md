# RESTfulExampleJAX-RS
Example project that creates a _**RESTful Web Service**_ using various **Java EE** technologies. The production service is deployed to **GlassFish 4.1** and uses a *JDBC Connection Pool* that ties to a *MySQL* database. Unit test environment uses **embedded OpenEJB** and **JAX-RS Client API** (via **Apache CXF WebClient API**) to test EJBs in a standalone SE environment using real container services.    

## Technologies Used:
The following **Java EE** technologies are used in this project:

- **EJB:** Enterprise JavaBeans
- **JAX-RS:** The Java API for RESTful Web Services 
- **JPA:** Java Persistence Architecture
- **JAXB:** Java Architecture for **XML** & **JSON** Bindings
- **EclipseLink:** The **JPA** *provider* responsible for ORM and Persistence. Alternates include *Hibernate*, *OpenJPA*, etc. 
- **EclipseLink MOXy:** The **JAXB** *provider* (for **XML** & **JSON** bindings) via the `jaxb.properties` file. Also defines annotation: `@XmlInverseReference` 
- **JDBC Connection Pools (DBCP)** and **JDBC Resource** on **GlassFish Server v4.1**
- **Embedded OpenEJB** *(Unit test ONLY)*
- **Apache CXF WebClient API** *(Unit test ONLY)*

## Entity Diagram:
Diagram created in Eclipse via: *Project->JPA Tools->Open Diagram*
![Data Diagram](diagrams/RESTfulExampleJAX-RS.png?raw=true "Data Diagram")

## Execution Steps:
- Start MySQL database server
- Start GlassFish server
- Create/configure: *JDBC Connection Pool* (on GlassFish)
- Create/configure: *JDBC Resource* (on GlassFish)
- Publish/deploy this web service
- Test **READ** of data using the following URLs:
    - [Fetch customer with: *ID = 1*](http://localhost:8080/RESTfulExampleJAX-RS/rest/customers/1)
    - [Fetch customers with: *city = Sacramento*](http://localhost:8080/RESTfulExampleJAX-RS/rest/customers/findCustomersByCity/Sacramento)
- Test **CREATE** or **UPDATE** of records using *Google's REST Console* as follows:
    - Target -> Request URI = `http://localhost:8080/RESTfulExampleJAX-RS/rest/customers`
    - Target -> Request Method = `POST` or `PUT`
    - Body -> Content Headers -> Content-Type = `application/xml` or `application/json` 
    - Body -> Request Payload -> Raw Body = `<customer><address><city>CITY-NAME</city><id>100</id><street>123 YOUR STREET</street></address><firstName>FIRST</firstName><id>100</id><lastName>LAST</lastName><phoneNumbers><id>100</id><num>916-123-456</num><type>HOME</type></phoneNumbers><phoneNumbers><id>101</id><num>800-555-1212</num><type>WORK</type></phoneNumbers></customer>`
    - *SEND* the request and the results should read: *Response Headers -> Status Code: 204*
- Test **DELETE** of a record using using *Google's REST Console* as follows:
    - Target -> Request URI = `http://localhost:8080/RESTfulExampleJAX-RS/rest/customers/100`
    - Target -> Request Method = `DELETE`
    - Body -> Content Headers -> Content-Type = *Clear checkbox*
    - Body -> Request Payload -> Raw Body = *Clear checkbox*
    - *SEND* the request and the results should read: *Response Headers -> Status Code: 204*
        

## References:

This project is based upon code found at:

- <em>Creating a RESTful Web Service:</em> http://blog.bdoughan.com/2010/08/creating-restful-web-service-part-15.html
 
## Issues:

### Ping error in GlassFish when creating JDBC connection pool for MySQL 
- **ERROR MESSAGE:**

    `Ping Connection Pool failed for CustomerService. Class name is wrong or classpath is not set for : com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource Please check the server.log for more details.`
	
- **RESOLUTION:**
    - COPIED: `mysql-connector-java-5.1.36.jar`
    - INTO:	`C:\Users\Sean\Desktop\sandbox\glassfish4\glassfish\domains\domain1\lib\ext`
    - See URL: http://stackoverflow.com/a/8350030/5046445
    - URL for MySQL .jar download: http://mvnrepository.com/artifact/mysql/mysql-connector-java    

### Deploy error - ClassNotFoundException: org.glassfish.jersey.spi.container.servlet.ServletContainer 
The example code specified a "servlet-class" valid for Jersey 1.x in the <b>web.xml</b>. However, <b>GlassFish 4.x</b> includes the RI of Jersey 2.x and its "servlet-class" is different as show below:

- Jersey 2.x servlet-class: <code>org.glassfish.jersey.servlet.ServletContainer</code>
- Jersey 1.x servlet-class: <code><strike>com.sun.jersey.spi.container.servlet.ServletContainer</strike></code>

Also needed to add a POM dependency for runtime:

- <code>org.glassfish.jersey.containers:jersey-container-servlet:2.10.4</code>

### Unit/Integration Test - NEED alternate *persistence.xml* for *Embedded GlassFish*
- **PROBLEM:** This project uses *Embedded GlassFish* for unit/integration testing to avoid mocking EJB container services, while still allowing tests to run standalone by embedding container and database resources. During Unit Test, if Embedded GlassFish uses the production version of `persistence.xml` it cannot properly create the EJB container because the `jta-data-source` of `TestRest` is NOT present, nor should it be at test time. Removing `jta-data-source` from  `persistence.xml` allows Embedded GlassFish to properly create the EJB container using a default data source via embedded Derby... all is good and the tests run as expected. 

- **RESOLUTION:**
Try as I might, I could NOT get Embedded GlassFish to use anything other than the `persistence.xml` located at: `src/main/resources/META-INF/persistence.xml`  So, as a work-around solution, a separate test version was created at: `src/test/resources/META-INF/persistence-test.xml` **without** the `jta-data-source` property. The `@BeforeClass` and `@AfterClass` methods of the unit test class were updated to take care of swapping the test and production versions of `persistence.xml`. **NOTE:** The files being manipulated by the test class are NOT the originals, but rather the generated versions that are located in `/target` directory.   

- **LINKS:** This work-around method was chosen over using a Maven plugin based solution since I wanted to preserve using Eclipse's feature: *Run as JUnit Test*. The following answer on Stack Overflow explains this issue and the workings of Embedded GlassFish:
    - [Running tests using Embedded Glassfish](http://stackoverflow.com/a/6740944/5046445) 

### Unit Test of EJBs using Embedded OpenEJB FAILS during Maven release:perform
Unit tests that ran successfully via `mvn clean install` are now failing when executed via `mvn release:perform`. Successful unit tests use **Embedded OpenEJB**, which deploys an EJB that test code calls via  `.getContext().lookup("java:global/RESTfulExampleJAX-RS/CustomerService")` and via **CXF WebClient** requests to URL: `http://localhost:4204/RESTfulExampleJAX-RS/customers`. However, unit tests that are run as part of the Maven Release Plugin's 'perform' goal are failing because OpenEJB is deploying the same EJB with a different JNDI name: `java:global/checkout/CustomerService` and a different request URL: `http://localhost:4204/checkout/customers`.

- **ERROR MESSAGES:**
    ```
    javax.naming.NameNotFoundException: Name "global/RESTfulExampleJAX-RS/CustomerService" not found.
    ```
    OR
    ```
    WARNING - No root resource matching request path /RESTfulExampleJAX-RS/customers/json/9 has been found
    WARNING - javax.ws.rs.WebApplicationException
    ``` 

- **EXPLANATION:**
The unit tests are failing for two different reasons: 1) JNDI name lookup not found, and 2) URL path not found.  Both are related to the Maven goal *release:perform* executing its unit tests under a different code subdirectory at: `target/checkout`. This causes **OpenEJB** to use a different **JNDI name** and **URL** path in the test class when executing the unit tests as shown below:
    - Unit test output via: `mvn clean install`
    ```
    Jndi(name="java:global/RESTfulExampleJAX-RS/CustomerService")
    ...
    Service URI: http://localhost:4204/RESTfulExampleJAX-RS/customers
    ```        
    - Unit test output via: `mvn release:perform`
    ```
    Jndi(name="java:global/checkout/CustomerService")
    ...
    Service URI: http://localhost:4204/checkout/customers
    ```

- **RESOLUTION:**
    Until a proper resolution is found, the work-around when performing a Maven release is to skip the unit tests by using the following commands:
    ```
    mvn -B release:prepare
    mvn release:perform -Darguments="-DskipTests"
    ```
