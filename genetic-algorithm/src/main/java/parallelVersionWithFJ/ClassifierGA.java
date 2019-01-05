package parallelVersionWithFJ;

import abstractsyntaxtree.ExpressionTree;
import utils.ParallelMergeSort;

public class ClassifierGA {

	// Constants
	private static final int SPLIT_THRESHOLD = 100;
	private static final int AMOUNT_ITERATIONS = 1000;
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

		this.amountPartsTrainingSet = Math.max(1, this.data.length / TRAINING_SET_SPLIT_SIZE);
		
		GeneratePopulation generatePopulationAction =
				new GeneratePopulation(population, this.variables, 0, AMOUNT_POPULATION);
		
		generatePopulationAction.compute();
	}

	public void startClassification() {

		int beginTrainingSet = 0;
		int endTrainingSet = 0;
		
		for (int geracao = 0; geracao < AMOUNT_ITERATIONS; geracao++) {
			
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
			
			// 1. Calcular o Fitness
			MeasureFitness measureFitness = 
					new MeasureFitness(population, data, dataOutput, 
							variables, 0, AMOUNT_POPULATION, beginTrainingSet, endTrainingSet);
			measureFitness.compute();

			// 2. Sort das arvores por ordem descendente
			ParallelMergeSort sort = new ParallelMergeSort(0, AMOUNT_POPULATION, population);
			sort.compute();

			System.out.println("Best individual at generation " + geracao + 
					" with fitness " + this.population[0].getFitness() + ": " + this.population[0]);

			// Create the new population
			ExpressionTree[] newPopulation = new ExpressionTree[AMOUNT_POPULATION];

			// 2.5 Copy the TOP_AMOUNT_ELITES to the new population
			for (int i = 0; i < TOP_AMOUNT_ELITES; i++)
				newPopulation[i] = this.population[i];

			// 3. CrossOver
			ApplyCrossOver applyCrossOver = 
					new ApplyCrossOver(population, newPopulation, variables, TOP_AMOUNT_ELITES, AMOUNT_POPULATION);
			applyCrossOver.compute();
			
			// 4. Mutacao
			ApplyMutation applyMutation = 
					new ApplyMutation(population, newPopulation, variables, TOP_AMOUNT_ELITES, AMOUNT_POPULATION);
			applyMutation.compute();

			this.population = newPopulation;
		}
	}
}
