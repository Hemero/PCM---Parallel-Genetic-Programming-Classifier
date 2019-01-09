package main;

import java.util.Arrays;

import abstractsyntaxtree.ExpressionTree;
import utils.LoadData;
import utils.TestsetHandler;

public class Main {
	
	public static void main(String[] args) {

		LoadData loadData = new LoadData("../toxicity/ld50.txt");
		// LoadData loadData = new LoadData("../students/StudentsPerformance.csv");

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
		// parallelVersionWithPhaser.ClassifierGA classificador = new parallelVersionWithPhaser.ClassifierGA(data, dataOutput, variables);
		
		// Versao paralela de Ilhas
		 parallelVersionIslands.ClassifierGA classificador = new parallelVersionIslands.ClassifierGA(data, dataOutput, variables);

//		TestsetHandler testHandler = new TestsetHandler(test, testOutput, new ExpressionTree(variables), variables);
//		System.out.println(testHandler.getError());
		
		classificador.startClassification();
	}
}
