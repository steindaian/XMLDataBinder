package library;

import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import classGenerator.ClassGenerator;

public class ClassCompiler {
	private static ClassCompiler c;
	private ClassCompiler() {
		
	}
	public static ClassCompiler instance() {
		if(c == null) c = new ClassCompiler();
		return c;
	}
	
	private void do_attribute(Node child,ClassGenerator c) {

		if(child.getNodeName().contains("complexType")) {
			for(int i = 0;i<child.getChildNodes().getLength();i++)
				if(child.getChildNodes().item(i).getNodeName().contains("xs")) {
					do_attribute(child.getChildNodes().item(i),c);
				}
			//c.endClass();
		}
		else if(child.getNodeName().contains("element")) {
			if(child.getAttributes().getNamedItem("type")==null) {
				ClassGenerator.addType(child.getAttributes().getNamedItem("name").getNodeValue(), child.getAttributes().getNamedItem("name").getNodeValue());
				String name = child.getAttributes().getNamedItem("name").getNodeValue();
				String type = child.getAttributes().getNamedItem("name").getNodeValue();
				if(child.getAttributes().getNamedItem("maxOccurs")!=null) {
					int nr;
					if(child.getAttributes().getNamedItem("maxOccurs").getNodeValue().equals("unbounded")) {
						nr = 0;
					}
					else {
						nr = Integer.parseInt(child.getAttributes().getNamedItem("maxOccurs").getNodeValue());
					}
					c.addSequence(name, type, nr);
				}
				else if(child.getAttributes().getNamedItem("minOccurs")!=null){
					int times = Integer.parseInt(child.getAttributes().getNamedItem("minOccurs").getNodeValue());
					for(int i=0;i<times;i++)
						c.addAttribute(name+""+i, type,null,null);
				}
				else {
					c.addAttribute(name, type,null,null);
				}				
				do_node(child);
			}
			else {
				String name = child.getAttributes().getNamedItem("name").getNodeValue();
				String type = child.getAttributes().getNamedItem("type").getNodeValue();
				if(child.getAttributes().getNamedItem("maxOccurs")!=null) {
					int nr;
					if(child.getAttributes().getNamedItem("maxOccurs").getNodeValue().equals("unbounded")) {
						nr = 0;
					}
					else {
						nr = Integer.parseInt(child.getAttributes().getNamedItem("maxOccurs").getNodeValue());
					}
					c.addSequence(name, type, nr);
				}
				if(child.getAttributes().getNamedItem("minOccurs")!=null){
					int times = Integer.parseInt(child.getAttributes().getNamedItem("minOccurs").getNodeValue());
					for(int i=0;i<times;i++)
						c.addAttribute(name+""+i, type,null,null);
				}
				else {
					c.addAttribute(name, type,null,null);
				}
			}
		}
		else if( child.getNodeName().contains("attribute")) {
			String name = child.getAttributes().getNamedItem("name").getNodeValue();
			String type = child.getAttributes().getNamedItem("type").getNodeValue();
			c.addAttribute(name, type,null,null);
		}
		else if(child.getNodeName().contains("sequence")) {
			for(int i=0;i<child.getChildNodes().getLength();i++) 
				if(child.getChildNodes().item(i).getNodeName().contains("xs")) {
					do_attribute(child.getChildNodes().item(i),c);
				}
		}
		else {
			//c.endClass();
			
		}
	}
	private void do_element(Node n) {
		ClassGenerator c = new ClassGenerator();
		String className = n.getAttributes().getNamedItem("name").getNodeValue();
		c.addClassName(className);
		if(n.getAttributes().getNamedItem("type")!=null) {
			c.addAttribute(className, n.getAttributes().getNamedItem("type").getNodeValue(), null, null);
			c.endClass();
			return;
		}
		for(int i=0;i<n.getChildNodes().getLength();i++) {
			if(n.getChildNodes().item(i).getNodeName().contains("xs")) {
				//System.out.println("HERE "+n.getChildNodes().item(i).getNodeName());
			
				do_attribute(n.getChildNodes().item(i),c);
			}
		}
		c.endClass();
		
	}
	private void do_node(Node n) {
		if(n.getNodeName().contains("element")) {
			do_element(n);
		}
		else if(n.getNodeName().contains("complexType")){
			ClassGenerator.addType(n.getAttributes().getNamedItem("name").getNodeValue(), n.getAttributes().getNamedItem("name").getNodeValue());
			do_element(n);
		}
	}
	public void GenerateClassFromSchema(String schema) {
		try {
	        // Setup classes to parse XSD file for complex types
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        //dbf.setValidating(true);
	        dbf.setNamespaceAware(true);
	        //dbf.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", 
	        //      "http://www.w3.org/2001/XMLSchema");
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.parse(new FileInputStream(schema));
	        doc.getDocumentElement().normalize();
	        NodeList elements = doc.getDocumentElement().getChildNodes();
	        //System.out.println(elements.getLength());
	        try {
		        for(int i=0;i<elements.getLength();i++) {
		        	do_node(elements.item(i));
		        }
	        }
	        catch(Exception e) {
	        	System.out.println("Schema not well defined");
	        	e.printStackTrace();
	        }

	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	}
	
	
	public static void main(String[] args) {
		ClassCompiler c= ClassCompiler.instance();
		c.GenerateClassFromSchema("dots.xsd");
		c.GenerateClassFromSchema("customer.xsd");
	}
}
