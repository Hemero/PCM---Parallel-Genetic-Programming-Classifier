package main;

import utils.LoadData;
import utils.TestsetHandler;
import abstractsyntaxtree.ExpressionTree;

/**
 * Main
 * @author Faculdade de Ciencias da Universidade de Lisboa - FC47806 FC49034
 */
public class Main {
	
	public static void main(String[] args) {
		
		LoadData loadData = new LoadData("toxicity/ld50.txt");

		// Variables
		String[] variables = loadData.getVariables();
		
		// Training Set
		double[][] data = loadData.getData();
		double[] dataOutput = loadData.getDataOutput();
		
		// Testset
		double[][] test = loadData.getTestData();
		double[] testOutput = loadData.getTestDataOutput();
		
		// Descomentar para escolher a versao desejada
		
		// Versao linear
		linearVersion.ClassifierGA classificador = new linearVersion.ClassifierGA( data, dataOutput, variables);
		// linearVersion.ClassifierGAAdaptative classificador = new linearVersion.ClassifierGAAdaptative(data, dataOutput, variables);
		
		// Versao paralela com Fork Join
		// parallelVersionWithFJ.ClassifierGA classificador = new parallelVersionWithFJ.ClassifierGA(data, dataOutput, variables);
		
		// Versao paralela com Phasers
		// parallelVersionWithPhaser.ClassifierGA classificador = new parallelVersionWithPhaser.ClassifierGA(data, dataOutput, variables);
		
		// Versao paralela de Ilhas
		// parallelVersionIslands.ClassifierGA classificador = new parallelVersionIslands.ClassifierGA(data, dataOutput, variables);

		classificador.startClassification();
	
		ExpressionTree bestIndividual = classificador.getBestIndividual();
		TestsetHandler testHandler = new TestsetHandler(test, testOutput, bestIndividual, variables);
		
		System.out.println("Best individual: " + bestIndividual);
		System.out.println("Test set error: " + testHandler.getError());
	}

}
