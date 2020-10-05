package it.toscana.rete.lamma.prototype.model.tables;

public class WaveresGenericTable {

	protected Parameter spd;
	protected OneDimTable<WavePeriods> waveGeneric;
	
	public WaveresGenericTable(Parameter spd) {
		super();
		this.spd=spd;
		waveGeneric = new OneDimTable<WavePeriods>( spd.getMin(), spd.getMax(), spd.getDelta(), spd.getSize(), WavePeriods.class);	
	}
	public void setWaveGeneric(WavePeriods[] waveGenericValues) {
		waveGeneric.setValues(waveGenericValues);
	}
	public double getCawValue(double speed, double period, double angle) {
		return waveGeneric.getValue(speed).getCawValue(sanitizePeriod(period), angle);
	}
	public double getTpValue(double speed, double period) {
		return waveGeneric.getValue(speed).getOptTValue(sanitizePeriod(period));
	}
	public Parameter getTmInfo() {
	 return waveGeneric.getFirst().getMainT();	
	}
	public Parameter getCawInfo() {
		 return waveGeneric.getFirst().getCaw();	
	}
	public Parameter getSpdInfo() {
		return spd;
	}
	public WavePeriods addWavePeriods(WavePeriods value, int idx) {
		return waveGeneric.setValue(value, idx);
	}
	public Boolean isValidTm(double period) {
		Parameter tm = getTmInfo();
		return period >= tm.getMin() && period <= tm.getMax();
	}
	public double sanitizePeriod(double period) {
		if(period < getTmInfo().getMin()) {
			return getTmInfo().getMin();
		}else if (period > getTmInfo().getMax()) {
			return getTmInfo().getMax();
		}
		return period;

	}
}
