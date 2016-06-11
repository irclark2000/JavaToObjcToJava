package com.gmail.irclark2000.objc.java.translate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dad
 * Maintains a list of the declarations obtained while parsing
 * 
 */
public class JavaClassDescription {
	String classType;
	String visibility;
	private ArrayList<JavaClassDescription> classes;
	private String className;
	private int indentLevel;
	
	/**
	 * @return classes
	 */
	public ArrayList<JavaClassDescription> getClasses() {
		return classes;
	}

	/**
	 * @param headers
	 */
	public void setClasses(ArrayList<JavaClassDescription> classes) {
		this.classes = classes;
	}


	/**
	 * @return class name based on file name
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param classFileName
	 */
	public void setClassName(String classFileName) {
		this.className = classFileName;
	}


	/**
	 * @param name class name
	 * @param type class type
	 */
	public JavaClassDescription(String name, String type) {
		classes = new ArrayList<JavaClassDescription>();
		indentLevel = 0;
		className = name;
		this.classType = type;
	}

	/**
	 * @return the indentLevel
	 */
	public int getIndentLevel() {
		return indentLevel;
	}

	/**
	 * @param indentLevel the indentLevel to set
	 */
	public void setIndentLevel(int indentLevel) {
		this.indentLevel = indentLevel;
	}


}
