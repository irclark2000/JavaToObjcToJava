package com.gmail.irclark2000.objc.java.translate;

import java.util.ArrayList;

/**
 * @author irclark Class to hold symbol table information
 *
 */
public class SymbolTable {
	String scope;

	/**
	 * @return the scope
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * @param scope
	 *            the scope to set
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}

	/**
	 * @return the symbols
	 */
	public ArrayList<Symbol> getSymbols() {
		return symbols;
	}

	/**
	 * @param symbols
	 *            the symbols to set
	 */
	public void setSymbols(ArrayList<Symbol> symbols) {
		this.symbols = symbols;
	}

	/**
	 * @return the parent
	 */
	public SymbolTable getParent() {
		return parent;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(SymbolTable parent) {
		this.parent = parent;
	}

	ArrayList<Symbol> symbols;
	SymbolTable parent;

	/**
	 * Constructor
	 * 
	 * @param scope
	 */
	public SymbolTable(String scope) {
		this.scope = scope;
		symbols = new ArrayList<Symbol>();
	}

	/**
	 * Add symbol to symbol table
	 * @param symbol
	 */
	public void addSymbol(Symbol symbol) {
		symbols.add(symbol);

	}
}
