package com.seanmunoz.examples;

import static org.eclipse.persistence.config.PersistenceUnitProperties.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 
 */

/**
 * @author Sean Munoz
 *
 */
public class CustomerServiceTest {

//	private static final String EJB_JAR_FILENAME = "target/";
	private static final String EJB_JAR_FILENAME = "target/classes";
	private static final String EJB_JNDI_NAME = "java:global/classes/CustomerService";
	private static final String JTA_UNIT_NAME = "RESTfulExampleJAX-RS";
	private static EJBContainer container;
	private static int recordCount = 0;
	private Customer validTestCustomer;

	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(EJBContainer.MODULES, new File(EJB_JAR_FILENAME));
//		container = EJBContainer.createEJBContainer(properties);
		container = EJBContainer.createEJBContainer();
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
		p.setNum("800-555-1212");
		validTestCustomer.getPhoneNumbers().add(p);
		p = new PhoneNumber();
		p.setType("Home");
		p.setNum("916-555-1212");
		validTestCustomer.getPhoneNumbers().add(p);
		
		// Create an INVALID customer record...

		
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

		instance.create(validTestCustomer);

		Customer createdCustomer = instance.read(validTestCustomer.getId()); 
		assertNotNull("Read created record", createdCustomer);
		
		assertEquals("Name match", "JOHN", createdCustomer.getFirstName());
		assertEquals("Street match", "123 Sample Street", createdCustomer.getAddress().getStreet());
		boolean isPhoneMatch = false;
		for (PhoneNumber phone : createdCustomer.getPhoneNumbers()) {
			if (phone.getNum().equals("916-555-1212")) {
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
		fail("Not yet implemented");
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
	 */
	@Test
	public void testFindCustomersByCity() {
		fail("Not yet implemented");
	}

}
