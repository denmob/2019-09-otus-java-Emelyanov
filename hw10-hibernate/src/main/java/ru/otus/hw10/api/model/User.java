package ru.otus.hw10.api.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tUser")
public class User {

	@Id
	@GeneratedValue(strategy =  GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;

	@Column(name = "user_name")
	private String name;

	@Column(name = "user_age")
	private int age;

	@OneToOne(targetEntity = Address.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "address_id")
	private Address address;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<Phone> phones = new ArrayList<>();

	public User(String name, int age) {
		this.name = name;
		this.age = age;
	}

	public User() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public List<Phone> getPhoneDataSet() {
		return phones;
	}

	public void setPhoneDataSet(List<Phone> phoneDataSet) {
		this.phones = phoneDataSet;
	}

	@Override
	public String toString() {
		return "User: id = " + id
				+ ", name = " + name
				+ ", age = " + age;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		User that = (User) o;
		return id == that.id &&
				name.equals(that.name) &&
				age == that.age;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, age);
	}

}
