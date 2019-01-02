package parallelVersionWithFJ;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveAction;

import abstractsyntaxtree.ExpressionTree;
import net.objecthunter.exp4j.Expression;

public class MeasureFitness extends RecursiveAction {

	private static final int LIMIT = 2;
	private static final double THRESHOLD = 0;
	
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
				
				measureExpression(population[i]);
				
//				MeasureExpression measureExpression =
//					new MeasureExpression(population[i], data, dataOutput, classes, variables, 0, this.data.length);
//				measureExpression.compute();
			}
		} else {

			int middle = (end+beg) / 2;
			
			MeasureFitness action1 = 
					new MeasureFitness (population, data, dataOutput, classes, variables, beg, middle);
			MeasureFitness action2 = 
					new MeasureFitness (population, data, dataOutput, classes, variables, middle, end);
			
			action1.fork();
			action2.compute();
			
			try {
				action1.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
	}
	
	private double measureExpression(ExpressionTree tree) {

		Expression express = tree.getExpression();
		int correctlyClassified = 0;

		for (int row = 0; row < data.length; row++) {

			setVariablesExpression(row, express);	

			try {
				double expressionEvaluation = express.evaluate();
				
				if ((expressionEvaluation < THRESHOLD && dataOutput[row] == classes[0]) || 
					(expressionEvaluation >= THRESHOLD && dataOutput[row] == classes[1])) {

					correctlyClassified++;
				}
			} catch (ArithmeticException ae) {
				// Do nothing - counts as incorrectly classified
			}
		}
		
		double fitness = 1.0*correctlyClassified / data.length;
		
		tree.setFitness(fitness);
		
		return fitness;
	}
	
	private void setVariablesExpression(int row, Expression express) {

		for (int col = 0; col < data[row].length; col++) {
			express.setVariable(variables[col], data[row][col]);
		}
	}

}