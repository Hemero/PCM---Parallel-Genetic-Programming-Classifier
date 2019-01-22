package parallelVersionWithPhaser;

import java.util.concurrent.Phaser;

import abstractsyntaxtree.ExpressionTree;

/**
 * @author Faculdade de Ciencias da Universidade de Lisboa - FC47806 FC49034
 */
public class ClassifierGA {

	// Constantes
	private static final int AMOUNT_POPULATION = 1000;
	private static final int AMOUNT_THREADS = Runtime.getRuntime().availableProcessors();
	
	// Atributos
	private double[][] data;
	private double[] dataOutput;
	private String[] variables;
	
	private ExpressionTree[] population;
	
	public ClassifierGA(double[][] data, double[] dataOutput, String[] variables) {
		
		this.data = data;
		this.dataOutput = dataOutput;
		this.variables = variables;
	}
	
	public void startClassification() {
		
		population = new ExpressionTree[AMOUNT_POPULATION];
		ClassifierThread[] classifiers = new ClassifierThread[AMOUNT_THREADS];

		int low = -1;
		int high = -1;
		int workPerThread = AMOUNT_POPULATION / AMOUNT_THREADS;
		
		Phaser phaser = new Phaser(AMOUNT_THREADS);
		
		for (int threadId = 0; threadId < classifiers.length; threadId++) {
			
			low = threadId * workPerThread;
			high = (threadId + 1) * workPerThread;
			
			if (threadId == classifiers.length - 1)
				high = population.length;
			
			classifiers[threadId] = new ClassifierThread(threadId, low, high, 
							data, dataOutput, variables, population, phaser);
			classifiers[threadId].start();
		}
				
		for (ClassifierThread classifierThread : classifiers)
			try {
				classifierThread.join();
			} catch (InterruptedException e) {
				classifierThread.interrupt();
				e.printStackTrace();
			}
		
		System.out.println("Classification has terminated.");
	}

	public ExpressionTree getBestIndividual() {
		
		return this.population[0];
	}
}
