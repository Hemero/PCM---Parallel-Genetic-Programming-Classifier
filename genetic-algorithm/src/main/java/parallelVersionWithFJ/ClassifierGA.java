package parallelVersionWithFJ;

import abstractsyntaxtree.ExpressionTree;
import utils.ParallelMergeSort;

public class ClassifierGA {

	// Constants
	private static final int SPLIT_THRESHOLD = 250;
	private static final int AMOUNT_ITERATIONS = 500;
	private static final int TRAINING_SET_SPLIT_SIZE = 100;

	// Population Constants
	private static final int AMOUNT_POPULATION = 1000;
	private static final int TOP_AMOUNT_ELITES = 1;

	// Atributos
	private double[][] data;
	private double[] dataOutput;
	private String[] variables;
	private int amountPartsTrainingSet;

	private ExpressionTree[] population;

	public ClassifierGA(double[][] data,  double[] dataOutput, String[] variables) {

		this.data = data;
		this.dataOutput = dataOutput;
		this.variables = variables;
		this.population = new ExpressionTree[AMOUNT_POPULATION];

		this.amountPartsTrainingSet = this.data.length / TRAINING_SET_SPLIT_SIZE + 1;

		GeneratePopulation generatePopulationAction =
				new GeneratePopulation(population, this.variables, 0, AMOUNT_POPULATION);

		generatePopulationAction.compute();
	}

	public void startClassification() {

		// Create the new population
		ExpressionTree[] newPopulation = new ExpressionTree[AMOUNT_POPULATION];

		// Calcular o Fitness
		MeasureFitness measureFitness = new MeasureFitness(population, data, dataOutput, variables,
				0, AMOUNT_POPULATION, getBeginningTrainingSet(0), getEndTrainingSet(0));
		measureFitness.compute();

		// Sort das arvores por ordem descendente
		ParallelMergeSort sort = new ParallelMergeSort(0, AMOUNT_POPULATION, population);
		sort.compute();

		System.out.println(0 + ";" + this.population[0].getFitness());

		for (int geracao = 1; geracao < AMOUNT_ITERATIONS; geracao++) {

			// Copy the TOP_AMOUNT_ELITES to the new population
			for (int i = 0; i < TOP_AMOUNT_ELITES; i++)
				newPopulation[i] = this.population[i];
		
			// Gather the rest of the sons into the array
			Operations op = new Operations(population, newPopulation, data, dataOutput, variables,
					0, AMOUNT_POPULATION, getBeginningTrainingSet(geracao), getEndTrainingSet(geracao));
			op.compute();

			// Sort the new population
			ParallelMergeSort sortNewPopulation = new ParallelMergeSort(0, AMOUNT_POPULATION, newPopulation);
			sortNewPopulation.compute();

			// Set the new population
			this.population = newPopulation;
			
			System.out.println(geracao + ";" + this.population[0].getFitness());

			newPopulation = new ExpressionTree[AMOUNT_POPULATION];
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
