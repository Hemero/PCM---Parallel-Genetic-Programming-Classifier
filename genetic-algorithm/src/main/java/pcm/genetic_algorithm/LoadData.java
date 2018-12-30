package pcm.genetic_algorithm;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.opencsv.CSVReader;

public class LoadData {

	// Atributos
	private double[][] data;
	private double[] dataOutput;
	private String[] variables;
	private double[] classes;

	/**
	 * Constructor
	 * @param fileName - String equals to csv file name
	 * @param classes - the possible classes
	 * @requires csv file must have name of columns in the firts row
	 * 			 values for the attributes must be doubles
	 * 			 classes must be a 2dim array
	 * 			 the last column of the csv must only have values from classes array
	 */
	public LoadData(String fileName, double[] classes) {

		try {
			
			this.classes = classes;
			
			CSVReader reader = new CSVReader(new FileReader(fileName));
			List<String[]> myEntries = reader.readAll();
			
			int rowLength = myEntries.get(0).length;
			int numRows = myEntries.size() - 1;
			
			data = new double[numRows][rowLength];
			
			// populate data
			for (int i = 1; i < numRows; i++) {
				for (int j = 0; j < (rowLength-1); j++) {
					data[i][j] = Double.parseDouble(myEntries.get(i)[j]);
				}
			}
			
			// populate variables
			for (int j = 0; j < (rowLength-1); j++) {
				variables[j] = myEntries.get(0)[j];
			}
			
			// populate output
			for (int i = 1; i < numRows; i++) {
				dataOutput[i] = Double.parseDouble(myEntries.get(rowLength-1)[i]);
			}
			
			reader.close();
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	public double[] getDataOutput() {
		return dataOutput;
	}

	public void setDataOutput(double[] dataOutput) {
		this.dataOutput = dataOutput;
	}

	public double[] getClasses() {
		return classes;
	}

	public void setClasses(double[] classes) {
		this.classes = classes;
	}

	public double[][] getData() {
		return data;
	}

	public void setData(double[][] data) {
		this.data = data;
	}

	public String[] getVariables() {
		return variables;
	}

	public void setVariables(String[] variables) {
		this.variables = variables;
	}
}
