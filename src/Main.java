import library.MyXMLDataBinder;

public class Main {
	public static void main(String[] args) {
		MyXMLDataBinder m = MyXMLDataBinder.instance();
		dots d = (dots) m.CreateObjectFromXML("dots.xml","dots.xsd");
		System.out.println(d);
		//m.CreateXMLFromObject(d, "dots_new.xml","dots.xsd");
	}
}
