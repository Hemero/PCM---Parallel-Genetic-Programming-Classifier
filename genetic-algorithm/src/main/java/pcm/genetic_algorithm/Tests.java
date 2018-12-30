package pcm.genetic_algorithm;

import abstractsyntaxtree.ExpressionTree;
import net.objecthunter.exp4j.Expression;

public class Tests {

	public static void main(String[] args) {
		
		String[] vars = {"x", "y", "z"};
		
		ExpressionTree tree = new ExpressionTree(vars);
		ExpressionTree tree2 = new ExpressionTree(vars);

		ExpressionTree crossed = tree.crossOverWith(tree2);
		
		System.out.println("Tree 1: " + tree);
		System.out.println("Tree 2: " + tree2);		
		System.out.println("Crossed Tree: " + crossed);
		
		Expression expression = crossed.getExpression();
		expression.setVariable("x", 10);
		expression.setVariable("y", 20);
		expression.setVariable("z", 30);
		
		System.out.println(expression.evaluate());
	}
}
