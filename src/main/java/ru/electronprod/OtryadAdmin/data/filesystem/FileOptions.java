package ru.electronprod.OtryadAdmin.data.filesystem;

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
import java.util.concurrent.locks.LockSupport;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import lombok.extern.slf4j.Slf4j;

/**
 * Simple methods for interaction with file. Contains: file loader,file
 * writer,JSON parser
 * 
 * @author Electron
 */
@Slf4j
public class FileOptions {
	private static void log(String msg) {
		log.debug(msg);
	}

	private static void logerr(String msg) {
		log.error(msg);
	}

	/**
	 * Method provides reading line by line
	 * 
	 * @param path - path of file Note: you can get the path using
	 *             yourfile.getPath() function
	 * @return List<String> lines
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
	 * Method provides reading in one string
	 * 
	 * @param f - file to load
	 * @return String data
	 */
	public static String getFileLine(File f) {
		List<String> infile = getFileLines(f.getPath());
		String in = "";
		for (int i = 0; i < infile.size(); i++)
			in = String.valueOf(in) + (String) infile.get(i);
		return in;
	}

	/**
	 * Method provides write to file function
	 * 
	 * @param in - the string to be written
	 * @param f  - file to write
	 */
	public static void writeFile(String in, File f) {
		for (; !f.canWrite() && !f.canRead(); LockSupport.parkNanos(100L))
			;
		Writer fr = null;
		try {
			fr = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8);

			fr.write(in);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method checks file for exists. If file didn't found it creates.
	 * 
	 * @param f - File to check
	 */
	public static boolean loadFile(File f) {
		if (f.exists()) {
			if (f.canRead() && f.canWrite()) {
				log("[FileOptions]: File " + f.getName() + " loaded.");
			} else {
				logerr("[FileOptions]: File " + f.getName() + " can't be read or wrote.");
				logerr("[FileOptions]: Please, check for other launched copy of this program and for args of file "
						+ f.getName());
				System.exit(1);
			}
			return false;
		} else {
			log("[FileOptions]: File not found. Creating it...");
			try {
				f.createNewFile();
				log("[FileOptions]: File " + f.getName() + " created and loaded.");
			} catch (IOException e) {
				logerr(e.getLocalizedMessage());
				logerr("[FileOptions]: Please, create " + f.getName() + " yourself and try again. Bye.");
				System.exit(1);
			}
			return true;
		}
	}

	public static void writeLinesToFile(List<String> lines, String filePath) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"))) {
			for (String line : lines) {
				writer.write(line);
				writer.newLine(); // Добавляем перенос строки
			}
		} catch (IOException e) {
			e.printStackTrace(); // Логируем ошибку, если она возникла
			throw e; // Прокидываем исключение дальше
		}
	}

	/**
	 * JSON format checker & loader
	 * 
	 * @param d - string in JSON format
	 * @return Object JSON
	 */
	public static Object ParseJs(String d) {
		if (d == null) {
			logerr("Error loading JSON. Please, check configs. Program will exit. \nError message: input string = null");
			System.exit(1);
			return null;
		}
		try {
			Object obj = (new JSONParser()).parse(d);
			return obj;
		} catch (ParseException e) {
			logerr("Error loading JSON. Please, check configs. Program will exit. \nError message: " + e.getMessage());
			System.exit(1);
			return null;
		}
	}

	/**
	 * JSON format checker & loader
	 * 
	 * @param d - string in JSON format
	 * @return Object JSON
	 * @throws ParseException
	 */
	public static Object ParseJsThrought(String d) throws ParseException {
		Object obj = (new JSONParser()).parse(d);
		return obj;
	}
}