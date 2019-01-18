package parallelVersionWithPhaser;

import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadLocalRandom;

import abstractsyntaxtree.ExpressionTree;
import net.objecthunter.exp4j.Expression;
import utils.ParallelMergeSort;

public class ClassifierThread extends Thread {

	// Constantes das definicoes do programa
	private static final int TRAINING_SET_SPLIT_SIZE = 100;
	private static final int SPLIT_THRESHOLD = 250;
	private static final int AMOUNT_ITERATIONS = 500;
	
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

		this.amountPartsTrainingSet = this.data.length / TRAINING_SET_SPLIT_SIZE + 1;
		
		this.phaser = phaser;
		this.population = population;
		this.random = ThreadLocalRandom.current();
	}
	
	@Override
	public void run() {

		// 0. Gerar a populacao inicial
		generatePopulation();

		// 1. Calcular o fitness
		for (int i = this.lowLimit; i < this.highLimit; i++)
			this.measureExpression(this.population[i], 0);
		
		// 2. Ordenar a populacao
		this.sortPopulation();
		
		// Wait for everyone to generate their population
		this.phaser.arriveAndAwaitAdvance();

		// Create the new population
		ExpressionTree[] newPopulation = new ExpressionTree[AMOUNT_POPULATION];
	
		for (int geracao = 0; geracao < AMOUNT_ITERATIONS; geracao++) {
			
			// Copy the topAmountElites to the new population and measure their fitness
			if (this.threadId == 0)
				for (int i = 0; i < TOP_AMOUNT_ELITES; i++) {
					newPopulation[i] = this.population[i];
					this.measureExpression(newPopulation[i], geracao);
				}
			
			// Generate the new population from the previous one
			for (int i = Math.max(this.lowLimit, TOP_AMOUNT_ELITES); i < this.highLimit; i++) 
				newPopulation[i] = generateIndividual(geracao);
			
			// Wait for everyone to generate individuals
			this.phaser.arriveAndAwaitAdvance();
			
			// Transition from population to newPopulation
			for (int i = this.lowLimit; i < this.highLimit; i++)
				this.population[i] = newPopulation[i];

			this.phaser.arriveAndAwaitAdvance();
			
			// Sort the current population
			if (this.threadId == 0) {
				sortPopulation();
				System.out.println(geracao + ";" + this.population[0].getFitness());
			}
			
			// Wait for population to be sorted
			this.phaser.arriveAndAwaitAdvance();
			
			newPopulation = new ExpressionTree[AMOUNT_POPULATION];
		}
	}

	private ExpressionTree generateIndividual(int geracao) {
		
		// CrossOver
		int parent1 = (int) (- Math.log(random.nextDouble()) * AMOUNT_POPULATION) % AMOUNT_POPULATION;
		int parent2 = (int) (- Math.log(random.nextDouble()) * AMOUNT_POPULATION) % AMOUNT_POPULATION;

		ExpressionTree resultado = this.population[parent1].crossOverWith(this.population[parent2]);

		// Mutacao
		if (random.nextDouble() < MUTATION_RATE) 
			resultado.mutate();

		// Calcular o Fitness
		measureExpression(resultado, geracao);
		
		return resultado;
	}
	
	private void generatePopulation() {
		
		for (int i = this.lowLimit; i < this.highLimit; i++)
			this.population[i] = new ExpressionTree(variables);
	}

	private void sortPopulation() {
		
		ParallelMergeSort mergeSort = new ParallelMergeSort(0, this.population.length, population);
		mergeSort.compute();
	}
	
	private void measureExpression(ExpressionTree tree, int geracao) {

		double fitness = 0;

		int beginTrainingSet = this.getBeginningTrainingSet(geracao);
		int endTrainingSet = this.getEndTrainingSet(geracao);
		
		Expression express = tree.getExpression();
		
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
		
		fitness = Math.sqrt(fitness / (endTrainingSet - beginTrainingSet));
		
		tree.setFitness(fitness);
	}

	private void setVariablesExpression(int row, Expression express) {

		for (int col = 0; col < data[row].length; col++)
			express.setVariable(variables[col], data[row][col]);
	}
	
	private int getBeginningTrainingSet(int geracao) {
		
		int resultado = 0;
		
		// Keep splitting until the threshold is reached
		if (geracao <= SPLIT_THRESHOLD)
			resultado = (geracao % this.amountPartsTrainingSet) * (this.data.length / this.amountPartsTrainingSet);
		
		return resultado;
	}
	
	private int getEndTrainingSet(int geracao) {
		
		int resultado = this.data.length;

		// Keep splitting until the threshold is reached
		if (geracao <= SPLIT_THRESHOLD) {
			
			resultado = (((geracao + 1) % this.amountPartsTrainingSet) * (this.data.length / this.amountPartsTrainingSet));

			if (((geracao + 1) % this.amountPartsTrainingSet) == 0)
				resultado = this.data.length;
		}
		
		return resultado;
	}
}
