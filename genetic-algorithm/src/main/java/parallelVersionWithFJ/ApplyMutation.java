package parallelVersionWithFJ;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveAction;

import abstractsyntaxtree.ExpressionTree;
import net.objecthunter.exp4j.Expression;

public class ApplyMutation extends RecursiveAction {

	private static final int LIMIT = 2;
	private static final double MUTATION_RATE = 0.1;
	
	private ExpressionTree[] population;
	private ExpressionTree[] newPopulation;
	private String[] variables;
	private int beg;
	private int end;
	
	private Random random;
	
	public ApplyMutation(ExpressionTree[] population, ExpressionTree[] newPopulation, String[] variables, int beg, int end) {
		
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
				
				if (random.nextDouble() < MUTATION_RATE) {
					newPopulation[i].mutate();
				}			
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