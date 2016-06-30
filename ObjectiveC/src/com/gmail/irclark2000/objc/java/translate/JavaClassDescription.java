package com.gmail.irclark2000.objc.java.translate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.gmail.irclark2000.objc.java.translate.MethodSignature.Type;

/**
 * @author Dad
 * Maintains a list of the declarations obtained while parsing
 * 
 */
public class JavaClassDescription {
	String classType;
	String visibility;
	private ArrayList<JavaClassDescription> classes;
	private ArrayList<MethodSignature> methods;
	private String superType;
	private ArrayList<String> interfaceList;
	private String className;
	private int indentLevel;
	private SymbolTable mySymbols;
	
	/**
	 * @return the mySymbols
	 */
	public SymbolTable getMySymbols() {
		return mySymbols;
	}

	/**
	 * @param mySymbols the mySymbols to set
	 */
	public void setMySymbols(SymbolTable mySymbols) {
		this.mySymbols = mySymbols;
	}

	/**
	 * @return classes
	 */
	public ArrayList<JavaClassDescription> getClasses() {
		return classes;
	}

	/**
	 * Add an inner class to a class description
	 * @param cl
	 */
	public void addClasss(JavaClassDescription cl) {
		classes.add(cl);
	}
	/**
	 * Adding a method
	 * @param methodSignature add a method to class
	 */
	public void addMethod(MethodSignature methodSignature) {
		methods.add(methodSignature);
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
		this.methods = new ArrayList<MethodSignature>();
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
