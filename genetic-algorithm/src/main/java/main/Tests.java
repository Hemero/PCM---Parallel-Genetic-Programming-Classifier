package main;

import abstractsyntaxtree.ExpressionTree;

public class Tests {

	public static void main(String[] args) {
		
		String[] vars = {"x", "y", "z"};
		
		ExpressionTree tree = new ExpressionTree(vars);
		ExpressionTree tree2 = new ExpressionTree(vars);

		ExpressionTree crossed = tree.crossOverWith(tree2);
		
		System.out.println("Tree 1: " + tree);
		System.out.println("Tree 2: " + tree2);		
		System.out.println("Crossed Tree: " + crossed);
		
		ExpressionTree crossedClone = crossed.clone();
		System.out.println("Cloned Tree: " + crossedClone);
		
		ExpressionTree mutatedTree = crossedClone.mutate();
		System.out.println("Mutated Tree: " + mutatedTree);
	}
}
