import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.seanmunoz.examples.Address;
import com.seanmunoz.examples.Customer;
import com.seanmunoz.examples.CustomerService;
import com.seanmunoz.examples.PhoneNumber;

/**
 * 
 */

/**
 * @author Sean Munoz
 *
 */
public class CustomerServiceTest {

	private static final String EJB_JAR_FILENAME = "X:/SourceCode/Java/Java-EE/RESTfulExampleJAX-RS/target/RESTfulExampleJAX-RS-0.0.2-SNAPSHOT/WEB-INF";
	private static final String EJB_JNDI_NAME = "java:global/classes/CustomerService";
	private static EJBContainer container;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(EJBContainer.MODULES, new File(EJB_JAR_FILENAME));
//		container = EJBContainer.createEJBContainer(properties);
		container = EJBContainer.createEJBContainer();
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
		assertNotNull(instance);
		
		Customer customer = new Customer();
		customer.setFirstName("JOHN");
		customer.setLastName("SMITH");
		customer.setAddress(new Address());
		customer.getAddress().setStreet("123 Sample Street");
		customer.getAddress().setCity("Sacramento");
		customer.setPhoneNumbers(new HashSet<PhoneNumber>());
		PhoneNumber p; 
		p = new PhoneNumber();
		p.setType("Work");
		p.setNum("800-555-1212");
		customer.getPhoneNumbers().add(p);
		p = new PhoneNumber();
		p.setType("Home");
		p.setNum("916-555-1212");
		customer.getPhoneNumbers().add(p);

		instance.create(customer);
		assertEquals(1, instance.findCustomersByCity("Sacramento").size());		
		
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
		assertNotNull(instance);
		
		Customer customer = instance.read(151);
		
		assertNotNull(customer);
		assertEquals("Verify name: FRANK ZAPA", customer.getLastName(), "ZAPA");
		assertEquals("Verify city.", customer.getAddress().getCity(), "Sacramento");
		assertEquals("Verify two phone numbers", customer.getPhoneNumbers().size(), 2);	
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
	 */
	@Test
	public void testUpdate() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.seanmunoz.examples.CustomerService#delete(long)}.
	 */
	@Test
	public void testDelete() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.seanmunoz.examples.CustomerService#findCustomersByCity(java.lang.String)}.
	 */
	@Test
	public void testFindCustomersByCity() {
		fail("Not yet implemented");
	}

}
