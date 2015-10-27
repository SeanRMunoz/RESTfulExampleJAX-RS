package com.seanmunoz.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;
import javax.json.stream.JsonGenerator;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jsonp.JsonProcessingFeature;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test class that uses Arquillian platform to perform Integration Tests.  
 * 
 * @author Sean Munoz
 *
 */
@RunWith(Arquillian.class)
public class CustomerServiceIT {
	
    @Inject
    private CustomerService customerService;
    
    @PersistenceContext
    EntityManager em;
    
    @Inject
    UserTransaction utx;    

    @Deployment
    public static WebArchive createNotTestableDeployment() {
        final WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war")
//        		.addPackage(Customer.class.getPackage())
        		.setWebXML(new File("src/main/webapp/WEB-INF", "/web.xml"))
        		.addClasses(Address.class, Customer.class, CustomerService.class, PhoneNumber.class)
//	            .addAsResource("META-INF/persistence-test.xml", "META-INF/persistence.xml")
	            .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
//	            .addAsResource("jaxb.properties", "com/seanmunoz/examples/jaxb.properties")
        		.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.println(war.toString(Formatters.VERBOSE));
        return war;
    }    
	
	private static int recordCount = 0;
    Customer validTestCustomer;
    
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

	@Test
	public void testFindCustomersByCity() throws Exception {
		final String uniqueCity = "UniqueCityName";
		int customerCount = customerService.getAllCustomers().size();

//	    utx.begin();
//	    em.joinTransaction();

		// PERSIST a valid customer with a unique city
		validTestCustomer.getAddress().setCity(uniqueCity);
		customerService.create(validTestCustomer);

//	    utx.commit();
//	    // clear the persistence context (first-level cache)
//	    em.clear();
		
		// READ back the newly created Customer
		Customer createdCustomer = customerService.read(validTestCustomer.getId()); 
		assertNotNull("Read created record", createdCustomer);
		assertEquals("Unique city name matches", uniqueCity, createdCustomer
				.getAddress().getCity());
		
		// QUERY for unique city and verify results
//		List<Customer> customersFound = customerService.findCustomersByCity(uniqueCity);
		List<Customer> customersFound = customerService.getAllCustomers();
		
		// PRINT the list of customers
		for (Customer customer : customersFound) {
		    System.out.println("Customer Name: " + customer.getFirstName() + " " + customer.getLastName() );
			System.out.println("Address: " + customer.getAddress().getStreet()
					+ ", " + customer.getAddress().getCity());
			System.out.println("Created: " + customer.getDateCreated().getTime() );
			for (PhoneNumber p : customer.getPhoneNumbers()) {
				System.out.println("Phone, " + p.getType() + ": " + p.getNum());
			}
		}
		
		assertEquals("Found matching record", customerCount+1, customersFound.size());
		assertEquals("Unique city name matches", uniqueCity, customersFound
				.get(0).getAddress().getCity());

	}
	
	@Test
	@RunAsClient
	public void testGetAllCustomers(@ArquillianResource URL baseURL) throws Exception {
		
		// Create HTTP client connection
        Client client = ClientBuilder.newBuilder()
                .register(JsonProcessingFeature.class)
                .property(JsonGenerator.PRETTY_PRINTING, true)
                .build();
        
        // READ list of all customers
        List<Customer> customerListBefore = client
				.target(baseURL + "rest/customers/all")
				.request()
                .get(new GenericType<List<Customer>>(){});
        
		// PERSIST a valid customer and save its unique ID
    	Customer persistedCustomer = client
				.target(baseURL + "rest/customers")
    			.request()
    			.post(Entity.entity(validTestCustomer, MediaType.APPLICATION_JSON),
    					Customer.class);
    	final long persistedCustomerId = persistedCustomer.getId();
        System.out.println("POST persistedCustomer.getId(): " + persistedCustomerId );

        // READ list of all customers again AFTER adding one
        Response response = client
//				.target(baseURL + "rest/customers/findCustomersByCity/Sacramento")
//				.target(baseURL + "rest/customers/byPhone/916%25")
				.target(baseURL + "rest/customers/all")
//				.request()
//				.request(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON)
				.request(MediaType.APPLICATION_JSON)
//				.request(MediaType.APPLICATION_XML)
                .get();
        response.bufferEntity();
		List<Customer> customerListAfter = response.readEntity(new GenericType<List<Customer>>(){});
        
		System.out.println("baseURL: " + baseURL.toString());
        System.out.println("response.getStatus(): " + response.getStatus());
        System.out.println("response.readEntity(): " + response.readEntity(String.class));
        
		// PRINT list of customers, verifying new customer was added
        boolean matchFound = false;
		for (Customer customer : customerListAfter) {
			if (customer.getId() == persistedCustomerId) {
				matchFound = true;
			}
		    System.out.println("Customer Name: " + customer.getFirstName() + " " + customer.getLastName() );
		    System.out.println("Customer ID: " + customer.getId() );
			System.out.println("Address: " + customer.getAddress().getStreet()
					+ ", " + customer.getAddress().getCity());
			System.out.println("Created: " + customer.getDateCreated().getTime() );
			for (PhoneNumber p : customer.getPhoneNumbers()) {
				System.out.println("Phone, " + p.getType() + ": " + p.getNum());
			}
		}

		// Perform assertions on results
        assertEquals("HTTP response code = OK", HttpServletResponse.SC_OK, response.getStatus());
		assertEquals("Customer count increased by ONE", customerListBefore.size() + 1,
				customerListAfter.size());
        assertEquals("Newly added customer found in list", true, matchFound);
        
	}

	public void preparePersistenceTest() throws Exception {
		System.out.println("BEFORE: running.");
	    clearData();
	    createData();
	    insertData();
	    startTransaction();
	}
	
//	@After
	public void commitTransaction() throws Exception {
		System.out.println("AFTER: running.");
	    utx.commit();
	}

	private void clearData() throws Exception {
	    utx.begin();
	    em.joinTransaction();
	    System.out.println("Dumping old records...");
	    em.createQuery("delete from Customer").executeUpdate();
	    utx.commit();
	}

	@Before
	public void createData() throws Exception {
		
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

	private void insertData() throws Exception {
	    utx.begin();
	    em.joinTransaction();
	    System.out.println("Inserting records...");
        em.persist(validTestCustomer);
	    utx.commit();
	    // clear the persistence context (first-level cache)
	    em.clear();
	}

	private void startTransaction() throws Exception {
	    utx.begin();
	    em.joinTransaction();
	}	
	
}
