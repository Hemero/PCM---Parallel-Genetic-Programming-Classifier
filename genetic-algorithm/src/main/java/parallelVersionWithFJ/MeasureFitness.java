package parallelVersionWithFJ;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveAction;

import abstractsyntaxtree.ExpressionTree;

public class MeasureFitness extends RecursiveAction {

	private static final int LIMIT = 2;
	
	private ExpressionTree[] population;
	private String[] variables;
	private double[][] data;
	private double[] classes;
	private double[] dataOutput;
	private int beg;
	private int end;
	
	public MeasureFitness(ExpressionTree[] population, double[][] data, 
			double[] dataOutput, double[] classes, String[] variables, int beg, int end) {
		
		this.population = population;
		this.variables = variables;
		this.data = data;
		this.dataOutput = dataOutput;
		this.classes = classes;
		this.beg = beg;
		this.end = end;
	}
	
	@Override
	protected void compute() {
		
		if (end-beg < LIMIT) {

			for (int i = beg; i < end; i++) {
				MeasureExpression measureExpression =
					new MeasureExpression(population[i], data, dataOutput, classes, variables);
				measureExpression.compute();
				
			}
		} else {

			int middle = (end+beg) / 2;
		
			MeasureFitness action1 = 
					new MeasureFitness (population, data, dataOutput, classes, variables, beg, middle);
			MeasureFitness action2 = 
					new MeasureFitness (population, data, dataOutput, classes, variables, middle, end);
			
			action1.fork();
			action2.compute();
			action1.join();
		}
	}
}