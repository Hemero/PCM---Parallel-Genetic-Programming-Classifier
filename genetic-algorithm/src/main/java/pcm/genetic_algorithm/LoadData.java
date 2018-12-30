package pcm.genetic_algorithm;

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

		try (CSVReader reader = new CSVReader(new FileReader(fileName));) {
			
			// Obter todas as linhas
			List<String[]> entries = reader.readAll();
			
			double[][] dadosTotaisFormatados = formatarEntradas(entries);
			
			// Quantidade de colunas e de linhas
			int qtdColunas = dadosTotaisFormatados[0].length;
			int qtdLinhas = dadosTotaisFormatados.length;
			
			this.data = getData(dadosTotaisFormatados, qtdLinhas, qtdColunas);
			this.dataOutput = getDataOutput(dadosTotaisFormatados, qtdLinhas, qtdColunas);
			this.classes = getClasses(dadosTotaisFormatados, qtdColunas, qtdLinhas);
			this.variables = getVariables(entries, qtdColunas);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	private double[][] formatarEntradas(List<String[]> entries) {
		
		// Quantidade de colunas e de linhas
		int qtdColunas = entries.get(0)[0].split(";").length;
		int qtdLinhas = entries.size() - 1;
		
		double[][] resultado = new double[qtdLinhas][qtdColunas];
		
		for (int i = 1; i < qtdLinhas; i++) {
			
			String[] temp = entries.get(i)[0].split(";");
			
			for (int j = 0; j < qtdColunas; j++)
				resultado[i][j] = Double.parseDouble(temp[j]);
		}
		
		return resultado;
	}


	private double[] getClasses(double[][] dadosTotaisFormatados, int qtdColunas, int qtdLinhas) {
		
		List<Double> classesTemp = new ArrayList<>();
		
		Double classe;
		
		for (int i = 1; i < qtdLinhas; i++) {
			
			classe = dadosTotaisFormatados[i][qtdColunas - 1];
			
			if (!classesTemp.contains(classe))
				classesTemp.add(classe);
		}
		
		
		double[] resultado = new double[classesTemp.size()];
		
		for (int i = 0; i < resultado.length; i++)
			resultado[i] = classesTemp.get(i);
		
		return resultado;
	}


	private String[] getVariables(List<String[]> entries, int qtdColunas) {
	
		String[] resultado = new String[qtdColunas - 1];
		
		System.arraycopy(entries.get(0)[0].split(";"), 0, resultado, 0, qtdColunas - 1);
								
		return resultado;
	}

	private double[] getDataOutput(double[][] dadosTotaisFormatados, int qtdLinhas, int qtdColunas) {
		
		double[] resultado = new double[qtdLinhas - 1];
		
		// populate output
		for (int i = 1; i < qtdLinhas - 1; i++)
			resultado[i] = dadosTotaisFormatados[i][qtdColunas - 1];
		
		return resultado;
	}

	private double[][] getData(double[][] dadosTotaisFormatados, int qtdLinhas, int qtdColunas) {

		double[][] resultado = new double[qtdLinhas - 1][qtdColunas - 1];
		
		// populate data
		for (int i = 1; i < qtdLinhas; i++)
			for (int j = 0; j < (qtdColunas - 1); j++)
				resultado[i - 1][j] = dadosTotaisFormatados[i][j];
		
		return resultado;
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
