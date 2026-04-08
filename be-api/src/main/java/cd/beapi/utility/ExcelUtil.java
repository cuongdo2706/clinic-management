package cd.beapi.utility;

import org.apache.poi.ss.usermodel.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ExcelUtil {

    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final ZoneId            ZONE         = ZoneId.of("Asia/Ho_Chi_Minh");

    private ExcelUtil() {}

    /** Tạo style cho hàng header: nền xanh đậm, chữ trắng, in đậm, căn giữa, có border */
    public static CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorderThin(style);
        return style;
    }

    /** Tạo style cho hàng dữ liệu: có border, wrap text */
    public static CellStyle createDataStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        setBorderThin(style);
        return style;
    }

    /** Tạo style cho hàng dữ liệu căn giữa */
    public static CellStyle createDataCenterStyle(Workbook wb) {
        CellStyle style = createDataStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /** Ghi header row từ danh sách tên cột */
    public static void writeHeader(Sheet sheet, CellStyle headerStyle, String... headers) {
        Row row = sheet.createRow(0);
        row.setHeightInPoints(20);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /** Auto-size tất cả cột */
    public static void autoSizeColumns(Sheet sheet, int numColumns) {
        for (int i = 0; i < numColumns; i++) {
            sheet.autoSizeColumn(i);
            // thêm padding nhỏ sau auto-size
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 512);
        }
    }

    /** Set giá trị cell — hỗ trợ String, Number, Boolean, LocalDate, Instant, null */
    public static void setCellValue(Cell cell, Object value) {
        switch (value) {
            case null                -> cell.setCellValue("");
            case String s            -> cell.setCellValue(s);
            case Number n            -> cell.setCellValue(n.doubleValue());
            case Boolean b           -> cell.setCellValue(b);
            case LocalDate ld        -> cell.setCellValue(ld.format(DATE_FMT));
            case Instant instant     -> cell.setCellValue(instant.atZone(ZONE).format(DATETIME_FMT));
            default                  -> cell.setCellValue(value.toString());
        }
    }

    private static void setBorderThin(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
