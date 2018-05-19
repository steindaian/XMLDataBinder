package library;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.*;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.*;

import classGenerator.ClassGenerator;

import org.w3c.dom.*;

public class MyXMLDataBinder {
	private static MyXMLDataBinder m = null;
	private MyXMLDataBinder() {
		
	}
	public static MyXMLDataBinder instance() {
		if(m == null) m = new MyXMLDataBinder();
		return m;
	}
	private Document getRootDocument(String file,String schema) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);

		SchemaFactory schemaFactory = 
		    SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

		factory.setSchema(schemaFactory.newSchema(
		    new Source[] {new StreamSource(schema)}));

		DocumentBuilder builder = factory.newDocumentBuilder();

		builder.setErrorHandler(new SimpleErrorHandler());

		return builder.parse(new InputSource(file));
	}
	private void addFieldFromChild(Object obj,Field field,Element e) {
		try {
			if(field.getType().getName().contains("List")) {

					ParameterizedType listType = (ParameterizedType) field.getGenericType();
			        Class<?> actualType = (Class<?>) listType.getActualTypeArguments()[0];
			        //System.out.println(actualType.getSimpleName());
					List list = (List)obj.getClass().getMethod("get"+field.getName(), new Class[] {}).invoke(obj, (Object[])null);
					NodeList children = e.getElementsByTagName(actualType.getSimpleName());
					for(int i=0;i<children.getLength();i++) {
						Object o = createObject((Element) children.item(i),actualType);
						list.add(o);
					}
				
			}
			else {
				Element child = (Element) e.getElementsByTagName(field.getName()).item(0);
				Object childObj = createObject(child,field.getType());
				obj.getClass().getMethod("set"+field.getName(), new Class[] {field.getType()}).invoke(obj, new Object[] {childObj});
				//TO DO
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException es) {
			// TODO Auto-generated catch block
			es.printStackTrace();
		}
	}
	private Object createObject(Element e,Class objClass) {
		try {
			if(e == null) return null;
			Object obj = objClass.newInstance();
			Field[] fields = objClass.getDeclaredFields();
			for(int i=0;i<fields.length;i++) {
				String value = e.getAttribute(fields[i].getName());
				//Object v = fields[i].getType().cast(value);
				if(value.equals("")) { //not an attribute
					Element child = (Element) e.getElementsByTagName(fields[i].getName()).item(0);
					if(child != null  && fields[i].getType().getName().startsWith("java.lang")) { //it is a tag value
						Constructor ct = fields[i].getType().getConstructor(new Class[] { String.class});
						Object a = ct.newInstance(new Object[] {child.getTextContent()});
						//System.out.println(child.getNodeValue());
						//System.out.println(a);
						objClass.getMethod("set"+fields[i].getName(), new Class[] { fields[i].getType()}).invoke(obj, new Object[] { a});
					
					}
					else { //it is compund
						addFieldFromChild(obj,fields[i],e);
					}
				}
				else { //it has an attribute
						Constructor ct = fields[i].getType().getConstructor(new Class[] { String.class});
						Object a = ct.newInstance(new Object[] {value});
						//System.out.println(a);
						objClass.getMethod("set"+fields[i].getName(), new Class[] { fields[i].getType()}).invoke(obj, new Object[] { a});
				}
			}
			return obj;
			
		}  catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}  catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
		
	}
	public Object CreateObjectFromXML(String file,String schema) {
		try {
			Element root = getRootDocument(file,schema).getDocumentElement();
			if(SimpleErrorHandler.ok == false) {
				System.out.println("Error at validation. Exit");
				return null;
			}
			Object result = createObject(root,Class.forName(root.getTagName()));
			return result;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			System.out.println("Error at parsing and validating. Message: "+e.getMessage());
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	private void addElements(Document document,Element e,Object o) {
		try {
			Field[] fields = o.getClass().getDeclaredFields();
			for(int i=0;i<fields.length;i++) {
				if(fields[i].getType().getName().startsWith("java.lang")) {
					String value = new String( o.getClass().getMethod("get"+fields[i].getName(), new Class[] { } ).invoke(o, new Object[] {}).toString());
					e.setAttribute(fields[i].getName(), value);
				}
				else {
					if(fields[i].getType().getName().contains("List")) {
						List list = (List) o.getClass().getMethod("get"+fields[i].getName(), new Class[] { } ).invoke(o, new Object[] {});
						for(Object el:list) {
							Element child = document.createElement(el.getClass().getSimpleName());
							addElements(document,child,el);
							e.appendChild(child);
						}
					}
					else {
						Element child = document.createElement(fields[i].getType().getSimpleName());
						Object value = o.getClass().getMethod("get"+fields[i].getName(), new Class[] { } ).invoke(o, new Object[] {});
						addElements(document,child,value);
						e.appendChild(child);
					}
				}
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	private Document createDocumentFromObject(Object o,String schema) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(true);
			factory.setNamespaceAware(true);
	
			SchemaFactory schemaFactory = 
			    SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
	
			
				factory.setSchema(schemaFactory.newSchema(
				    new Source[] {new StreamSource(schema)}));
		
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new SimpleErrorHandler());
	
			Document document = builder.newDocument();
			
			Element root = document.createElement(o.getClass().getSimpleName());
			root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			root.setAttribute("xsi:noNamespaceSchemaLocation", "dots.xsd");
			document.appendChild(root);
			addElements(document,root,o);
			return document;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public void CreateXMLFromObject(Object o, String file,String schema) {
		try {
			Document doc = createDocumentFromObject(o,schema);
			try {
				   TransformerFactory tranFact = TransformerFactory.newInstance( );
				   Transformer tran = tranFact.newTransformer( );
				   DOMSource DSource = new DOMSource(doc);
				   StreamResult SResult = new StreamResult(new FileOutputStream(file));
				   tran.transform(DSource, SResult);
				
			} catch (TransformerConfigurationException tce) {
				tce.printStackTrace();
			} catch (TransformerException te) {
				te.printStackTrace();
			} 

			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
