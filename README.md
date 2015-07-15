# RESTfulExampleJAX-RS
Example of creating a RESTful web service using Java EE's JAX-RS API. 


## Issues:

### Ping error in GlassFish when creating JDBC connection pool for MySQL 
- <b>ERROR MSG:</b>
	Ping Connection Pool failed for CustomerService. Class name is wrong or classpath is not set for : com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource Please check the server.log for more details.
- <b>RESOLUTION:</b>
  COPIED: mysql-connector-java-5.1.36.jar
  INTO:	C:\Users\Sean\Desktop\sandbox\glassfish4\glassfish\domains\domain1\lib\ext
- See URL: http://stackoverflow.com/a/8350030/5046445
- URL for MySQL .jar download: http://mvnrepository.com/artifact/mysql/mysql-connector-java    
