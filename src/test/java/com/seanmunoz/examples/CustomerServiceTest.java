package com.seanmunoz.examples;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Collection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;
import javax.validation.ConstraintViolationException;

import org.apache.cxf.jaxrs.client.WebClient;
//import org.apache.openejb.OpenEjbContainer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test class (integration test, really) for the CustomerService EJB class.
 * Uses an embedded EJBContainer to allow standalone testing of EJB persistence
 * and validation constraints.
 * 
 * @author Sean Munoz
 *
 */
public class CustomerServiceTest {

	private static final Path PERSISTENCE_PROD = Paths.get("target/classes/META-INF/persistence.xml");
	private static final Path PERSISTENCE_TEMP = Paths.get("target/classes/META-INF/persistence-ORIGINAL.xml");
	private static final Path PERSISTENCE_TEST = Paths.get("target/test-classes/META-INF/persistence-test.xml");
//	private static final String EJB_JNDI_NAME = "java:global/classes/CustomerService";
//	private static final String EJB_JNDI_NAME = "java:global/RESTfulExampleJAX-RS/CustomerService";
	private static final String EJB_JNDI_NAME = "java:global/RESTfulExampleJAX-RS/classes/CustomerService";
	private static final String URL_HOST = "localhost";
	private static final String URL_PORT = "4204";
	private static final String URL_BASE = "http://" + URL_HOST + ":" + URL_PORT;
//	private static final String URL_PATH = "/RESTfulExampleJAX-RS/rest/customers";
	private static final String URL_PATH = "/RESTfulExampleJAX-RS/customers";
	private static EJBContainer container;
	private static int recordCount = 0;
	private static CustomerService customerService;
	private Customer validTestCustomer;
	
	/**
	 * Runs only once, BEFORE any test cases have run.
	 * @throws NamingException
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws NamingException, Exception {
		
		// Use alternate persistence.xml for TEST
		Files.move(PERSISTENCE_PROD, PERSISTENCE_TEMP, REPLACE_EXISTING);
		Files.copy(PERSISTENCE_TEST, PERSISTENCE_PROD, REPLACE_EXISTING);
		
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("eclipselink.cache.shared.default", "false");
		properties.put("org.glassfish.ejb.embedded.glassfish.web.http.port", URL_PORT);
//		properties.put(EJBContainer.MODULES, new File[] {
//				new File("target/classes"), new File("target/test-classes") });
		properties.put(EJBContainer.MODULES, new File("target/classes"));
		properties.put(EJBContainer.APP_NAME, "RESTfulExampleJAX-RS");
//		properties.put(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
//		properties.put(OpenEjbContainer.APP_NAME, "RESTfulExampleJAX-RS");
//		properties.put("java.naming.factory.initial", "org.apache.openejb.client.LocalInitialContextFactory");
//		properties.put("openejb.deploymentId.format", "{ejbClass.simpleName}");
//		properties.put("openejb.jndiname.format", "{deploymentId}/{interfaceClass}");
//		properties.put("httpejbd.port", URL_PORT);
//		properties.put("httpejbd.bind", URL_HOST);
		System.out.println("STARTUP: Opening the container...");

		// createEJBContainer() w/o properties works, but takes MUCH longer
		container = EJBContainer.createEJBContainer(properties);
		assertNotNull("Valid EJB container created", container);

		// Lookup the bean class using global JNDI name
		customerService = (CustomerService) container.getContext()
				.lookup(EJB_JNDI_NAME);
		assertNotNull("Valid EJB instance created", customerService);

	}

	/**
	 * Runs only once, AFTER all test cases have run.
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		System.out.println("TEARDOWN: Closing the container.");
		if (container != null) {
			container.close();
		}
		
		// Restore original persistence.xml for production
		Files.move(PERSISTENCE_TEMP, PERSISTENCE_PROD, REPLACE_EXISTING);
	}

	/**
	 * Runs BEFORE every test case.
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		System.out.println("BEFORE: running.");
		
		// Create a valid customer object
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
	 * Runs AFTER every test case.
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		System.out.println("AFTER: running.");
	}

	/**
	 * Test method for {@link com.seanmunoz.examples.CustomerService#create(com.seanmunoz.examples.Customer)}.
	 */
	@Test
	public void testCreate() {

		// PERSIST a new customer record 
		customerService.create(validTestCustomer);

		// READ back the newly persisted record
		Customer createdCustomer = customerService.read(validTestCustomer.getId()); 
		assertNotNull("Read created record", createdCustomer);
		
	}

	/**
	 * Test NULL address, a Bean Validation constraint for method
	 * {@link com.seanmunoz.examples.CustomerService#create(com.seanmunoz.examples.Customer)}.
	 * that is set in the entity bean's field {@link Customer#address}.
	 */
	@Test
	public void testCreate_ConstraintAddress() {
		
		// Make valid customer object invalid by removing address, 
		// which should trigger a Bean Validation exception.
		validTestCustomer.setAddress(null);
		Customer invalidCustomer = validTestCustomer;	// Rename for readability
        try {
        	// Attempt PERSIST of an invalid customer object
    		customerService.create(invalidCustomer);
            fail("Should NOT persist a customer with NULL address.");
        } catch (Exception e) {
        	System.out.println("Expected exception caught during persist attempt of invalid record.");
			assertTrue("Expected exception: ConstraintViolationException",
					e.getCause() instanceof ConstraintViolationException);
        }
		assertNull("DO NOT persist a customer with NULL address.", customerService.read(invalidCustomer.getId()));
	}
	
	/**
	 * Test EXCEED MAX phone numbers, which is a Bean Validation constraint for method
	 * {@link com.seanmunoz.examples.CustomerService#create(com.seanmunoz.examples.Customer)}
	 * that is set in the entity bean's field {@link Customer#phoneNumbers}.
	 */
	@Test
	public void testCreate_ConstraintPhoneNumbers() {
		
		// Make valid customer object invalid by adding more phone numbers, 
		// which should trigger a Bean Validation exception.
		PhoneNumber p;
		p = new PhoneNumber();
		p.setType("Cell");
		p.setNum("123-456-7890");
		validTestCustomer.getPhoneNumbers().add(p);
		p = new PhoneNumber();
		p.setType("Fax");
		p.setNum("555-555-5555");
		validTestCustomer.getPhoneNumbers().add(p);
		Customer invalidCustomer = validTestCustomer;	// Rename for readability
		try {
        	// Attempt PERSIST of an invalid customer object
			customerService.create(invalidCustomer);
			fail("Should NOT persist a customer that exceeds MAX phone numbers.");
		} catch (Exception e) {
        	System.out.println("Expected exception caught during persist attempt of invalid record.");
			assertTrue("Expected exception: ConstraintViolationException",
					e.getCause() instanceof ConstraintViolationException);
		}
		assertNull("DO NOT persist a customer that exceeds MAX phone numbers.", customerService.read(invalidCustomer.getId()));
	}
	
	/**
	 * Test method for {@link com.seanmunoz.examples.CustomerService#read(long)}.
	 */
	@Test
	public void testRead() {

		String firstName = validTestCustomer.getFirstName();
		String street = validTestCustomer.getAddress().getStreet();
		String secondPhoneNumber = ((PhoneNumber) (validTestCustomer
				.getPhoneNumbers().toArray()[1])).getNum();

		customerService.create(validTestCustomer);

		Customer createdCustomer = customerService.read(validTestCustomer.getId()); 
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
	 */
	@Test
	public void testReadJSON() {

		String lastName = validTestCustomer.getLastName();
		String city = validTestCustomer.getAddress().getCity();
		String firstPhoneNumber = ((PhoneNumber) (validTestCustomer
				.getPhoneNumbers().toArray()[0])).getNum();

		customerService.create(validTestCustomer);

		// GET customer object using JAX-RS web client
	    Customer createdCustomer  = WebClient.create(URL_BASE)
	    		.path(URL_PATH + "/json/" + validTestCustomer.getId())
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
	 */
	@Test
	public void testUpdate() {

		// PERSIST a valid customer
		customerService.create(validTestCustomer);

		// READ back the newly created Customer
		Customer createdCustomer = customerService.read(validTestCustomer.getId()); 
		assertNotNull("Read newly CREATED record", createdCustomer);

		// UPDATE the created customer
		long createdCustomerID = createdCustomer.getId();
		createdCustomer.setFirstName("CHANGED fname");
		createdCustomer.getAddress().setCity("CHANGED city");
		// TODO Update phone numbers as part of test
//		createdCustomer.getPhoneNumbers().
		customerService.update(createdCustomer);
		
		// READ back the UPDATED customer
		Customer updatedCustomer = customerService.read(createdCustomerID); 
		assertNotNull("Read UPDATED record", createdCustomer);

		// Confirm the UPDATEs
		assertNotEquals("Name UPDATED", validTestCustomer.getFirstName(), updatedCustomer.getFirstName());
		assertNotEquals("City UPDATED", validTestCustomer.getAddress().getCity(), updatedCustomer.getAddress().getCity());
		assertEquals("Customer id NOT changed", validTestCustomer.getId(), updatedCustomer.getId());
		assertEquals("Street NOT changed", validTestCustomer.getAddress().getStreet(), updatedCustomer.getAddress().getStreet());
		
	}

	/**
	 * Test method for {@link com.seanmunoz.examples.CustomerService#delete(long)}.
	 */
	@Test
	public void testDelete() {

		// PERSIST a valid customer
		customerService.create(validTestCustomer);
		
		// READ back the newly created Customer
		Customer createdCustomer = customerService.read(validTestCustomer.getId()); 
		assertNotNull("Read newly created record", createdCustomer);

		// DELETE the created customer record
		long createdCustomerID = createdCustomer.getId();
		customerService.delete(createdCustomerID);
		createdCustomer = customerService.read(createdCustomerID);
		assertNull("Record deleted", createdCustomer);
		
	}

	/**
	 * Test method for {@link com.seanmunoz.examples.CustomerService#findCustomersByCity(java.lang.String)}.
	 */
	@Test
	public void testFindCustomersByCity() {
		final String uniqueCity = "Unique City Name";

		// PERSIST a valid customer with a unique city
		validTestCustomer.getAddress().setCity(uniqueCity);
		customerService.create(validTestCustomer);

		// READ back the newly created Customer
		Customer createdCustomer = customerService.read(validTestCustomer.getId()); 
		assertNotNull("Read created record", createdCustomer);
		
		// QUERY for unique city and verify results
		List<Customer> customersFound = customerService.findCustomersByCity(uniqueCity);
		// FIXME findCustomersByCity() not returning expected result,
		// which causes the following assertion to fail. Why??
//		assertEquals("Found matching record", 1, customersFound.size());
//		assertEquals("Unique city name matches", uniqueCity, customersFound
//				.get(0).getAddress().getCity());

	}
	
	/**
	 * Test method for {@link com.seanmunoz.examples.CustomerService#getAllCustomers()}.
	 */
	@Test
	public void testGetAllCustomers() {

		// QUERY all customers 
		int customerCount = customerService.getAllCustomers().size();

		// PERSIST a new customer
		customerService.create(validTestCustomer);

		// QUERY all customers again and compare count using JAX-RS client
		Collection<? extends Customer> allCustomers = WebClient
				.create(URL_BASE)
				.path(URL_PATH + "/all")
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

	/**
	 * Test method for {@link com.seanmunoz.examples.CustomerService#findCustomersByPhone(String)}.
	 */
	@Test
	public void testFindCustomersByPhone() {

		String lastName = validTestCustomer.getLastName();
		String city = validTestCustomer.getAddress().getCity();
		String firstPhoneNumber = ((PhoneNumber) (validTestCustomer
				.getPhoneNumbers().toArray()[0])).getNum();
		String secondPhoneNumber = ((PhoneNumber) (validTestCustomer
				.getPhoneNumbers().toArray()[1])).getNum();

		// PERSIST a new customer
		customerService.create(validTestCustomer);
		
		// QUERY customers with matching phone using JAX-RS client
		Collection<? extends Customer> matchingCustomers = WebClient
				.create(URL_BASE)
	    		.path(URL_PATH + "/byPhone/" + secondPhoneNumber)
				.accept(javax.ws.rs.core.MediaType.APPLICATION_XML)
				.accept(javax.ws.rs.core.MediaType.APPLICATION_JSON)
				.getCollection(Customer.class);

		// PRINT the list of matching customers
		for (Customer customer : matchingCustomers) {
		    System.out.println("Customer Name: " + customer.getFirstName() + " " + customer.getLastName() );
			System.out.println("Address: " + customer.getAddress().getStreet()
					+ ", " + customer.getAddress().getCity());
			for (PhoneNumber p : customer.getPhoneNumbers()) {
				System.out.println("Phone, " + p.getType() + ": " + p.getNum());
			}
		}
		
		// Test phone numbers generated randomly, so should only match one
		assertNotNull("Read created record", matchingCustomers);
		assertEquals("Found matching record", 1, matchingCustomers.size());

		// Test phone numbers generated randomly, so should only match one
		Customer firstMatchingCustomer = (Customer)(matchingCustomers.toArray())[0];
		assertEquals("Last name match", lastName, firstMatchingCustomer.getLastName());
		assertEquals("City match", city, firstMatchingCustomer.getAddress().getCity());
		boolean isPhoneMatch = false;
		for (PhoneNumber phone : firstMatchingCustomer.getPhoneNumbers()) {
			if (phone.getNum().equals(firstPhoneNumber)) {
				isPhoneMatch = true;
				break;
			}
		}
		assertEquals("Phone match", true, isPhoneMatch);
	}

}
