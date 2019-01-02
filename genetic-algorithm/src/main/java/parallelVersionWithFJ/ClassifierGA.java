package parallelVersionWithFJ;

import abstractsyntaxtree.ExpressionTree;
import utils.ParallelMergeSort;

public class ClassifierGA {

	// Constantes
	private static final int AMOUNT_POPULATION = 1000;
	private static final int TOP_AMOUNT_ELITES = 1;
	private static final int AMOUNT_ITERATIONS = 1000;

	// Atributos
	private double[][] data;
	private double[] classes;
	private double[] dataOutput;
	private String[] variables;
	private ExpressionTree[] population;

	public ClassifierGA(double[][] data, double[] classes, double[] dataOutput, String[] variables) {

		this.data = data;
		this.classes = classes;
		this.dataOutput = dataOutput;
		this.variables = variables;
		this.population = new ExpressionTree[AMOUNT_POPULATION];

		GeneratePopulation generatePopulationAction =
				new GeneratePopulation(population, this.variables, 0, AMOUNT_POPULATION);
		generatePopulationAction.compute();
	}

	public void startClassification() {
		for (int geracao = 0; geracao < AMOUNT_ITERATIONS; geracao++) {

			// 1. Calcular o Fitness
			MeasureFitness measureFitnessAction = 
					new MeasureFitness(population, data, dataOutput, classes, 
							variables, 0, AMOUNT_POPULATION);
			measureFitnessAction.compute();

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
					new ApplyCrossOver(population, newPopulation, variables, TOP_AMOUNT_ELITES-1, AMOUNT_POPULATION);
			applyCrossOver.compute();
			
			// 4. Mutacao
			ApplyMutation applyMutation = 
					new ApplyMutation(population, newPopulation, variables, TOP_AMOUNT_ELITES-1, AMOUNT_POPULATION);
			applyMutation.compute();

			this.population = newPopulation;
		}
	}
}
