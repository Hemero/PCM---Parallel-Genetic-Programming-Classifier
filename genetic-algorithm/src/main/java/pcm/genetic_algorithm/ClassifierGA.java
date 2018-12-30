package pcm.genetic_algorithm;

import java.util.Arrays;

import abstractsyntaxtree.ExpressionTree;
import net.objecthunter.exp4j.Expression;

public class ClassifierGA {

	// Constantes
	private static final double THRESHOLD = 0;
	private static final int AMOUNT_ITERATIONS = 0;
	private static final int AMOUNT_POPULATION = 0;
	
	// Atributos
	private double[][] data;
	private String[] variables;
	private ExpressionTree[] population = new ExpressionTree[AMOUNT_POPULATION];
	
	public ClassifierGA(double[][] data, String[] variables) {
		
		this.data = data;
		this.variables = variables;
		
		// Gerar a populacao inicial
		generatePopulation(population);
	}
	
	public void startClassification() {
		
		for (int geracao = 0; geracao < AMOUNT_ITERATIONS; geracao++) {
			
			// 1. Calcular o Fitness
			
			// 2. Sort das arvores por ordem descendente
			Arrays.sort(population);
			
			// 3. CrossOver
			
			// 4. Mutacao
		}
		
		variables = new String[] {"x", "y", "z"};

		ExpressionTree tree = new ExpressionTree(variables);

		tree.generateTree();

		ExpressionTree tree2 = tree.clone();

		System.out.println(tree);
		System.out.println(tree2);

		Expression express = tree.getExpression();
		express.setVariable("x", 1.0);
		express.setVariable("y", 1.0);
		express.setVariable("z", 1.0);

		//		System.out.println(express.evaluate());

		data = new double[][] {{0.2,0.3,0.3,0.2},{5.6,0.124,2348.3,5.6},{123.0,124.223,12.3214,123.0}};

		double fitness = measureFitness(express);
		
		tree.setFitness(fitness);
		System.out.println(fitness);
	}

	private void generatePopulation(ExpressionTree[] population) {
		// TODO 	
	}

	private double measureFitness(Expression express) {

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
	
	private void setVariablesExpression(int row, Expression express) {

		for (int col = 0; col < (data[0].length-1); col++) {
			express.setVariable(variables[col], data[row][col]);
		}

	}
}
