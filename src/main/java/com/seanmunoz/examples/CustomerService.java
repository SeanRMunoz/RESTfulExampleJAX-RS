/**
 * Stateless session bean for entity classes
 */
package com.seanmunoz.examples;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Stateless
@LocalBean
@Path("/customers")
public class CustomerService {

	// NOTE: unitName is the name of the "persistence unit" as defined in the
	// persistence.xml file
    @PersistenceContext(unitName="RESTfulExampleJAX-RS",
                        type=PersistenceContextType.TRANSACTION)
    EntityManager entityManager;

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void create(Customer customer) {
        entityManager.persist(customer);
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{id}")
    public Customer read(@PathParam("id") long id) {
        return entityManager.find(Customer.class, id);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/json/{id}")
    public Customer readJSON(@PathParam("id") long id) {
    	return entityManager.find(Customer.class, id);
    }
    
    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void update(Customer customer) {
        entityManager.merge(customer);
    }

    @DELETE
    @Path("{id}")
    public void delete(@PathParam("id") long id) {
        Customer customer = read(id);
        if(null != customer) {
            entityManager.remove(customer);
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("findCustomersByCity/{city}")
    public List<Customer> findCustomersByCity(@PathParam("city") String city) {
        Query query = entityManager.createNamedQuery("findCustomersByCity");
        query.setParameter("city", city);
        return query.getResultList();
    }

	@GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/all")
    @SuppressWarnings("unchecked")
    public List<Customer> getAllCustomers() {
    	Query query = entityManager.createNamedQuery("getAllCustomers");
    	return query.getResultList();
    }
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/byPhone/{phone}")
    public List<Customer> findCustomersByPhone(@PathParam("phone") String phone) {
    	@SuppressWarnings("unchecked")
		List<Customer> customers = entityManager
				.createNamedQuery("findCustomersByPhone")
				.setParameter("phone", phone)
				.getResultList();
		return customers;
    }

}
