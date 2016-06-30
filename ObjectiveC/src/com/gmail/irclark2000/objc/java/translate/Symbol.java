package com.gmail.irclark2000.objc.java.translate;

import java.util.ArrayList;

/**
 * Handle new symbols discovered during parsing.
 * @author irclark
 *
 */
public class Symbol {
	String name;
	SymbolClass symClass;
	String visibility;
	ArrayList<String> parameters;  // not for mere variables;
	
	/**
	 * @param name
	 * @param symClass
	 */
	public Symbol(String name, SymbolClass symClass) {
		this.name = name;
		this.symClass = symClass;
		parameters = new ArrayList<String>();
	}

	/**
	 * Names for various symbol types
	 * @author irclark
	 *
	 */
	public enum SymbolClass {
		FUNCTION, VARIABLE, CONSTRUCTOR, CLASS, INTERFACE;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the symClass
	 */
	public SymbolClass getSymClass() {
		return symClass;
	}

	/**
	 * @param symClass the symClass to set
	 */
	public void setSymClass(SymbolClass symClass) {
		this.symClass = symClass;
	}

	/**
	 * @return the visibility
	 */
	public String getVisibility() {
		return visibility;
	}

	/**
	 * @param visibility the visibility to set
	 */
	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	/**
	 * @return the parameters
	 */
	public ArrayList<String> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(ArrayList<String> parameters) {
		this.parameters = parameters;
	}
}


