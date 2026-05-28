package ru.hikeload.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;
import ru.hikeload.domain.AssignmentType;
import ru.hikeload.web.dto.LoadPlanExportResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Формирование PDF-отчёта по раскладке (OpenPDF + шрифт DejaVu из jasperreports-fonts).
 */
@Component
public class LoadPlanPdfExporter {

    private static final String FONT_RESOURCE = "/net/sf/jasperreports/fonts/dejavu/DejaVuSans.ttf";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final Map<String, String> TYPE_LABELS = Map.of(
            AssignmentType.GEAR_SHARED.name(), "Общее снаряжение",
            AssignmentType.GEAR_PERSONAL.name(), "Личное снаряжение",
            AssignmentType.FOOD.name(), "Питание"
    );

    public byte[] export(LoadPlanExportResponse data) {
        try {
            BaseFont baseFont = loadCyrillicFont();
            Font titleFont = new Font(baseFont, 16, Font.BOLD);
            Font headerFont = new Font(baseFont, 11, Font.BOLD);
            Font bodyFont = new Font(baseFont, 10, Font.NORMAL);
            Font smallFont = new Font(baseFont, 9, Font.NORMAL);

            Document document = new Document(PageSize.A4, 36, 36, 48, 36);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Раскладка в походе", titleFont));
            document.add(new Paragraph("Поход: " + data.hikeName(), bodyFont));
            document.add(new Paragraph(
                    "Экспорт: " + DATE_TIME.format(data.exportedAt()) + ", версия плана: " + data.planVersion(),
                    smallFont
            ));
            document.add(new Paragraph(" ", bodyFont));

            for (LoadPlanExportResponse.ParticipantExport participant : data.participants()) {
                document.add(new Paragraph(participant.name(), headerFont));
                String emailLine = participant.email() != null && !participant.email().isBlank()
                        ? "Email: " + participant.email()
                        : "Email: —";
                document.add(new Paragraph(emailLine, smallFont));

                double total = participant.totalWeightKg() != null ? participant.totalWeightKg() : 0;
                double max = participant.maxWeightKg() != null ? participant.maxWeightKg() : 0;
                String limitStatus = total <= max ? "в пределах лимита" : "превышен лимит";
                document.add(new Paragraph(
                        String.format("Итого: %.2f кг из %.2f кг (%s)", total, max, limitStatus),
                        bodyFont
                ));

                PdfPTable table = new PdfPTable(new float[]{3f, 2f, 1f});
                table.setWidthPercentage(100);
                table.setSpacingBefore(6);
                table.setSpacingAfter(12);

                addHeaderCell(table, "Предмет", headerFont);
                addHeaderCell(table, "Тип", headerFont);
                addHeaderCell(table, "кг", headerFont);

                for (LoadPlanExportResponse.ItemExport item : participant.items()) {
                    addBodyCell(table, item.name(), bodyFont);
                    addBodyCell(table, labelType(item.type()), bodyFont);
                    addBodyCell(table, formatWeight(item.weightKg()), bodyFont, Element.ALIGN_RIGHT);
                }

                if (participant.items().isEmpty()) {
                    PdfPCell empty = new PdfPCell(new Phrase("Нет назначений", smallFont));
                    empty.setColspan(3);
                    empty.setPadding(6);
                    table.addCell(empty);
                }

                document.add(table);
            }

            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new BusinessException("Не удалось сформировать PDF: " + e.getMessage());
        }
    }

    private BaseFont loadCyrillicFont() throws IOException, DocumentException {
        try (InputStream fontStream = getClass().getResourceAsStream(FONT_RESOURCE)) {
            if (fontStream == null) {
                throw new BusinessException("Шрифт для PDF не найден в classpath");
            }
            byte[] fontBytes = fontStream.readAllBytes();
            return BaseFont.createFont(
                    "DejaVuSans.ttf",
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED,
                    false,
                    fontBytes,
                    null
            );
        }
    }

    private static String labelType(String typeCode) {
        return TYPE_LABELS.getOrDefault(typeCode, typeCode);
    }

    private static String formatWeight(Double kg) {
        return kg != null ? String.format("%.2f", kg) : "—";
    }

    private static void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setBackgroundColor(new java.awt.Color(232, 240, 228));
        table.addCell(cell);
    }

    private static void addBodyCell(PdfPTable table, String text, Font font) {
        addBodyCell(table, text, font, Element.ALIGN_LEFT);
    }

    private static void addBodyCell(PdfPTable table, String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }
}
