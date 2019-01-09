package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class LoadData {

	// Atributos
	private double[][] trainingData;
	private double[][] testData;
	private double[] testDataOutput;
	private double[] dataOutput;
	private String[] variables;

	/**
	 * Constructor
	 * @param fileName - String equals to csv file name
	 * @param classes - the possible classes
	 * @requires csv file must have name of columns in the firts row
	 * 			 values for the attributes must be doubles
	 * 			 classes must be a 2dim array
	 * 			 the last column of the csv must only have values from classes array
	 */
	public LoadData(String fileName) {

		List<Double[]> dataTemp = new ArrayList<>();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(fileName));) {
			
			String linha;
			String[] linhaSplit;
			
			while((linha = reader.readLine()) != null) {

				linhaSplit = linha.split(" +");
				
				// ------------------ Adicionar ao data ------------------ 
				Double[] data = new Double[linhaSplit.length - 1];
				
				for (int i = 1; i <= data.length; i++) {
					data[i - 1] = Double.parseDouble(linhaSplit[i]);
				}
				
				dataTemp.add(data);
			}
			
			// Shuffle do dataSet
			Collections.shuffle(dataTemp);
			
			// Split the data set size
			int trainingSize = (int) Math.floor(dataTemp.size() * 0.7);
			
			// Variables
			this.variables = new String[dataTemp.get(0).length];
			
			// Variables of training data
			this.trainingData = new double[trainingSize][dataTemp.get(0).length];
			this.dataOutput = new double[trainingSize];
			
			// Variables of test data
			this.testData = new double[dataTemp.size() - trainingSize][dataTemp.get(0).length];
			this.testDataOutput = new double[dataTemp.size() - trainingSize];
			
			// Set the training set and data training output set
			for (int i = 0; i < trainingSize; i++) {
				this.trainingData[i] = Arrays.stream(Arrays.copyOfRange(dataTemp.get(i), 0, dataTemp.get(i).length - 1)).mapToDouble(Double::doubleValue).toArray();
				this.dataOutput[i] = dataTemp.get(i)[dataTemp.get(i).length - 1];
			}
			
			// Set the test data and data test output set
			for (int i = trainingSize; i < dataTemp.size(); i++) {
				this.testData[i - trainingSize] = Arrays.stream(Arrays.copyOfRange(dataTemp.get(i), 0, dataTemp.get(i).length - 1)).mapToDouble(Double::doubleValue).toArray();
				this.testDataOutput[i - trainingSize] = dataTemp.get(i)[dataTemp.get(i).length - 1];
			}
			
			for (int i = 0; i < this.variables.length; i++)
				this.variables[i] = "x" + i;
		} 
		
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Getters for training data
	public double[] getDataOutput() {
		return dataOutput;
	}

	public double[][] getData() {
		return trainingData;
	}

	// Getters for test data
	public double[] getTestDataOutput() {
		return this.testDataOutput;
	}
	
	public double[][] getTestData() {
		return this.testData;
	}
	
	// Getter for variables
	public String[] getVariables() {
		return variables;
	}
}
