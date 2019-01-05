package parallelVersionWithFJ;

import java.util.concurrent.RecursiveAction;

import abstractsyntaxtree.ExpressionTree;

public class MeasureFitness extends RecursiveAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -877576993390821093L;

	private static final int LIMIT = 2;
	
	private ExpressionTree[] population;
	private String[] variables;
	private double[][] data;
	private double[] dataOutput;
	
	private int beg;
	private int end;
	
	private int beginTrainingSet;
	private int endTrainingSet;
	
	public MeasureFitness(ExpressionTree[] population, double[][] data, 
			double[] dataOutput, String[] variables, int beg, int end,
			int beginTrainingSet, int endTrainingSet) {
		
		this.population = population;
		this.variables = variables;
		this.data = data;
		this.dataOutput = dataOutput;
		this.beg = beg;
		this.end = end;
		this.beginTrainingSet = beginTrainingSet;
		this.endTrainingSet = endTrainingSet;
	}
	
	@Override
	protected void compute() {
		
		if (end-beg < LIMIT) {

			for (int i = beg; i < end; i++) {
				MeasureExpression measureExpression =
					new MeasureExpression(population[i], data, dataOutput, 
										  variables, beginTrainingSet, endTrainingSet);
				measureExpression.compute();
				
			}
		} else {

			int middle = (end+beg) / 2;
		
			MeasureFitness action1 = 
					new MeasureFitness (population, data, dataOutput, variables, 
										beg, middle, beginTrainingSet, endTrainingSet);
			MeasureFitness action2 = 
					new MeasureFitness (population, data, dataOutput, variables, 
										middle, end, beginTrainingSet, endTrainingSet);
			
			action1.fork();
			action2.compute();
			action1.join();
		}
	}
}