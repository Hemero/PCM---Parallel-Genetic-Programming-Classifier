package parallelVersionWithFJ;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveAction;

import abstractsyntaxtree.ExpressionTree;

/**
 * @author Faculdade de Ciencias da Universidade de Lisboa - FC47806 FC49034
 */
public class GeneratePopulation extends RecursiveAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -409105094126970841L;

	private static final int LIMIT = 2;
	
	private ExpressionTree[] population;
	private String[] variables;
	private int beg;
	private int end;
	
	public GeneratePopulation(ExpressionTree[] population, String[] variables, int beg, int end) {
		
		this.population = population;
		this.variables = variables;
		this.beg = beg;
		this.end = end;
	}
	
	@Override
	protected void compute() {
		
		if (end-beg < LIMIT) {
			
			for (int i = beg; i < end; i++) {
				population[i] = new ExpressionTree(variables);
			}
		} else {

			int middle = (end+beg) / 2;
			
			GeneratePopulation action1 = new GeneratePopulation(population, variables, beg, middle);
			GeneratePopulation action2 = new GeneratePopulation(population, variables, middle, end);
			
			action1.fork();
			action2.compute();
			
			try {
				action1.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

}