package main;


import java.util.Random;

import abstractsyntaxtree.ExpressionTree;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;

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
		
		 Random random = new Random();

		 int CROSS_OVER_VARIATION = 0;
		 int POPULATION_SIZE = 1000;
		 
		 // Propused New formula for crossOver
		 System.out.println((int) ((Math.abs(random.nextGaussian()) * ((POPULATION_SIZE / 3) + CROSS_OVER_VARIATION)) % POPULATION_SIZE));
		 
		 // Old formula for crossOver
		 System.out.println((int) ((-Math.log(random.nextDouble())) * POPULATION_SIZE) % POPULATION_SIZE);

		 // Comparing two formulas to check how similar they are
		 NormalizedLevenshtein  l = new NormalizedLevenshtein (); 
		 System.out.println(l.distance(tree2.toString(), crossed.toString()));
	}
}
