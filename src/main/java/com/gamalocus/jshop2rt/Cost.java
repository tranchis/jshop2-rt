package com.gamalocus.jshop2rt;

import java.io.Serializable;

public interface Cost extends Comparable<Cost>, Serializable
{
	/**
	 * Add the amount described by the given term to the cost.
	 * 
	 * @param t
	 */
	void add(Term t);

	/**
	 * Remove the amount described by the given term from the cost.
	 * 
	 * @param t
	 */
	void remove(Term t);
	
	/**
	 * @return Deep copy of this instance.
	 */
	Object clone();
}
