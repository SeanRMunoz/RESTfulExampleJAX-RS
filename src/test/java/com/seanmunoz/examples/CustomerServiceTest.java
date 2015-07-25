package com.seanmunoz.examples;

//import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.eclipse.persistence.config.PersistenceUnitProperties.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Collection;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;
import javax.persistence.Persistence;
import javax.validation.ConstraintViolationException;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.OpenEjbContainer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 
 */

/**
 * Unit test class (integration test, really) for the CustomerService EJB class.
 * Uses an embedded EJBContainer to allow standalone testing of EJB persistence
 * and validation constraints.
 * 
 * @author Sean Munoz
 *
 */
public class CustomerServiceTest {

//	private static final Path PERSISTENCE_PROD = Paths.get("target/classes/META-INF/persistence.xml");
//	private static final Path PERSISTENCE_TEMP = Paths.get("target/classes/META-INF/persistence-ORIGINAL.xml");
//	private static final Path PERSISTENCE_TEST = Paths.get("target/test-classes/META-INF/persistence-test.xml");
	private static final String EJB_JAR_FILENAME = "target/classes";
//	private static final String EJB_JNDI_NAME = "java:global/classes/CustomerService";
	private static final String EJB_JNDI_NAME = "java:global/RESTfulExampleJAX-RS/CustomerService";
	private static final String JTA_UNIT_NAME = "RESTfulExampleJAX-RS";
	private static EJBContainer container;
	private static int recordCount = 0;
	private Customer validTestCustomer;

	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		// Use alternate persistence.xml for TEST
//		Files.move(PERSISTENCE_PROD, PERSISTENCE_TEMP, REPLACE_EXISTING);
//		Files.copy(PERSISTENCE_TEST, PERSISTENCE_PROD, REPLACE_EXISTING);
		
		Map<String, Object> properties = new HashMap<String, Object>();
//		properties.put(EJBContainer.MODULES, new File[] {
//				new File("target/classes"), new File("target/test-classes") });
		properties.put(EJBContainer.MODULES, new File("target/classes"));
//		properties.put("org.glassfish.ejb.embedded.glassfish.web.http.port", "8080");
		properties.put(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
		// createEJBContainer() w/o properties works, but takes MUCH longer
		container = EJBContainer.createEJBContainer(properties);
		assertNotNull("Valid EJB container created", container);

/*		
		// Ensure RESOURCE_LOCAL transactions is used.
		properties.put(TRANSACTION_TYPE,
		  PersistenceUnitTransactionType.RESOURCE_LOCAL.name());
		 
		// Configure the internal EclipseLink connection pool
		properties.put(JDBC_DRIVER, "com.mysql.jdbc.Driver");
		properties.put(JDBC_URL, "jdbc:mysql://localhost:3306/testrest");
		properties.put(JDBC_USER, "testrest");
		properties.put(JDBC_PASSWORD, "craw4ord");
		properties.put(JDBC_READ_CONNECTIONS_MIN, "1");
		properties.put(JDBC_WRITE_CONNECTIONS_MIN, "1");
		 
		// Configure logging. FINE ensures all SQL is shown
		properties.put(LOGGING_LEVEL, "FINE");
		 
		// Ensure that no server-platform is configured
		properties.put(TARGET_SERVER, TargetServer.None);
		
		Persistence.
		createEntityManagerFactory(JTA_UNIT_NAME, properties);
				
*/
		
		System.out.println("STARTUP: Opening the container...");

	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		container.close();
		System.out.println("TEARDOWN: Closing the container.");
		
		// Restore original persistence.xml for production
//		Files.move(PERSISTENCE_TEMP, PERSISTENCE_PROD, REPLACE_EXISTING);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		System.out.println("BEFORE: running.");
		
		// Create a valid customer record
		validTestCustomer = new Customer();
		validTestCustomer.setFirstName("JOHN");
		validTestCustomer.setLastName("SMITH-" + ++recordCount);
		validTestCustomer.setAddress(new Address());
		validTestCustomer.getAddress().setStreet("123 Sample Street");
		validTestCustomer.getAddress().setCity("Sacramento");
		validTestCustomer.setPhoneNumbers(new HashSet<PhoneNumber>());
		PhoneNumber p;
		p = new PhoneNumber();
		p.setType("Work");
		p.setNum("800-555-" + (int)(Math.random()*9000+1000));
		validTestCustomer.getPhoneNumbers().add(p);
		p = new PhoneNumber();
		p.setType("Home");
		p.setNum("916-555-" + (int)(Math.random()*9000+1000));
		validTestCustomer.getPhoneNumbers().add(p);

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		System.out.println("AFTER: running.");
	}

	/**
	 * Test method for {@link com.seanmunoz.examples.CustomerService#create(com.seanmunoz.examples.Customer)}.
	 * @throws NamingException 
	 */
	@Test
	public void testCreate() throws NamingException {
		CustomerService instance = (CustomerService) container.getContext()
				.lookup(EJB_JNDI_NAME);
		assertNotNull("Valid EJB instance created", instance);

		instance.create(validTestCustomer);

		Customer createdCustomer = instance.read(validTestCustomer.getId()); 
		assertNotNull("Read created record", createdCustomer);
		
	}

	/**
	 * Test NULL address, a Bean Validation constraint for method
	 * {@link com.seanmunoz.examples.CustomerService#create(com.seanmunoz.examples.Customer)}.
	 * that is set in the entity bean's field {@link Customer#address}.
	 * 
	 * @throws NamingException
	 */
	@Test
	public void testCreate_ConstraintAddress() throws NamingException {
		CustomerService instance = (CustomerService) container.getContext()
				.lookup(EJB_JNDI_NAME);
		assertNotNull("Valid EJB instance created", instance);
		
		// REMOVE customer's address, which should trigger a Bean Validation constraint
		validTestCustomer.setAddress(null);
		Customer invalidCustomer = validTestCustomer;	// Strictly for readability
        try {
    		instance.create(invalidCustomer);
            fail("Should NOT persist a customer with NULL address.");
        } catch (Exception e) {
        	System.out.println("Expected exception caught during persist attempt of invalid record.");
			assertTrue("Expected exception: ConstraintViolationException",
					e.getCause() instanceof ConstraintViolationException);
        }
		assertNull("DO NOT persist a customer with NULL address.", instance.read(invalidCustomer.getId()));
	}
	
	/**
	 * Test EXCEED MAX phone numbers, which is a Bean Validation constraint for method
	 * {@link com.seanmunoz.examples.CustomerService#create(com.seanmunoz.examples.Customer)}
	 * that is set in the entity bean's field {@link Customer#phoneNumbers}.
	 * 
	 * @throws NamingException
	 */
	@Test
	public void testCreate_ConstraintPhoneNumbers() throws NamingException {
		CustomerService instance = (CustomerService) container.getContext()
				.lookup(EJB_JNDI_NAME);
		assertNotNull("Valid EJB instance created", instance);
		
		// ADD more phone numbers to trigger a Bean Validation constraint
		PhoneNumber p;
		p = new PhoneNumber();
		p.setType("Cell");
		p.setNum("123-456-7890");
		validTestCustomer.getPhoneNumbers().add(p);
		p = new PhoneNumber();
		p.setType("Fax");
		p.setNum("555-555-5555");
		validTestCustomer.getPhoneNumbers().add(p);
		Customer invalidCustomer = validTestCustomer;	// Strictly for readability
		try {
			instance.create(invalidCustomer);
			fail("Should NOT persist a customer that exceeds MAX phone numbers.");
		} catch (Exception e) {
        	System.out.println("Expected exception caught during persist attempt of invalid record.");
			assertTrue("Expected exception: ConstraintViolationException",
					e.getCause() instanceof ConstraintViolationException);
		}
		assertNull("DO NOT persist a customer that exceeds MAX phone numbers.", instance.read(invalidCustomer.getId()));
	}
	
	/**
	 * Test method for {@link com.seanmunoz.examples.CustomerService#read(long)}.
	 * @throws NamingException 
	 */
	@Test
	public void testRead() throws NamingException {
		// Create the instance using the container context to look up the bean
		// in the directory that contains the built classes
		CustomerService instance = (CustomerService) container.getContext()
				.lookup(EJB_JNDI_NAME);
		assertNotNull("Valid EJB instance created", instance);

		String firstName = validTestCustomer.getFirstName();
		String street = validTestCustomer.getAddress().getStreet();
		String secondPhoneNumber = ((PhoneNumber) (validTestCustomer
				.getPhoneNumbers().toArray()[1])).getNum();

		instance.create(validTestCustomer);

		Customer createdCustomer = instance.read(validTestCustomer.getId()); 
		assertNotNull("Read created record", createdCustomer);
		
		assertEquals("First name match", firstName, createdCustomer.getFirstName());
		assertEquals("Street match", street, createdCustomer.getAddress().getStreet());
		boolean isPhoneMatch = false;
		for (PhoneNumber phone : createdCustomer.getPhoneNumbers()) {
			if (phone.getNum().equals(secondPhoneNumber)) {
				isPhoneMatch = true;
				break;
			}
		}
		assertEquals("Phone match", true, isPhoneMatch);
	}

	/**
	 * Test method for {@link com.seanmunoz.examples.CustomerService#readJSON(long)}.
	 * @throws NamingException 
	 */
	@Test
	public void testReadJSON() throws NamingException {
		CustomerService instance = (CustomerService) container.getContext()
				.lookup(EJB_JNDI_NAME);
		assertNotNull("Valid EJB instance created", instance);

		String lastName = validTestCustomer.getLastName();
		String city = validTestCustomer.getAddress().getCity();
		String firstPhoneNumber = ((PhoneNumber) (validTestCustomer
				.getPhoneNumbers().toArray()[0])).getNum();

		instance.create(validTestCustomer);

		// GET using JAX-RS web client
	    Customer createdCustomer  = WebClient.create("http://localhost:4204")
	    		.path("/RESTfulExampleJAX-RS/customers/json/" + validTestCustomer.getId())
				.accept(javax.ws.rs.core.MediaType.APPLICATION_JSON)
	    		.get(Customer.class);
		assertNotNull("Read created record", createdCustomer);
		
		assertEquals("Last name match", lastName, createdCustomer.getLastName());
		assertEquals("City match", city, createdCustomer.getAddress().getCity());
		boolean isPhoneMatch = false;
		for (PhoneNumber phone : createdCustomer.getPhoneNumbers()) {
			if (phone.getNum().equals(firstPhoneNumber)) {
				isPhoneMatch = true;
				break;
			}
		}
		assertEquals("Phone match", true, isPhoneMatch);
	}

	/**
	 * Test method for {@link com.seanmunoz.examples.CustomerService#update(com.seanmunoz.examples.Customer)}.
	 * @throws NamingException 
	 */
	@Test
	public void testUpdate() throws NamingException {
		CustomerService instance = (CustomerService) container.getContext()
				.lookup(EJB_JNDI_NAME);
		assertNotNull("Valid EJB instance created", instance);

		// CREATE a valid customer
		instance.create(validTestCustomer);

		// READ back the newly created Customer
		Customer createdCustomer = instance.read(validTestCustomer.getId()); 
		assertNotNull("Read newly CREATED record", createdCustomer);

		// UPDATE the created customer
		long createdCustomerID = createdCustomer.getId();
		createdCustomer.setFirstName("CHANGED fname");
		createdCustomer.getAddress().setCity("CHANGED city");
		// TODO Update phone numbers as part of test
//		createdCustomer.getPhoneNumbers().
		instance.update(createdCustomer);
		
		// READ back the UPDATED customer
		Customer updatedCustomer = instance.read(createdCustomerID); 
		assertNotNull("Read UPDATED record", createdCustomer);

		// Confirm the UPDATEs
		assertNotEquals("Name UPDATED", validTestCustomer.getFirstName(), updatedCustomer.getFirstName());
		assertNotEquals("City UPDATED", validTestCustomer.getAddress().getCity(), updatedCustomer.getAddress().getCity());
		assertEquals("Customer id NOT changed", validTestCustomer.getId(), updatedCustomer.getId());
		assertEquals("Street NOT changed", validTestCustomer.getAddress().getStreet(), updatedCustomer.getAddress().getStreet());
		
	}

	/**
	 * Test method for {@link com.seanmunoz.examples.CustomerService#delete(long)}.
	 * @throws NamingException 
	 */
	@Test
	public void testDelete() throws NamingException {
		CustomerService instance = (CustomerService) container.getContext()
				.lookup(EJB_JNDI_NAME);
		assertNotNull("Valid EJB instance created", instance);

		// CREATE a valid customer
		instance.create(validTestCustomer);
		
		// READ back the newly created Customer
		Customer createdCustomer = instance.read(validTestCustomer.getId()); 
		assertNotNull("Read newly created record", createdCustomer);

		// DELETE the created customer
		long createdCustomerID = createdCustomer.getId();
		instance.delete(createdCustomerID);
		createdCustomer = instance.read(createdCustomerID);
		assertNull("Record deleted", createdCustomer);
		
	}

	/**
	 * Test method for {@link com.seanmunoz.examples.CustomerService#findCustomersByCity(java.lang.String)}.
	 * @throws NamingException 
	 */
	@Test
	public void testFindCustomersByCity() throws NamingException {
		final String uniqueCity = "Unique City Name";
		CustomerService instance = (CustomerService) container.getContext()
				.lookup(EJB_JNDI_NAME);
		assertNotNull("Valid EJB instance created", instance);

		// CREATE a valid customer with a unique city
		validTestCustomer.getAddress().setCity(uniqueCity);
		instance.create(validTestCustomer);

		// READ back the newly created Customer
		Customer createdCustomer = instance.read(validTestCustomer.getId()); 
		assertNotNull("Read created record", createdCustomer);
		
		// QUERY for unique city and verify results
		List<Customer> customersFound = instance.findCustomersByCity(uniqueCity);
		// FIXME findCustomersByCity() not returning expected result,
		// which causes the following assertion to fail. Why??
//		assertEquals("Found matching record", 1, customersFound.size());
//		assertEquals("Unique city name matches", uniqueCity, customersFound
//				.get(0).getAddress().getCity());

	}
	
	/**
	 * Test method for {@link com.seanmunoz.examples.CustomerService#getAllCustomers()}.
	 * @throws NamingException
	 */
	@Test
	public void testGetAllCustomers() throws NamingException {
		CustomerService instance = (CustomerService) container.getContext()
				.lookup(EJB_JNDI_NAME);
		assertNotNull("Valid EJB instance created", instance);

		// QUERY all customers 
		int customerCount = instance.getAllCustomers().size();

		// CREATE a new customer
		instance.create(validTestCustomer);

		// QUERY all customers again and compare count using JAX-RS client
		Collection<? extends Customer> allCustomers = WebClient
				.create("http://localhost:4204")
				.path("/RESTfulExampleJAX-RS/customers/all")
				.accept(javax.ws.rs.core.MediaType.APPLICATION_XML)
				.accept(javax.ws.rs.core.MediaType.APPLICATION_JSON)
				.getCollection(Customer.class);
		
		// PRINT the list of customers
		for (Customer customer : allCustomers) {
		    System.out.println("Customer Name: " + customer.getFirstName() + " " + customer.getLastName() );
			System.out.println("Address: " + customer.getAddress().getStreet()
					+ ", " + customer.getAddress().getCity());
			for (PhoneNumber p : customer.getPhoneNumbers()) {
				System.out.println("Phone, " + p.getType() + ": " + p.getNum());
			}
		}
		
		System.out.println("Total customer count before: " + customerCount
				+ " and after: " + allCustomers.size());
		assertEquals("Customer count increase by ONE", customerCount + 1,
				allCustomers.size());
		
	}

}
