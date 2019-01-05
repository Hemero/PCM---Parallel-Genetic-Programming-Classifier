package linearVersion;

import java.util.Arrays;
import java.util.Random;

import abstractsyntaxtree.ExpressionTree;
import net.objecthunter.exp4j.Expression;

public class ClassifierGA {

	// Constantes
	private static final int TRAINING_SET_SPLIT_SIZE = 100;
	private static final int AMOUNT_ITERATIONS = 500;
	
	// Population Constants
	private static final int TOP_AMOUNT_ELITES = 1;
	private static final int AMOUNT_POPULATION = 1000;

	// Operations Constants
	private static final double THRESHOLD = 0;
	private static final double MUTATION_RATE = 0.1;
	private static final int SPLIT_THRESHOLD = 100;
	
	
	// Atributos
	private ExpressionTree[] population;
	
	// Data-set information
	private double[][] data;
	private double[] classes;
	private double[] dataOutput;
	private String[] variables;
	private int amountPartsTrainingSet;
	
	private Random random;
	
	public ClassifierGA(double[][] data, double[] classes, double[] dataOutput, String[] variables) {
		
		this.data = data;
		this.classes = classes;
		this.variables = variables;
		this.dataOutput = dataOutput;
		
		this.random = new Random();
		this.population = new ExpressionTree[AMOUNT_POPULATION];
		
		this.amountPartsTrainingSet = Math.max(1, this.data.length / TRAINING_SET_SPLIT_SIZE);
		
		// 0. Gerar a populacao inicial
		generatePopulation();
	}
	
	public void startClassification() {
		
		int beginTrainingSet = 0;
		int endTrainingSet = 0;

		for (int geracao = 0; geracao < AMOUNT_ITERATIONS; geracao++) {
		
			if (geracao > SPLIT_THRESHOLD) {
				
				beginTrainingSet = 0;
				endTrainingSet = AMOUNT_POPULATION;
			} else {
				
				beginTrainingSet = (geracao % this.amountPartsTrainingSet) * (this.data.length / this.amountPartsTrainingSet);
				endTrainingSet = (((geracao + 1) % this.amountPartsTrainingSet) * (this.data.length / this.amountPartsTrainingSet));
				
				if (((geracao + 1) % this.amountPartsTrainingSet) == 0)
					endTrainingSet = this.data.length;
			}
			
			// 1. Calcular o Fitness
			measureFitness(beginTrainingSet, endTrainingSet);
			
			// 2. Sort das arvores por ordem descendente
			Arrays.sort(this.population);

			System.out.println("Best individual at generation " + geracao + 
					" with fitness " + this.population[0].getFitness() + ": " + this.population[0]);

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

	private void measureFitness(int beginTrainingSet, int endTrainingSet) {
		
		for (int i = 0; i < AMOUNT_POPULATION; i++) {
			measureExpression(population[i], beginTrainingSet, endTrainingSet);
		}
	}
	
	private double measureExpression(ExpressionTree tree, int beginTrainingSet, int endTrainingSet) {

		Expression express = tree.getExpression();
		double fitness = 0;

		for (int row = beginTrainingSet; row < endTrainingSet; row++) {

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
