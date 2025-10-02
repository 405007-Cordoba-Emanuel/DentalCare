package com.dentalCare.be_core.services;

import com.dentalCare.be_core.entities.Prescription;

public interface PrescriptionPdfService {

    byte[] generatePrescriptionPdf(Prescription prescription);
}
