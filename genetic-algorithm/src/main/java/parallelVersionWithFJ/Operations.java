package parallelVersionWithFJ;

import java.util.Random;
import java.util.concurrent.RecursiveAction;

import abstractsyntaxtree.ExpressionTree;
import net.objecthunter.exp4j.Expression;

public class Operations extends RecursiveAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -877576993390821093L;

	private static final int LIMIT = 2;
	private static final int TOP_AMOUNT_ELITES = 1;
	private static final double MUTATION_RATE = 0.1;
	
	private ExpressionTree[] population;
	private ExpressionTree[] newPopulation;
	private String[] variables;
	private double[][] data;
	private double[] dataOutput;
	
	private int beg;
	private int end;
	
	private int beginTrainingSet;
	private int endTrainingSet;
	
	private Random random;
	
	public Operations(ExpressionTree[] population, ExpressionTree[] newPopulation,
			double[][] data, double[] dataOutput, String[] variables, int beg, int end,
			int beginTrainingSet, int endTrainingSet) {
		
		this.population = population;
		this.newPopulation = newPopulation;
		this.variables = variables;
		this.data = data;
		this.dataOutput = dataOutput;
		this.beg = beg;
		this.end = end;
		this.beginTrainingSet = beginTrainingSet;
		this.endTrainingSet = endTrainingSet;
		
		this.random = new Random();
	}
	
	@Override
	protected void compute() {
		
		if (end-beg < LIMIT) {

			for (int i = beg; i < end; i++) {
				if (i >= TOP_AMOUNT_ELITES) {
					// CrossOver
					int parent1 = (int) (- Math.log(random.nextDouble()) * this.population.length) % this.population.length;
					int parent2 = (int) (- Math.log(random.nextDouble()) * this.population.length) % this.population.length;

					newPopulation[i] = this.population[parent1].crossOverWith(this.population[parent2]);

					// Mutacao
					if (random.nextDouble() < MUTATION_RATE) {
						newPopulation[i].mutate();
					}
				}

				// Calcular o Fitness
				measureFitness(newPopulation[i]);
				
			}
		} else {

			int middle = (end+beg) / 2;
		
			Operations action1 = 
					new Operations (population, newPopulation, data, dataOutput, variables, 
										beg, middle, beginTrainingSet, endTrainingSet);
			Operations action2 = 
					new Operations (population, newPopulation, data, dataOutput, variables, 
										middle, end, beginTrainingSet, endTrainingSet);
			
			action1.fork();
			action2.compute();
			action1.join();
		}
	}
	
	private double measureFitness(ExpressionTree tree) {
		
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
}