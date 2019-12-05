package it.toscana.rete.lamma.prototype.model.tables;

public class FuelRateTable {
	protected Parameter spd;
	protected OneDimTable<OneDimDouble> fuelRateValues;
	protected Parameter rAdd;
	protected OneDimDouble rHullValues;
	protected String id;
	
	
	public FuelRateTable(Parameter spd, Parameter fuelRate, String id){
		this(spd, fuelRate);
		this.id = id;
	}

	public FuelRateTable(Parameter spd, Parameter fuelRate) {
		super();
		this.spd = spd;
		this.rAdd= fuelRate;
		fuelRateValues = new OneDimTable<OneDimDouble>(spd.getMin() , spd.getMax(), spd.getDelta(), spd.getSize(), OneDimDouble.class);
	}
	public FuelRateTable(Parameter spd, Parameter fuelRate, Parameter rHull ) {
		super();
		this.spd = spd;
		this.rAdd= fuelRate;
		fuelRateValues = new OneDimTable<OneDimDouble>( spd.getMin() ,  spd.getMax(), spd.getDelta(), spd.getSize(), OneDimDouble.class);
		rHullValues = new OneDimDouble( spd.getMin() ,  spd.getMax(),  spd.getDelta());
	}
	
	public void addfuelRateValue(OneDimTable<Double> fuelRateValue, int idx) {
		fuelRateValues.setValue((OneDimDouble) fuelRateValue, idx);
	}
	public void addRhullTValue( double rHullValue, int idx){
		rHullValues.setValue(rHullValue, idx);
	}
	public double getFuelRate(double speed, double rAdd) {
		
		return fuelRateValues.getValue(speed).getWeightedValue(rAdd, true);	
		
	}
	public double getRhull(double speed) {
		return rHullValues.getValue(speed);
	}
	public Parameter getSpd() {
		return spd;
	}
	public Parameter getrAdd() {
		return rAdd;
	}
	public String getId() {
		return id;		
	}
	
}
