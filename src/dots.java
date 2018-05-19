import java.util.*;
public class dots {
	private List<dot> listdot = new LinkedList<dot>();
	public List<dot> getlistdot() {
		return listdot;
	}
	public void setlistdot(List<dot> listdot){
		this.listdot=listdot;
	}
	public String toString() {
		return listdot.toString() + "";
	}
}