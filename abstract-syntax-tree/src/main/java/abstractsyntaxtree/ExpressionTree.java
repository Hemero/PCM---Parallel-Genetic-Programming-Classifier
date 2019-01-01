package abstractsyntaxtree;

import java.util.concurrent.ThreadLocalRandom;

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
	
	private static final int UPPER_BOUND = 100;

	// ExpressionTree Attributes
	private TreeNode root;
	
	private ThreadLocalRandom rand;
	
	private double fitness;
	private double constVarGen;
	private String[] variables;

	/**
	 * Constructor of the current Expression tree
	 * @param variables variables required which represent this expresion tree
	 * @requires variables != null
	 */
	public ExpressionTree(String[] variables) {

		this.root = null;

		this.variables = variables;
		constVarGen = 1.0 / variables.length;
		
		this.rand = ThreadLocalRandom.current();
		this.generateTree();
	}
	
	/**
	 * Private constructor for clone
	 */
	private ExpressionTree() {
		
		this.root = null;
		this.rand = ThreadLocalRandom.current();
	}

	// ########################################################################
	// ############################ Tree Generation ###########################
	
	/**
	 * Randomly generates this expression tree
	 */
	private void generateTree() {
		
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

		if (prob < constVarGen) {
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
	
	// ########################################################################
	// ############################### CrossOver ##############################
	
	/**
	 * @see ExpressionTreeInterface#crossOver(ExpressionTree)
	 */
	@Override
	public ExpressionTree crossOverWith(ExpressionTree other) {

		ExpressionTree result = this.clone();
		
		calculateTreeSizes(result.root);
		
		int randomIndexOther = this.rand.nextInt(other.root.treeSize);
		TreeNode treeNodeOther = this.findNode(other.root, randomIndexOther);

		if (result.root.treeSize == 1)
			result.root = treeNodeOther;
		
		else
			applyCrossOver((BinaryOperatorTreeNode) result.root, treeNodeOther, result.root.treeSize, 0);
		
		calculateTreeSizes(result.root);
		
		return result;
	}
	
	private void applyCrossOver(BinaryOperatorTreeNode currentNode, TreeNode treeNodeOther, int originalSize, int depth) {
		
		// If father of two leaf constant tree nodes
		if ((currentNode.left instanceof ConstantTreeNode) && (currentNode.right instanceof ConstantTreeNode)) {
			
			boolean randomSon = this.rand.nextBoolean();
			
			// Choose left son
			if (randomSon)
				currentNode.left = treeNodeOther;
			
			// Choose right son
			else
				currentNode.right = treeNodeOther;
			
			checkDepth(currentNode, depth);
		}
		
		// If leaf node is a constant tree node
		else if (currentNode.left instanceof ConstantTreeNode) {
			
			double chanceOfLeft = 1.0 / originalSize;
			double chanceOfRight = chanceOfLeft + 1.0 / originalSize;
			double chosenDirection = this.rand.nextDouble();

			if (chosenDirection < chanceOfLeft) {
				currentNode.left = treeNodeOther;
				checkDepth(currentNode, depth);
			}
			
			else if (chosenDirection < chanceOfRight) {
				currentNode.right = treeNodeOther;
				checkDepth(currentNode, depth);
			}
			
			else
				applyCrossOver((BinaryOperatorTreeNode) currentNode.right, treeNodeOther, originalSize, depth + 1);
		}
		
		// If right node is a constant tree node
		else {

			double chanceOfRight = 1.0 / originalSize;
			double chanceOfLeft = chanceOfRight + 1.0 / originalSize;
			double chosenDirection = this.rand.nextDouble();

			if (chosenDirection < chanceOfRight) {
				currentNode.right = treeNodeOther;
				checkDepth(currentNode, depth);
			}
			
			else if (chosenDirection < chanceOfLeft) {
				currentNode.left = treeNodeOther;
				checkDepth(currentNode, depth);
			}
			
			else
				applyCrossOver((BinaryOperatorTreeNode) currentNode.left, treeNodeOther, originalSize, depth + 1);
		}
	}
	
	private void checkDepth(BinaryOperatorTreeNode treeNode, int depth) {
		
		if (depth < ExpressionTree.TREE_DEPTH - 1) {
			if (!(treeNode.left instanceof ConstantTreeNode))
				checkDepth((BinaryOperatorTreeNode) treeNode.left, depth + 1);
			
			if (!(treeNode.right instanceof ConstantTreeNode))
				checkDepth((BinaryOperatorTreeNode) treeNode.right, depth + 1);
		}
		
		else {
			// Prune the left node of the current tree
			if(!(treeNode.left instanceof ConstantTreeNode))	
				treeNode.left = auxiliaryGenerateConstantTreeNode();
			
			// Prune the right side of the current tree
			if (!(treeNode.right instanceof ConstantTreeNode))
				treeNode.right = auxiliaryGenerateConstantTreeNode();
		}
	}

	// ########################################################################
	// ################################ Mutation ##############################
	
	/**
	 * @see ExpressionTreeInterface#mutate()
	 */
	@Override
	public ExpressionTree mutate() {

		ExpressionTree result = this.clone();
		
		int mutationPoint = rand.nextInt(result.root.treeSize);
		
		TreeNode nodeToMutate = findNode(result.root, mutationPoint);
		
		if (nodeToMutate != null) {
			
			// generate a binary op node
			if (nodeToMutate instanceof BinaryOperatorTreeNode) {
				BinaryOperatorTreeNode treeNode = (BinaryOperatorTreeNode) nodeToMutate;
				treeNode.operator = ExpressionBinaryOperator.randomBinaryOperator();	
			} 
			
			else {	
				ConstantTreeNode treeNode = (ConstantTreeNode) nodeToMutate;
				
				// if not generate a constant
				double prob = rand.nextDouble();

				if (prob < constVarGen)
					// root will be one of the variable names
					treeNode.constant = generateVariableName();
				else 
					// root will be an int
					treeNode.constant = generateValue();
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
	private TreeNode findNode(TreeNode node, int indice) {
				
		if (node instanceof ConstantTreeNode) {
		
			ConstantTreeNode resultado = (ConstantTreeNode) node.clone();
			resultado.treeSize = 1;
			return resultado;
		}
		
		else {
			BinaryOperatorTreeNode treeNode = (BinaryOperatorTreeNode) node;
			
			int leftSize = treeNode.left.treeSize;
			
			if (indice < leftSize) 
				return findNode(treeNode.left, indice);
			
			else if (indice > leftSize)
				return findNode(treeNode.right, indice - leftSize - 1);
			
			else {
			
				BinaryOperatorTreeNode resultado = treeNode.clone();
				this.auxiliaryClone(resultado, treeNode);
				return resultado;
			}
		}
	}
	
	// ########################################################################
	// ############################ Get/Set Fitness ###########################
	
	@Override
	public void setFitness(double fitness) {
		this.fitness = fitness;	
	}

	@Override
	public double getFitness() {
		return this.fitness;
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

		ExpressionTree result = new ExpressionTree();
		result.variables = this.variables.clone();
		result.constVarGen = this.constVarGen;
		result.fitness = this.fitness;
		
		if (this.root != null) {

			result.root = this.root.clone();

			if (result.root instanceof BinaryOperatorTreeNode)
				auxiliaryClone((BinaryOperatorTreeNode) result.root, (BinaryOperatorTreeNode) this.root);
		
			result.calculateTreeSizes(result.root);
		}

		return result;
	}


	private void auxiliaryClone(BinaryOperatorTreeNode currentNodeResult, BinaryOperatorTreeNode currentNodeThis) {

		// Copy the left side
		currentNodeResult.left = currentNodeThis.left.clone();

		// Copy the right side
		currentNodeResult.right = currentNodeThis.right.clone();

		// Check if there are more values on the left to copy
		if (currentNodeThis.left instanceof BinaryOperatorTreeNode) 
			auxiliaryClone((BinaryOperatorTreeNode) currentNodeResult.left, (BinaryOperatorTreeNode) currentNodeThis.left);

		// Check if there are more values on the right to copy
		if (currentNodeThis.right instanceof BinaryOperatorTreeNode) 
			auxiliaryClone((BinaryOperatorTreeNode) currentNodeResult.right, (BinaryOperatorTreeNode) currentNodeThis.right);
	}
	

	/**
	 * Compares if the current tree has a better fitness than the other tree
	 * @return smaller than 0 if it has a smaller fitness, 0 if it has the same 
	 * fitness, greater than 0 if it has a higher fitness.
	 */
	@Override
	public int compareTo(ExpressionTreeInterface other) {
		
		int resultado = 0;
		
		if (this.fitness > other.getFitness())
			resultado = -1;
		
		else if (this.fitness < other.getFitness())
			resultado = 1;
		
		return resultado;
	}
}
