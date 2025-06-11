package com.agora.facerecovery.utils;

import com.agora.facerecovery.model.ResultRow;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelExporter {

    public static void exportToExcel(List<ResultRow> resultRows, File file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Results");

            String[] headers = {"Файл", "GAN Score", "GAN Время", "MTCNN Score", "MTCNN Время", "Ошибка"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            for (int i = 0; i < resultRows.size(); i++) {
                ResultRow row = resultRows.get(i);
                Row r = sheet.createRow(i + 1);
                r.createCell(0).setCellValue(row.getFileName());
                r.createCell(1).setCellValue(row.getGanScore());
                r.createCell(2).setCellValue(row.getGanTime());
                r.createCell(3).setCellValue(row.getMtcnnScore());
                r.createCell(4).setCellValue(row.getMtcnnTime());
                r.createCell(5).setCellValue(row.getError());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream out = new FileOutputStream(file)) {
                workbook.write(out);
            }
        }
    }
}
