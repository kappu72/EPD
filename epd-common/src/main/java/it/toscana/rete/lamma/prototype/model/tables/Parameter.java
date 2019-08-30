package it.toscana.rete.lamma.prototype.model.tables;

public class Parameter {
	String Name;
	double min;
	double max;
	double delta;
	int size;
	boolean isRow = false;

	public Parameter(String name, double min, double max, double delta, int size, boolean isRow) {
		super();
		Name = name;
		this.min = min;
		this.max = max;
		this.delta = delta;
		this.size = size;
		this.isRow = isRow;
	}

	public String getName() {
		return Name;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	public double getDelta() {
		return delta;
	}

	public int getSize() {
		return size;
	}

	public boolean isRow() {
		return isRow;
	}

}
