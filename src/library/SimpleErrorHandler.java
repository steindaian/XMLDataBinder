package library;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SimpleErrorHandler implements ErrorHandler {
	public static boolean ok = true;
	@Override
	public void error(SAXParseException arg0) throws SAXException {
		// TODO Auto-generated method stub
		ok = false;
		 System.out.println(arg0.getMessage());
	}

	@Override
	public void fatalError(SAXParseException arg0) throws SAXException {
		// TODO Auto-generated method stub
		ok = false;
		 System.out.println(arg0.getMessage());
	}

	@Override
	public void warning(SAXParseException arg0) throws SAXException {
		// TODO Auto-generated method stub
		System.out.println(arg0.getMessage());
	}
}