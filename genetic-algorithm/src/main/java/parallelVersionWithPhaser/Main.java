package parallelVersionWithPhaser;

import utils.LoadData;

public class Main {
	
	public static void main(String[] args) {

		LoadData loadData = new LoadData("../students/CompleteStudentsPerformance.csv");
		// LoadData loadData = new LoadData("../students/StudentsPerformance.csv");
		
		double[][] data = loadData.getData();
		double[] dataOutput = loadData.getDataOutput();
		
		String[] variables = loadData.getVariables();
	
		ClassifierGA classificador = new ClassifierGA(data, dataOutput, variables);
		classificador.startClassification();
	}
}
