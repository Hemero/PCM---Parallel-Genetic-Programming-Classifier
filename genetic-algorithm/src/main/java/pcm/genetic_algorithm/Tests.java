package pcm.genetic_algorithm;

import abstractsyntaxtree.ExpressionTree;

public class Tests {

	public static void main(String[] args) {
		
		String[] vars = {"x", "y", "z"};
		
		ExpressionTree tree = new ExpressionTree(vars);
		tree.generateTree();
		ExpressionTree tree2 = new ExpressionTree(vars);
		tree2.generateTree();

		ExpressionTree crossed = tree.crossOverWith(tree2);
		
		System.out.println("Tree 1: " + tree);
		System.out.println("Tree 2: " + tree2);
		
		System.out.println("Crossed Tree: " + crossed);
	}
}
