package it.toscana.rete.lamma.prototype.model.tables;

public class WaveresCompTable {
	
	protected Parameter spd;
	protected OneDimTable<WavePeriods> waveComp;
	
	public WaveresCompTable(Parameter spd) {
		super();
		this.spd=spd;
		waveComp = new OneDimTable<WavePeriods>((int) spd.getMin(), (int) spd.getMax(), (int) spd.getDelta(), spd.getSize(), WavePeriods.class);	
	}
	public void setWaveComp(WavePeriods[] waveCompValues) {
		waveComp.setValues(waveCompValues);
	}
	public double getCawValue(float speed, float period, float angle) {
		return waveComp.getValue(speed).getCawValue(period, angle);
	}
	public double getTmValue(float speed, float period) {
		return waveComp.getValue(speed).getOptTValue(period);
	}
	public Parameter getTmInfo() {
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
	

}
