package com.seanmunoz.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CustomerServiceIT {
	
    @Inject
    private CustomerService customerService;
    
    @PersistenceContext
    EntityManager em;
    
    @Inject
    UserTransaction utx;    

/*	
    @Deployment
    public static JavaArchive createDeployment() {
    	JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
            .addClasses(Address.class, Customer.class, CustomerService.class, PhoneNumber.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.println(jar.toString(true));
        return jar;        
    }
*/

//    @Deployment(testable = false)
    @Deployment
    public static WebArchive createNotTestableDeployment() {
        final WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war")
        		.addPackage(Customer.class.getPackage())
//        		.addClasses(Address.class, Customer.class, CustomerService.class, PhoneNumber.class)
//	            .addAsResource("META-INF/persistence-test.xml", "META-INF/persistence.xml")
	            .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
	            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.println(war.toString(Formatters.VERBOSE));
        return war;
    }    
	
	private static int recordCount = 0;
    Customer validTestCustomer;
    
//    @RunAsClient // Same as @Deployment(testable = false), should only be used in mixed mode
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
