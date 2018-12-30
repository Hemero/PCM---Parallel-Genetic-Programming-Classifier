package linearVersion;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Random;

import abstractsyntaxtree.ExpressionTree;
import net.objecthunter.exp4j.Expression;

public class ClassifierGA {

	// Constantes
	private static final double THRESHOLD = 0;
	private static final int AMOUNT_ITERATIONS = 1000;
	private static final int AMOUNT_POPULATION = 1000;
	private static final int TOP_AMOUNT_ELITES = 1;
	
	private static final double MUTATION_RATE = 0.1;
	
	// Atributos
	private double[][] data;
	private double[] classes;
	private double[] dataOutput;
	private String[] variables;
	
	private Random random;
	private ExpressionTree[] population;
	
	public ClassifierGA(double[][] data, double[] classes, double[] dataOutput, String[] variables) {
		
		this.data = data;
		this.classes = classes;
		this.dataOutput = dataOutput;
		this.variables = variables;
		
		this.random = new Random();
		this.population = new ExpressionTree[AMOUNT_POPULATION];
		
		// 0. Gerar a populacao inicial
		generatePopulation();
	}
	
	public void startClassification() {
		for (int geracao = 0; geracao < AMOUNT_ITERATIONS; geracao++) {
			
			// 1. Calcular o Fitness
			measureFitness();
			
			// 2. Sort das arvores por ordem descendente
			Arrays.sort(this.population);

			System.out.println("Best individual at generation " + geracao + ": " + this.population[0] + " with fitness " + this.population[0].getFitness());
			
			// Create the new population
			ExpressionTree[] newPopulation = new ExpressionTree[AMOUNT_POPULATION];
			
			// 2.5 Copy the TOP_AMOUNT_ELITES to the new population
			for (int i = 0; i < TOP_AMOUNT_ELITES; i++)
				newPopulation[i] = this.population[i];
			
			// 3. CrossOver
			applyCrossOvers(newPopulation);
			
			// 4. Mutacao
			applyMutations(newPopulation);
			
			this.population = newPopulation;
		}
	}

	private void generatePopulation() {
		
		for (int i = 0; i < AMOUNT_POPULATION; i++)
			this.population[i] = new ExpressionTree(variables);
	}

	private void measureFitness() {
		
		for (int i = 0; i < AMOUNT_POPULATION; i++) {
			measureExpression(population[i]);
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

	private void applyCrossOvers(ExpressionTree[] newPopulation) {

		/*
		 * There are many ways to choose how to apply crossOvers to the next iteration:
		 * Steady Choice, Elitism Selection, Roulette Selection, Tournament Selection, Entropy-Boltzmann Selection
		 * 
		 * The study presented in [5] (check github) shows that Tournament Selection is more efficient than
		 * Roulette selection. 
		 * 
		 * Also, a small portion of the population should be elites and not suffer any crossOvers/Mutations
		 * These elites have the best genes so they carry their genes to the following generations.
		 * Too many elites can cause the population to degenerate.
		 */		
		for (int i = TOP_AMOUNT_ELITES; i < AMOUNT_POPULATION; i++) {

			// The first elements in the population have higher probability of being selected
			int parent1 = (int) (- Math.log(random.nextDouble()) * AMOUNT_POPULATION) % AMOUNT_POPULATION;
			int parent2 = (int) (- Math.log(random.nextDouble()) * AMOUNT_POPULATION) % AMOUNT_POPULATION;

			newPopulation[i] = this.population[parent1].crossOverWith(this.population[parent2]);
		}
	}

	private void applyMutations(ExpressionTree[] newPopulation) {
		
		for (int i = TOP_AMOUNT_ELITES; i < AMOUNT_POPULATION; i++) {
			
			if (random.nextDouble() < MUTATION_RATE) {
				newPopulation[i].mutate();
			}
		}
	}
}
