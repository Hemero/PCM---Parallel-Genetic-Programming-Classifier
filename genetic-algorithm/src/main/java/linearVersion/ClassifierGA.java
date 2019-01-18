package linearVersion;

import java.util.Arrays;
import java.util.Random;

import abstractsyntaxtree.ExpressionTree;
import net.objecthunter.exp4j.Expression;

public class ClassifierGA {

	// Constantes do Programa 
	private static final int TRAINING_SET_SPLIT_SIZE = 100;
	private static final int AMOUNT_ITERATIONS = 500;

	// Population Constants
	private static final int TOP_AMOUNT_ELITES = 1;
	private static final int AMOUNT_POPULATION = 1000;

	// Operations Constants
	private static final double MUTATION_RATE = 0.1;
	private static final int SPLIT_THRESHOLD = 250;

	// Atributos
	private ExpressionTree[] population;

	// Data-set information
	private double[][] data;
	private double[] dataOutput;
	private String[] variables;
	private int amountPartsTrainingSet;

	private Random random;

	public ClassifierGA(double[][] data, double[] dataOutput, String[] variables) {

		this.data = data;
		this.variables = variables;
		this.dataOutput = dataOutput;

		this.random = new Random();
		this.population = new ExpressionTree[AMOUNT_POPULATION];

		this.amountPartsTrainingSet = this.data.length / TRAINING_SET_SPLIT_SIZE + 1;

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
		
//		System.out.println("Best individual at generation 0 with fitness " +
//							this.population[0].getFitness() + ": " + this.population[0]);
		System.out.println(0 + ";" + this.population[0].getFitness());
		
		for (int geracao = 1; geracao < AMOUNT_ITERATIONS; geracao++) {

			// Copy the TOP_AMOUNT_ELITES to the new population
			for (int i = 0; i < TOP_AMOUNT_ELITES; i++)
				newPopulation[i] = this.population[i];
		
			// Gather the rest of the sons into the array
			for (int j = 0; j < AMOUNT_POPULATION; j++)
				operations(newPopulation, j, geracao);

			// Sort the new population
			Arrays.sort(newPopulation);

			// Set the new population
			this.population = newPopulation;
			
//			System.out.println("Best individual at generation " + geracao + 
//							   " with fitness " + this.population[0].getFitness() + 
//							   ": " + this.population[0]);
			System.out.println(geracao + ";" + this.population[0].getFitness());
			newPopulation = new ExpressionTree[AMOUNT_POPULATION];
		}
	}

	private void operations(ExpressionTree[] newPopulation, int j, int geracao) {

		if (j >= TOP_AMOUNT_ELITES) {
			// CrossOver
			int parent1 = (int) (- Math.log(random.nextDouble()) * AMOUNT_POPULATION) % AMOUNT_POPULATION;
			int parent2 = (int) (- Math.log(random.nextDouble()) * AMOUNT_POPULATION) % AMOUNT_POPULATION;

			newPopulation[j] = this.population[parent1].crossOverWith(this.population[parent2]);

			// Mutacao
			if (random.nextDouble() < MUTATION_RATE) {
				newPopulation[j].mutate();
			}
		}

		// Calcular o Fitness
		measureFitness(newPopulation[j], geracao);
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
