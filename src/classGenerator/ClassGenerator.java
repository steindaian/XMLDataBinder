package classGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashMap;

public class ClassGenerator {
	private String name;
	private StringBuilder importList,classDefinition, attributeDeclaration, methodDeclaration, toStringBuilder;
	private static HashMap<String,String> types = new HashMap<String,String>();
	public ClassGenerator() {
		this.classDefinition = new StringBuilder();
		this.attributeDeclaration = new StringBuilder();
		this.methodDeclaration = new StringBuilder();
		this.importList = new StringBuilder();
		this.toStringBuilder = new StringBuilder();
		toStringBuilder.append("\tpublic String toString() {\n\t\treturn ");
		types.put("xs:integer", "Integer");
		types.put("xs:string","String");
		types.put("xs:date","String");
		types.put("xs:float", "Float");
		types.put("xs:double", "Double");
		types.put("xs:boolean","Boolean");
		this.importList.append("import java.util.*;\n");
	}
	public static void addType(String type,String mappedType) {
		if(types.get(type)==null)
			types.put(type, mappedType);
	}
	public void addClassName(String name) {
		this.name = name;
		classDefinition.append("public class "+name+" ");
	}
	public void addSequence(String name,String type,int nr) {
		
		String listType = "LinkedList<"+type+">";
		String listName = "list"+name;
		if(nr == 0) 
			attributeDeclaration.append("\tprivate List<"+type+"> "+listName+" = new "+listType+"();\n");
		else
			attributeDeclaration.append("\tprivate List<"+type+"> "+listName+" = new "+listType+"("+nr+");\n"); 
		this.toStringBuilder.append(listName+".toString() + ");
		this.addGetter(listName, "List<"+type+">");
		this.addSetter(listName, "List<"+type+">");
	}
	public void endClass() {
		StringBuilder result = new StringBuilder();
		this.toStringBuilder.append("\"\";\n\t}\n");
		result.append(this.importList+this.classDefinition.toString()+"{\n"+this.attributeDeclaration.toString()+this.methodDeclaration.toString()+this.toStringBuilder+"}");
		System.out.println(result.toString());
		File f = new File("src/"+name+".java");
		try {
			OutputStreamWriter o = new OutputStreamWriter(new FileOutputStream(f));
			o.write(result.toString());
			o.flush();
			o.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public void addAttribute(String name,String type,String fixed,String def) {
		if(fixed!=null) {
			this.attributeDeclaration.append("\tprivate final "+types.get(type)+" "+name+" = "+fixed+";\n");
			this.addGetter(name, types.get(type));
		}
		else if(def!=null) {
			this.attributeDeclaration.append("\tprivate "+types.get(type)+" "+name+" = "+def+";\n");
			this.addGetter(name, types.get(type));
			this.addSetter(name, types.get(type));
		}
		else {
			this.attributeDeclaration.append("\tprivate "+types.get(type)+" "+name+";\n");
			this.addGetter(name, types.get(type));
			this.addSetter(name, types.get(type));
		}
		this.toStringBuilder.append(name+".toString()+ \" \" + ");
		
	}
	private void addGetter(String name,String type) {
		this.methodDeclaration.append("\tpublic "+type+" get"+name+"() {\n\t\treturn "+name+";\n\t}\n");
		
	}
	private void addSetter(String name,String type) {
		this.methodDeclaration.append("\tpublic void set"+name+"("+type+" "+name+"){\n\t\tthis."+name+"="+name+";\n\t}\n");
		
	}
}
