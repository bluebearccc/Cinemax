package com.bluebear.cinemax.service.cashier;

import com.bluebear.cinemax.dto.cashier.BookingResponseDTO;
import com.bluebear.cinemax.dto.cashier.BookingResponseDTO.FoodItemDetail;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class PdfGenerationService {

    // Helper to format currency
    private String formatCurrency(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        return formatter.format(price);
    }

    public byte[] generateInvoicePdf(BookingResponseDTO bookingResult) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(30, 30, 30, 30);

        PdfFont font = PdfFontFactory.createFont();
        document.setFont(font);

        document.add(new Paragraph("CINEMAX BOOKING INVOICE")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20)
                .setBold());
        document.add(new Paragraph("Invoice ID: #" + bookingResult.getInvoiceId())
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Booking Date: " + bookingResult.getBookingDate())
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Customer Information").setBold().setFontColor(ColorConstants.BLUE));
        document.add(new Paragraph("Full Name: " + bookingResult.getCustomerName()));
        document.add(new Paragraph("Phone Number: " + bookingResult.getCustomerPhone()));

        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Movie Information").setBold().setFontColor(ColorConstants.BLUE));
        document.add(new Paragraph("Movie Name: " + bookingResult.getMovieName()));
        document.add(new Paragraph("Showtime: " + bookingResult.getScheduleTime()));
        document.add(new Paragraph("Room: " + bookingResult.getRoomName()));
        document.add(new Paragraph("Seats: " + String.join(", ", bookingResult.getSeatPositions())));

        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Invoice Details").setBold().setFontColor(ColorConstants.BLUE));
        Table table = new Table(UnitValue.createPercentArray(new float[]{4, 2, 2, 2}));
        table.setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(new Cell().add(new Paragraph("Description").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Quantity").setBold().setTextAlignment(TextAlignment.CENTER)));
        table.addHeaderCell(new Cell().add(new Paragraph("Unit Price").setBold().setTextAlignment(TextAlignment.RIGHT)));
        table.addHeaderCell(new Cell().add(new Paragraph("Total").setBold().setTextAlignment(TextAlignment.RIGHT)));

        table.addCell(new Cell().add(new Paragraph("Movie Tickets")));
        table.addCell(new Cell().add(new Paragraph(String.valueOf(bookingResult.getSeatPositions().size())).setTextAlignment(TextAlignment.CENTER)));
        table.addCell(new Cell()); // Leave unit price for tickets blank
        table.addCell(new Cell().add(new Paragraph(formatCurrency(bookingResult.getTotalTicketPrice())).setTextAlignment(TextAlignment.RIGHT)));

        if (bookingResult.getFoodItems() != null && !bookingResult.getFoodItems().isEmpty()) {
            for (FoodItemDetail food : bookingResult.getFoodItems()) {
                table.addCell(new Cell().add(new Paragraph(food.getName())));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(food.getQuantity())).setTextAlignment(TextAlignment.CENTER)));
                table.addCell(new Cell().add(new Paragraph(formatCurrency(food.getUnitPrice())).setTextAlignment(TextAlignment.RIGHT)));
                table.addCell(new Cell().add(new Paragraph(formatCurrency(food.getUnitPrice() * food.getQuantity())).setTextAlignment(TextAlignment.RIGHT)));
            }
        }

        document.add(table);

        document.add(new Paragraph("\n"));
        Table totalTable = new Table(UnitValue.createPercentArray(new float[]{2, 1}));
        totalTable.setWidth(UnitValue.createPercentValue(50)).setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);

        totalTable.addCell(new Cell().add(new Paragraph("Total Ticket Price:").setBold()).setBorder(null));
        totalTable.addCell(new Cell().add(new Paragraph(formatCurrency(bookingResult.getTotalTicketPrice())).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));

        if (bookingResult.getTotalFoodPrice() > 0) {
            totalTable.addCell(new Cell().add(new Paragraph("Total Food & Beverage:").setBold()).setBorder(null));
            totalTable.addCell(new Cell().add(new Paragraph(formatCurrency(bookingResult.getTotalFoodPrice())).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));
        }

        totalTable.addCell(new Cell().add(new Paragraph("GRAND TOTAL:").setBold().setFontSize(14)).setBorder(null));
        totalTable.addCell(new Cell().add(new Paragraph(formatCurrency(bookingResult.getTotalPrice())).setBold().setFontSize(14).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));

        document.add(totalTable);

        // === Footer ===
        document.add(new Paragraph("\n\nThank you for your purchase and see you again!")
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic());

        document.close();
        return baos.toByteArray();
    }
}