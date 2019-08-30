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
		public WavePeriods(Parameter mainT, Parameter pdsComp ) {
			super();
			this.mainT = mainT;
			this.cawComp= pdsComp;
			cawValues = new OneDimTable<OneDimDouble>((int) (mainT.getMin() * 10), (int) (mainT.getMax() * 10), (int) (mainT.getDelta() * 10), mainT.getSize(), OneDimDouble.class);
			optionalTValues = new OneDimDouble((int) (mainT.getMin() * 10), (int) (mainT.getMax() * 10), (int) (mainT.getDelta() * 10), mainT.getSize(), Double.class);
		}

		public void addCawValues( OneDimDouble[] pdsValues){
			cawValues.setValues(pdsValues);
		}
		public void addCawValue(OneDimTable<Double> pdsValue, int idx) {
			cawValues.setValue((OneDimDouble) pdsValue, idx);
		}
		public void addOptionalTValues( Double[] optTValues){
			optionalTValues.setValues(optTValues);
		}
		public void addOptionalTValue( Double optTValue, int idx){
			optionalTValues.setValue(optTValue, idx);
		}
		/**
		 * 
		 * @param period
		 * @param angle Domain -180 180
		 * @return
		 */
		public double getCawValue(float period, float angle) {
			return cawValues.getValue(period * 10).getWeightedValue(Math.abs(angle));
		}
		
		/**
		 * 
		 * @param period
		 * @return
		 */
		public double getOptTValue(float period) {
			return (double) optionalTValues.getValue(period* 10);
		}

		public Parameter getMainT() {
			return mainT;
		}

		public Parameter getCaw() {
			return cawComp;
		}

		
		
}
