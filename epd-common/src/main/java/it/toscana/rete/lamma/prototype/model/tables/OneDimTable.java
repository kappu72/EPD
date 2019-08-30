package it.toscana.rete.lamma.prototype.model.tables;

import java.lang.reflect.Array;

/**
 * The value in the array is indexed by MAX - MIN / STEP
 * 
 * @author kappu
 *
 */
public class OneDimTable<E> {
	protected int minIdx;
	protected int maxIdx;
	private int delta;
	private  E[] values;
	
	
	
	public OneDimTable() {
		super();
	}
	
	public OneDimTable(int minIdx, int maxIdx, int delta) {
		super();
		this.minIdx = minIdx;
		this.maxIdx = maxIdx;
		this.delta	= delta;
	}
	public OneDimTable(int minIdx, int maxIdx, int delta, int size, Class<E> c) {
		super();
		this.minIdx = minIdx;
		this.maxIdx = maxIdx;
		this.delta	= delta;
		@SuppressWarnings("unchecked")
		final E[] values = (E[]) Array.newInstance(c, size);
        this.values = values;
	}
	public OneDimTable(int minIdx, int maxIdx, int delta, E[] values) {
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
	 * Return the parm value for an angle -180 _ 180
	 * @param idx
	 * @return
	 */
	public E getValue(float idx) throws IllegalArgumentException {
		
		normalizeIdex(idx);
		int index = (int) Math.round(normalizeIdex(idx) / delta);
		return values[index];
		
	}
	/**
	 * Return the weighted average of param value don't use if T isn't a double
	 * @param idx
	 * @return
	 */
	public double getWeightedValue(float idx) throws IllegalArgumentException{
		float nIdex = normalizeIdex(idx);
		double rest = nIdex%delta;
		
		if(rest == 0.0) {
			return (double) values[(int) nIdex/delta];
		}
		double up = (double) values[(int) Math.ceil(nIdex/delta)];
		double down = (double) values[(int) Math.floor(nIdex/delta)];
		double upD = delta-rest;
		double downD = rest;
		return  (up * upD + down * downD) / (upD + downD);
	}
	private float normalizeIdex(float idx) throws IllegalArgumentException{
		if(idx > maxIdx || idx < minIdx) {
			throw new IllegalArgumentException("Value outside range " + maxIdx + " " + minIdx);
		}
		return idx - minIdx;
	}
	public E getFirst() {
		return values[0];
	}
}
