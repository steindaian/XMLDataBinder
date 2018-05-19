import java.util.*;
public class Customer {
	private String Dob;
	private AddressType Address;
	public String getDob() {
		return Dob;
	}
	public void setDob(String Dob){
		this.Dob=Dob;
	}
	public AddressType getAddress() {
		return Address;
	}
	public void setAddress(AddressType Address){
		this.Address=Address;
	}
	public String toString() {
		return Dob.toString()+ " " + Address.toString()+ " " + "";
	}
}