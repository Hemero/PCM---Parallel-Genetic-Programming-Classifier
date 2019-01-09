package linearVersion;

import java.util.Arrays;
import java.util.Random;

import abstractsyntaxtree.ExpressionTree;
import net.objecthunter.exp4j.Expression;

public class ClassifierGAAdaptative {

	// Constantes do Programa 
	private static final int TRAINING_SET_SPLIT_SIZE = 100;
	private static final int AMOUNT_ITERATIONS = 500;

	// Population Constants
	private static final int TOP_AMOUNT_ELITES = 1;
	private static final int AMOUNT_POPULATION = 1000;

	// Operations Constants
	private static final int SPLIT_THRESHOLD = 100;
	private static final double MUTATION_OFFSET = 0.05;
	private static final double CROSS_OVER_OFFSET = AMOUNT_POPULATION * 0.025;

	// Atributos
	private ExpressionTree[] population;

	// Atributos de variacao 
	private double mutationRate;
	private double crossOverRate;
	
	// Data-set information
	private double[][] data;
	private double[] dataOutput;
	private String[] variables;
	private int amountPartsTrainingSet;

	private Random random;

	public ClassifierGAAdaptative(double[][] data, double[] dataOutput, String[] variables) {

		this.data = data;
		this.variables = variables;
		this.dataOutput = dataOutput;

		this.random = new Random();
		this.population = new ExpressionTree[AMOUNT_POPULATION];

		this.amountPartsTrainingSet = Math.max(1, this.data.length / TRAINING_SET_SPLIT_SIZE);

		this.mutationRate = 0.1;
		this.crossOverRate = 0.0;
		
		// 0. Gerar a populacao inicial
		generatePopulation();
	}

	public void startClassification() {

		// Create the new population
		ExpressionTree[] newPopulation = new ExpressionTree[AMOUNT_POPULATION];

		// Calcular o Fitness
		for (int j = 0; j < AMOUNT_POPULATION; j++)
			measureFitness(population[j], 0);

		// Sort das arvores por ordem descendente
		Arrays.sort(this.population);
		
		System.out.format("Best individual at generation 0 with fitness %.10f: %s%n",
							this.population[0].getFitness(), this.population[0]);

		for (int geracao = 1; geracao < AMOUNT_ITERATIONS; geracao++) {

			// Copy the TOP_AMOUNT_ELITES to the new population
			for (int i = 0; i < TOP_AMOUNT_ELITES; i++)
				newPopulation[i] = this.population[i];
		
			// Gather the rest of the offspring into the array
			computeOffsprings(newPopulation, geracao);

			// Sort the new population
			Arrays.sort(newPopulation);

			// Set the new population
			this.population = newPopulation;
			
			System.out.format("Best individual at generation %d with fitness %.10f: %s%n",
							   geracao, this.population[0].getFitness(), this.population[0]);
			
			newPopulation = new ExpressionTree[AMOUNT_POPULATION];
		}
	}

	private void computeOffsprings(ExpressionTree[] newPopulation, int geracao) {
		
		// Auxiliary variables for adapting crossOver and Mutation Rates
		double crossOverProgress = 0.0;
		double mutationProgress = 0.0;
		
		int amountMutations = 0;
		
		double[] resultadoOperations;
		
		// Compute the operations
		for (int j = 0; j < AMOUNT_POPULATION; j++) {
		
			resultadoOperations = operations(newPopulation, j, geracao);
			
			crossOverProgress += resultadoOperations[0];
			
			// If it also contains a mutation progress
			if (resultadoOperations.length == 2) {
				
				mutationProgress += resultadoOperations[1];
				amountMutations++;
			}
		}
		
		crossOverProgress = crossOverProgress / (AMOUNT_POPULATION - TOP_AMOUNT_ELITES);
		
		if (amountMutations > 0)
			mutationProgress = mutationProgress / amountMutations;
		
		// Set up the new crossOver and mutation ratios
		if (crossOverProgress < mutationProgress) {
			
			this.mutationRate = Math.min(1.0, this.mutationRate + MUTATION_OFFSET);
			this.crossOverRate = Math.max(-AMOUNT_POPULATION, this.crossOverRate - CROSS_OVER_OFFSET);
		}
		
		else if (crossOverProgress > mutationProgress) {
			
			this.mutationRate = Math.max(MUTATION_OFFSET, this.mutationRate - MUTATION_OFFSET);
			this.crossOverRate = Math.min(AMOUNT_POPULATION, this.crossOverRate + CROSS_OVER_OFFSET);
		}
	}
	
	private double[] operations(ExpressionTree[] newPopulation, int j, int geracao) {
		
		double[] resultado = new double[1];
		
		double crossOverProgress;
		
		double mutationProgress;
		double oldIndividualFitness;
		
		if (j >= TOP_AMOUNT_ELITES) {
			// CrossOver
			int parent1 = (int) Math.abs(((Math.abs(random.nextGaussian()) * (AMOUNT_POPULATION / 3.0  + this.crossOverRate)) % AMOUNT_POPULATION));
			int parent2 = (int) Math.abs(((Math.abs(random.nextGaussian()) * (AMOUNT_POPULATION / 3.0 + this.crossOverRate)) % AMOUNT_POPULATION));

			newPopulation[j] = this.population[parent1].crossOverWith(this.population[parent2]);
			
			// Measure the new individual fitness
			measureFitness(newPopulation[j], geracao);
			
			crossOverProgress = newPopulation[j].getFitness() - ((this.population[parent1].getFitness() + this.population[parent2].getFitness()) / 2.0);
			
			// Assign the crossOverProgress to result
			resultado[0] = crossOverProgress;
			
			// Mutacao
			if (random.nextDouble() < this.mutationRate) {
				
				// Keep the old individual fitness
				oldIndividualFitness = newPopulation[j].getFitness();

				newPopulation[j] = newPopulation[j].mutate();

				// Re-measure the new individual fitness after fitness
				measureFitness(newPopulation[j], geracao);
	
				mutationProgress = newPopulation[j].getFitness() - oldIndividualFitness;
				
				// Re-assign the output so it has the mutationProgress
				resultado = new double[] {crossOverProgress, mutationProgress};
			}
		}

		// Measure topAmountElites fitness
		else
			measureFitness(newPopulation[j], geracao);
		
		return resultado;
	}

	private void generatePopulation() {

		for (int i = 0; i < AMOUNT_POPULATION; i++)
			this.population[i] = new ExpressionTree(variables);
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

		fitness = Math.sqrt(fitness) / (endTrainingSet - beginTrainingSet);

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
