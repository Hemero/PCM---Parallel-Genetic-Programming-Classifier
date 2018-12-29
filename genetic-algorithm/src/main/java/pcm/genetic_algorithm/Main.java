package pcm.genetic_algorithm;

import abstractsyntaxtree.ExpressionTree;
import net.objecthunter.exp4j.Expression;

public class Main {

	public static void main( String[] args ) {
		
		String[] variables = {"x", "y", "z"};
		
		ExpressionTree tree = new ExpressionTree(variables);
		
		tree.generateTree();
		
		ExpressionTree tree2 = tree.clone();
		
		System.out.println(tree);
		System.out.println(tree2);
		
		Expression express = tree.getExpression();
		express.setVariable("x", 1.0);
		express.setVariable("y", 1.0);
		express.setVariable("z", 1.0);
		
		System.out.println(express.evaluate());
	}
}
