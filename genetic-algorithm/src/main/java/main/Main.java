package main;

import utils.LoadData;

public class Main {
	
	public static void main(String[] args) {

		LoadData loadData = new LoadData("../students/CompleteStudentsPerformance.csv");
		// LoadData loadData = new LoadData("../students/StudentsPerformance.csv");
		
		double[][] data = loadData.getData();
		double[] dataOutput = loadData.getDataOutput();
		
		String[] variables = loadData.getVariables();
	
		// Versao linear
		linearVersion.ClassifierGA classificador = new linearVersion.ClassifierGA(data, dataOutput, variables);
		
		// Versao paralela com Fork Join
		// parallelVersionWithFJ.ClassifierGA classificador = new parallelVersionWithFJ.ClassifierGA(data, dataOutput, variables);
		
		// Versao paralela com Phasers
		// parallelVersionWithPhaser.ClassifierGA classificador = new parallelVersionWithPhaser.ClassifierGA(data, dataOutput, variables);
		
		// Versao paralela de Ilhas
		// parallelVersionIslands.ClassifierGA classificador = new parallelVersionIslands.ClassifierGA(data, dataOutput, variables);

		classificador.startClassification();
	}
}
