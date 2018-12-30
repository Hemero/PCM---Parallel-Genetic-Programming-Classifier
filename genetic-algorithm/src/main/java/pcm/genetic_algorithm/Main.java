package pcm.genetic_algorithm;

public class Main {
	
	public static void main(String[] args) {
		
		// TODO:
		// load data
		// Start classifierGA
		// run classifierGA
		LoadData loadData = loadData("../students/StudentsPerformance.csv");
		
		double[][] data = loadData.getData();
		double[] classes = loadData.getClasses();
		double[] dataOutput = loadData.getDataOutput();
		
		String[] variables = loadData.getVariables();
	}

	private static LoadData loadData(String fileName) {
		
		return new LoadData(fileName);
	}
}
