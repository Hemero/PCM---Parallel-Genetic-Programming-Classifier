package pcm.genetic_algorithm;

import java.util.Arrays;
import java.util.Random;

import abstractsyntaxtree.ExpressionTree;
import net.objecthunter.exp4j.Expression;

public class ClassifierGA {

	// Constantes
	private static final double THRESHOLD = 0;
	private static final int AMOUNT_ITERATIONS = 1000;
	private static final int AMOUNT_POPULATION = 1000;
	private static final int TOP_AMOUNT_ELITES = Math.max(1, AMOUNT_POPULATION / 100);  // 1% of the population is elite
	
	private static final double MUTATION_RATE = 0; //TODO:
	
	// Atributos
	private double[][] data;
	private String[] variables;

	private Random random;
	
	private ExpressionTree[] population;
	
	public ClassifierGA(double[][] data, String[] variables) {
		
		this.data = data;
		this.variables = variables;
		
		this.random = new Random();
		this.population = new ExpressionTree[AMOUNT_POPULATION];
		
		// Gerar a populacao inicial
		generatePopulation(population);
	}
	
	public void startClassification() {
		
		for (int geracao = 0; geracao < AMOUNT_ITERATIONS; geracao++) {
			
			// 1. Calcular o Fitness
			
			// 2. Sort das arvores por ordem descendente
			Arrays.sort(population);

			/*
			 * There are many ways to choose how to apply crossOvers to the next iteration:
			 * Steady Choice, Elitism Selection, Roulette Selection, Tournament Selection, Entropy-Boltzmann Selection
			 * 
			 * The study presented in [5] (check github) shows that Tournament Selection is more efficient than
			 * Roulette selection. 
			 * 
			 * Also, a small portion of the population should be elites and not suffer any crossOvers/Mutations
			 * These elites have the best genes so they carry their genes to the following generations.
			 * To many elites can cause the population to degenerate.
			 */			
			ExpressionTree[] newPopulation = new ExpressionTree[AMOUNT_POPULATION];
			
			// 2.5 Copy the TOP_AMOUNT_ELITES to the new population
			for (int i = 0; i < TOP_AMOUNT_ELITES; i++)
				newPopulation[i] = this.population[i];
			
			// 3. CrossOver
			applyCrossOvers(newPopulation);
			
			// 4. Mutacao
			
			
			this.population = newPopulation;
		}
		
		variables = new String[] {"x", "y", "z"};

		ExpressionTree tree = new ExpressionTree(variables);

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
				double expressionEvaluation = express.evaluate();
				
				if ((expressionEvaluation < THRESHOLD && data[row][data[0].length-1] < THRESHOLD) || 
					(expressionEvaluation >= THRESHOLD && data[row][data[0].length-1] >= THRESHOLD)) {

					correctlyClassified++;
				}
			} catch (ArithmeticException ae) {
				// Do nothing - counts as incorrectly classified
			}
		}

		return 1.0*correctlyClassified / data.length;
	}
	
	private void setVariablesExpression(int row, Expression express) {

		for (int col = 0; col < (data[row].length-1); col++) {
			express.setVariable(variables[col], data[row][col]);
		}
	}

	private void applyCrossOvers(ExpressionTree[] newPopulation) {
		
		for (int i = TOP_AMOUNT_ELITES; i < this.population.length; i++) {

			// The first elements in the population have higher probability of being selected
			int parent1 = (int) (- Math.log(random.nextDouble()) * AMOUNT_POPULATION) % AMOUNT_POPULATION;
			int parent2 = (int) (- Math.log(random.nextDouble()) * AMOUNT_POPULATION) % AMOUNT_POPULATION;

			newPopulation[i] = this.population[parent1].crossOverWith(this.population[parent2]);
		}
	}
}
