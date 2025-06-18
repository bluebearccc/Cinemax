package com.bluebear.cinemax.config;

import com.bluebear.cinemax.dto.Detail_FDDTO;
import com.bluebear.cinemax.entity.Detail_FD;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ExcelGeneratoForDetailItemSold {

    private List <Detail_FDDTO> listFD;
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private CellStyle dateCellStyle;

    public ExcelGeneratoForDetailItemSold(List <Detail_FDDTO> listFD) {
        this.listFD = listFD;
        workbook = new XSSFWorkbook();

        // Create date cell style with dd/MM/yyyy format
        dateCellStyle = workbook.createCellStyle();
        short dateFormat = workbook.createDataFormat().getFormat("dd/MM/yyyy");
        dateCellStyle.setDataFormat(dateFormat);
    }
    private void writeHeader() {
        sheet = workbook.createSheet("Detail_Item_Sold");
        Row row = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);
        createCell(row, 0, "InvoiceID", style);
        createCell(row, 1, "Item_Name", style);
        createCell(row, 2, "Quantity_Sold", style);
        createCell(row, 3, "Date_Sold", style);
        createCell(row, 4, "Total_Price", style);
    }
    private void createCell(Row row, int columnCount, Object valueOfCell, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (valueOfCell instanceof Integer) {
            cell.setCellValue((Integer) valueOfCell);
        } else if (valueOfCell instanceof Long) {
            cell.setCellValue((Long) valueOfCell);
        } else if (valueOfCell instanceof String) {
            cell.setCellValue((String) valueOfCell);
        } else if (valueOfCell instanceof Date) {
            cell.setCellValue((Date) valueOfCell);
            // Create a new cell style that combines date format with the original style's font
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.cloneStyleFrom(style);
            dateStyle.setDataFormat(dateCellStyle.getDataFormat());
            cell.setCellStyle(dateStyle);
            return; // Return early as we've already set the style
        } else if (valueOfCell instanceof Boolean) {
            cell.setCellValue((Boolean) valueOfCell);
        } else if (valueOfCell instanceof Float) {
            cell.setCellValue((Float) valueOfCell);
        } else if (valueOfCell instanceof Double) {
            cell.setCellValue((Double) valueOfCell);
        } else if (valueOfCell != null) {
            cell.setCellValue(valueOfCell.toString());
        } else {
            cell.setCellValue("");
        }
        cell.setCellStyle(style);
    }
    private void write() {
        int rowCount = 1;
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);
        for (Detail_FDDTO record: listFD) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, record.getInvoiceId(), style);
            createCell(row, columnCount++, record.getItemName(), style);
            createCell(row, columnCount++, record.getQuantity(), style);
            createCell(row, columnCount++, record.getBookingDate(), style);
            createCell(row, columnCount++, record.getTotalPrice(), style);
        }
    }
    public void generateExcelFile(HttpServletResponse response) throws IOException {
        writeHeader();
        write();
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}
