package utils;

import abstractsyntaxtree.ExpressionTree;
import net.objecthunter.exp4j.Expression;

public class TestsetHandler {

	private double[][] testset;
	private double[] output;
	private double error; // Squared Root Mean Error
	public double getError() {
		return error;
	}

	public void setError(double error) {
		this.error = error;
	}

	private Expression expression;
	private String[] variables;
	
	public TestsetHandler(double[][] testset, double[] output, 
			ExpressionTree tree, String[] variables) {
	
		this.testset = testset;
		this.output = output;
		this.expression = tree.getExpression();
		this.variables = variables;
		
		this.calculateError();
	}
	
	private void calculateError() {
		
		for (int i = 0; i < this.testset.length; i++) {
			
			setVariablesExpression(i, this.expression);	

			try {

				double expressionEvaluation = this.expression.evaluate();
				error += Math.pow(expressionEvaluation - output[i], 2);

			} catch (ArithmeticException ae) {
				// assume error is really big
				error += Integer.MAX_VALUE;
			}
		}

		error = Math.sqrt(error / testset.length);
	}
	
	private void setVariablesExpression(int row, Expression express) {

		for (int col = 0; col < testset[row].length; col++) {
			express.setVariable(variables[col], testset[row][col]);
		}
	}
}
