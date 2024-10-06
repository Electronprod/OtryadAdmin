package ru.electronprod.OtryadAdmin.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.SquadStats;

@Service
public class ExcelGenerationService {
	@Transactional(readOnly = true)
	public byte[] generateHuman(List<Human> data, String sheetName) throws IOException {
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet(sheetName);
		// Creating header
		Row headerRow = sheet.createRow(0);
		headerRow.createCell(0).setCellValue("Фамилия");
		headerRow.createCell(1).setCellValue("Имя");
		headerRow.createCell(2).setCellValue("Отчество");
		headerRow.createCell(3).setCellValue("Д. Р.");
		headerRow.createCell(4).setCellValue("Школа");
		headerRow.createCell(5).setCellValue("Класс");
		headerRow.createCell(6).setCellValue("Телефон");
		headerRow.createCell(7).setCellValue("Звено");
		// Filling values
		for (int i = 0; i < data.size(); i++) {
			Row dataRow = sheet.createRow(i + 1);
			Human hmn = data.get(i);
			dataRow.createCell(0).setCellValue(hmn.getLastname());
			dataRow.createCell(1).setCellValue(hmn.getName());
			dataRow.createCell(2).setCellValue(hmn.getSurname());
			dataRow.createCell(3).setCellValue(hmn.getBirthday());
			dataRow.createCell(4).setCellValue(hmn.getSchool());
			dataRow.createCell(5).setCellValue(hmn.getClassnum());
			dataRow.createCell(6).setCellValue(hmn.getPhone());
			dataRow.createCell(7).setCellValue(hmn.getSquad().getCommanderName());
		}
		// Запись в ByteArrayOutputStream
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		workbook.write(out);
		workbook.close();
		return out.toByteArray();
	}
//	@Transactional(readOnly = true)
//	public byte[] generateSquadStats(List<SquadStats> data, String sheetName) {
//		Workbook workbook = new XSSFWorkbook();
//		Sheet sheet = workbook.createSheet(sheetName);
//		// Creating header
//		Row headerRow = sheet.createRow(0);
//		headerRow.createCell(0).setCellValue("Фамилия");
//		headerRow.createCell(1).setCellValue("Имя");
//		headerRow.createCell(2).setCellValue("Дата");
//		headerRow.createCell(3).setCellValue("Пришел");
//
//		Row dataRow = sheet.createRow(1);
//		dataRow.createCell(0).setCellValue("Data1");
//		dataRow.createCell(1).setCellValue("Data2");
//
//		// Запись в ByteArrayOutputStream
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		workbook.write(out);
//		workbook.close();
//		byte[] bytes = out.toByteArray();
//	}
}
