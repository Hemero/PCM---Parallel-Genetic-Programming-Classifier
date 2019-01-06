package parallelVersionIslands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.tuple.Pair;

import abstractsyntaxtree.ExpressionTree;
import utils.ParallelMergeSort;

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
	public static final int AMOUNT_ITERATIONS = 1000;
	private static final int TRAINING_SET_SPLIT_SIZE = 100;
	private static final int EXCHANGE_EXPRESSIONS_RATE = 20;
	
	// Constantes da populacao
	private static final double MUTATION_RATE = 0.1;
	private static final int TOP_AMOUNT_ELITES = 1;
	private static final int AMOUNT_POPULATION = 1000;
	
	
	// Atributos da Ilha
	private int islandId;
	private ExpressionTree[] population;
	private ConcurrentLinkedQueue<Pair<Integer,Integer>> threadBuffer;
	private ConcurrentLinkedQueue<ExpressionTree> expressionsBuffer;
	
	private Island[] otherIslands;
	private int[] availableOtherIslands;
	private List<InnerIslandThread> innerIslandThreads;
	
	// Atributos do data set
	private double[][] data;
	private double[] dataOutput;
	private String[] variables;
	
	private int lowLimit;
	private int highLimit;
	
	private Phaser phaser;
	private ThreadLocalRandom random;
	
	public Island(int islandId, double[][] data, double[] dataOutput, String[] variables,
				   Island[] otherIslands) {
		
		this.islandId = islandId;
		
		this.data = data;
		this.variables = variables;
		this.dataOutput = dataOutput;
		
		this.population = new ExpressionTree[AMOUNT_POPULATION];
		
		this.otherIslands = otherIslands;
		this.availableOtherIslands = new int[this.otherIslands.length];
	
		int quantidadeThreads = AMOUNT_THREADS / this.otherIslands.length;
		
		if (AMOUNT_THREADS % this.otherIslands.length - this.islandId > 0)
			quantidadeThreads++;

		this.phaser = new Phaser(quantidadeThreads);
		
		// It is reduced by one because of the current Island Thread which also works
		this.innerIslandThreads = new ArrayList<>();
		this.initializeInnerIslands(quantidadeThreads - 1);
		
		this.threadBuffer = new ConcurrentLinkedQueue<>();
		this.expressionsBuffer = new ConcurrentLinkedQueue<>();
		
		this.random = ThreadLocalRandom.current();

	}
	
	/**
	 * Create and initializes the inner threads of the current island
	 */
	private void initializeInnerIslands(int quantidadeThreads) {
		
		// 0. Start the threads and split the work
		int amountWorkPerThread = this.population.length / (quantidadeThreads + 1);
		
		this.lowLimit = 0;
		this.highLimit = amountWorkPerThread;
	
		for (int threadId = 1; threadId <= quantidadeThreads; threadId++) {
		
			int startLimit = threadId * amountWorkPerThread;
			int endLimit = (threadId + 1) * amountWorkPerThread;
			
			if (threadId == quantidadeThreads)
				endLimit = this.population.length;
			
			this.innerIslandThreads.add(new InnerIslandThread(threadId, startLimit, endLimit, phaser));
			this.innerIslandThreads.get(threadId - 1).start();
		}
	}
	
	@Override
	public void run() {

		// 1. Create the population
		this.generatePopulation();
		
		// 2. Sort the population
		this.sortPopulation();
		
		this.phaser.arriveAndAwaitAdvance();
		
		// Create the new population
		ExpressionTree[] newPopulation = new ExpressionTree[AMOUNT_POPULATION];
		
		for (int geracao = 0; geracao < AMOUNT_ITERATIONS; geracao++) {

			// Exchange with another island
			if (geracao % EXCHANGE_EXPRESSIONS_RATE == 0) {
				// Choose a random island
				// Send best individual to that island
			}
			
			// Copy the elite expression trees to the new population
			// TODO
			
			// Generate the new population from the previous one
			// TODO
			
			this.phaser.arriveAndAwaitAdvance();
			// Transition from the previous generation to the new one
			// TODO
			
			// Sort the current population
			this.phaser.arriveAndAwaitAdvance();
			this.sortPopulation();
			System.out.format("Best individual at island %d, generation %d with fitness %f: %s%n", 
							  this.islandId, geracao, this.population[0].getFitness(), this.population[0]);
			
			// I can create some amount of threads which will be available
			if (!this.threadBuffer.isEmpty()) 
				this.createNewInnerIslandThreads();
			
			this.phaser.arriveAndAwaitAdvance();
			
			newPopulation = new ExpressionTree[AMOUNT_POPULATION];
		}
		
		
		// Wait for everyone to finish their computations
		this.phaser.arriveAndAwaitAdvance();
		
		// Tell everyone that they can now have the threads I was occupying
		int indice = this.otherIslands.length - 1;
		int quantidadeThreadsDisponiveis = this.innerIslandThreads.size() + 1;
		
		while(quantidadeThreadsDisponiveis > 0) {
			
			if (this.availableOtherIslands[indice] == 0 && indice != this.islandId)
				this.otherIslands[indice].sendCreateThreads(1);
			
			quantidadeThreadsDisponiveis--;
			indice = (this.otherIslands.length + (indice - 1)) % this.otherIslands.length;
		}
	}

	private void generatePopulation() {
		
		for (int i =  0; i < AMOUNT_POPULATION; i++)
			this.population[i] = new ExpressionTree(this.variables);
	}

	private void sortPopulation() {
		
		ParallelMergeSort mergeSort = new ParallelMergeSort(0, this.population.length, population);
		mergeSort.compute();
	}	
	
	private void createNewInnerIslandThreads() {
		
		int amountThreads = 0;
		
		while (!this.threadBuffer.isEmpty()) {
		
			Pair<Integer, Integer> par = this.threadBuffer.poll();
			
			this.availableOtherIslands[par.getLeft()] = 1;
			amountThreads += par.getRight();
		}
		
		// Criar as novas threads
		while (amountThreads > 0) {
			
			InnerIslandThread newThread = new InnerIslandThread(this.innerIslandThreads.size(), -1, -1, phaser);
			this.innerIslandThreads.add(newThread);
			
			this.phaser.register();

			newThread.start();
			amountThreads--;
		}
		
		// Redefinir os limites das Threads
		int amountWorkPerThread = this.population.length / (this.innerIslandThreads.size() + 1);
		
		for (int i = 1; i <= this.innerIslandThreads.size(); i++) {

			int startLimit = i * amountWorkPerThread;
			int endLimit = (i + 1) * amountWorkPerThread;
			
			this.innerIslandThreads.get(i - 1).setStartLimit(startLimit);
			this.innerIslandThreads.get(i - 1).setEndLimit(endLimit);
		}
	}
	
	/**
	 * Sends an individual to the current island
	 */
	public void sendExpression(ExpressionTree expression) {
		
		this.expressionsBuffer.add(expression);	
	}
	
	public void sendCreateThreads(int amount) {
		
		this.threadBuffer.add(Pair.of(this.islandId, amount));
	}
}
