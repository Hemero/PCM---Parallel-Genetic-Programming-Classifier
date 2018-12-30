package pcm.genetic_algorithm;

import abstractsyntaxtree.ExpressionTree;

public class Tests {

	public static void main(String[] args) {
		
		String[] vars = {"x", "y", "z"};
		
		ExpressionTree tree = new ExpressionTree(vars);
		tree.generateTree();
		ExpressionTree tree2 = new ExpressionTree(vars);
		tree2.generateTree();

		System.out.println(tree);
		System.out.println(tree2);
		
		ExpressionTree crossed = tree.crossOverWith(tree2);
		
		System.out.println(crossed);
	}
}
