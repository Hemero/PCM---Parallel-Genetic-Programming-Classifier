package parallelVersionWithPhaser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

import abstractsyntaxtree.ExpressionTree;

public class ClassifierGA {

	// Constantes
	private static final int AMOUNT_POPULATION = 1000;
	private static final int AMOUNT_THREADS = Runtime.getRuntime().availableProcessors();
	
	// Atributos
	private double[][] data;
	private double[] classes;
	private double[] dataOutput;
	private String[] variables;
	
	public ClassifierGA(double[][] data, double[] classes, double[] dataOutput, String[] variables) {
		
		this.data = data;
		this.classes = classes;
		this.dataOutput = dataOutput;
		this.variables = variables;
	}
	
	public void startClassification() {
		
		ExpressionTree[] population = new ExpressionTree[AMOUNT_POPULATION];
		ClassifierThread[] classifiers = new ClassifierThread[AMOUNT_THREADS];

		int low = -1;
		int high = -1;
		int workPerThread = AMOUNT_POPULATION / AMOUNT_THREADS;
		
		// ExecutorService executorService = Executors.newCachedThreadPool();
		Phaser phaser = new Phaser();
		
		for (int threadId = 0; threadId < classifiers.length; threadId++) {
			
			low = threadId * workPerThread;
			high = (threadId + 1) * workPerThread;
			
			if (threadId == classifiers.length - 1)
				high = population.length;
			
			classifiers[threadId] = new ClassifierThread(threadId, low, high, 
							data, dataOutput, classes, variables, population, phaser);
		}
		
		for (ClassifierThread classifierThread : classifiers)
			classifierThread.start();
		
		for (ClassifierThread classifierThread : classifiers)
			try {
				classifierThread.join();
			} catch (InterruptedException e) {
				classifierThread.interrupt();
				e.printStackTrace();
			}
		// phaser.arriveAndAwaitAdvance();
		
		System.out.println("Classification has terminated.");
	}
}
