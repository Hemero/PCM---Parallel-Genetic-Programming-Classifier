package parallelVersionIslands;

import java.util.Scanner;

import abstractsyntaxtree.ExpressionTree;

public class ClassifierGA {

	// Constantes
	private static final int AMOUNT_POPULATION = 1000;
	private static final int AMOUNT_THREADS = Runtime.getRuntime().availableProcessors();
	
	// Constantes de erro
	private static final String ERROR_QTD_ILHAS = "The inserted value %d is invalid.";
	
	// Atributos
	private double[][] data;
	private double[] dataOutput;
	private String[] variables;
	
	private Island[] ilhas;
	
	public ClassifierGA(double[][] data, double[] dataOutput, String[] variables) {
		
		this.data = data;
		this.dataOutput = dataOutput;
		this.variables = variables;	
	}
	
	public void startClassification() {
	
		int qtdIlhas = 0;

		Scanner leitor = new Scanner(System.in);
		
		do {
			// System.out.println("Introduza uma quantidade de ilhas inferior a " + AMOUNT_THREADS + ": ");
			qtdIlhas = 24;
			
			if (qtdIlhas <= 0 || qtdIlhas > AMOUNT_THREADS)
				System.out.println(String.format(ERROR_QTD_ILHAS, qtdIlhas));

		} while (qtdIlhas <= 0 || qtdIlhas > AMOUNT_THREADS);
		
		ilhas = new Island[qtdIlhas];
		
		int amountPopulation = AMOUNT_POPULATION / qtdIlhas;

		for (int islandId = 0; islandId < qtdIlhas; islandId++) {
		
			int currentAmountPopulation = amountPopulation;
			
			if (AMOUNT_POPULATION % this.ilhas.length - islandId > 0)
				currentAmountPopulation++;
			
			ilhas[islandId] = new Island(islandId, data, dataOutput, currentAmountPopulation, variables, ilhas);
		}
			
		for (Island ilha : ilhas)
			ilha.start();
		
		for (Island ilha : ilhas) {
			try {
				ilha.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
			
		// System.out.println("Classification has terminated.");
	}
	
	public ExpressionTree getBestIndividual() {
		
		ExpressionTree result = this.ilhas[0].getBestIndividual();
		
		for (Island ilha : this.ilhas)
			if (ilha.getBestIndividual().getFitness() < result.getFitness())
				result = ilha.getBestIndividual();
		
		return result;
	}
}
