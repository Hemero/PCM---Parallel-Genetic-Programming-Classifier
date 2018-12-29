package abstractsyntaxtree;

import java.util.Random;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * Immutable class representative of an expressions tree. Each node of this tree
 * may contain a value, a variable or a binary operator.
 */
public class ExpressionTree implements ExpressionTreeInterface {

	/**
	 * Auxiliary inner class which represents a node of the tree.
	 */
	private abstract class TreeNode implements Cloneable {
		
		// Each node contains its own size
		protected int treeSize;
		
		// Does nothing
		public abstract TreeNode clone();
	}

	/**
	 * Represents a node of the tree which is a constant attribute, either
	 * a value or a variable
	 */
	private class ConstantTreeNode extends TreeNode {

		// Attributes
		private String constant;
		
		public ConstantTreeNode(String constant) {

			super();
			this.constant = constant;
		}

		@Override
		public ConstantTreeNode clone() {

			return new ConstantTreeNode(constant);
		}
	}

	/**
	 * Represents a node of the tree which is a binary operator
	 */
	private class BinaryOperatorTreeNode extends TreeNode {

		// Attributes
		private ExpressionBinaryOperator operator;

		private TreeNode left;
		private TreeNode right;

		public BinaryOperatorTreeNode(ExpressionBinaryOperator operator) {

			super();
			this.operator = operator;
		}

		@Override
		public BinaryOperatorTreeNode clone() {
			
			BinaryOperatorTreeNode result = new BinaryOperatorTreeNode(operator);
			result.treeSize = this.treeSize;
			
			return result;
		}
	}

	// Constants
	private static final int TREE_DEPTH = 10;
	private static final double CONST_NODE_GEN = 0.5;
	private static double CONST_VAR_GEN;
	
	private static final int UPPER_BOUND = Integer.MAX_VALUE;

	// ExpressionTree Attributes
	private TreeNode root;
	
	private Random rand;
	private int fitness;
	private String[] variables;

	/**
	 * Constructor of the current Expression tree
	 * @param variables variables required which represent this expresion tree
	 * @requires variables != null
	 */
	public ExpressionTree(String[] variables) {

		this.root = null;

		this.variables = variables;
		CONST_VAR_GEN = 1.0 / variables.length;
		
		rand = new Random();
	}

	/**
	 * @see ExpressionTreeInterface#generateTree()
	 */
	@Override
	public void generateTree() {
		
		generateRoot(0);
		calculateTreeSizes(this.root);
	}

	private int calculateTreeSizes(TreeNode current) {
		
		// If it is a leaf node
		if (current instanceof ConstantTreeNode)
			current.treeSize = 1;
		
		// If it is a Binary Operator Node
		else {
			
			BinaryOperatorTreeNode binaryTreeNode = (BinaryOperatorTreeNode) current;
			
			current.treeSize = calculateTreeSizes(binaryTreeNode.left) + 
							   calculateTreeSizes(binaryTreeNode.right) + 1;
		}
		
		return current.treeSize;
	}
	
	/**
	 * Generate the root
	 * @param depth 
	 */
	private void generateRoot(int depth) {
		// Node can be either constant or binary op
		double prob = rand.nextDouble();

		// if prob is less than the constant, generate a binary operator
		if (prob < CONST_NODE_GEN) {
			
			root = new BinaryOperatorTreeNode(ExpressionBinaryOperator.randomBinaryOperator());
			
			depth++;
			
			generateNode(depth, root, true);
			generateNode(depth, root, false);
		} else {
			
			this.root = auxiliaryGenerateConstantTreeNode();
		}
	}

	/**
	 * Generates a node that can be constant or binary operator
	 * @param depth
	 * @param currentNode - parent of the node to be generated
	 * @param isLeft - true if it will be left child
	 */
	private void generateNode(int depth, TreeNode currentNode, boolean isLeft) {
		if (depth < TREE_DEPTH) {

			// Node can be either constant or binary op
			double prob = rand.nextDouble();

			if (prob < CONST_NODE_GEN) {
				
				// node will be a binary operator
				generateBinaryOperatorTreeNode(currentNode, isLeft);
				depth++;
				
				// children must be generated
				if (isLeft) {
					generateNode(depth,((BinaryOperatorTreeNode)currentNode).left, true);
					generateNode(depth,((BinaryOperatorTreeNode)currentNode).left, false);
				} else {
					generateNode(depth,((BinaryOperatorTreeNode)currentNode).right, true);
					generateNode(depth,((BinaryOperatorTreeNode)currentNode).right, false);
				}
			} else {
				
				// node will be constant and will have no children
				generateConstantTreeNode(currentNode, isLeft);
			}

		} else {
			// if max depth is reached node has to be constant
			generateConstantTreeNode(currentNode, isLeft);
		}
	}


	/**
	 * Generates a child of currentNode as a constant tree node 
	 * @param currentNode - parent of the node to be generated
	 * @param isLeft - true if it will be left child
	 */
	private void generateConstantTreeNode(TreeNode currentNode, boolean isLeft) {
		
		if (isLeft) {
			((BinaryOperatorTreeNode)currentNode).left = 
					auxiliaryGenerateConstantTreeNode();
		} else {
			((BinaryOperatorTreeNode)currentNode).right = 
					auxiliaryGenerateConstantTreeNode();
		}
	}
	
	/**
	 * Auxiliary method that decides if node will have a variable or integer value
	 * @return a Constant Tree Node
	 */
	private ConstantTreeNode auxiliaryGenerateConstantTreeNode() {
		ConstantTreeNode node;
		
		// if not generate a constant
		double prob = rand.nextDouble();

		if (prob < CONST_VAR_GEN) {
			// root will be one of the variable names
			node = new ConstantTreeNode(generateVariableName());
		} else {
			// root will be an int
			node = new ConstantTreeNode(generateValue());
		}
		
		return node;
	}
	
	/**
	 * Chooses one of the variables according to the prob value
	 * @param prob - probability value between 0 and 1
	 * @return one of the variable names
	 */
	private String generateVariableName() {
		int i = rand.nextInt(variables.length);
		return variables[i];
	}

	/**
	 * Generates a random integer between -UPPER_BOUND and UPPER_BOUND
	 * @return a String representation of the generated value
	 */
	private String generateValue() {
		return String.valueOf(rand.nextInt(UPPER_BOUND) - rand.nextInt(UPPER_BOUND));
	}

	/**
	 * 
	 * @param currentNode - parent of the node to be generated
	 * @param isLeft - true if it will be left child
	 */
	private void generateBinaryOperatorTreeNode(TreeNode currentNode, boolean isLeft) {
		if (isLeft) {
			((BinaryOperatorTreeNode)currentNode).left = 
					new BinaryOperatorTreeNode(ExpressionBinaryOperator.randomBinaryOperator());
		} else {
			((BinaryOperatorTreeNode)currentNode).right = 
					new BinaryOperatorTreeNode(ExpressionBinaryOperator.randomBinaryOperator());
		}
	}
	
	/**
	 * @see ExpressionTreeInterface#crossOver(ExpressionTree)
	 */
	@Override
	public ExpressionTree crossOverWith(ExpressionTree other) {

		ExpressionTree result = this.clone();
		
		TreeNode otherSubTree = otherGetRandomSubTree(other.root, other.root.treeSize);

		startCrossOver(result, otherSubTree);
		
		return result;
	}

	private void startCrossOver(ExpressionTree result, TreeNode otherSubTree) {
		
		// If it is a Leaf node then it is chosen
		if (result.root instanceof ConstantTreeNode)
			result.root = otherSubTree;
		
		else {
		
			double probTreeNode = 1.0 / result.root.treeSize;
			double probEscolhido = this.rand.nextDouble();
			
			if (probEscolhido < probTreeNode)
				result.root = otherSubTree;
				
			else
				makeCrossOver((BinaryOperatorTreeNode) result.root, otherSubTree);
		}
	}
	
	private void makeCrossOver(BinaryOperatorTreeNode resultTreeNode, TreeNode otherSubTree) {
		
		
	}

	private TreeNode otherGetRandomSubTree(TreeNode otherTreeNode, int treeSize) {
		
		// If it isn't a leaf node
		if (!(otherTreeNode instanceof ConstantTreeNode)) {

			//TODO:
		}	
		
		return otherTreeNode;
	}

	/**
	 * @see ExpressionTreeInterface#mutate()
	 */
	@Override
	public ExpressionTree mutate() {

		ExpressionTree result = this.clone();
		
		int mutationPoint = rand.nextInt(this.root.treeSize);
		
		TreeNode nodeToMutate = findNode(mutationPoint, this.root);
		
		if (nodeToMutate != null) {
			
			// generate a binary op node
			if (nodeToMutate instanceof BinaryOperatorTreeNode) {
				
				nodeToMutate = new BinaryOperatorTreeNode(ExpressionBinaryOperator.randomBinaryOperator());
			} else {
				
				nodeToMutate = auxiliaryGenerateConstantTreeNode();
			}
		}
		
		return result;
	}

	/**
	 * Find node which has tree
	 * @param value
	 * @param node
	 * @return
	 */
	private TreeNode findNode(int value, TreeNode node) {
		TreeNode res = null;
		
		if (value == node.treeSize) {
			
			res = node;
		} else {
			
			BinaryOperatorTreeNode binNode = (BinaryOperatorTreeNode)node;
			
			// go to the subtree where treeSize is >= value
			if (binNode.left.treeSize >= value) {
				findNode(value - binNode.left.treeSize - 1, binNode.left);
			} else {
				findNode(value - binNode.right.treeSize - 1, binNode.right);
			}
		}
		
		return res;
	}

	@Override
	public void setFitness(double fitness) {
		// TODO Auto-generated method stub	
	}

	@Override
	public double getFitness() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * @see ExpressionTreeInterface#getExpression()
	 */
	@Override
	public Expression getExpression() {

		Expression expression = new ExpressionBuilder(this.toString())
				.variables(this.variables)
				.build();

		return expression;
	}

	/**
	 * Auxiliary method to gather all the expression attributes in the tree and
	 * returns an expression
	 * @param current current tree node
	 * @param sb accumulates the expression attributes
	 * @requires sb != null
	 */
	private void getAuxiliaryExpression(TreeNode current, StringBuilder sb) {

		// If there is no current node, return
		if (current == null)
			return;

		// If it is a constant attribute, variable or value
		if (current instanceof ConstantTreeNode) {

			ConstantTreeNode constantTreeNode = (ConstantTreeNode) current;
			sb.append(constantTreeNode.constant);
		}

		// If it is a binary operator attribute
		else if (current instanceof BinaryOperatorTreeNode) {

			BinaryOperatorTreeNode binOperatorTreeNode = (BinaryOperatorTreeNode) current;

			sb.append("(");

			getAuxiliaryExpression(binOperatorTreeNode.left, sb);
			sb.append(" " + binOperatorTreeNode.operator.getOperator() + " ");
			getAuxiliaryExpression(binOperatorTreeNode.right, sb);

			sb.append(")");
		}
	}

	/**
	 * @see ExpressionTreeInterface#toString()
	 */
	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		getAuxiliaryExpression(root, sb);

		return sb.toString();
	}

	/**
	 * A clone of the current tree
	 */
	@Override
	public ExpressionTree clone() {

		ExpressionTree result = new ExpressionTree(this.variables);

		if (this.root != null) {

			result.root = this.root.clone();

			if (this.root instanceof BinaryOperatorTreeNode)
				auxiliaryClone((BinaryOperatorTreeNode) result.root, (BinaryOperatorTreeNode) this.root);
		}

		return result;
	}


	private void auxiliaryClone(BinaryOperatorTreeNode currentNodeResult, BinaryOperatorTreeNode currentNodeThis) {

		// Check if left side exists
		if (currentNodeThis.left != null) {
			// Copy the left side
			currentNodeResult.left = currentNodeThis.left.clone();

			// Check if there are more values on the left to copy
			if (currentNodeThis.left instanceof BinaryOperatorTreeNode) 
				auxiliaryClone((BinaryOperatorTreeNode) currentNodeResult.left, (BinaryOperatorTreeNode) currentNodeThis.left);
		}

		// Check if right side exists
		if (currentNodeThis.right != null) {
			// Copy the right side
			currentNodeResult.right = currentNodeThis.right.clone();

			// Check if there are more values on the right to copy
			if (currentNodeThis.right instanceof BinaryOperatorTreeNode) 
				auxiliaryClone((BinaryOperatorTreeNode) currentNodeResult.right, (BinaryOperatorTreeNode) currentNodeThis.right);
		}
	}
	

	/**
	 * Compares if the current tree has a better fitness than the other tree
	 * @return smaller than 0 if it has a smaller fitness, 0 if it has the same 
	 * fitness, greater than 0 if it has a higher fitness.
	 */
	@Override
	public int compareTo(ExpressionTreeInterface other) {
		
		int resultado = 0;
		
		if (this.fitness < other.getFitness())
			resultado = -1;
		
		else if (this.fitness > other.getFitness())
			resultado = 1;
		
		return resultado;
	}
}
