-- ===============================================
-- SEED DATA PARA DENTAL CARE - SOLO TESTING
-- ===============================================
-- IMPORTANTE: Este archivo es solo para desarrollo/testing
-- Se ejecuta automáticamente al iniciar la aplicación
-- Los datos son ficticios pero realistas para pruebas

-- ===============================================
-- LIMPIAR DATOS EXISTENTES (para testing limpio)
-- ===============================================
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE appointments;
TRUNCATE TABLE medical_histories;
TRUNCATE TABLE prescriptions;
TRUNCATE TABLE treatments;
TRUNCATE TABLE patients;
TRUNCATE TABLE dentists;

SET FOREIGN_KEY_CHECKS = 1;

-- ===============================================
-- INSERTAR DENTISTAS
-- ===============================================
INSERT INTO dentists (id, user_id, license_number, specialty, active) VALUES
(1, 101, 'DENT-001-MED', 'Odontología General', true),
(2, 102, 'DENT-002-ORT', 'Ortodoncia', true),
(3, 103, 'DENT-003-END', 'Endodoncia', true),
(4, 104, 'DENT-004-PER', 'Periodoncia', true),
(5, 105, 'DENT-005-CIR', 'Cirugía Oral', true);

-- ===============================================
-- INSERTAR PACIENTES
-- ===============================================
-- Algunos pacientes ya vinculados a dentistas
INSERT INTO patients (id, user_id, dentist_id, dni, birth_date, active) VALUES
(1, 201, 1, '12345678', '1985-03-15', true),
(2, 202, 1, '23456789', '1990-07-22', true),
(3, 203, 2, '34567890', '1978-11-08', true),
(4, 204, 3, '45678901', '1992-05-12', true),
(5, 205, 4, '56789012', '1988-09-30', true);

-- ===============================================
-- INSERTAR TRATAMIENTOS
-- ===============================================
INSERT INTO treatments (id, patient_id, dentist_id, name, description, start_date, estimated_end_date, actual_end_date, status, total_sessions, completed_sessions, notes, active) VALUES
(1, 1, 1, 'Limpieza Dental', 'Limpieza profesional y profilaxis dental', '2024-01-15', '2024-01-15', '2024-01-15', 'completado', 1, 1, 'Paciente con buena higiene oral', true),
(2, 2, 1, 'Empaste Dental', 'Restauración de caries en molar superior', '2024-02-01', '2024-02-01', '2024-02-01', 'completado', 1, 1, 'Caries moderada, restauración exitosa', true),
(3, 3, 2, 'Tratamiento de Ortodoncia', 'Corrección de maloclusión con brackets', '2024-01-10', '2024-12-31', NULL, 'en progreso', 24, 8, 'Progreso satisfactorio, paciente colaborador', true),
(4, 4, 3, 'Endodoncia', 'Tratamiento de conducto en premolar', '2024-03-05', '2024-03-12', NULL, 'en progreso', 3, 1, 'Primera sesión completada, próxima cita en 7 días', true),
(5, 5, 4, 'Tratamiento Periodontal', 'Limpieza profunda y curetaje', '2024-02-20', '2024-03-20', NULL, 'en progreso', 4, 2, 'Mejora notable en salud gingival', true);

-- ===============================================
-- INSERTAR CITAS MÉDICAS
-- ===============================================
INSERT INTO appointments (id, patient_id, dentist_id, start_datetime, end_datetime, status, reason, notes, active) VALUES
(1, 1, 1, '2024-12-20 09:00:00', '2024-12-20 09:30:00', 'PROGRAMADO', 'Control post-tratamiento', 'Revisar evolución del empaste realizado', true),
(2, 2, 1, '2024-12-21 10:00:00', '2024-12-21 10:45:00', 'PROGRAMADO', 'Consulta general', 'Dolor en muela del juicio', true),
(3, 3, 2, '2024-12-22 14:00:00', '2024-12-22 14:30:00', 'PROGRAMADO', 'Ajuste de ortodoncia', 'Control mensual de brackets', true),
(4, 4, 3, '2024-12-23 11:00:00', '2024-12-23 12:00:00', 'PROGRAMADO', 'Continuación endodoncia', 'Segunda sesión del tratamiento', true),
(5, 5, 4, '2024-12-24 16:00:00', '2024-12-24 17:00:00', 'PROGRAMADO', 'Seguimiento periodontal', 'Tercera sesión de limpieza profunda', true);

-- ===============================================
-- INSERTAR HISTORIAL MÉDICO
-- ===============================================
INSERT INTO medical_histories (id, patient_id, dentist_id, treatment_id, date, description, observations, diagnosis, treatment_plan, active) VALUES
(1, 1, 1, 1, '2024-01-15', 'Limpieza dental profesional', 'Paciente con buena higiene oral, mínima placa bacteriana', 'Gingivitis leve', 'Limpieza profesional y recomendaciones de higiene', true),
(2, 2, 1, 2, '2024-02-01', 'Restauración de caries', 'Caries moderada en molar 16, sin compromiso pulpar', 'Caries de esmalte y dentina', 'Empaste con resina compuesta', true),
(3, 3, 2, 3, '2024-01-10', 'Inicio de tratamiento ortodóncico', 'Maloclusión clase II, apiñamiento moderado', 'Maloclusión clase II', 'Tratamiento con brackets metálicos', true),
(4, 4, 3, 4, '2024-03-05', 'Inicio de endodoncia', 'Dolor intenso, necrosis pulpar confirmada', 'Necrosis pulpar', 'Tratamiento de conducto en dos sesiones', true),
(5, 5, 4, 5, '2024-02-20', 'Evaluación periodontal', 'Bolsones de 4-6mm, sangrado al sondaje', 'Periodontitis moderada', 'Tratamiento periodontal no quirúrgico', true);

-- ===============================================
-- INSERTAR RECETAS MÉDICAS
-- ===============================================
INSERT INTO prescriptions (id, patient_id, dentist_id, prescription_date, medications, instructions, notes, active) VALUES
(1, 1, 1, '2024-01-15', 'Ibuprofeno 400mg', 'Tomar cada 8 horas por 3 días si hay molestias', 'Analgésico post-limpieza', true),
(2, 2, 1, '2024-02-01', 'Amoxicilina 500mg, Ibuprofeno 400mg', 'Amoxicilina cada 8 horas por 7 días. Ibuprofeno cada 8 horas por 3 días', 'Antibiótico y analgésico post-empaste', true),
(3, 3, 2, '2024-01-10', 'Cera para brackets', 'Aplicar sobre brackets que causen molestias', 'Alivio de irritación por brackets', true),
(4, 4, 3, '2024-03-05', 'Ibuprofeno 400mg, Clindamicina 300mg', 'Ibuprofeno cada 8 horas por 5 días. Clindamicina cada 8 horas por 7 días', 'Tratamiento post-endodoncia', true),
(5, 5, 4, '2024-02-20', 'Clorhexidina 0.12%', 'Enjuague bucal 2 veces al día por 2 semanas', 'Antiséptico bucal post-tratamiento periodontal', true);

-- ===============================================
-- NOTAS IMPORTANTES PARA TESTING
-- ===============================================
-- 
-- USUARIOS DISPONIBLES (sin dentista vinculado):
-- - user_id: 206, 207, 208, 209, 210 (estos NO están en la tabla patients)
-- - Estos usuarios aparecerán en el endpoint GET /api/dentists/available-patients
--
-- USUARIOS YA VINCULADOS:
-- - user_id: 201, 202, 203, 204, 205 (estos SÍ están en la tabla patients)
-- - Estos usuarios NO aparecerán en el endpoint de disponibles
--
-- DENTISTAS DISPONIBLES:
-- - user_id: 101, 102, 103, 104, 105
-- - Todos están activos y pueden recibir nuevos pacientes
--
-- RELACIONES COHERENTES:
-- - Cada paciente tiene un dentista asignado
-- - Cada tratamiento está vinculado a un paciente y dentista
-- - Cada cita está vinculada a un paciente y dentista
-- - Cada receta está vinculada a un paciente y dentista
-- - Cada entrada de historial está vinculada a un paciente y dentista
--
-- ===============================================
