package linearVersionIslands;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import abstractsyntaxtree.ExpressionTree;
import net.objecthunter.exp4j.Expression;

/**
 * This represents an island where a certain population develops independently
 * from other islands. Ocasionally the best individual of this island is sent 
 * to other islands
 */
public class Island extends Thread {

	// Constantes das definicoes do programa
	private static final int SPLIT_THRESHOLD = 100;
	public static final int AMOUNT_ITERATIONS = 1000;
	private static final int TRAINING_SET_SPLIT_SIZE = 100;
	private static final int EXCHANGE_EXPRESSIONS_RATE = 20;

	// Constantes da populacao
	private static final double MUTATION_RATE = 0.1;
	private static final int TOP_AMOUNT_ELITES = 1;


	// Atributos da Ilha
	private int islandId;
	private ExpressionTree[] population;
	private ConcurrentLinkedQueue<ExpressionTree> expressionsBuffer;

	private Island[] allIslands;

	// Atributos do data set
	private double[][] data;
	private double[] dataOutput;
	private String[] variables;
	private int amountPartsTrainingSet;

	private int popIlha;

	private Random random;

	public Island(int islandId, double[][] data, double[] dataOutput, String[] variables,
			Island[] otherIslands, int popIlha) {

		this.islandId = islandId;

		this.data = data;
		this.variables = variables;
		this.dataOutput = dataOutput;
		this.popIlha = popIlha;

		this.population = new ExpressionTree[this.popIlha];
		this.allIslands = otherIslands;
		this.expressionsBuffer = new ConcurrentLinkedQueue<>();

		this.random = new Random();

		this.amountPartsTrainingSet = Math.max(1, this.data.length / TRAINING_SET_SPLIT_SIZE);
	}

	@Override
	public void run() {

		// 1. Create the population
		this.generatePopulation();

		// Calcular o Fitness
		for (int j = 0; j < this.popIlha; j++)
			measureFitness(population[j], 0);

		// 2. Sort the population
		Arrays.sort(this.population);

		// Create the new population
		ExpressionTree[] newPopulation = new ExpressionTree[this.popIlha];

		for (int geracao = 0; geracao < AMOUNT_ITERATIONS; geracao++) {

			// Exchange with another island
			if (geracao % EXCHANGE_EXPRESSIONS_RATE == 0) {
				// Choose a random island
				int islandIndex = this.random.nextInt(this.allIslands.length);

				if (islandIndex == this.islandId) {
					islandIndex = (islandIndex + 1) % allIslands.length;
				}

				// Send best individual to that island
				this.allIslands[islandIndex].sendExpression(this.population[0]);
			}

			// Copy the elite expression trees to the new population
			for (int i = 0; i < TOP_AMOUNT_ELITES; i++)
				newPopulation[i] = this.population[i];

			// Generate the new population from the previous one
			for (int j = 0; j < this.popIlha; j++)
				operations(newPopulation, j, geracao);

			// Sort the new population
			Arrays.sort(newPopulation);

			// Set the new population
			this.population = newPopulation;

			System.out.println("Best individual at generation " + geracao + 
					" with fitness " + this.population[0].getFitness() + 
					": " + this.population[0]);

			newPopulation = new ExpressionTree[this.popIlha];

		}

	}

	private void operations(ExpressionTree[] newPopulation, int j, int geracao) {

		if (j >= TOP_AMOUNT_ELITES) {
			// CrossOver
			int parent1 = (int) (- Math.log(random.nextDouble()) * this.popIlha) % this.popIlha;
			int parent2 = (int) (- Math.log(random.nextDouble()) * this.popIlha) % this.popIlha;

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

	private void generatePopulation() {

		for (int i =  0; i < this.popIlha; i++)
			this.population[i] = new ExpressionTree(this.variables);
	}

	/**
	 * Sends an individual to the current island
	 */
	public void sendExpression(ExpressionTree expression) {

		this.expressionsBuffer.add(expression);	
	}

}
