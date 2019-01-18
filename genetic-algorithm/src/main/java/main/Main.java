package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import abstractsyntaxtree.ExpressionTree;
import utils.LoadData;
import utils.StopWatch;
import utils.TestsetHandler;

public class Main {
	
	private static final int AMOUNT_TESTS = 30;
	
	private static final String CHOOSEN_OUTPUT = "forkjoin";
	
	public static void main(String[] args) throws FileNotFoundException {
		
		LoadData loadData = new LoadData("toxicity/ld50.txt");
	
		new File(CHOOSEN_OUTPUT).mkdir();
		
		for (int i = 0; i < AMOUNT_TESTS; i++) {
			
			System.setOut(new PrintStream(CHOOSEN_OUTPUT + "/" + CHOOSEN_OUTPUT + i + ".csv"));
			// SYSO For non-island versions
			System.out.println("generation;fitness");
			// SYSO For island versions
			// System.out.println("generation;island;fitness");
			
			startClassification(i, loadData);
		}
	}
	
	private static void startClassification(int iteracao, LoadData loadData) throws FileNotFoundException {

		StopWatch contador = new StopWatch();
		contador.start();
		
		// Variables
		String[] variables = loadData.getVariables();
		
		// Training Set
		double[][] data = loadData.getData();
		double[] dataOutput = loadData.getDataOutput();
		
		// Testset
		double[][] test = loadData.getTestData();
		double[] testOutput = loadData.getTestDataOutput();
		
		
		// Versao linear
		// linearVersion.ClassifierGA classificador = new linearVersion.ClassifierGA(data, dataOutput, variables);
		// linearVersion.ClassifierGAAdaptative classificador = new linearVersion.ClassifierGAAdaptative(data, dataOutput, variables);
		
		// Versao paralela com Fork Join
		// parallelVersionWithFJ.ClassifierGA classificador = new parallelVersionWithFJ.ClassifierGA(data, dataOutput, variables);
		
		// Versao paralela com Phasers
		parallelVersionWithPhaser.ClassifierGA classificador = new parallelVersionWithPhaser.ClassifierGA(data, dataOutput, variables);
		
		// Versao paralela de Ilhas
		// parallelVersionIslands.ClassifierGA classificador = new parallelVersionIslands.ClassifierGA(data, dataOutput, variables);

		classificador.startClassification();
		contador.stop();
		
		ExpressionTree bestIndividual = classificador.getBestIndividual();
		
		TestsetHandler testHandler = new TestsetHandler(test, testOutput, bestIndividual, variables);
		
		System.out.flush();
		
		System.setOut(new PrintStream(CHOOSEN_OUTPUT + "/" + CHOOSEN_OUTPUT + iteracao + "info.csv"));
		
		System.out.println("Best individual;" + bestIndividual.toString());
		System.out.println("Duration;" + contador.getDuration());
		System.out.println("Testset error;" + testHandler.getError());
		
		System.out.flush();
	}
}
