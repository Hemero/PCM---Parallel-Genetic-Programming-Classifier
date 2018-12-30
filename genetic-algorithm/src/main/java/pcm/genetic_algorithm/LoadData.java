package pcm.genetic_algorithm;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.opencsv.CSVReader;

public class LoadData {

	// Atributos
	private double[][] data;
	private String[] variables;

	/**
	 * Constructor
	 * @param fileName - String equals to csv file name
	 * @requires csv file must have name of columns in the firts row
	 * 			 target must be the last column
	 * 			 values must be doubles
	 */
	public LoadData(String fileName) {

		try {
			
			CSVReader reader = new CSVReader(new FileReader(fileName));
			List<String[]> myEntries = reader.readAll();
			
			int rowLength = myEntries.get(0).length;
			int numRows = myEntries.size() - 1;
			
			data = new double[numRows][rowLength];
			
			for (int i = 1; i < numRows; i++) {
				for (int j = 0; j < rowLength; j++) {
					data[i][j] = Double.parseDouble(myEntries.get(i)[j]);
				}
			}
			
			for (int j = 0; j < rowLength; j++) {
				variables[j] = myEntries.get(0)[j];
			}
			
			reader.close();
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
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
