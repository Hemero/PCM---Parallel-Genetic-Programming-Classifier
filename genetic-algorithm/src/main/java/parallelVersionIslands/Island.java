package parallelVersionIslands;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadLocalRandom;

import abstractsyntaxtree.ExpressionTree;

/**
 * This represents an island where a certain population develops independently
 * from other islands. Ocasionally the best individual of this island is sent 
 * to other islands
 */
public class Island extends Thread {
	
	// Constantes
	private static final int AMOUNT_THREADS = Runtime.getRuntime().availableProcessors();
	
	// Constantes das definicoes do programa
	private static final int SPLIT_THRESHOLD = 100;
	private static final int AMOUNT_ITERATIONS = 1000;
	private static final int TRAINING_SET_SPLIT_SIZE = 100;
	private static final int EXCHANGE_EXPRESSIONS_RATE = 20;
	
	// Constantes da populacao
	private static final double MUTATION_RATE = 0.1;
	private static final int TOP_AMOUNT_ELITES = 1;
	private static final int AMOUNT_POPULATION = 1000;
	
	
	// Atributos da Ilha
	private int islandId;
	private ExpressionTree[] population;
	private ConcurrentLinkedQueue<ExpressionTree> expressionsBuffer;
	
	private Island[] otherIslands;
	private InnerIslandThread[] innerIslandThreads;
	
	// Atributos do data set
	private double[][] data;
	private double[] dataOutput;
	private String[] variables;
	
	private int lowLimit;
	private int highLimit;
	
	private Phaser phaser;
	private ThreadLocalRandom random;
	
	private Island(int islandId, double[][] data, double[] dataOutput, String[] variables,
				   Island[] otherIslands) {
		
		this.islandId = islandId;
		
		this.data = data;
		this.variables = variables;
		this.dataOutput = dataOutput;
		
		this.population = new ExpressionTree[AMOUNT_POPULATION];
		this.otherIslands = otherIslands;
	
		int quantidadeThreads = AMOUNT_THREADS / this.otherIslands.length;
		
		if (AMOUNT_THREADS % this.otherIslands.length - this.islandId > 0)
			quantidadeThreads++;
		
		// It is reduced by one because of the current Island Thread which also works
		this.innerIslandThreads = new InnerIslandThread[quantidadeThreads - 1];
		this.expressionsBuffer = new ConcurrentLinkedQueue<>();
		
		this.phaser = new Phaser(quantidadeThreads);
		this.random = ThreadLocalRandom.current();
	}
	
	@Override
	public void run() {
		
		// 0. Create the population
		this.generatePopulation();
		
		// Create islands and set their limits
		// ...
		// Run Islands and user Phasers
		// ...
		
		this.phaser.arriveAndAwaitAdvance();
	}
	
	private void generatePopulation() {
		
		for (int i = 0; i < this.population.length; i++)
			this.population[i] = new ExpressionTree(this.variables);
	}

	/**
	 * Sends an individual to the current island
	 */
	public void send(ExpressionTree expression) {
		
		this.expressionsBuffer.add(expression);	
	}
	
	/**
	 * Represents inner threads which can split the work with the main island
	 */
	private class InnerIslandThread extends Thread {
		
		@Override
		public void run() {
			
			// TODO:
		}
	}
}
