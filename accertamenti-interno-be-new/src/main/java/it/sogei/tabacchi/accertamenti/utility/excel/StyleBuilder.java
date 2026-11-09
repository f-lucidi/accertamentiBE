package it.sogei.tabacchi.accertamenti.utility.excel;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.apache.poi.ss.usermodel.*;

import java.util.Objects;

public class StyleBuilder {

    private final Workbook workbook;
    private final CellStyle style;
    private Font font;

    private StyleBuilder(Workbook workbook, CellStyle style) {
        this.workbook = workbook;
        this.style = style;
    }

    public static StyleBuilder from(Workbook workbook) {
        return new StyleBuilder(workbook, workbook.createCellStyle());
    }

    public static StyleBuilder from(Sheet sheet) {
        return from(sheet.getWorkbook());
    }

    public static StyleBuilder from(Row row) {
        return from(row.getSheet());
    }

    public static StyleBuilder from(Cell cell) {
        return from(cell.getSheet());
    }

    public StyleBuilder clone(CellStyle style) {
        this.style.cloneStyleFrom(style);
        return this;
    }

    public StyleBuilder horizontalAlignment(HorizontalAlignment alignment) {
        this.style.setAlignment(alignment);
        return this;
    }
    
    public StyleBuilder verticalAlignment(VerticalAlignment alignment) {
        this.style.setVerticalAlignment(alignment);
        return this;   
    }
    
    public StyleBuilder wrapText() {
        this.style.setWrapText(true);
        return this;  
    }

    @CanIgnoreReturnValue
    public StyleBuilder borderTop(BorderStyle borderStyle) {
        this.style.setBorderTop(borderStyle);
        return this;
    }

    @CanIgnoreReturnValue
    public StyleBuilder borderLeft(BorderStyle borderStyle) {
        this.style.setBorderLeft(borderStyle);
        return this;
    }

    @CanIgnoreReturnValue
    public StyleBuilder borderRight(BorderStyle borderStyle) {
        this.style.setBorderRight(borderStyle);
        return this;
    }

    @CanIgnoreReturnValue
    public StyleBuilder borderBottom(BorderStyle borderStyle) {
        this.style.setBorderBottom(borderStyle);
        return this;
    }

    public StyleBuilder border(BorderStyle borderStyle) {
        return border(borderStyle, true, true, true, true);
    }

    public StyleBuilder border(BorderStyle borderStyle, boolean top, boolean bottom, boolean left, boolean right) {
        if (top) this.borderTop(borderStyle);
        if (bottom) this.borderBottom(borderStyle);
        if (left) this.borderLeft(borderStyle);
        if (right) this.borderRight(borderStyle);

        return this;
    }

    public StyleBuilder fontBold(boolean bold) {
        this.getOrCreateFont().setBold(bold);
        return this;
    }

    public StyleBuilder background(Color color) {
        this.style.setFillForegroundColor(color);
        this.style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return this;
    }

    public StyleBuilder fontSize(int size) {
        this.getOrCreateFont().setFontHeightInPoints((short) size);
        return this;
    }

    private Font getOrCreateFont() {
        if (Objects.isNull(this.font)) {
            this.font = this.workbook.createFont();

            if(this.style.getFontIndex() != 0){
                Font copyFont = this.workbook.getFontAt(this.style.getFontIndex());
                font.setBold(copyFont.getBold());
                font.setFontHeightInPoints(copyFont.getFontHeightInPoints());
            }
            this.style.setFont(this.font);
        }
        return this.font;
    }

    public CellStyle build() {
        return this.style;
    }
}
