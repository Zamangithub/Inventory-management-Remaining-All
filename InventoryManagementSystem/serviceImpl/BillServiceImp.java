package com.example.InventoryManagementSystem.serviceImpl;

import com.example.InventoryManagementSystem.Constants.UserConstant;
import com.example.InventoryManagementSystem.JWT.JwtFilter;
import com.example.InventoryManagementSystem.Models.Bill;
import com.example.InventoryManagementSystem.Repsitory.BillRepository;
import com.example.InventoryManagementSystem.services.BillService;
import com.example.InventoryManagementSystem.utils.UserUtils;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPage;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
public class BillServiceImp implements BillService {

    @Autowired
    BillRepository billRepository;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public ResponseEntity<String> generateReport(Map<String, Object> requestMap) {
        log.info("Inside GenerateReport");
        try {
            String filename;
            if (validateRequestMap(requestMap)) {
                if (requestMap.containsKey("isGenerate") && !(Boolean) requestMap.get("isGenerate")) {
                    filename = (String) requestMap.get("uuid");
                } else {
                    filename = UserUtils.getUUID();
                    requestMap.put("uuid", filename);
                    insertBill(requestMap);
                }

                String data = "Name :" + requestMap.get("name") + "\n" +
                        "Contact Number :" + requestMap.get("contactNumber") + "\n" +
                        "Email :" + requestMap.get("email") + "\n" +
                        "Payment Method :" + requestMap.get("paymentMethod");

                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(UserConstant.STORAGE_LOCATION + "\\" + filename + ".pdf"));
                document.open();
                setRectanglePdf(document);

                Paragraph chunk = new Paragraph("Restaurant Management System", getFont("Header"));
                chunk.setAlignment(Element.ALIGN_CENTER);
                document.add(chunk);

                Paragraph paragraph = new Paragraph(data + "\n \n", getFont("Data"));
                document.add(paragraph);

                PdfPTable pTable = new PdfPTable(5);
                pTable.setWidthPercentage(100);
                addTableHeader(pTable);

                JSONArray jsonArray = UserUtils.getJsonArrayFromString((String) requestMap.get("productDetail"));
                for (int i = 0; i < jsonArray.length(); i++) {
                    addRows(pTable, UserUtils.getMapFromJson(jsonArray.getString(i)));
                }
                document.add(pTable);

                Paragraph footer = new Paragraph("Total :" + requestMap.get("totalAmount") + "\n"
                        + "Thank You for Visiting,Please Visit Again...!!", getFont("Data"));
                document.add(footer);

                // Create a SimpleDateFormat to format the date and time
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = dateFormat.format(new Date());
                // Add the date and time stamp to the PDF
                Paragraph dateStamp = new Paragraph("Date and Time: " + formattedDate, getFont("Data"));
                dateStamp.setAlignment(Element.ALIGN_BOTTOM); // Align the text to the center
                document.add(dateStamp);

               // Convert the JSON data to a string
//                String jsonData = jsonArray.toString();
//
//              // Generate the QR code from the JSON data
//                QRCode qrCode = QRCode.from(jsonData).to(ImageType.PNG).withSize(100, 100);
//
//                BufferedImage qrImage = qrCode.file();
//
//               // Convert the QR code image to an iText Image
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                ImageIO.write(qrImage, "png", baos);
//                Image qrCodeImage = Image.getInstance(baos.toByteArray());
//
//// Add the QR code image to the PDF
//                document.add(qrCodeImage);

                Paragraph stampParagraph = getElements();

               // Add the stamp Paragraph to the document
                document.add(stampParagraph);

                // Close the PDF document
                document.close();


                return new ResponseEntity<>("{\"uuid\":\"" + filename + "\"}", HttpStatus.OK);


            }
            return UserUtils.getResponseEntity("Required Data not found", HttpStatus.BAD_REQUEST);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return UserUtils.getResponseEntity(UserConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }



    private static Paragraph getElements() {
        Font stampFont = new Font(Font.FontFamily.COURIER, 12, Font.BOLD, new BaseColor(255, 69, 0));
        // Choose your preferred font and color
        // Create a Phrase with the stamp text and style
        Phrase stampPhrase = new Phrase("Bill Generated By : POST-X", stampFont);

        // Create a Paragraph with the stamp Phrase
        Paragraph stampParagraph = new Paragraph(stampPhrase);
        stampParagraph.setAlignment(Element.ALIGN_RIGHT); // Align the text to the right
        return stampParagraph;
    }

    private void addRows(PdfPTable pTable, Map<String, Object> data) {
        log.info("inside addRows");
        pTable.addCell((String) data.get("name"));
        pTable.addCell((String) data.get("Category"));
        pTable.addCell((String) data.get("quantity"));
        pTable.addCell(Double.toString((Double) data.get("price")));
        pTable.addCell(Double.toString((Double) data.get("total")));
    }

    private void addTableHeader(PdfPTable pTable) {
        log.info("inside PdfTable");
        Stream.of("Name", "Category", "Quantity", "Prize", "Sub Total")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();

                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);

                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));

                    header.setBackgroundColor(BaseColor.YELLOW);

                    header.setHorizontalAlignment(Element.ALIGN_CENTER);

                    header.setVerticalAlignment(Element.ALIGN_CENTER);

                    pTable.addCell(header);

                });

    }

    private Font getFont(String type) {
        log.info("inside getFont");
        switch (type) {
            case "Header":
                Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 18, BaseColor.BLACK);
                fontHeader.setStyle(Font.BOLD);
                return fontHeader;

            case "Data":
                Font dataFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, BaseColor.BLACK);
                dataFont.setStyle(Font.BOLD);
                return dataFont;
            default:
                return new Font();
        }
    }

    private void setRectanglePdf(Document document) throws DocumentException {
        log.info("Inside setRectanglePdf");
        Rectangle rectangle = new Rectangle(577, 825, 18, 15);
        rectangle.enableBorderSide(1);
        rectangle.enableBorderSide(2);
        rectangle.enableBorderSide(4);
        rectangle.enableBorderSide(8);
        rectangle.setBorderColor(BaseColor.BLACK);
        rectangle.setBorderWidth(1);
        document.add(rectangle);
    }

    private void insertBill(Map<String, Object> requestMap) {
        try {
            Bill bill = new Bill();
            bill.setUuid((String) requestMap.get("uuid"));
            bill.setName((String) requestMap.get("name"));
            bill.setEmail((String) requestMap.get("email"));
            bill.setContactNumber((String) requestMap.get("contactNumber"));
            bill.setPaymentMethod((String) requestMap.get("paymentMethod"));
            bill.setTotal(Integer.parseInt((String) requestMap.get("totalAmount")));
            bill.setProductDetail((String) requestMap.get("productDetail"));
            bill.setCreatedBy(jwtFilter.getCurrentUser());
            billRepository.save(bill);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean validateRequestMap(Map<String, Object> requestMap) {
        return requestMap.containsKey("name") &&
                requestMap.containsKey("contactNumber") &&
                requestMap.containsKey("email") &&
                requestMap.containsKey("paymentMethod") &&
                requestMap.containsKey("productDetail") &&
                requestMap.containsKey("totalAmount");

    }
}
