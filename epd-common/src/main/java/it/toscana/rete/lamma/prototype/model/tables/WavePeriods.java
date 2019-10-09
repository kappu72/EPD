package it.toscana.rete.lamma.prototype.model.tables;

public class WavePeriods {
		protected Parameter mainT;
		protected Parameter cawComp;
		protected OneDimTable<OneDimDouble> cawValues;
		protected OneDimDouble optionalTValues;
		/**
		 * Period has 0.5 delta so * 10 before casting
		 * @param mainT
		 * @param optionalT
		 * @param pdsComp
		 */
		public WavePeriods(Parameter mainT, Parameter cawComp ) {
			super();
			this.mainT = mainT;
			this.cawComp= cawComp;
			cawValues = new OneDimTable<OneDimDouble>(mainT.getMin() * 10, mainT.getMax() * 10, mainT.getDelta() * 10, mainT.getSize(), OneDimDouble.class);
			optionalTValues = new OneDimDouble(mainT.getMin() * 10, mainT.getMax() * 10, mainT.getDelta() * 10, mainT.getSize(), Double.class);
		}

		public void addCawValues( OneDimDouble[] cawValues){
			this.cawValues.setValues(cawValues);
		}
		public void addCawValue(OneDimTable<Double> cawValue, int idx) {
			this.cawValues.setValue((OneDimDouble) cawValue, idx);
		}
		public void addOptionalTValues( Double[] optTValues){
			optionalTValues.setValues(optTValues);
		}
		public void addOptionalTValue( double optTValue, int idx){
			optionalTValues.setValue(optTValue, idx);
		}
		/**
		 * 
		 * @param period
		 * @param angle Domain -180 180
		 * @return
		 */
		public double getCawValue(double period, double angle) {
			return cawValues.getValue(period * 10).getWeightedValue(Math.abs(angle));
		}
		
		/**
		 * 
		 * @param period
		 * @return
		 */
		public double getOptTValue(double period) {
			return (double) optionalTValues.getValue(period* 10);
		}

		public Parameter getMainT() {
			return mainT;
		}

		public Parameter getCaw() {
			return cawComp;
		}

		
		
}
