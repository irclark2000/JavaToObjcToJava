package com.gmail.irclark2000.objc.java.translate;

import java.util.ArrayList;

/**
 * Class to handle method information
 * @author irclark
 *
 */

public class MethodSignature {
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
	 * @return the returnType
	 */
	public Type getReturnType() {
		return returnType;
	}

	/**
	 * @param returnType the returnType to set
	 */
	public void setReturnType(Type returnType) {
		this.returnType = returnType;
	}

	/**
	 * @return the parameters
	 */
	public ArrayList<Type> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(ArrayList<Type> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the visibility
	 */
	public boolean isVisibility() {
		return visibility;
	}

	/**
	 * @param visibility the visibility to set
	 */
	public void setVisibility(boolean visibility) {
		this.visibility = visibility;
	}

	/**
	 * @return the mType
	 */
	public MethodType getmType() {
		return mType;
	}

	/**
	 * @param mType the mType to set
	 */
	public void setmType(MethodType mType) {
		this.mType = mType;
	}

	/**
	 * @return the body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * @param body the body to set
	 */
	public void setBody(String body) {
		this.body = body;
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

	/**
	 * @param name
	 * @param returnType
	 */
	public MethodSignature(String name, String returnType) {
		this.name = name;
		this.returnType = new Type(returnType);
		parameters = new ArrayList<Type>();
		indentLevel = 1;
	}

	private String name;
	private Type returnType;
	private int indentLevel;
	private ArrayList<Type> parameters;
	private boolean visibility;
	private MethodType mType;
	private String body;
		
	/**
	 * Variable Type Info
	 * @author irclark
	 *
	 */
	public class Type {
		private String types;
		
		public Type (String types){
			this.types = types;
		}
	}
	
	enum MethodType {
		CONSTRUCTOR, METHOD;
	}

	public void addParameterType(String p) {
		Type t = new Type(p);
		parameters.add(t);
		
	}
}
