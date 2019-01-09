package main;


import java.util.Random;

import abstractsyntaxtree.ExpressionTree;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import net.objecthunter.exp4j.Expression;
import utils.LoadData;

public class Tests {

	public static void main(String[] args) {

		LoadData loadData = new LoadData("../toxicity/ld50.txt");
		
		String[] vars = loadData.getVariables();
		
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
		
		Expression asd = tree.getExpression();
		
		 Random random = new Random();

		 int CROSS_OVER_VARIATION = 0;
		 int AMOUNT_POPULATION = 1000;
		 
		 // Propused New formula for crossOver
		 System.out.println((int) Math.abs(((Math.abs(random.nextGaussian()) * (AMOUNT_POPULATION / 3.0) + CROSS_OVER_VARIATION) % AMOUNT_POPULATION)));
		 
		 // Old formula for crossOver
		 System.out.println((int) ((-Math.log(random.nextDouble())) * AMOUNT_POPULATION) % AMOUNT_POPULATION);

		 // Comparing two formulas to check how similar they are
		 NormalizedLevenshtein  l = new NormalizedLevenshtein (); 
		 System.out.println("% Difference: " + l.distance(tree.toString(), crossed.toString()));
	}
}
