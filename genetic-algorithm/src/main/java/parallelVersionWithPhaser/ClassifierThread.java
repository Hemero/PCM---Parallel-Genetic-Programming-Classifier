package parallelVersionWithPhaser;

import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadLocalRandom;

import abstractsyntaxtree.ExpressionTree;
import net.objecthunter.exp4j.Expression;
import utils.ParallelMergeSort;

public class ClassifierThread extends Thread {

	// Constantes das definicoes do programa
	private static final int TRAINING_SET_SPLIT_SIZE = 100;
	private static final int SPLIT_THRESHOLD = 100;
	private static final int AMOUNT_ITERATIONS = 1000;
	
	// Constantes da populacao
	private static final double MUTATION_RATE = 0.1;
	private static final int TOP_AMOUNT_ELITES = 1;
	private static final int AMOUNT_POPULATION = 1000;
	
	// Atributos
	private int threadId;
	private int lowLimit;
	private int highLimit;
	
	// Atributos dataset
	private double[][] data;
	private double[] dataOutput;
	private String[] variables;
	private int amountPartsTrainingSet;
	
	// Atributos da Thread
	private Phaser phaser;
	private ThreadLocalRandom random;
	private ExpressionTree[] population;

	public ClassifierThread(int threadId, int lowLimit, int highLimit,
							double[][] data, double[] dataOutput, 
							String[] variables, ExpressionTree[] population,
							Phaser phaser) {
		
		this.threadId = threadId;
		this.lowLimit = lowLimit;
		this.highLimit = highLimit;
		
		this.data = data;
		this.dataOutput = dataOutput;
		this.variables = variables;

		this.amountPartsTrainingSet = Math.max(1, this.data.length / TRAINING_SET_SPLIT_SIZE);
		
		this.phaser = phaser;
		this.population = population;
		this.random = ThreadLocalRandom.current();
	}
	
	@Override
	public void run() {

		// 0. Gerar a populacao inicial
		generatePopulation();
		
		// Wait for everyone to generate their population
		this.phaser.arriveAndAwaitAdvance();
		
		for (int geracao = 0; geracao < AMOUNT_ITERATIONS; geracao++) {

			// 1. Calcular o Fitness
			measureFitness(geracao);
			
			// Await for everyone to do the operations
			this.phaser.arriveAndAwaitAdvance();
			
			// 2. Sort das arvores por ordem descendente
			if (this.threadId == 0)
				sortPopulation();
			
			// Await for array to be sorted
			this.phaser.arriveAndAwaitAdvance();
			
			if (threadId == 0)
				System.out.println("Best individual at generation " + geracao + 
						" with fitness " + this.population[0].getFitness() + ": " + this.population[0]);			
			// Create the new population
			ExpressionTree[] newPopulation = new ExpressionTree[AMOUNT_POPULATION];
			
			// 2.5 Copy the TOP_AMOUNT_ELITES to the new population
			if (threadId == 0)
				for (int i = 0; i < TOP_AMOUNT_ELITES; i++)
					newPopulation[i] = this.population[i];
			
			// 3. CrossOver
			applyCrossOvers(newPopulation);

			// 4. Mutacao
			applyMutations(newPopulation);
			
			// Transition from population to newPopulation

			for (int i = this.lowLimit; i < this.highLimit; i++)
				this.population[i] = newPopulation[i];
		}
	}

	private void generatePopulation() {
		
		for (int i = this.lowLimit; i < this.highLimit; i++)
			this.population[i] = new ExpressionTree(variables);
	}

	private void measureFitness(int geracao) {
		
		int beginTrainingSet = 0;
		int endTrainingSet = 0;
		
		// Get if we should get the all data-set or partitions of it
		if (geracao > SPLIT_THRESHOLD) {
			
			beginTrainingSet = 0;
			endTrainingSet = this.data.length;
		} else {
			
			beginTrainingSet = (geracao % this.amountPartsTrainingSet) * (this.data.length / this.amountPartsTrainingSet);
			endTrainingSet = (((geracao + 1) % this.amountPartsTrainingSet) * (this.data.length / this.amountPartsTrainingSet));
			
			if (((geracao + 1) % this.amountPartsTrainingSet) == 0)
				endTrainingSet = this.data.length;
		}
		
		for (int i = this.lowLimit; i < this.highLimit; i++)
			measureExpression(population[i], beginTrainingSet, endTrainingSet);
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
		
		fitness = Math.sqrt(fitness) / data.length;
		
		tree.setFitness(fitness);
		
		return fitness;
	}

	private void setVariablesExpression(int row, Expression express) {

		for (int col = 0; col < data[row].length; col++)
			express.setVariable(variables[col], data[row][col]);
	}
	
	private void sortPopulation() {
		
		ParallelMergeSort mergeSort = new ParallelMergeSort(0, this.population.length, population);
		mergeSort.compute();
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
		for (int i = Math.max(TOP_AMOUNT_ELITES, this.lowLimit); i < this.highLimit; i++) {

			// The first elements in the population have higher probability of being selected
			int parent1 = (int) (- Math.log(random.nextDouble()) * AMOUNT_POPULATION) % AMOUNT_POPULATION;
			int parent2 = (int) (- Math.log(random.nextDouble()) * AMOUNT_POPULATION) % AMOUNT_POPULATION;

			newPopulation[i] = this.population[parent1].crossOverWith(this.population[parent2]);
		}
	}

	private void applyMutations(ExpressionTree[] newPopulation) {
		
		for (int i = Math.max(TOP_AMOUNT_ELITES, this.lowLimit); i < this.highLimit; i++) 
			if (random.nextDouble() < MUTATION_RATE) 
				newPopulation[i] = newPopulation[i].mutate();
	}
}
