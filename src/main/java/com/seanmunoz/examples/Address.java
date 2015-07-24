/**
 * This code is from a blog at URL:
 *    http://blog.bdoughan.com/2010/08/creating-restful-web-service-part-25.html
 */

package com.seanmunoz.examples;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

import org.eclipse.persistence.oxm.annotations.XmlInverseReference;
 
@Entity
public class Address implements Serializable {
    private static final long serialVersionUID = 1L;
 
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
 
    private String city;
 
    private String street;
 
    @OneToOne
//	@PrimaryKeyJoinColumn
    @JoinColumn(name="ID_CUSTOMER")
    private Customer customer;
 
    public Address() {
	}

    public long getId() {
        return this.id;
    }
 
    public void setId(long id) {
        this.id = id;
    }
 
    public String getCity() {
        return this.city;
    }
 
    public void setCity(String city) {
        this.city = city;
    }
 
    public String getStreet() {
        return this.street;
    }
 
    public void setStreet(String street) {
        this.street = street;
    }
 
    @XmlInverseReference(mappedBy="address")
    public Customer getCustomer() {
        return customer;
    }
 
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
 
}