# RESTfulExampleJAX-RS
Example project that creates a _**RESTful Web Service**_ using **Java EE** technologies. The service is deployed to **GlassFish** and uses a *JDBC connection pool* that ties to a *MySQL* database on the backend. 

## Technologies Used:
The following **Java EE** technologies are used in this project:

- **JAX-RS:** The Java API for RESTful Web Services 
- **JPA:** Java Persistence Architecture
- **JAXB:** Java Architecture for XML Binding
- **EclipseLink:** The **JPA** *provider* responsible for ORM and Persistence. Alternates include Hibernate, OpenJPA, etc. 
- **EclipseLink MOXy:** The **JAXB** *provider* (for **XML** binding) via the `jaxb.properties` file. Also defines annotation: `@XmlInverseReference` 
- **EJB:** Enterprise JavaBeans
- **JDBC Connection Pools (DBCP)** and **JDBC Resource** on **GlassFish Server v4.1** 

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
    - Body -> Content Headers -> Content-Type = `application/xml`
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
- <b>ERROR MSG:</b>
	<em>Ping Connection Pool failed for CustomerService. Class name is wrong or classpath is not set for : com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource Please check the server.log for more details.</em>
- <b>RESOLUTION:</b>
  COPIED: mysql-connector-java-5.1.36.jar
  INTO:	C:\Users\Sean\Desktop\sandbox\glassfish4\glassfish\domains\domain1\lib\ext
- See URL: http://stackoverflow.com/a/8350030/5046445
- URL for MySQL .jar download: http://mvnrepository.com/artifact/mysql/mysql-connector-java    

### Deploy error - ClassNotFoundException: org.glassfish.jersey.spi.container.servlet.ServletContainer 
The example code specified a "servlet-class" valid for Jersey 1.x in the <b>web.xml</b>. However, <b>GlassFish 4.x</b> includes the RI of Jersey 2.x and its "servlet-class" is different as show below:

- Jersey 2.x servlet-class: <code>org.glassfish.jersey.servlet.ServletContainer</code>
- Jersey 1.x servlet-class: <code><strike>com.sun.jersey.spi.container.servlet.ServletContainer</strike></code>

Also needed to add a POM dependency for runtime:

- <code>org.glassfish.jersey.containers:jersey-container-servlet:2.10.4</code>

