package parallelVersionWithFJ;

import java.util.concurrent.RecursiveAction;

import abstractsyntaxtree.ExpressionTree;
import net.objecthunter.exp4j.Expression;

/**
 * @author Faculdade de Ciencias da Universidade de Lisboa - FC47806 FC49034
 */
public class MeasureExpression extends RecursiveAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4124808217038711638L;
	
	// Atributos
	private ExpressionTree tree;
	private String[] variables;
	private double[][] data;
	private double[] dataOutput;
	private int beginTrainingSet;
	private int endTrainingSet;
	
	public MeasureExpression(ExpressionTree tree, double[][] data, 
			double[] dataOutput, String[] variables, int beginTrainingSet, int endTrainingSet) {
		
		this.tree = tree;
		this.variables = variables;
		this.data = data;
		this.dataOutput = dataOutput;
		
		this.beginTrainingSet = beginTrainingSet;
		this.endTrainingSet = endTrainingSet;
	}
	
	@Override
	protected void compute() {

		Expression express = tree.getExpression();
		double fitness = 0;
		
		for (int row = this.beginTrainingSet; row < this.endTrainingSet; row++) {

			setVariablesExpression(row, express);	

			try {
				double expressionEvaluation = express.evaluate();
				
				fitness += Math.pow(expressionEvaluation - dataOutput[row], 2);
				
			} catch (ArithmeticException ae) {
				// assume error is really big
				fitness += Integer.MAX_VALUE;
			}
		}
		
		fitness = Math.sqrt(fitness) / (endTrainingSet - beginTrainingSet);
		
		tree.setFitness(fitness);
	}
	
	private void setVariablesExpression(int row, Expression express) {

		for (int col = 0; col < data[row].length; col++) {
			express.setVariable(variables[col], data[row][col]);
		}
	}

}