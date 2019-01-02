package parallelVersionWithFJ;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVReader;

public class LoadData {

	// Atributos
	private double[][] data;
	private double[] classes;
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
		List<Double> dataOutputTemp = new ArrayList<>();
		List<Double> classesTemp = new ArrayList<>();
		
		try (CSVReader reader = new CSVReader(new FileReader(fileName));) {
			
			// Ler a primeira linha que correspondem as variaveis
			String[] linha = reader.readNext()[0].split(";");
			
			this.variables = new String[linha.length - 1];
			System.arraycopy(linha, 0, this.variables, 0, linha.length - 1);
			
			while((linha = reader.readNext()) != null) {
				
				linha = linha[0].split(";");
				
				// ------------------ Adicionar ao data ------------------ 
				Double[] dataSemClasses = new Double[linha.length - 1];
				
				for (int i = 0; i < dataSemClasses.length; i++)
					dataSemClasses[i] = Double.parseDouble(linha[i]);
				
				dataTemp.add(dataSemClasses);
				
				// ------------------ Adicionar ao dataOutput ------------------ 
				double classeDaLinha = Double.parseDouble(linha[linha.length - 1]);
				
				dataOutputTemp.add(classeDaLinha);
				
				// ------------------ Adicionar as classes ------------------ 
				if (!classesTemp.contains(classeDaLinha))
					classesTemp.add(classeDaLinha);
			}
			
			this.data = new double[dataTemp.size()][dataTemp.get(0).length];
			
			for (int i = 0; i < this.data.length; i++)
				this.data[i] = Arrays.stream(dataTemp.get(i)).mapToDouble(Double::doubleValue).toArray();
			
			this.dataOutput = dataOutputTemp.stream().mapToDouble(i -> i).toArray();
			this.classes = classesTemp.stream().mapToDouble(i -> i).toArray();
		} 
		
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Getters
	public double[] getDataOutput() {
		return dataOutput;
	}

	public double[] getClasses() {
		return classes;
	}

	public double[][] getData() {
		return data;
	}

	public String[] getVariables() {
		return variables;
	}
}
