package parallelVersionWithPhaser;

import abstractsyntaxtree.ExpressionTree;

public class Tests {

	public static void main(String[] args) {
		
		String[] vars = {"x", "y", "z"};
//		
//		ExpressionTree tree = new ExpressionTree(vars);
//		ExpressionTree tree2 = new ExpressionTree(vars);
//
//		ExpressionTree crossed = tree.crossOverWith(tree2);
//		
//		System.out.println("Tree 1: " + tree);
//		System.out.println("Tree 2: " + tree2);		
//		System.out.println("Crossed Tree: " + crossed);
//		
//		ExpressionTree crossedClone = crossed.clone();
//		System.out.println("Cloned Tree: " + crossedClone);
//		
//		// TODO: Not mutating
//		ExpressionTree mutatedTree = crossedClone.mutate();
//		System.out.println("Mutated Tree: " + mutatedTree);
		
		ExpressionTree tree1 = new ExpressionTree(vars);
		ExpressionTree clone;
		
		do {
			
			tree1 = new ExpressionTree(vars);
			clone = tree1.clone();
						
		} while (tree1.toString().equals(clone.toString()));
		
		System.out.println("####################");
		System.out.println(tree1);
		System.out.println(clone);
	}
}
