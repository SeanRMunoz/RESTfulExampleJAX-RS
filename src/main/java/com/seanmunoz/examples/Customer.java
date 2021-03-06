package com.seanmunoz.examples;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@NamedQueries({
	@NamedQuery(name = "findCustomersByCity", 
		query = "SELECT c " +
				"FROM Customer c " +
				"WHERE c.address.city = :city"),
	@NamedQuery(name = "getAllCustomers", 
		query = "SELECT c " +
				"FROM Customer c " +
				"ORDER BY c.dateCreated DESC"),
	@NamedQuery(name = "findCustomersByPhone", 
		query = "SELECT c " +
				"FROM Customer c " +
				"JOIN c.phoneNumbers p " +
				"WHERE p.num LIKE :phone")
})
@XmlRootElement
public class Customer implements Serializable {
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue(strategy=GenerationType.AUTO)
   private long id;

   @Column(name="FIRST_NAME")
   private String firstName;

   @Column(name="LAST_NAME")
   private String lastName;

   @NotNull							// JPA validation constraint
   @Column(nullable = false)		// Database constraint
   @Temporal(TemporalType.TIMESTAMP)
   private Calendar dateCreated;
   
   @NotNull
   @OneToOne(mappedBy="customer", cascade={CascadeType.ALL})
   private Address address;

   @Size(max=3)
   @OrderBy("type ASC")		// FIXME OrderBy not working on phoneNumbers
   @OneToMany(mappedBy="customer", cascade={CascadeType.ALL})
   private Set<PhoneNumber> phoneNumbers;

   public Customer() {
	}

   public long getId() {
       return this.id;
   }

   public void setId(long id) {
       this.id = id;
   }

   public String getFirstName() {
       return this.firstName;
   }

   public void setFirstName(String firstName) {
       this.firstName = firstName;
   }

   public String getLastName() {
       return this.lastName;
   }

   public void setLastName(String lastName) {
       this.lastName = lastName;
   }

   public Address getAddress() {
       return this.address;
   }

   public void setAddress(Address address) {
       this.address = address;
   }
    
   public Set<PhoneNumber> getPhoneNumbers() {
       return this.phoneNumbers;
   }

   public void setPhoneNumbers(Set<PhoneNumber> phoneNumbers) {
       this.phoneNumbers = phoneNumbers;
   }

	public Calendar getDateCreated() {
		return dateCreated;
	}
	
	public void setDateCreated(Calendar dateCreated) {
		this.dateCreated = dateCreated;
	}
    
}
