package it.toscana.rete.lamma.prototype.model.tables;

import java.lang.reflect.Array;

/**
 * The value in the array is indexed by MAX - MIN / STEP
 * @author kappu
 *
 */
public class OneDimTable<E> {
	protected double minIdx;
	protected double maxIdx;
	private double delta;
	private  E[] values;
	

	public OneDimTable() {
		super();
	}
	
	public OneDimTable(double minIdx, double maxIdx, double delta) {
		super();
		this.minIdx = minIdx;
		this.maxIdx = maxIdx;
		this.delta	= delta;
	}
	public OneDimTable(double minIdx, double maxIdx, double delta, int size, Class<E> c) {
		super();
		this.minIdx = minIdx;
		this.maxIdx = maxIdx;
		this.delta	= delta;
		@SuppressWarnings("unchecked")
		final E[] values = (E[]) Array.newInstance(c, size);
        this.values = values;
	}
	public OneDimTable(double minIdx, double maxIdx, double delta, E[] values) {
		super();
		this.values = values;
		this.minIdx = minIdx;
		this.maxIdx = maxIdx;
		this.delta	= delta;
	}
	public void setValues(E[] values) {
		this.values = values;
	}
	public E setValue(E value, int idx) {
		values[idx] = value;
		return value;
	}
	/**
	 * Return the parm value
	 * @param idx
	 * @return
	 */
	public E getValue(double idx) throws IllegalArgumentException {
		return getValue(idx, false);
	}
	/**
	 * Return the parm value
	 * @param idx
	 * @return
	 */
	public E getValue(double idx, boolean force) throws IllegalArgumentException {
		int index = Math.toIntExact(Math.round(normalizeIdex(idx, force) / delta));
		return values[index];
	}


	/**
	 * Return the weighted average of param value don't use if E isn't a double
	 * @param idx
	 * @return
	 */
	public double getWeightedValue(double idx) throws IllegalArgumentException{
		return  getWeightedValue(idx, false);
	}
	/**
	 * Return the weighted average of param value don't use if E isn't a double
	 * @param idx
	 * @param boolean force bound to min max value
	 * @return
	 */
	public double getWeightedValue(double idx, boolean force) throws IllegalArgumentException{
		double nIdx = normalizeIdex(idx, force);
		double rest = nIdx % delta;
		
		if(rest == 0.0) {
			return (Double) getValue(idx, force);
		}
		double up = (Double) values[(int) (Math.ceil(nIdx/delta))];
		double down = (Double) values[(int) Math.floor(nIdx/delta)];
		double upD = delta - rest;
		double downD = rest;
		return  (up * downD + down * upD) / (upD + downD);
	}



	private double normalizeIdex(double idx, boolean force) throws IllegalArgumentException{
		if(!force && (idx > maxIdx || idx < minIdx )) {
			throw new IllegalArgumentException("Value outside range " + maxIdx + " " + minIdx);
		}
		return force ? Math.max(minIdx, Math.min(idx, maxIdx)) - minIdx : idx - minIdx;
	}
	public E getFirst() {
		return values[0];
	}
}
