package abstractsyntaxtree;

import net.objecthunter.exp4j.Expression;

/**
 * Represents an expression tree interface
 */
public interface ExpressionTreeInterface extends Comparable<ExpressionTreeInterface>, Cloneable {
	
	/**
	 * Given an expression tree, crossovers with the current tree.
	 * @param other tree to be crossovered with the current one
	 * @return tree which results in the combination of both trees
	 */
	public ExpressionTree crossOverWith(ExpressionTree other);
	
	/**
	 * Randomly applies a mutation to a branch of the tree
	 * @return tree which results of the mutation of a branch of the tree
	 */
	public ExpressionTree mutate();

	/**
	 * Sets the fitness on the current tree
	 * @param fitness value of the fitness to be set
	 */
	public void setFitness(double fitness);
	
	/**
	 * Returns the fitness of the current tree
	 */
	public double getFitness();
	
	/**
	 * Returns the full expression from the current tree.
	 */
	public Expression getExpression();
	
	/**
	 * Returns a textual representation of the current expression tree 
	 */
	public String toString();
}
