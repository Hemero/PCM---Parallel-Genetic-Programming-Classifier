package parallelVersionWithFJ;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveAction;

import abstractsyntaxtree.ExpressionTree;

public class ApplyCrossOver extends RecursiveAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4715110914974747078L;

	private static final int LIMIT = 2;
	
	private ExpressionTree[] population;
	private ExpressionTree[] newPopulation;
	private String[] variables;
	private int beg;
	private int end;
	
	private Random random;
	
	public ApplyCrossOver(ExpressionTree[] population, ExpressionTree[] newPopulation, String[] variables, int beg, int end) {
		
		this.population = population;
		this.newPopulation = newPopulation;
		this.variables = variables;
		this.beg = beg;
		this.end = end;
		this.random = new Random();
	}
	
	@Override
	protected void compute() {
		
		int populationLength = this.population.length;
		
		if (end-beg < LIMIT) {
			
			for (int i = beg; i < end; i++) {
				// The first elements in the population have higher probability of being selected
				int parent1 = (int) (- Math.log(random.nextDouble()) * populationLength) % populationLength;
				int parent2 = (int) (- Math.log(random.nextDouble()) * populationLength) % populationLength;

				newPopulation[i] = this.population[parent1].crossOverWith(this.population[parent2]);
			}
		} else {

			int middle = (end+beg) / 2;
			
			ApplyCrossOver action1 = new ApplyCrossOver (population, newPopulation, variables, beg, middle);
			ApplyCrossOver action2 = new ApplyCrossOver(population, newPopulation, variables, middle, end);
			
			action1.fork();
			action2.compute();
			
			try {
				action1.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

}