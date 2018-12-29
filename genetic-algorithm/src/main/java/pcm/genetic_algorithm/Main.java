package pcm.genetic_algorithm;

import abstractsyntaxtree.ExpressionTree;
import net.objecthunter.exp4j.Expression;

public class Main {

	private static double[][] data;
	private static Expression express;
	private static String[] variables;

	private static final double THRESHOLD = 0;

	private static void loadData(String fileName) {
		// TODO
	}

	private static double measureFitness(Expression express) {

		int correctlyClassified = 0;

		for (int row = 0; row < data.length; row++) {

			setVariablesExpression(row, express);	

			try {
				if ((express.evaluate() < THRESHOLD && data[row][data[0].length-1] < THRESHOLD) || 
						(express.evaluate() >= THRESHOLD && data[row][data[0].length-1] >= THRESHOLD)) {

					correctlyClassified++;
				}
			} catch (ArithmeticException ae) {
				// Do nothing - counts as incorrectly classified
			}
		}

		return 1.0*correctlyClassified / data.length;

	}

	private static void setVariablesExpression(int row, Expression express) {

		for (int col = 0; col < (data[0].length-1); col++) {
			express.setVariable(variables[col], data[row][col]);
		}

	}

	public static void main( String[] args ) {

		variables = new String[] {"x", "y", "z"};

		ExpressionTree tree = new ExpressionTree(variables);

		tree.generateTree();

		ExpressionTree tree2 = tree.clone();

		System.out.println(tree);
		System.out.println(tree2);

		express = tree.getExpression();
		express.setVariable("x", 1.0);
		express.setVariable("y", 1.0);
		express.setVariable("z", 1.0);

		//		System.out.println(express.evaluate());

		data = new double[][] {{0.2,0.3,0.3,0.2},{5.6,0.124,2348.3,5.6},{123.0,124.223,12.3214,123.0}};

		double fitness = measureFitness(express);
		
		tree.setFitness(fitness);
		System.out.println(fitness);
	}
}
