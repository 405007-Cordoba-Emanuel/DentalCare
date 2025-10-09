package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.dtos.external.UserDetailDto;
import com.dentalCare.be_core.entities.Prescription;
import com.dentalCare.be_core.services.PrescriptionPdfService;
import com.dentalCare.be_core.services.UserServiceClient;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.Period;

@Service
@Slf4j
public class PrescriptionPdfServiceImpl implements PrescriptionPdfService {

    @Autowired
    private UserServiceClient userServiceClient;

    @Override
    public byte[] generatePrescriptionPdf(Prescription prescription) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            UserDetailDto dentistUser = userServiceClient.getUserById(prescription.getDentist().getUserId());
            UserDetailDto patientUser = userServiceClient.getUserById(prescription.getPatient().getUserId());
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            document.setMargins(30, 40, 30, 40);

            DeviceRgb primaryColor = new DeviceRgb(41, 128, 185);
            DeviceRgb secondaryColor = new DeviceRgb(52, 73, 94);

            document.add(new Paragraph("DENTAL CARE")
                    .setFontSize(18)
                    .setBold()
                    .setFontColor(primaryColor)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(2));

            document.add(new Paragraph("Dr./Dra. " + dentistUser.getFirstName() + " " + dentistUser.getLastName())
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(2));

            document.add(new Paragraph("Matrícula Profesional: " + prescription.getDentist().getLicenseNumber())
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(2));

            if (prescription.getDentist().getSpecialty() != null) {
                document.add(new Paragraph("Especialidad: " + prescription.getDentist().getSpecialty())
                        .setFontSize(8)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(2));
            }

            if (dentistUser.getPhone() != null || dentistUser.getEmail() != null) {
                String contactInfo = "";
                if (dentistUser.getPhone() != null) {
                    contactInfo += "Tel: " + dentistUser.getPhone();
                }
                if (dentistUser.getEmail() != null) {
                    if (!contactInfo.isEmpty()) contactInfo += " | ";
                    contactInfo += "Email: " + dentistUser.getEmail();
                }
                document.add(new Paragraph(contactInfo)
                        .setFontSize(8)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(2));
            }

            if (dentistUser.getAddress() != null) {
                document.add(new Paragraph(dentistUser.getAddress())
                        .setFontSize(8)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(5));
            }

            Table separatorLine = new Table(1);
            separatorLine.setWidth(UnitValue.createPercentValue(100));
            separatorLine.addCell(new Cell().add(new Paragraph(""))
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(primaryColor, 1.5f))
                    .setMarginBottom(5));
            document.add(separatorLine);

            document.add(new Paragraph("RECETA MÉDICA ODONTOLÓGICA")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(secondaryColor)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(3));

            document.add(new Paragraph("Fecha: " + prescription.getPrescriptionDate().toString())
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(8));

            document.add(new Paragraph("DATOS DEL PACIENTE")
                    .setFontSize(11)
                    .setBold()
                    .setFontColor(secondaryColor)
                    .setMarginBottom(3));

            Table patientTable = new Table(2);
            patientTable.setWidth(UnitValue.createPercentValue(100));
            patientTable.addCell(createInfoCell("Nombre completo:", false));
            patientTable.addCell(createInfoCell(patientUser.getFirstName() + " " + patientUser.getLastName(), true));
            patientTable.addCell(createInfoCell("DNI:", false));
            patientTable.addCell(createInfoCell(prescription.getPatient().getDni(), true));

            if (patientUser.getBirthDate() != null) {
                int age = Period.between(patientUser.getBirthDate(), LocalDate.now()).getYears();
                patientTable.addCell(createInfoCell("Edad:", false));
                patientTable.addCell(createInfoCell(age + " años", true));
            }

            document.add(patientTable);
            document.add(new Paragraph(" ").setMarginBottom(5));

            document.add(new Paragraph("MEDICACIÓN PRESCRITA")
                    .setFontSize(11)
                    .setBold()
                    .setFontColor(secondaryColor)
                    .setMarginBottom(3));

            Table medicationBox = new Table(1);
            medicationBox.setWidth(UnitValue.createPercentValue(100));
            medicationBox.addCell(new Cell()
                    .add(new Paragraph(prescription.getMedications() != null ? prescription.getMedications() : "")
                            .setFontSize(10))
                    .setMinHeight(60)
                    .setPadding(8)
                    .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1)));
            document.add(medicationBox);

            document.add(new Paragraph(" ").setMarginBottom(5));

            document.add(new Paragraph("OBSERVACIONES E INDICACIONES")
                    .setFontSize(11)
                    .setBold()
                    .setFontColor(secondaryColor)
                    .setMarginBottom(3));

            Table observationsBox = new Table(1);
            observationsBox.setWidth(UnitValue.createPercentValue(100));
            observationsBox.addCell(new Cell()
                    .add(new Paragraph(prescription.getObservations() != null ? prescription.getObservations() : "")
                            .setFontSize(10))
                    .setMinHeight(50)
                    .setPadding(8)
                    .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1)));
            document.add(observationsBox);

            document.add(new Paragraph(" ").setMarginBottom(15));

            Table signatureTable = new Table(1);
            signatureTable.setWidth(UnitValue.createPercentValue(45));
            signatureTable.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);
            signatureTable.addCell(new Cell()
                    .add(new Paragraph("\n\n"))
                    .setBorder(Border.NO_BORDER)
                    .setBorderTop(new SolidBorder(ColorConstants.BLACK, 1)));
            signatureTable.addCell(new Cell()
                    .add(new Paragraph("Firma y Sello del Profesional")
                            .setFontSize(9)
                            .setTextAlignment(TextAlignment.CENTER))
                    .setBorder(Border.NO_BORDER));
            document.add(signatureTable);

            document.add(new Paragraph("Matrícula: " + prescription.getDentist().getLicenseNumber())
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginTop(5));

            document.close();

            log.info("PDF generated successfully for prescription ID: {}", prescription.getId());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF for prescription ID: {}", prescription.getId(), e);
            throw new RuntimeException("Error generating prescription PDF", e);
        }
    }

    private Cell createInfoCell(String content, boolean isValue) {
        Cell cell = new Cell()
                .add(new Paragraph(content).setFontSize(10))
                .setBorder(Border.NO_BORDER)
                .setPadding(2);

        if (!isValue) {
            cell.setFontColor(secondaryColor);
            cell.setBold();
        }

        return cell;
    }

    private static final DeviceRgb secondaryColor = new DeviceRgb(52, 73, 94);
}
