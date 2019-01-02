package parallelVersionWithFJ;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveAction;

import abstractsyntaxtree.ExpressionTree;
import net.objecthunter.exp4j.Expression;

public class MeasureExpression extends RecursiveAction {

	private static final int LIMIT = 2;
	private static final double THRESHOLD = 0;
	
	private ExpressionTree tree;
	private String[] variables;
	private double[][] data;
	private double[] classes;
	private double[] dataOutput;
	private int beg;
	private int end;
	
	public MeasureExpression(ExpressionTree tree, double[][] data, 
			double[] dataOutput, double[] classes, String[] variables, int beg, int end) {
		
		this.tree = tree;
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
				
				tree.setFitness(1.0*correctlyClassified / data.length);
			}
		} else {

			int middle = (end+beg) / 2;
			
			MeasureExpression action1 = new MeasureExpression (tree, data, dataOutput, classes, variables, beg, middle);
			MeasureExpression action2 = new MeasureExpression (tree, data, dataOutput, classes, variables, middle, end);
			
			action1.fork();
			action2.compute();
			
			try {
				action1.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void setVariablesExpression(int row, Expression express) {

		for (int col = 0; col < data[row].length; col++) {
			express.setVariable(variables[col], data[row][col]);
		}
	}

}