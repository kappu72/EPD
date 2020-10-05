package it.toscana.rete.lamma.prototype.model.tables;

public class WaveresCompTable {
	
	protected Parameter spd;
	protected OneDimTable<WavePeriods> waveComp;
	
	public WaveresCompTable(Parameter spd) {
		super();
		this.spd=spd;
		waveComp = new OneDimTable<WavePeriods>(spd.getMin(), spd.getMax(), spd.getDelta(), spd.getSize(), WavePeriods.class);	
	}
	public void setWaveComp(WavePeriods[] waveCompValues) {
		waveComp.setValues(waveCompValues);
	}
	public double getCawValue(double speed, double period, double angle) {
		return waveComp.getValue(speed).getCawValue(sanitizePeriod(period), angle);
	}
	public double getTmValue(double speed, double period) {
		return waveComp.getValue(speed).getOptTValue(sanitizePeriod(period));
	}
	public Parameter getTpInfo() {
	 return waveComp.getFirst().getMainT();	
	}
	public Parameter getCawInfo() {
		 return waveComp.getFirst().getCaw();	
	}
	public Parameter getSpdInfo() {
		return spd;
	}
	public WavePeriods addWavePeriods(WavePeriods value, int idx) {
		return waveComp.setValue(value, idx);
	}
	public Boolean isValidTp(double period) {
		Parameter tp = getTpInfo();
		return period > 0 && period <= tp.getMax() + 5;
	}
	public double sanitizePeriod(double period) {
		if(period < getTpInfo().getMin()) {
			return getTpInfo().getMin();
		}else if (period > getTpInfo().getMax()) {
			return getTpInfo().getMax();
		}
		return period;

	}

}
