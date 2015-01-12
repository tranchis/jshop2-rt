package com.gamalocus.jshop2rt;

/**
 * Default cost model.
 * 
 * @author j0rg3n
 */
public class DoubleCost implements Cost
{
	private double value;
	
	public DoubleCost(double value)
	{
		this.value = value;
	}

	public DoubleCost()
	{
		this(0);
	}

	public void add(Term t)
	{
		value += ((TermNumber)t).getNumber();
	}

	public void remove(Term t)
	{
		value -= ((TermNumber)t).getNumber();
	}

	public int compareTo(Cost o)
	{
		return Double.compare(value, ((DoubleCost)o).value);
	}

	@Override
	public Object clone()
	{
		return new DoubleCost(value);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final DoubleCost other = (DoubleCost) obj;
		if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value))
			return false;
		return true;
	}
	
	@Override
	public String toString()
	{
		return Double.toString(value);
	}
}
