package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.dtos.external.UserDetailDto;
import com.dentalCare.be_core.entities.Dentist;
import com.dentalCare.be_core.entities.Patient;
import com.dentalCare.be_core.entities.Prescription;
import com.dentalCare.be_core.services.UserServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionPdfServiceImplTest {

	@Mock
	private UserServiceClient userServiceClient;

	@InjectMocks
	private PrescriptionPdfServiceImpl prescriptionPdfService;

	private Prescription prescription;
	private Dentist dentist;
	private Patient patient;
	private UserDetailDto dentistUser;
	private UserDetailDto patientUser;

	@BeforeEach
	void setUp() {
		dentist = new Dentist();
		dentist.setId(1L);
		dentist.setUserId(10L);
		dentist.setLicenseNumber("LIC123456");
		dentist.setSpecialty("Orthodontics");

		patient = new Patient();
		patient.setId(2L);
		patient.setUserId(20L);
		patient.setDni("12345678A");

		prescription = new Prescription();
		prescription.setId(1L);
		prescription.setDentist(dentist);
		prescription.setPatient(patient);
		prescription.setPrescriptionDate(LocalDate.of(2024, 11, 15));
		prescription.setMedications("Amoxicillin 500mg - 1 tablet every 8 hours for 7 days");
		prescription.setObservations("Take with food. Avoid alcohol during treatment.");

		dentistUser = new UserDetailDto();
		dentistUser.setUserId(10L);
		dentistUser.setFirstName("John");
		dentistUser.setLastName("Smith");
		dentistUser.setEmail("john.smith@dental.com");
		dentistUser.setPhone("+1234567890");
		dentistUser.setAddress("123 Main St, Medical Center");

		patientUser = new UserDetailDto();
		patientUser.setUserId(20L);
		patientUser.setFirstName("Jane");
		patientUser.setLastName("Doe");
		patientUser.setBirthDate(LocalDate.of(1990, 5, 15));
	}

	@Test
	void generatePrescriptionPdf_Success() {
		// Arrange
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		byte[] pdfBytes = prescriptionPdfService.generatePrescriptionPdf(prescription);

		// Assert
		assertNotNull(pdfBytes);
		assertTrue(pdfBytes.length > 0);

		// Verificar que comienza con el header de PDF
		String pdfHeader = new String(pdfBytes, 0, 4);
		assertEquals("%PDF", pdfHeader);

		verify(userServiceClient, times(1)).getUserById(10L);
		verify(userServiceClient, times(1)).getUserById(20L);
	}

	@Test
	void generatePrescriptionPdf_WithMinimalData() {
		// Arrange
		dentist.setSpecialty(null);
		dentistUser.setPhone(null);
		dentistUser.setEmail(null);
		dentistUser.setAddress(null);
		patientUser.setBirthDate(null);
		prescription.setObservations(null);

		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		byte[] pdfBytes = prescriptionPdfService.generatePrescriptionPdf(prescription);

		// Assert
		assertNotNull(pdfBytes);
		assertTrue(pdfBytes.length > 0);

		String pdfHeader = new String(pdfBytes, 0, 4);
		assertEquals("%PDF", pdfHeader);
	}

	@Test
	void generatePrescriptionPdf_WithOnlyPhone() {
		// Arrange
		dentistUser.setEmail(null);

		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		byte[] pdfBytes = prescriptionPdfService.generatePrescriptionPdf(prescription);

		// Assert
		assertNotNull(pdfBytes);
		assertTrue(pdfBytes.length > 0);
	}

	@Test
	void generatePrescriptionPdf_WithOnlyEmail() {
		// Arrange
		dentistUser.setPhone(null);

		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		byte[] pdfBytes = prescriptionPdfService.generatePrescriptionPdf(prescription);

		// Assert
		assertNotNull(pdfBytes);
		assertTrue(pdfBytes.length > 0);
	}

	@Test
	void generatePrescriptionPdf_WithNullMedications() {
		// Arrange
		prescription.setMedications(null);

		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		byte[] pdfBytes = prescriptionPdfService.generatePrescriptionPdf(prescription);

		// Assert
		assertNotNull(pdfBytes);
		assertTrue(pdfBytes.length > 0);
	}

	@Test
	void generatePrescriptionPdf_UserServiceClientThrowsException() {
		// Arrange
		when(userServiceClient.getUserById(10L))
				.thenThrow(new RuntimeException("User service unavailable"));

		// Act & Assert
		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> prescriptionPdfService.generatePrescriptionPdf(prescription));

		assertTrue(exception.getMessage().contains("Error generating prescription PDF"));
		verify(userServiceClient, times(1)).getUserById(10L);
	}

	@Test
	void generatePrescriptionPdf_VerifyPdfStructure() {
		// Arrange
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		byte[] pdfBytes = prescriptionPdfService.generatePrescriptionPdf(prescription);

		// Assert
		assertNotNull(pdfBytes);

		// Verificar tamaño mínimo razonable para un PDF con contenido
		assertTrue(pdfBytes.length > 1000, "PDF should have reasonable size");

		// Verificar estructura básica de PDF
		String pdfContent = new String(pdfBytes);
		assertTrue(pdfContent.contains("%PDF"), "Should contain PDF header");
		assertTrue(pdfContent.contains("%%EOF"), "Should contain PDF footer");
	}

	@Test
	void generatePrescriptionPdf_WithLongMedications() {
		// Arrange
		prescription.setMedications(
				"Amoxicillin 500mg - 1 tablet every 8 hours for 7 days\n" +
				"Ibuprofen 400mg - 1 tablet every 6 hours as needed for pain\n" +
				"Chlorhexidine mouthwash 0.12% - Rinse twice daily for 10 days"
		);

		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		byte[] pdfBytes = prescriptionPdfService.generatePrescriptionPdf(prescription);

		// Assert
		assertNotNull(pdfBytes);
		assertTrue(pdfBytes.length > 0);
	}

	@Test
	void generatePrescriptionPdf_WithYoungPatient() {
		// Arrange - Patient is 10 years old
		patientUser.setBirthDate(LocalDate.now().minusYears(10));

		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		byte[] pdfBytes = prescriptionPdfService.generatePrescriptionPdf(prescription);

		// Assert
		assertNotNull(pdfBytes);
		assertTrue(pdfBytes.length > 0);
	}

	@Test
	void generatePrescriptionPdf_WithElderlyPatient() {
		// Arrange - Patient is 80 years old
		patientUser.setBirthDate(LocalDate.now().minusYears(80));

		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		byte[] pdfBytes = prescriptionPdfService.generatePrescriptionPdf(prescription);

		// Assert
		assertNotNull(pdfBytes);
		assertTrue(pdfBytes.length > 0);
	}
}