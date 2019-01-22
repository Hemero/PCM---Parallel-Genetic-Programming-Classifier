package parallelVersionIslands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.tuple.Pair;

import abstractsyntaxtree.ExpressionTree;
import main.Main;
import net.objecthunter.exp4j.Expression;
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
	private static final int SPLIT_THRESHOLD = 250;
	public static final int AMOUNT_ITERATIONS = 500;
	private static final int TRAINING_SET_SPLIT_SIZE = 100;
	private static final int EXCHANGE_EXPRESSIONS_RATE = 20;
	
	// Constantes da populacao
	private static final double MUTATION_RATE = 0.1;
	private static final int TOP_AMOUNT_ELITES = 1;
	
	
	// Atributos da Ilha
	private int islandId;
	private int amountPopulation;
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
	
	private int amountPartsTrainingSet;
	
	private Phaser phaser;
	private ThreadLocalRandom random;
	
	public Island(int islandId, double[][] data, double[] dataOutput, int amountPopulation, 
				  String[] variables, Island[] otherIslands) {
		
		this.islandId = islandId;
		this.amountPopulation = amountPopulation;
		
		this.data = data;
		this.variables = variables;
		this.dataOutput = dataOutput;
		
		this.population = new ExpressionTree[amountPopulation];
		
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
		

		this.amountPartsTrainingSet = this.data.length / TRAINING_SET_SPLIT_SIZE + 1;
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
			
			this.innerIslandThreads.add(new InnerIslandThread(startLimit, endLimit, 0, 
										this.amountPopulation, this.data, this.dataOutput, 
										this.variables, this.population, this.phaser));
			this.innerIslandThreads.get(threadId - 1).start();
		}
	}
	
	@Override
	public void run() {

		// 1. Create the population
		this.generatePopulation();
		
		// 1.5 Measure the new population fitness
		for (int i = 0; i < this.population.length; i++)
			measureFitness(this.population[i], 0);
		
		// 2. Sort the population
		this.sortPopulation();
		
		this.phaser.arriveAndAwaitAdvance();
		
		// Create the new population
		ExpressionTree[] newPopulation = new ExpressionTree[this.amountPopulation];
		
		for (int geracao = 0; geracao < AMOUNT_ITERATIONS; geracao++) {

			// Exchange with another island
			if (geracao % EXCHANGE_EXPRESSIONS_RATE == 0) {
				// Choose a random island
				int chosenIsland = this.random.nextInt(this.otherIslands.length);
				
				if (chosenIsland == this.islandId)
					chosenIsland = (chosenIsland + 1) % this.otherIslands.length;
				
				// Send best individual to that island
				this.otherIslands[chosenIsland].sendExpression(this.population[0]);
			}
			
			// Copy the elite expression trees to the new population
			for (int i = 0; i < TOP_AMOUNT_ELITES; i++)
				newPopulation[i] = this.population[i];
			
			// Generate the new population from the previous one
			for (int j = this.lowLimit; j < this.highLimit; j++)
				operations(newPopulation, j, geracao);
			
			// Transition from the previous generation to the new one
			this.phaser.arriveAndAwaitAdvance();
			
			for (int i = this.lowLimit; i < this.highLimit; i++)
				this.population[i] = newPopulation[i];
			
			// Sort the current population
			this.phaser.arriveAndAwaitAdvance();
			
			if (!this.expressionsBuffer.isEmpty())
				this.population[this.population.length - 1] = this.expressionsBuffer.poll();
				
			this.sortPopulation();
			
			if (this.islandId == 0) {
				
				Main.contador.stop();
				System.out.println(geracao + ";" + this.islandId + ";" + this.population[0].getFitness() + ";" + Main.contador.getDuration());
			}
			// I can create some amount of threads which will be available
			if (this.highLimit - this.lowLimit > 10 && !this.threadBuffer.isEmpty()) 
				this.createNewInnerIslandThreads(geracao);
			
			this.phaser.arriveAndAwaitAdvance();
			
			newPopulation = new ExpressionTree[this.amountPopulation];
		}
		
		
		// Wait for everyone to finish their computations
		this.phaser.arriveAndAwaitAdvance();
		
		// Tell everyone that they can now have the threads I was occupying
		int indice = this.otherIslands.length - 1;
		int quantidadeThreadsDisponiveis = this.innerIslandThreads.size() + 1;
		
		while(quantidadeThreadsDisponiveis > 0) {
			
			if (this.availableOtherIslands[indice] == 0 && indice != this.islandId) {
				this.otherIslands[indice].sendCreateThreads(1);
				quantidadeThreadsDisponiveis--;
			}
			
			indice = (this.otherIslands.length + (indice - 1)) % this.otherIslands.length;
		}

	
		for (InnerIslandThread thread : this.innerIslandThreads)
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	private void generatePopulation() {
		
		for (int i =  0; i < this.population.length; i++)
			this.population[i] = new ExpressionTree(this.variables);
	}

	private void sortPopulation() {
		
		ParallelMergeSort mergeSort = new ParallelMergeSort(0, this.population.length, population);
		mergeSort.compute();
	}	
	
	private void createNewInnerIslandThreads(int geracao) {
		
		int amountThreads = 0;
		
		while (!this.threadBuffer.isEmpty()) {
		
			Pair<Integer, Integer> par = this.threadBuffer.poll();
			
			this.availableOtherIslands[par.getLeft()] = 1;
			amountThreads += par.getRight();
		}
		
		// Criar as novas threads
		while (amountThreads > 0) {
			
			InnerIslandThread newThread = new InnerIslandThread(-1, -1, geracao + 1, this.amountPopulation,
										this.data, this.dataOutput, this.variables, 
										this.population, this.phaser);
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

	private void operations(ExpressionTree[] newPopulation, int j, int geracao) {

		if (j >= TOP_AMOUNT_ELITES) {
			// CrossOver
			int parent1 = (int) (- Math.log(random.nextDouble()) * this.population.length) % this.population.length;
			int parent2 = (int) (- Math.log(random.nextDouble()) * this.population.length) % this.population.length;

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

	public ExpressionTree getBestIndividual() {
		
		return this.population[0];
	}
}
