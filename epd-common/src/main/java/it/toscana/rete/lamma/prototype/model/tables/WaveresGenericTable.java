package it.toscana.rete.lamma.prototype.model.tables;

public class WaveresGenericTable {

	protected Parameter spd;
	protected OneDimTable<WavePeriods> waveGeneric;
	
	public WaveresGenericTable(Parameter spd) {
		super();
		this.spd=spd;
		waveGeneric = new OneDimTable<WavePeriods>((int) spd.getMin(), (int) spd.getMax(), (int) spd.getDelta(), spd.getSize(), WavePeriods.class);	
	}
	public void setWaveGeneric(WavePeriods[] waveGenericValues) {
		waveGeneric.setValues(waveGenericValues);
	}
	public double getCawValue(float speed, float period, float angle) {
		return waveGeneric.getValue(speed).getCawValue(period, angle);
	}
	public double getTpValue(float speed, float period) {
		return waveGeneric.getValue(speed).getOptTValue(period);
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
}
