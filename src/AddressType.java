import java.util.*;
public class AddressType {
	private String Line1;
	private String Line2;
	public String getLine1() {
		return Line1;
	}
	public void setLine1(String Line1){
		this.Line1=Line1;
	}
	public String getLine2() {
		return Line2;
	}
	public void setLine2(String Line2){
		this.Line2=Line2;
	}
	public String toString() {
		return Line1.toString()+ " " + Line2.toString()+ " " + "";
	}
}