package parallelVersionIslands;

import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadLocalRandom;

import abstractsyntaxtree.ExpressionTree;
import net.objecthunter.exp4j.Expression;


/**
 * Represents inner threads which can split the work with the main island
 */
public class InnerIslandThread extends Thread {
	
	// Constantes das definicoes do programa
	private static final int SPLIT_THRESHOLD = 100;
	private static final int TRAINING_SET_SPLIT_SIZE = 100;
	
	// Constantes da populacao
	private static final double MUTATION_RATE = 0.1;
	private static final int TOP_AMOUNT_ELITES = 1;
	
	// Atributos da Thread
	private int geracao;
	private int startLimit;
	private int endLimit;
	private ExpressionTree[] population;
	
	// Atributos do data set
	private double[][] data;
	private double[] dataOutput;
	private String[] variables;

	private int amountPopulation;
	private int amountPartsTrainingSet;
	
	private Phaser phaser;
	private ThreadLocalRandom random;
	
	public InnerIslandThread(int startLimit, int endLimit, int geracao, int amountPopulation,
							 double[][] data, double[] dataOutput, String[] variables,
							 ExpressionTree[] population, Phaser phaser) {
		
		this.geracao = geracao;
		this.startLimit = startLimit;
		this.endLimit = endLimit;
		
		this.data = data;
		this.dataOutput = dataOutput;
		this.variables = variables;
		
		this.phaser = phaser;
		this.population = population;
		this.random = ThreadLocalRandom.current();
		
		this.amountPopulation = amountPopulation;
		this.amountPartsTrainingSet = Math.max(1, this.data.length / TRAINING_SET_SPLIT_SIZE);
	}

	@Override
	public void run() {
		
		phaser.arriveAndAwaitAdvance();

		// Create the new population
		ExpressionTree[] newPopulation = new ExpressionTree[this.amountPopulation];
		
		for (int geracaoAtual = this.geracao; geracaoAtual < Island.AMOUNT_ITERATIONS; geracaoAtual++) {
		
			// Generate the new population from the previous one
			for (int i = this.startLimit; i < endLimit; i++)
				operations(newPopulation, i, geracao);
			
			// Transition from the previous generation to the new one
			phaser.arriveAndAwaitAdvance();

			for (int i = this.startLimit; i < this.endLimit; i++)
				this.population[i] = newPopulation[i];
			
			phaser.arriveAndAwaitAdvance();
			// Does nothing - Main thread island responsible for this action
			phaser.arriveAndAwaitAdvance();	
			
			newPopulation = new ExpressionTree[this.amountPopulation];
		}

		System.out.println("TERMINOU!!");
		phaser.arriveAndAwaitAdvance();
		System.out.println("APOS TERMINOU!!");
	}
	
	public void setStartLimit(int startLimit) {
		
		this.startLimit = startLimit;
	}
	
	public void setEndLimit(int endLimit) {
		
		this.endLimit = endLimit;
	}
	
	private void operations(ExpressionTree[] newPopulation, int j, int geracao) {

		if (j >= TOP_AMOUNT_ELITES) {
			// CrossOver
			int parent1 = (int) (- Math.log(random.nextDouble()) * this.amountPopulation) % this.amountPopulation;
			int parent2 = (int) (- Math.log(random.nextDouble()) * this.amountPopulation) % this.amountPopulation;

			newPopulation[j] = this.population[parent1].crossOverWith(this.population[parent2]);

			// Mutacao
			if (random.nextDouble() < MUTATION_RATE) {
				newPopulation[j].mutate();
			}
		}

		// Calcular o Fitness
		measureFitness(newPopulation[j], geracao);
	}
	
	private double measureFitness(ExpressionTree tree, int geracao) {

		int beginTrainingSet = this.getBeginningTrainingSet(geracao);
		int endTrainingSet = this.getEndTrainingSet(geracao);
		
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

		fitness = Math.sqrt(fitness / (endTrainingSet - beginTrainingSet));

		tree.setFitness(fitness);

		return fitness;
	}

	private void setVariablesExpression(int row, Expression express) {

		for (int col = 0; col < data[row].length; col++) {
			express.setVariable(variables[col], data[row][col]);
		}
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