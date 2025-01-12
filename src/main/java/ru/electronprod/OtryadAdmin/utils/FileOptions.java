package ru.electronprod.OtryadAdmin.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Simple methods for interaction with text file.
 * 
 * @author Electron_prod
 */
public class FileOptions {
	/**
	 * Method reads data from file in UTF8
	 * 
	 * @param path - path to file
	 * @return List < String > lines or null, if path is incorrect
	 */
	public static List<String> getFileLines(String path) {
		try {
			List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
			return lines;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get data as a string from file
	 * 
	 * @param file - file to load
	 * @return String data
	 */
	public static String getFileLine(File file) {
		List<String> infile = getFileLines(file.getPath());
		String in = "";
		for (int i = 0; i < infile.size(); i++)
			in = String.valueOf(in) + (String) infile.get(i);
		return in;
	}

	/**
	 * Writes line to file in UTF8
	 * 
	 * @param line - String to write
	 * @param file - file to write
	 * @return result of operation (true-success, false-fail)
	 */
	public static boolean writeFile(String line, File file) {
		Writer fr;
		try {
			fr = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
			fr.write(line);
			fr.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Writes strings to File in UTF8
	 * 
	 * @param lines    - List to write
	 * @param filePath - path to file
	 * @throws IOException
	 */
	public static void writeLinesToFile(List<String> lines, String filePath) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
		for (String line : lines) {
			writer.write(line);
			writer.newLine();
		}
		writer.close();
	}

	/**
	 * @param toParse - String in JSON format
	 * @return Object with JSON included
	 * @throws ParseException
	 */
	public static Object ParseJS(String toParse) throws ParseException {
		Object obj = (new JSONParser()).parse(toParse);
		return obj;
	}

	public static String getFileLineWithSeparator(List<String> lines1, String splitter) {
		List<String> lines = lines1;
		if (lines == null) {
			return "Error loading file: null";
		}
		String result = "";
		for (int i = 0; i < lines.size(); i++) {
			result = result + lines.get(i) + splitter;
		}
		return result;
	}
}