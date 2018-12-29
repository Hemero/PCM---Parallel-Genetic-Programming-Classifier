package pcm.genetic_algorithm;

import abstractsyntaxtree.ExpressionTree;

public class Example {

	public static void main(String[] args) {
		String[] vars = new String[4];
		vars[0] = "Paulo";
		vars[1] = "Maria";
		vars[2] = "André";
		vars[3] = "Mariana";
		ExpressionTree tree = new ExpressionTree(vars);
		tree.generateTree();
		System.out.println(tree);
	}

}
