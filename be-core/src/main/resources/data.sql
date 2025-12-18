-- ===============================================
-- SEED DATA PARA DENTAL CARE - DEMOSTRACIÓN
-- ===============================================
-- IMPORTANTE: Este archivo es solo para desarrollo/testing/demostración
-- Se ejecuta automáticamente al iniciar la aplicación
-- Los datos son ficticios pero realistas para pruebas
--
-- ESTRUCTURA DE DATOS:
-- - 1 Dentista (user_id: 2)
-- - 8 Pacientes (user_id: 3-10) todos vinculados al mismo dentista
-- - 24 Tratamientos: 3 por paciente (1 completado, 1 abandonado, 1 en curso)
-- - 80 Recetas: 10 por paciente (distribuidas en varios meses para probar filtros)
-- - 32 Odontogramas: 4 por paciente (evolución a lo largo del tiempo)
-- - 80 Entradas de Historia Clínica: 10 por paciente (algunas vinculadas, otras independientes)
-- - 8 Conversaciones de Chat
-- - 80 Mensajes de Chat: 10 por conversación (tipo consulta)
-- - Múltiples Turnos distribuidos: algunos en noviembre (pasados), muchos en diciembre/enero (futuros)
--   Incluye días completos para probar visualización de días ocupados
--
-- FECHAS: Todos los datos (excepto turnos) desde 15/12/2025 hacia atrás
-- Turnos: Pasados en noviembre, futuros en diciembre y enero
--
-- ===============================================

-- ===============================================
-- LIMPIAR DATOS EXISTENTES (para testing limpio)
-- ===============================================
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE chat_messages;
TRUNCATE TABLE conversations;
TRUNCATE TABLE odontograms;
TRUNCATE TABLE appointments;
TRUNCATE TABLE medical_history;
TRUNCATE TABLE prescriptions;
TRUNCATE TABLE treatments;
TRUNCATE TABLE patients;
TRUNCATE TABLE dentists;

SET FOREIGN_KEY_CHECKS = 1;

-- ===============================================
-- INSERTAR DENTISTAS
-- ===============================================
-- Campos: id, user_id, license_number, specialty, active
INSERT INTO dentists (id, user_id, license_number, specialty, active) VALUES
(1, 2, 'DENT-001-MED', 'Odontología General', true);

-- ===============================================
-- INSERTAR PACIENTES
-- ===============================================
-- Campos: id, user_id, dentist_id, dni, active
-- 8 Pacientes todos vinculados al dentista ID 1
INSERT INTO patients (id, user_id, dentist_id, dni, active) VALUES
(1, 3, 1, '12345678', true),
(2, 4, 1, '23456789', true),
(3, 5, 1, '34567890', true),
(4, 6, 1, '45678901', true),
(5, 7, 1, '56789012', true),
(6, 8, 1, '67890123', true),
(7, 9, 1, '78901234', true),
(8, 10, 1, '89012345', true);

-- ===============================================
-- INSERTAR TRATAMIENTOS
-- ===============================================
-- Campos: id, patient_id, dentist_id, name, description, start_date, estimated_end_date, actual_end_date, status, total_sessions, completed_sessions, notes, active
-- 24 Tratamientos: 3 por paciente (1 completado, 1 abandonado, 1 en curso)
INSERT INTO treatments (id, patient_id, dentist_id, name, description, start_date, estimated_end_date, actual_end_date, status, total_sessions, completed_sessions, notes, active) VALUES
-- Paciente 1 (María Elena Pérez) - 3 tratamientos
  (1, 1, 1, 'Blanqueamiento Dental', 'Tratamiento de blanqueamiento con peróxido de carbamida', '2024-09-15', '2024-10-15', '2024-10-15', 'COMPLETADO', 3, 3, 'Resultado excelente, paciente muy satisfecho. Color mejorado en 4 tonos', true),
  (2, 1, 1, 'Rehabilitación Estética', 'Mejora estética con carillas de porcelana', '2024-10-20', '2025-01-20', NULL, 'ABANDONADO', 4, 2, 'Paciente abandonó el tratamiento después de dos sesiones por motivos económicos', true),
  (3, 1, 1, 'Tratamiento de Sensibilidad', 'Tratamiento para sensibilidad dental con aplicación de flúor', '2024-11-10', '2025-12-15', NULL, 'EN_CURSO', 3, 2, 'Mejora notable en la sensibilidad. Dos sesiones completadas exitosamente. Próxima sesión programada', true),
-- Paciente 2 (Carlos Alberto García) - 3 tratamientos
  (4, 2, 1, 'Empaste Dental', 'Restauración de caries en molar superior derecho', '2024-08-20', '2024-08-20', '2024-08-20', 'COMPLETADO', 1, 1, 'Caries moderada, restauración exitosa con resina compuesta', true),
  (5, 2, 1, 'Corona Dental', 'Colocación de corona de porcelana', '2024-09-10', '2024-11-10', NULL, 'ABANDONADO', 2, 1, 'Paciente no pudo continuar con el tratamiento', true),
  (6, 2, 1, 'Limpieza Profunda', 'Limpieza profunda y curetaje periodontal', '2024-10-05', '2025-01-05', NULL, 'EN_CURSO', 4, 2, 'Progreso satisfactorio. Mejora notable en salud gingival', true),
-- Paciente 3 (Ana Beatriz Silva) - 3 tratamientos
  (7, 3, 1, 'Limpieza Dental', 'Limpieza profesional y profilaxis dental', '2024-09-01', '2024-09-01', '2024-09-01', 'COMPLETADO', 1, 1, 'Paciente con buena higiene oral', true),
  (8, 3, 1, 'Tratamiento de Ortodoncia', 'Corrección de maloclusión con brackets metálicos', '2024-08-15', '2026-06-30', NULL, 'ABANDONADO', 24, 6, 'Paciente abandonó tratamiento ortodóncico después de 6 meses', true),
  (9, 3, 1, 'Endodoncia', 'Tratamiento de conducto radicular en premolar inferior', '2024-11-05', '2024-12-20', NULL, 'EN_CURSO', 3, 1, 'Primera sesión completada exitosamente. Necrosis pulpar confirmada', true),
-- Paciente 4 (Roberto Carlos Morales) - 3 tratamientos
  (10, 4, 1, 'Extracción de Cordal', 'Extracción quirúrgica de muela del juicio', '2024-09-25', '2024-09-25', '2024-09-25', 'COMPLETADO', 1, 1, 'Extracción exitosa, sin complicaciones', true),
  (11, 4, 1, 'Implante Dental', 'Colocación de implante de titanio', '2024-10-10', '2025-02-10', NULL, 'ABANDONADO', 6, 2, 'Paciente abandonó tratamiento por motivos personales', true),
  (12, 4, 1, 'Tratamiento Periodontal', 'Limpieza profunda y curetaje para periodontitis', '2024-10-20', '2024-12-20', NULL, 'EN_CURSO', 4, 3, 'Mejora notable en salud gingival. Reducción de bolsones confirmada', true),
-- Paciente 5 (Laura Patricia Vargas) - 3 tratamientos
  (13, 5, 1, 'Empaste Múltiple', 'Restauración de múltiples caries', '2024-08-10', '2024-09-10', '2024-09-10', 'COMPLETADO', 3, 3, 'Todas las caries restauradas exitosamente', true),
  (14, 5, 1, 'Rehabilitación Oral', 'Prótesis parcial removible', '2024-09-20', '2024-12-20', NULL, 'ABANDONADO', 6, 3, 'Paciente decidió no continuar con el tratamiento', true),
  (15, 5, 1, 'Blanqueamiento Dental', 'Tratamiento de blanqueamiento profesional', '2024-11-15', '2024-12-15', NULL, 'EN_CURSO', 3, 2, 'Progreso excelente. Segunda sesión completada', true),
-- Paciente 6 (Diego Alejandro Torres) - 3 tratamientos
  (16, 6, 1, 'Limpieza y Control', 'Limpieza dental de rutina y control general', '2024-09-05', '2024-09-05', '2024-09-05', 'COMPLETADO', 1, 1, 'Revisión completa, sin problemas detectados', true),
  (17, 6, 1, 'Corona Dental', 'Preparación para corona de porcelana', '2024-10-15', '2024-12-15', NULL, 'ABANDONADO', 2, 1, 'Paciente no asistió a segunda cita', true),
  (18, 6, 1, 'Tratamiento de Sensibilidad', 'Aplicación de flúor y sellantes', '2024-11-01', '2024-12-15', NULL, 'EN_CURSO', 3, 2, 'Mejora en sensibilidad. Continuando con tratamiento', true),
-- Paciente 7 (Valentina Jiménez) - 3 tratamientos
  (19, 7, 1, 'Extracción Simple', 'Extracción de diente temporal', '2024-08-25', '2024-08-25', '2024-08-25', 'COMPLETADO', 1, 1, 'Extracción realizada sin complicaciones', true),
  (20, 7, 1, 'Tratamiento de Ortodoncia', 'Corrección con brackets', '2024-09-15', '2026-03-15', NULL, 'ABANDONADO', 30, 8, 'Paciente abandonó tratamiento ortodóncico', true),
  (21, 7, 1, 'Endodoncia', 'Tratamiento de conducto en molar', '2024-10-25', '2024-12-10', NULL, 'EN_CURSO', 3, 2, 'Segunda sesión completada. Progreso satisfactorio', true),
-- Paciente 8 (Fernando Herrera) - 3 tratamientos
  (22, 8, 1, 'Limpieza Profunda', 'Profilaxis y limpieza profunda', '2024-09-30', '2024-09-30', '2024-09-30', 'COMPLETADO', 1, 1, 'Limpieza completada exitosamente', true),
  (23, 8, 1, 'Implante Dental', 'Colocación de implante', '2024-10-30', '2025-02-28', NULL, 'ABANDONADO', 5, 2, 'Paciente suspendió tratamiento por recomendación médica', true),
  (24, 8, 1, 'Tratamiento Periodontal', 'Raspado y alisado radicular', '2024-11-08', '2025-01-08', NULL, 'EN_CURSO', 4, 2, 'Tratamiento en progreso. Mejora en salud gingival', true);

-- ===============================================
-- INSERTAR CITAS MÉDICAS
-- ===============================================
-- Campos: id, patient_id, dentist_id, start_datetime, end_datetime, status, reason, notes, active
-- Estados: PROGRAMADO, CONFIRMADO, COMPLETADO, CANCELADO, AUSENTE
-- Turnos distribuidos: Noviembre (pasados), Diciembre y Enero (futuros)
-- Incluye días completos para probar visualización de días ocupados
INSERT INTO appointments (id, patient_id, dentist_id, start_datetime, end_datetime, status, reason, notes, active) VALUES
-- NOVIEMBRE 2025 (PASADOS) - Turnos completados, cancelados y ausentes
-- Día 5 de noviembre - Varios turnos
(1, 1, 1, '2025-11-05 09:00:00', '2025-11-05 09:30:00', 'COMPLETADO', 'Control de rutina', 'Revisión general completada', true),
(2, 2, 1, '2025-11-05 10:00:00', '2025-11-05 10:45:00', 'COMPLETADO', 'Limpieza dental', 'Limpieza profesional completada', true),
(3, 3, 1, '2025-11-05 11:00:00', '2025-11-05 11:30:00', 'COMPLETADO', 'Consulta general', 'Consulta de rutina completada', true),
(4, 4, 1, '2025-11-05 14:00:00', '2025-11-05 15:00:00', 'COMPLETADO', 'Control tratamiento', 'Seguimiento de tratamiento periodontal', true),
(5, 5, 1, '2025-11-05 15:30:00', '2025-11-05 16:00:00', 'CANCELADO', 'Control', 'Paciente canceló', true),

-- Día 12 de noviembre - Día completo (muchos turnos)
(6, 1, 1, '2025-11-12 09:00:00', '2025-11-12 09:30:00', 'COMPLETADO', 'Limpieza dental', 'Limpieza completada', true),
(7, 2, 1, '2025-11-12 09:30:00', '2025-11-12 10:15:00', 'COMPLETADO', 'Empaste', 'Restauración completada', true),
(8, 3, 1, '2025-11-12 10:30:00', '2025-11-12 11:00:00', 'COMPLETADO', 'Control', 'Revisión general', true),
(9, 4, 1, '2025-11-12 11:00:00', '2025-11-12 12:00:00', 'COMPLETADO', 'Endodoncia', 'Primera sesión completada', true),
(10, 5, 1, '2025-11-12 14:00:00', '2025-11-12 14:30:00', 'COMPLETADO', 'Limpieza', 'Profilaxis realizada', true),
(11, 6, 1, '2025-11-12 14:30:00', '2025-11-12 15:15:00', 'COMPLETADO', 'Consulta inicial', 'Evaluación completa', true),
(12, 7, 1, '2025-11-12 15:30:00', '2025-11-12 16:00:00', 'AUSENTE', 'Control', 'Paciente no asistió', true),
(13, 8, 1, '2025-11-12 16:00:00', '2025-11-12 16:45:00', 'COMPLETADO', 'Tratamiento', 'Sesión de tratamiento completada', true),

-- Día 18 de noviembre
(14, 1, 1, '2025-11-18 09:00:00', '2025-11-18 10:00:00', 'COMPLETADO', 'Blanqueamiento', 'Primera sesión blanqueamiento', true),
(15, 2, 1, '2025-11-18 10:30:00', '2025-11-18 11:00:00', 'CANCELADO', 'Control', 'Cancelada por paciente', true),
(16, 3, 1, '2025-11-18 11:00:00', '2025-11-18 11:45:00', 'COMPLETADO', 'Ortodoncia', 'Ajuste de brackets', true),
(17, 4, 1, '2025-11-18 14:00:00', '2025-11-18 15:00:00', 'COMPLETADO', 'Implante', 'Control post-implante', true),

-- Día 25 de noviembre - Día completo
(18, 1, 1, '2025-11-25 09:00:00', '2025-11-25 09:30:00', 'COMPLETADO', 'Seguimiento tratamiento', 'Control sensibilidad dental', true),
(19, 2, 1, '2025-11-25 09:30:00', '2025-11-25 10:15:00', 'COMPLETADO', 'Corona', 'Preparación corona', true),
(20, 3, 1, '2025-11-25 10:30:00', '2025-11-25 11:00:00', 'COMPLETADO', 'Control', 'Revisión', true),
(21, 4, 1, '2025-11-25 11:00:00', '2025-11-25 12:00:00', 'COMPLETADO', 'Periodontal', 'Segunda sesión', true),
(22, 5, 1, '2025-11-25 14:00:00', '2025-11-25 15:00:00', 'COMPLETADO', 'Blanqueamiento', 'Segunda sesión', true),
(23, 6, 1, '2025-11-25 15:00:00', '2025-11-25 15:45:00', 'COMPLETADO', 'Consulta', 'Evaluación', true),
(24, 7, 1, '2025-11-25 16:00:00', '2025-11-25 16:30:00', 'COMPLETADO', 'Limpieza', 'Profilaxis', true),
(25, 8, 1, '2025-11-25 16:30:00', '2025-11-25 17:15:00', 'CANCELADO', 'Control', 'Cancelada', true),

-- DICIEMBRE 2025 - Algunos pasados (antes del 15), mayoría futuros (después del 15)
-- Día 1 de diciembre (pasado - antes del 15)
(26, 1, 1, '2025-12-01 09:00:00', '2025-12-01 09:30:00', 'COMPLETADO', 'Limpieza dental', 'Limpieza mensual completada', true),
(27, 2, 1, '2025-12-01 10:00:00', '2025-12-01 10:45:00', 'COMPLETADO', 'Control corona', 'Revisión corona', true),
(28, 3, 1, '2025-12-01 11:00:00', '2025-12-01 11:30:00', 'COMPLETADO', 'Ortodoncia', 'Ajuste mensual', true),

-- Día 8 de diciembre (pasado)
(29, 4, 1, '2025-12-08 09:00:00', '2025-12-08 10:00:00', 'COMPLETADO', 'Endodoncia', 'Segunda sesión', true),
(30, 5, 1, '2025-12-08 10:30:00', '2025-12-08 11:30:00', 'COMPLETADO', 'Blanqueamiento', 'Tercera sesión', true),
(31, 6, 1, '2025-12-08 14:00:00', '2025-12-08 14:45:00', 'COMPLETADO', 'Control', 'Revisión general', true),
(32, 7, 1, '2025-12-08 15:00:00', '2025-12-08 16:00:00', 'COMPLETADO', 'Endodoncia', 'Primera sesión', true),

-- Día 10 de diciembre (pasado)
(33, 1, 1, '2025-12-10 09:00:00', '2025-12-10 09:45:00', 'COMPLETADO', 'Control post-blanqueamiento', 'Revisión resultados', true),
(34, 8, 1, '2025-12-10 10:30:00', '2025-12-10 11:15:00', 'COMPLETADO', 'Periodontal', 'Tercera sesión', true),

-- Día 15 de diciembre (futuro - día completo para demostración)
(35, 1, 1, '2025-12-16 09:00:00', '2025-12-16 09:30:00', 'PROGRAMADO', 'Control mensual', 'Revisión de rutina', true),
(36, 2, 1, '2025-12-16 09:30:00', '2025-12-16 10:15:00', 'CONFIRMADO', 'Limpieza profunda', 'Limpieza periodontal', true),
(37, 3, 1, '2025-12-16 10:30:00', '2025-12-16 11:00:00', 'PROGRAMADO', 'Ortodoncia', 'Ajuste brackets', true),
(38, 4, 1, '2025-12-16 11:00:00', '2025-12-16 12:00:00', 'CONFIRMADO', 'Endodoncia', 'Tercera sesión', true),
(39, 5, 1, '2025-12-16 14:00:00', '2025-12-16 15:00:00', 'PROGRAMADO', 'Control blanqueamiento', 'Revisión resultados', true),
(40, 6, 1, '2025-12-16 15:00:00', '2025-12-16 15:45:00', 'CONFIRMADO', 'Consulta', 'Evaluación', true),
(41, 7, 1, '2025-12-16 16:00:00', '2025-12-16 16:30:00', 'PROGRAMADO', 'Limpieza', 'Profilaxis', true),
(42, 8, 1, '2025-12-16 16:30:00', '2025-12-16 17:15:00', 'CONFIRMADO', 'Periodontal', 'Cuarta sesión', true),

-- Día 18 de diciembre (futuro)
(43, 1, 1, '2025-12-18 10:00:00', '2025-12-18 11:00:00', 'PROGRAMADO', 'Consulta urgencia', 'Revisión absceso', true),
(44, 2, 1, '2025-12-18 11:00:00', '2025-12-18 11:45:00', 'PROGRAMADO', 'Corona', 'Colocación corona', true),
(45, 3, 1, '2025-12-18 14:00:00', '2025-12-18 14:30:00', 'CANCELADO', 'Control', 'Paciente canceló', true),

-- Día 20 de diciembre (futuro - día completo)
(46, 1, 1, '2025-12-20 09:00:00', '2025-12-20 09:30:00', 'CONFIRMADO', 'Control', 'Revisión post-tratamiento', true),
(47, 2, 1, '2025-12-20 09:30:00', '2025-12-20 10:15:00', 'PROGRAMADO', 'Limpieza', 'Profilaxis', true),
(48, 3, 1, '2025-12-20 10:30:00', '2025-12-20 11:00:00', 'CONFIRMADO', 'Ortodoncia', 'Control mensual', true),
(49, 4, 1, '2025-12-20 11:00:00', '2025-12-20 12:00:00', 'PROGRAMADO', 'Control implante', 'Revisión implante', true),
(50, 5, 1, '2025-12-20 14:00:00', '2025-12-20 14:30:00', 'CONFIRMADO', 'Consulta', 'Revisión general', true),
(51, 6, 1, '2025-12-20 15:00:00', '2025-12-20 15:45:00', 'PROGRAMADO', 'Sensibilidad', 'Control tratamiento', true),
(52, 7, 1, '2025-12-20 16:00:00', '2025-12-20 16:30:00', 'CONFIRMADO', 'Endodoncia', 'Segunda sesión', true),
(53, 8, 1, '2025-12-20 16:30:00', '2025-12-20 17:15:00', 'PROGRAMADO', 'Periodontal', 'Control final', true),

-- Día 23 de diciembre (futuro)
(54, 2, 1, '2025-12-23 09:00:00', '2025-12-23 09:45:00', 'PROGRAMADO', 'Control', 'Revisión corona', true),
(55, 4, 1, '2025-12-23 10:30:00', '2025-12-23 11:30:00', 'PROGRAMADO', 'Endodoncia', 'Finalización', true),

-- Día 27 de diciembre (futuro)
(56, 5, 1, '2025-12-27 09:00:00', '2025-12-27 10:00:00', 'PROGRAMADO', 'Control', 'Revisión post-blanqueamiento', true),
(57, 7, 1, '2025-12-27 10:30:00', '2025-12-27 11:30:00', 'PROGRAMADO', 'Endodoncia', 'Tercera sesión', true),

-- Día 30 de diciembre (futuro - día completo)
(58, 1, 1, '2025-12-30 09:00:00', '2025-12-30 09:30:00', 'PROGRAMADO', 'Control final año', 'Revisión anual', true),
(59, 2, 1, '2025-12-30 09:30:00', '2025-12-30 10:15:00', 'PROGRAMADO', 'Limpieza', 'Profilaxis final', true),
(60, 3, 1, '2025-12-30 10:30:00', '2025-12-30 11:00:00', 'PROGRAMADO', 'Ortodoncia', 'Último ajuste año', true),
(61, 4, 1, '2025-12-30 11:00:00', '2025-12-30 12:00:00', 'PROGRAMADO', 'Control', 'Revisión general', true),
(62, 5, 1, '2025-12-30 14:00:00', '2025-12-30 14:30:00', 'PROGRAMADO', 'Consulta', 'Evaluación final', true),
(63, 6, 1, '2025-12-30 15:00:00', '2025-12-30 15:45:00', 'PROGRAMADO', 'Control', 'Revisión', true),
(64, 7, 1, '2025-12-30 16:00:00', '2025-12-30 16:30:00', 'PROGRAMADO', 'Limpieza', 'Profilaxis', true),
(65, 8, 1, '2025-12-30 16:30:00', '2025-12-30 17:15:00', 'PROGRAMADO', 'Periodontal', 'Control', true),

-- ENERO 2026 (TODOS FUTUROS)
-- Día 3 de enero - Día completo
(66, 1, 1, '2026-01-03 09:00:00', '2026-01-03 09:30:00', 'CONFIRMADO', 'Control inicio año', 'Primera consulta del año', true),
(67, 2, 1, '2026-01-03 09:30:00', '2026-01-03 10:15:00', 'PROGRAMADO', 'Consulta', 'Evaluación inicial', true),
(68, 3, 1, '2026-01-03 10:30:00', '2026-01-03 11:00:00', 'CONFIRMADO', 'Ortodoncia', 'Ajuste mensual', true),
(69, 4, 1, '2026-01-03 11:00:00', '2026-01-03 12:00:00', 'PROGRAMADO', 'Control implante', 'Revisión', true),
(70, 5, 1, '2026-01-03 14:00:00', '2026-01-03 14:30:00', 'CONFIRMADO', 'Control', 'Revisión', true),
(71, 6, 1, '2026-01-03 15:00:00', '2026-01-03 15:45:00', 'PROGRAMADO', 'Limpieza', 'Profilaxis', true),
(72, 7, 1, '2026-01-03 16:00:00', '2026-01-03 16:30:00', 'CONFIRMADO', 'Endodoncia', 'Control post-tratamiento', true),
(73, 8, 1, '2026-01-03 16:30:00', '2026-01-03 17:15:00', 'PROGRAMADO', 'Periodontal', 'Control', true),

-- Día 8 de enero
(74, 2, 1, '2026-01-08 09:00:00', '2026-01-08 09:45:00', 'PROGRAMADO', 'Corona', 'Revisión corona', true),
(75, 4, 1, '2026-01-08 10:30:00', '2026-01-08 11:30:00', 'PROGRAMADO', 'Endodoncia', 'Control final', true),
(76, 6, 1, '2026-01-08 14:00:00', '2026-01-08 14:45:00', 'PROGRAMADO', 'Sensibilidad', 'Control tratamiento', true),

-- Día 10 de enero - Día completo
(77, 1, 1, '2026-01-10 09:00:00', '2026-01-10 09:30:00', 'PROGRAMADO', 'Control mensual', 'Revisión mensual', true),
(78, 2, 1, '2026-01-10 09:30:00', '2026-01-10 10:15:00', 'CONFIRMADO', 'Limpieza profunda', 'Profilaxis', true),
(79, 3, 1, '2026-01-10 10:30:00', '2026-01-10 11:00:00', 'PROGRAMADO', 'Ortodoncia', 'Ajuste', true),
(80, 4, 1, '2026-01-10 11:00:00', '2026-01-10 12:00:00', 'CONFIRMADO', 'Control', 'Revisión', true),
(81, 5, 1, '2026-01-10 14:00:00', '2026-01-10 14:30:00', 'PROGRAMADO', 'Consulta', 'Evaluación', true),
(82, 6, 1, '2026-01-10 15:00:00', '2026-01-10 15:45:00', 'CONFIRMADO', 'Control', 'Revisión', true),
(83, 7, 1, '2026-01-10 16:00:00', '2026-01-10 16:30:00', 'PROGRAMADO', 'Endodoncia', 'Seguimiento', true),
(84, 8, 1, '2026-01-10 16:30:00', '2026-01-10 17:15:00', 'CONFIRMADO', 'Periodontal', 'Control', true),

-- Día 15 de enero
(85, 1, 1, '2026-01-15 09:00:00', '2026-01-15 09:30:00', 'PROGRAMADO', 'Control', 'Revisión mensual', true),
(86, 3, 1, '2026-01-15 10:30:00', '2026-01-15 11:00:00', 'PROGRAMADO', 'Ortodoncia', 'Ajuste', true),
(87, 5, 1, '2026-01-15 14:00:00', '2026-01-15 15:00:00', 'PROGRAMADO', 'Blanqueamiento', 'Seguimiento', true),

-- Día 18 de enero - Día completo
(88, 1, 1, '2026-01-18 09:00:00', '2026-01-18 10:00:00', 'PROGRAMADO', 'Sensibilidad', 'Control tratamiento', true),
(89, 2, 1, '2026-01-18 10:30:00', '2026-01-18 11:15:00', 'PROGRAMADO', 'Corona', 'Control', true),
(90, 3, 1, '2026-01-18 11:30:00', '2026-01-18 12:00:00', 'PROGRAMADO', 'Ortodoncia', 'Ajuste', true),
(91, 4, 1, '2026-01-18 14:00:00', '2026-01-18 15:00:00', 'PROGRAMADO', 'Implante', 'Control', true),
(92, 5, 1, '2026-01-18 15:30:00', '2026-01-18 16:00:00', 'PROGRAMADO', 'Consulta', 'Evaluación', true),
(93, 6, 1, '2026-01-18 16:00:00', '2026-01-18 16:45:00', 'PROGRAMADO', 'Limpieza', 'Profilaxis', true),
(94, 7, 1, '2026-01-18 17:00:00', '2026-01-18 17:30:00', 'PROGRAMADO', 'Endodoncia', 'Control', true),
(95, 8, 1, '2026-01-18 17:30:00', '2026-01-18 18:15:00', 'PROGRAMADO', 'Periodontal', 'Control', true),

-- Día 22 de enero
(96, 2, 1, '2026-01-22 09:00:00', '2026-01-22 09:45:00', 'PROGRAMADO', 'Control', 'Revisión', true),
(97, 4, 1, '2026-01-22 10:30:00', '2026-01-22 11:30:00', 'PROGRAMADO', 'Endodoncia', 'Finalización', true),
(98, 6, 1, '2026-01-22 14:00:00', '2026-01-22 14:45:00', 'PROGRAMADO', 'Sensibilidad', 'Control final', true),

-- Día 25 de enero
(99, 1, 1, '2026-01-25 09:00:00', '2026-01-25 09:30:00', 'CONFIRMADO', 'Control', 'Revisión post-blanqueamiento', true),
(100, 3, 1, '2026-01-25 10:30:00', '2026-01-25 11:00:00', 'CONFIRMADO', 'Ortodoncia', 'Ajuste', true),
(101, 5, 1, '2026-01-25 14:00:00', '2026-01-25 15:00:00', 'CONFIRMADO', 'Control', 'Revisión', true),

-- Día 28 de enero - Día completo
(102, 1, 1, '2026-01-28 09:00:00', '2026-01-28 09:30:00', 'PROGRAMADO', 'Control mensual', 'Revisión', true),
(103, 2, 1, '2026-01-28 09:30:00', '2026-01-28 10:15:00', 'PROGRAMADO', 'Limpieza', 'Profilaxis', true),
(104, 3, 1, '2026-01-28 10:30:00', '2026-01-28 11:00:00', 'PROGRAMADO', 'Ortodoncia', 'Ajuste', true),
(105, 4, 1, '2026-01-28 11:00:00', '2026-01-28 12:00:00', 'PROGRAMADO', 'Control', 'Revisión', true),
(106, 5, 1, '2026-01-28 14:00:00', '2026-01-28 14:30:00', 'PROGRAMADO', 'Consulta', 'Evaluación', true),
(107, 6, 1, '2026-01-28 15:00:00', '2026-01-28 15:45:00', 'PROGRAMADO', 'Control', 'Revisión', true),
(108, 7, 1, '2026-01-28 16:00:00', '2026-01-28 16:30:00', 'PROGRAMADO', 'Endodoncia', 'Control', true),
(109, 8, 1, '2026-01-28 16:30:00', '2026-01-28 17:15:00', 'PROGRAMADO', 'Periodontal', 'Control final', true);

-- ===============================================
-- INSERTAR RECETAS MÉDICAS
-- ===============================================
-- Campos: id, patient_id, dentist_id, prescription_date, observations, medications, active
-- 80 Recetas: 10 por paciente, distribuidas en varios meses (septiembre-diciembre 2024/2025) para probar filtros
INSERT INTO prescriptions (id, patient_id, dentist_id, prescription_date, observations, medications, active) VALUES
-- Paciente 1 (María Elena Pérez) - 10 recetas
(1, 1, 1, '2024-09-15', 'Post-blanqueamiento. Analgésico solo si hay molestias.', 'Ibuprofeno 400mg - Cada 8 horas por 3 días si hay molestias', true),
(2, 1, 1, '2024-09-20', 'Post-blanqueamiento. Gel desensibilizante.', 'Gel desensibilizante con flúor - Aplicar 2 veces al día por 2 semanas', true),
(3, 1, 1, '2024-10-05', 'Mantenimiento post-blanqueamiento.', 'Pasta dental desensibilizante - Usar 2 veces al día', true),
(4, 1, 1, '2024-10-22', 'Control tratamiento estético.', 'Enjuague bucal con flúor - Usar cada noche', true),
(5, 1, 1, '2024-11-10', 'Tratamiento sensibilidad. Primera sesión.', 'Gel desensibilizante - Aplicar diariamente', true),
(6, 1, 1, '2024-11-25', 'Seguimiento sensibilidad. Segunda sesión.', 'Pasta desensibilizante - Continuar uso', true),
(7, 1, 1, '2024-12-01', 'Limpieza dental. Mantenimiento higiene.', 'Enjuague bucal con flúor - Cada noche\nHilo dental - Una vez al día', true),
(8, 1, 1, '2024-12-10', 'Control post-blanqueamiento.', 'Pasta dental desensibilizante - Mantener uso', true),
(9, 1, 1, '2024-12-18', 'Urgencia absceso dental. Antibiótico.', 'Amoxicilina 500mg - Cada 8 horas por 7 días\nIbuprofeno 400mg - Cada 8 horas por 5 días', true),
(10, 1, 1, '2024-12-27', 'Limpieza rutina. Higiene oral.', 'Enjuague bucal flúor - Cada noche\nHilo dental - Diario', true),
-- Paciente 2 (Carlos Alberto García) - 10 recetas
(11, 2, 1, '2024-08-20', 'Post-empaste. Analgésico y antibiótico.', 'Amoxicilina 500mg - Cada 8 horas por 7 días\nIbuprofeno 400mg - Cada 8 horas por 3 días', true),
(12, 2, 1, '2024-09-10', 'Preparación corona. Profilaxis.', 'Ibuprofeno 400mg - Cada 8 horas por 5 días si hay dolor\nAmoxicilina 500mg - Cada 8 horas por 7 días', true),
(13, 2, 1, '2024-10-05', 'Limpieza profunda. Mantenimiento.', 'Clorhexidina 0.12% - Enjuague 2 veces al día por 2 semanas', true),
(14, 2, 1, '2024-10-18', 'Control limpieza profunda.', 'Enjuague bucal con flúor - Usar diariamente', true),
(15, 2, 1, '2024-11-05', 'Seguimiento periodontal.', 'Cepillo cerdas suaves - Usar exclusivamente\nClorhexidina - Continuar enjuague', true),
(16, 2, 1, '2024-11-18', 'Control tratamiento periodontal.', 'Enjuague bucal - Mantener uso\nHilo dental - Usar diariamente', true),
(17, 2, 1, '2024-12-01', 'Limpieza profunda. Segunda sesión.', 'Clorhexidina 0.12% - Enjuague 2 veces al día', true),
(18, 2, 1, '2024-12-15', 'Post-corona. Analgésico preventivo.', 'Ibuprofeno 400mg - Cada 8 horas por 3 días si hay molestias', true),
(19, 2, 1, '2024-12-20', 'Limpieza rutina semestral.', 'Enjuague bucal flúor - Cada noche', true),
(20, 2, 1, '2024-12-28', 'Consulta general. Revisión.', 'Pasta dental con flúor - Continuar uso habitual', true),
-- Paciente 3 (Ana Beatriz Silva) - 10 recetas
(21, 3, 1, '2024-09-01', 'Post-limpieza. Analgésico si necesario.', 'Ibuprofeno 400mg - Cada 8 horas por 3 días si hay molestias', true),
(22, 3, 1, '2024-09-15', 'Inicio ortodoncia. Alivio molestias.', 'Cera para brackets - Aplicar según necesidad\nEnjuague clorhexidina 0.12% - 2 veces al día', true),
(23, 3, 1, '2024-10-10', 'Control ortodoncia.', 'Cepillo interdental - Usar diariamente\nEnjuague bucal - Mantener', true),
(24, 3, 1, '2024-10-25', 'Ajuste brackets. Alivio dolor.', 'Paracetamol 500mg - Cada 8 horas por 2 días\nCera ortodóncica - Aplicar si necesario', true),
(25, 3, 1, '2024-11-05', 'Control ortodoncia mensual.', 'Enjuague clorhexidina - Continuar uso\nCepillo interdental - Mantener', true),
(26, 3, 1, '2024-11-18', 'Ajuste brackets. Seguimiento.', 'Cera ortodóncica - Aplicar sobre brackets molestos', true),
(27, 3, 1, '2024-12-01', 'Control ortodoncia.', 'Enjuague bucal clorhexidina 0.12% - 2 veces al día', true),
(28, 3, 1, '2024-12-05', 'Ajuste ortodoncia. Alivio.', 'Paracetamol 500mg - Cada 8 horas por 2 días', true),
(29, 3, 1, '2024-12-15', 'Control ortodoncia rutina.', 'Cepillo interdental - Diario\nEnjuague - Mantener', true),
(30, 3, 1, '2024-12-22', 'Mantenimiento ortodoncia.', 'Enjuague clorhexidina - 2 veces al día', true),
-- Paciente 4 (Roberto Carlos Morales) - 10 recetas
(31, 4, 1, '2024-09-25', 'Post-extracción cordal. Antibiótico.', 'Ibuprofeno 400mg - Cada 8 horas por 5 días\nClindamicina 300mg - Cada 8 horas por 7 días', true),
(32, 4, 1, '2024-10-10', 'Post-implante. Profilaxis intensiva.', 'Amoxicilina 500mg - Cada 8 horas por 7 días\nClorhexidina 0.12% - Enjuague 2 veces al día\nNO fumar ni alcohol', true),
(33, 4, 1, '2024-10-20', 'Inicio periodontal. Antiséptico.', 'Clorhexidina 0.12% - Enjuague 2 veces al día por 2 semanas', true),
(34, 4, 1, '2024-11-05', 'Control implante. Mantenimiento.', 'Enjuague clorhexidina - Continuar uso\nIbuprofeno - Si hay dolor', true),
(35, 4, 1, '2024-11-12', 'Seguimiento periodontal.', 'Clorhexidina - Mantener enjuague\nCepillo suave - Usar exclusivamente', true),
(36, 4, 1, '2024-11-18', 'Control periodontal. Segunda sesión.', 'Enjuague bucal - Continuar\nHilo dental - Agregar uso diario', true),
(37, 4, 1, '2024-12-01', 'Endodoncia. Primera sesión.', 'Ibuprofeno 400mg - Cada 8 horas por 5 días si hay dolor', true),
(38, 4, 1, '2024-12-08', 'Endodoncia. Segunda sesión.', 'Enjuague flúor - Usar diariamente\nIbuprofeno - Si hay sensibilidad', true),
(39, 4, 1, '2024-12-12', 'Control post-endodoncia.', 'Enjuague bucal flúor - Mantener\nIbuprofeno - Solo si necesario', true),
(40, 4, 1, '2024-12-25', 'Seguimiento implante. Control.', 'Enjuague clorhexidina - Mantener\nRadiografía control programada', true),
-- Paciente 5 (Laura Patricia Vargas) - 10 recetas
(41, 5, 1, '2024-08-10', 'Post-empaste múltiple. Antibiótico.', 'Amoxicilina 500mg - Cada 8 horas por 7 días\nIbuprofeno 400mg - Cada 8 horas por 3 días', true),
(42, 5, 1, '2024-09-20', 'Rehabilitación oral. Suplementación.', 'Calcio 600mg - 2 comprimidos al día\nVitamina D 2000 UI - 1 cápsula al día', true),
(43, 5, 1, '2024-10-15', 'Control rehabilitación.', 'Enjuague bucal - Mantener higiene\nSuplementos - Continuar', true),
(44, 5, 1, '2024-11-01', 'Inicio blanqueamiento. Preparación.', 'Pasta dental desensibilizante - Comenzar uso', true),
(45, 5, 1, '2024-11-15', 'Blanqueamiento. Primera sesión.', 'Gel desensibilizante - Aplicar 2 veces al día', true),
(46, 5, 1, '2024-11-25', 'Blanqueamiento. Segunda sesión.', 'Pasta desensibilizante - Mantener uso\nGel - Continuar aplicación', true),
(47, 5, 1, '2024-12-01', 'Control blanqueamiento.', 'Pasta desensibilizante - Continuar\nEnjuague flúor - Agregar', true),
(48, 5, 1, '2024-12-08', 'Blanqueamiento. Tercera sesión.', 'Gel desensibilizante - Mantener aplicación', true),
(49, 5, 1, '2024-12-15', 'Seguimiento periodontal.', 'Clorhexidina 0.12% - Enjuague 2 veces al día', true),
(50, 5, 1, '2024-12-27', 'Control post-blanqueamiento.', 'Pasta dental - Mantener desensibilizante', true),
-- Paciente 6 (Diego Alejandro Torres) - 10 recetas
(51, 6, 1, '2024-09-05', 'Post-limpieza. Rutina.', 'Enjuague bucal flúor - Usar cada noche', true),
(52, 6, 1, '2024-10-15', 'Preparación corona. Profilaxis.', 'Ibuprofeno 400mg - Cada 8 horas por 5 días si hay dolor\nAmoxicilina - Preventivo', true),
(53, 6, 1, '2024-10-28', 'Control preparación.', 'Enjuague bucal - Mantener higiene', true),
(54, 6, 1, '2024-11-01', 'Inicio sensibilidad. Tratamiento.', 'Gel desensibilizante - Aplicar 2 veces al día', true),
(55, 6, 1, '2024-11-08', 'Sensibilidad. Primera sesión.', 'Pasta desensibilizante - Comenzar uso', true),
(56, 6, 1, '2024-11-15', 'Seguimiento sensibilidad.', 'Gel - Mantener aplicación\nPasta - Continuar uso', true),
(57, 6, 1, '2024-11-25', 'Control sensibilidad. Segunda sesión.', 'Pasta desensibilizante - Mantener\nEnjuague flúor - Agregar', true),
(58, 6, 1, '2024-12-01', 'Control tratamiento.', 'Enjuague bucal flúor - Cada noche', true),
(59, 6, 1, '2024-12-08', 'Sensibilidad. Tercera sesión.', 'Gel desensibilizante - Mantener aplicación', true),
(60, 6, 1, '2024-12-15', 'Control final sensibilidad.', 'Pasta desensibilizante - Continuar uso', true),
-- Paciente 7 (Valentina Jiménez) - 10 recetas
(61, 7, 1, '2024-08-25', 'Post-extracción. Antibiótico.', 'Ibuprofeno 400mg - Cada 8 horas por 5 días\nClindamicina 300mg - Cada 8 horas por 7 días', true),
(62, 7, 1, '2024-09-15', 'Inicio ortodoncia. Cuidados.', 'Cera para brackets - Aplicar según necesidad\nEnjuague clorhexidina - 2 veces al día', true),
(63, 7, 1, '2024-10-10', 'Control ortodoncia.', 'Cepillo interdental - Usar diariamente', true),
(64, 7, 1, '2024-10-25', 'Endodoncia. Primera sesión.', 'Ibuprofeno 400mg - Cada 8 horas por 5 días\nClindamicina - Si hay infección', true),
(65, 7, 1, '2024-11-05', 'Control endodoncia.', 'Enjuague flúor - Usar diariamente\nIbuprofeno - Solo si hay dolor', true),
(66, 7, 1, '2024-11-12', 'Endodoncia. Segunda sesión.', 'Ibuprofeno - Si hay sensibilidad post-tratamiento', true),
(67, 7, 1, '2024-11-18', 'Seguimiento endodoncia.', 'Enjuague bucal flúor - Mantener uso diario', true),
(68, 7, 1, '2024-12-01', 'Control ortodoncia.', 'Enjuague clorhexidina - Continuar\nCepillo interdental - Mantener', true),
(69, 7, 1, '2024-12-08', 'Endodoncia. Seguimiento.', 'Enjuague flúor - Continuar\nRadiografía control programada', true),
(70, 7, 1, '2024-12-22', 'Control post-endodoncia.', 'Enjuague bucal - Mantener higiene oral', true),
-- Paciente 8 (Fernando Herrera) - 10 recetas
(71, 8, 1, '2024-09-30', 'Post-limpieza profunda. Antiséptico.', 'Clorhexidina 0.12% - Enjuague 2 veces al día por 2 semanas', true),
(72, 8, 1, '2024-10-30', 'Post-implante. Profilaxis.', 'Amoxicilina 500mg - Cada 8 horas por 7 días\nClorhexidina - Enjuague 2 veces al día', true),
(73, 8, 1, '2024-11-08', 'Inicio periodontal. Antiséptico.', 'Clorhexidina 0.12% - Enjuague 2 veces al día\nCepillo cerdas suaves - Usar exclusivamente', true),
(74, 8, 1, '2024-11-15', 'Control implante. Mantenimiento.', 'Enjuague clorhexidina - Continuar\nIbuprofeno - Si hay molestias', true),
(75, 8, 1, '2024-11-22', 'Periodontal. Primera sesión.', 'Clorhexidina - Mantener enjuague\nCepillo suave - Continuar uso', true),
(76, 8, 1, '2024-11-29', 'Control periodontal.', 'Enjuague bucal - Continuar\nHilo dental - Agregar uso diario', true),
(77, 8, 1, '2024-12-01', 'Periodontal. Segunda sesión.', 'Clorhexidina - Mantener enjuague 2 veces al día', true),
(78, 8, 1, '2024-12-08', 'Seguimiento periodontal.', 'Enjuague clorhexidina - Continuar\nCepillo suave - Mantener', true),
(79, 8, 1, '2024-12-15', 'Control periodontal. Tercera sesión.', 'Clorhexidina 0.12% - Enjuague 2 veces al día', true),
(80, 8, 1, '2024-12-22', 'Periodontal. Control final.', 'Enjuague bucal - Mantener higiene oral completa', true);

-- ===============================================
-- INSERTAR HISTORIA CLÍNICA
-- ===============================================
-- Campos: id, patient_id, dentist_id, entry_date, description, prescription_id, treatment_id, active
-- Nota: entry_date se puede omitir y se asignará automáticamente, pero aquí la incluimos para datos de prueba consistentes
INSERT INTO medical_history (id, patient_id, dentist_id, entry_date, description, prescription_id, treatment_id, active) VALUES
-- Entradas vinculadas a tratamientos (sesiones de tratamiento) - María (patient_id = 1)
(1, 1, 1, '2024-09-15', 'Limpieza dental profesional completada exitosamente. Paciente con buena higiene oral, mínima placa bacteriana detectada. Se realizó profilaxis dental completa y aplicación tópica de flúor. Paciente sin molestias post-tratamiento.', 1, 1, true),
(2, 2, 1, '2024-08-20', 'Restauración de caries en molar 16 (superior derecho). Caries moderada sin compromiso pulpar confirmada mediante radiografía. Empaste realizado con resina compuesta de alta calidad. Oclusión verificada. Paciente sin molestias post-tratamiento.', NULL, 4, true),
(3, 3, 1, '2024-09-15', 'Inicio de tratamiento ortodóncico con brackets metálicos convencionales. Maloclusión clase II con apiñamiento moderado diagnosticado. Colocación de brackets en arcada superior e inferior. Progreso esperado en 18-24 meses. Paciente informado sobre cuidados y alimentación.', NULL, 8, true),
(4, 4, 1, '2024-10-10', 'Inicio de endodoncia en premolar 44 (inferior derecho). Dolor intenso con necrosis pulpar confirmada mediante pruebas de vitalidad y radiografía. Primera sesión completada exitosamente: limpieza y desinfección del conducto. Próxima sesión programada en 7 días.', NULL, 11, true),
(5, 5, 1, '2024-10-20', 'Evaluación periodontal inicial completa. Bolsones de 4-6mm con sangrado al sondaje detectados. Diagnóstico: periodontitis moderada. Inicio de tratamiento no quirúrgico: raspado y alisado radicular en cuadrante superior derecho. Paciente informado sobre importancia de higiene oral.', NULL, 12, true),
(6, 1, 1, '2024-09-15', 'Inicio de blanqueamiento dental con peróxido de carbamida al 16%. Coloración amarillenta generalizada en dientes anteriores. Buena salud dental general sin caries activas. Primera sesión completada con resultados prometedores. Instrucciones de uso de férulas domiciliarias proporcionadas.', NULL, 1, true),
(7, 2, 1, '2024-09-10', 'Preparación para corona dental de porcelana. Diente tratado endodónticamente (molar 16) requiere protección con corona. Preparación del muñón completada exitosamente. Impresión tomada para laboratorio. Próxima cita programada para colocación de corona definitiva en 2 semanas.', NULL, 5, true),
(8, 3, 1, '2024-09-25', 'Extracción quirúrgica de muela del juicio superior derecha (cordal) impactada. Procedimiento realizado bajo anestesia local. Extracción completada sin complicaciones. Hemostasia obtenida. Radiografía post-operatoria muestra extracción completa. Instrucciones post-operatorias proporcionadas.', NULL, 10, true),
(9, 4, 1, '2024-10-10', 'Primera fase de colocación de implante dental de titanio en zona posterior mandibular (región de premolares). Implante de 4.5mm de diámetro colocado con buen anclaje óseo primario confirmado. Sutura realizada. Se espera osteointegración en 3-4 meses. Próxima fase: colocación de pilar y corona.', NULL, 11, true),
(10, 5, 1, '2024-09-20', 'Evaluación inicial para rehabilitación oral integral. Pérdida múltiple de dientes posteriores en ambos maxilares. Plan de tratamiento establecido: prótesis parcial removible. Impresiones preliminares tomadas. Paciente informado sobre opciones de tratamiento y tiempos estimados.', NULL, 14, true),

-- Entradas de control y seguimiento - María (patient_id = 1) - Fechas actualizadas a 2025
(11, 1, 1, '2024-12-01', 'Control post-blanqueamiento realizado en septiembre. Resultados excelentes mantenidos, paciente muy satisfecho con el resultado obtenido. Dientes con aspecto saludable y brillante. Sin sensibilidad residual. Higiene oral adecuada. Recomendación de control semestral.', NULL, NULL, true),
(12, 1, 1, '2024-12-05', 'Consulta de rutina. Revisión general completa de la salud bucal. Paciente mantiene excelente higiene oral. Sin caries nuevas detectadas. Encías saludables sin signos de inflamación. Radiografías de control sin alteraciones. Recomendación de mantener rutina de cepillado actual.', NULL, NULL, true),
(13, 2, 1, '2024-12-10', 'Finalización de tratamiento de corona dental. Corona de porcelana colocada exitosamente en molar 16. Ajuste oclusal verificado. Paciente sin molestias. Oclusión correcta confirmada. Se programó control en 6 meses para verificar estado de la corona y tejidos circundantes.', NULL, NULL, true),
(14, 3, 1, '2024-12-08', 'Ajuste de brackets mensual. Progreso satisfactorio en la corrección de la maloclusión. Alineación dental mejorando notablemente según plan de tratamiento. Cambio de arcos realizado. Paciente colaborador, sigue indicaciones correctamente. Próximo ajuste programado en 4 semanas.', NULL, NULL, true),
(15, 4, 1, '2024-12-12', 'Control post-endodoncia. Diente tratado asintomático, sin dolor ni sensibilidad a la percusión. Radiografía de control muestra buena obturación del conducto radicular. Sin signos de patología periapical. Tratamiento exitoso. Paciente informado sobre importancia de corona protectora.', NULL, NULL, true),
(16, 5, 1, '2024-12-15', 'Seguimiento periodontal. Mejora significativa en la salud gingival desde inicio del tratamiento. Reducción de bolsones de 4-6mm a 2-3mm confirmada mediante sondaje. Paciente mantiene buena higiene oral. Continuar con enjuague de clorhexidina según indicación. Próxima sesión en 1 mes.', NULL, NULL, true),
(17, 1, 1, '2024-12-18', 'Consulta de urgencia por dolor intenso en molar superior izquierdo. Diagnóstico: absceso dental periapical. Tratamiento realizado: drenaje del absceso, limpieza de la zona afectada. Prescripción de antibióticos y analgésicos. Control programado en 3 días para evaluar evolución. Paciente informado sobre signos de alarma.', 9, NULL, true),
(18, 2, 1, '2024-12-20', 'Limpieza dental de rutina semestral. Buena salud bucal general. Sin problemas detectados. Profilaxis dental realizada. Aplicación de flúor tópico. Paciente mantiene adecuada higiene oral. Recomendación de mantener hábitos actuales y retornar en 6 meses para próximo control.', NULL, NULL, true),
(19, 3, 1, '2024-12-22', 'Control de ortodoncia de rutina. Brackets funcionando correctamente, sin despegues ni fracturas. Paciente colaborador con el tratamiento, sigue todas las indicaciones. Progreso según lo esperado según plan de tratamiento. Ajuste menor de arcos realizado. Paciente motivado.', NULL, NULL, true),
(20, 4, 1, '2024-12-25', 'Seguimiento de implante dental. Radiografía de control muestra buena osteointegración del implante colocado en octubre. Implante estable, sin signos de rechazo ni movilidad. Tejidos blandos circundantes saludables. Próxima fase (colocación de pilar y corona) programada para dentro de 1 mes.', NULL, 11, true),

-- Más entradas para tener variedad de datos - María (patient_id = 1) - Fechas actualizadas
(21, 1, 1, '2024-11-15', 'Consulta de seguimiento. Revisión de sensibilidad dental post-blanqueamiento. Mejora notable en la sensibilidad. Segunda sesión de aplicación de gel desensibilizante realizada. Paciente reporta mayor confort. Continuar con tratamiento domiciliario.', 5, NULL, true),
(22, 2, 1, '2024-11-20', 'Evaluación estética. Planificación de rehabilitación estética con carillas de porcelana. Estudio fotográfico realizado. Impresiones para mock-up tomadas. Paciente informado sobre proceso y expectativas realistas.', NULL, 5, true),
(23, 3, 1, '2024-11-10', 'Control ortodoncia. Progreso normal del tratamiento. Alineación dental mejorando progresivamente. Cambio de elásticos realizado. Paciente sigue indicaciones correctamente. Motivación del paciente excelente.', NULL, NULL, true),
(24, 4, 1, '2024-11-25', 'Consulta de urgencia por dolor post-endodoncia. Evaluación realizada: normal post-tratamiento. Dolor leve esperado. Prescripción de analgésicos. Control en 1 semana programado si persisten molestias.', NULL, NULL, true),
(25, 5, 1, '2024-11-30', 'Seguimiento rehabilitación oral. Prótesis parcial removible funcionando correctamente. Ajuste menor realizado para mejorar adaptación. Paciente satisfecho con resultado. Instrucciones de cuidado y limpieza reforzadas.', NULL, NULL, true);

-- ===============================================
-- INSERTAR ODONTOGRAMAS
-- ===============================================
-- Campos: id, patient_id, dentition_type, teeth_data, created_datetime, last_updated_datetime, created_user, last_updated_user, is_active
-- Odontogramas históricos para poder ver la evolución del estado dental de los pacientes
-- Los teeth_data están en formato JSON con los estados de cada diente

-- Odontogramas de María (patient_id = 1) - Evolución a lo largo del tiempo
INSERT INTO odontograms (id, patient_id, dentition_type, teeth_data, created_datetime, last_updated_datetime, created_user, last_updated_user, is_active) VALUES
-- Odontograma inicial de María (septiembre 2024) - Estado inicial con algunas caries y dientes con trabajos previos
(1, 1, 'adult', '{"18":{"statuses":["healthy"]},"17":{"statuses":["healthy"]},"16":{"statuses":["cavity-repair"]},"15":{"statuses":["previous-work"]},"14":{"statuses":["healthy"]},"13":{"statuses":["healthy"]},"12":{"statuses":["healthy"]},"11":{"statuses":["healthy"]},"21":{"statuses":["healthy"]},"22":{"statuses":["healthy"]},"23":{"statuses":["healthy"]},"24":{"statuses":["healthy"]},"25":{"statuses":["previous-work"]},"26":{"statuses":["healthy"]},"27":{"statuses":["healthy"]},"28":{"statuses":["missing"]},"48":{"statuses":["missing"]},"47":{"statuses":["healthy"]},"46":{"statuses":["previous-work"]},"45":{"statuses":["healthy"]},"44":{"statuses":["healthy"]},"43":{"statuses":["healthy"]},"42":{"statuses":["healthy"]},"41":{"statuses":["healthy"]},"31":{"statuses":["healthy"]},"32":{"statuses":["healthy"]},"33":{"statuses":["healthy"]},"34":{"statuses":["healthy"]},"35":{"statuses":["healthy"]},"36":{"statuses":["cavity-repair"]},"37":{"statuses":["healthy"]},"38":{"statuses":["healthy"]}}', '2024-09-15 09:30:00', '2024-09-15 09:30:00', 2, 2, true),

-- Odontograma post-blanqueamiento (octubre 2024)
(2, 1, 'adult', '{"18":{"statuses":["healthy"]},"17":{"statuses":["healthy"]},"16":{"statuses":["previous-work"]},"15":{"statuses":["previous-work"]},"14":{"statuses":["healthy"]},"13":{"statuses":["healthy"]},"12":{"statuses":["healthy"]},"11":{"statuses":["healthy"]},"21":{"statuses":["healthy"]},"22":{"statuses":["healthy"]},"23":{"statuses":["healthy"]},"24":{"statuses":["healthy"]},"25":{"statuses":["previous-work"]},"26":{"statuses":["healthy"]},"27":{"statuses":["healthy"]},"28":{"statuses":["missing"]},"48":{"statuses":["missing"]},"47":{"statuses":["healthy"]},"46":{"statuses":["previous-work"]},"45":{"statuses":["healthy"]},"44":{"statuses":["healthy"]},"43":{"statuses":["healthy"]},"42":{"statuses":["healthy"]},"41":{"statuses":["healthy"]},"31":{"statuses":["healthy"]},"32":{"statuses":["healthy"]},"33":{"statuses":["healthy"]},"34":{"statuses":["healthy"]},"35":{"statuses":["healthy"]},"36":{"statuses":["previous-work"]},"37":{"statuses":["healthy"]},"38":{"statuses":["healthy"]}}', '2024-10-15 15:30:00', '2024-10-15 15:30:00', 2, 2, true),

-- Odontograma durante tratamiento sensibilidad (noviembre 2024)
(3, 1, 'adult', '{"18":{"statuses":["healthy"]},"17":{"statuses":["healthy"]},"16":{"statuses":["previous-work"]},"15":{"statuses":["previous-work"]},"14":{"statuses":["cavity-repair"]},"13":{"statuses":["healthy"]},"12":{"statuses":["healthy"]},"11":{"statuses":["healthy"]},"21":{"statuses":["healthy"]},"22":{"statuses":["healthy"]},"23":{"statuses":["healthy"]},"24":{"statuses":["healthy"]},"25":{"statuses":["previous-work"]},"26":{"statuses":["healthy"]},"27":{"statuses":["healthy"]},"28":{"statuses":["missing"]},"48":{"statuses":["missing"]},"47":{"statuses":["healthy"]},"46":{"statuses":["previous-work"]},"45":{"statuses":["healthy"]},"44":{"statuses":["healthy"]},"43":{"statuses":["healthy"]},"42":{"statuses":["healthy"]},"41":{"statuses":["healthy"]},"31":{"statuses":["healthy"]},"32":{"statuses":["healthy"]},"33":{"statuses":["healthy"]},"34":{"statuses":["healthy"]},"35":{"statuses":["healthy"]},"36":{"statuses":["previous-work"]},"37":{"statuses":["healthy"]},"38":{"statuses":["healthy"]}}', '2024-11-25 14:00:00', '2024-11-25 14:00:00', 2, 2, true),

-- Odontograma reciente (diciembre 2024) - Control post-tratamiento sensibilidad
(4, 1, 'adult', '{"18":{"statuses":["healthy"]},"17":{"statuses":["healthy"]},"16":{"statuses":["previous-work"]},"15":{"statuses":["previous-work"]},"14":{"statuses":["cavity-repair"]},"13":{"statuses":["healthy"]},"12":{"statuses":["healthy"]},"11":{"statuses":["healthy"]},"21":{"statuses":["healthy"]},"22":{"statuses":["healthy"]},"23":{"statuses":["healthy"]},"24":{"statuses":["healthy"]},"25":{"statuses":["previous-work"]},"26":{"statuses":["healthy"]},"27":{"statuses":["healthy"]},"28":{"statuses":["missing"]},"48":{"statuses":["missing"]},"47":{"statuses":["healthy"]},"46":{"statuses":["previous-work"]},"45":{"statuses":["healthy"]},"44":{"statuses":["healthy"]},"43":{"statuses":["healthy"]},"42":{"statuses":["healthy"]},"41":{"statuses":["healthy"]},"31":{"statuses":["healthy"]},"32":{"statuses":["healthy"]},"33":{"statuses":["healthy"]},"34":{"statuses":["healthy"]},"35":{"statuses":["healthy"]},"36":{"statuses":["previous-work"]},"37":{"statuses":["healthy"]},"38":{"statuses":["healthy"]}}', '2024-12-10 10:45:00', '2024-12-10 10:45:00', 2, 2, true);

-- Odontogramas de otros pacientes

-- Juan (patient_id = 2) - Paciente con empaste y corona en progreso
INSERT INTO odontograms (id, patient_id, dentition_type, teeth_data, created_datetime, last_updated_datetime, created_user, last_updated_user, is_active) VALUES
(5, 2, 'adult', '{"18":{"statuses":["healthy"]},"17":{"statuses":["healthy"]},"16":{"statuses":["crown-pending"]},"15":{"statuses":["healthy"]},"14":{"statuses":["healthy"]},"13":{"statuses":["healthy"]},"12":{"statuses":["healthy"]},"11":{"statuses":["healthy"]},"21":{"statuses":["healthy"]},"22":{"statuses":["healthy"]},"23":{"statuses":["healthy"]},"24":{"statuses":["healthy"]},"25":{"statuses":["healthy"]},"26":{"statuses":["previous-work"]},"27":{"statuses":["healthy"]},"28":{"statuses":["healthy"]},"48":{"statuses":["healthy"]},"47":{"statuses":["healthy"]},"46":{"statuses":["healthy"]},"45":{"statuses":["cavity-repair"]},"44":{"statuses":["healthy"]},"43":{"statuses":["healthy"]},"42":{"statuses":["healthy"]},"41":{"statuses":["healthy"]},"31":{"statuses":["healthy"]},"32":{"statuses":["healthy"]},"33":{"statuses":["healthy"]},"34":{"statuses":["healthy"]},"35":{"statuses":["healthy"]},"36":{"statuses":["healthy"]},"37":{"statuses":["healthy"]},"38":{"statuses":["healthy"]}}', '2024-09-10 10:45:00', '2024-09-10 10:45:00', 2, 2, true),

-- Odontograma más reciente de Carlos (diciembre 2024) - Corona abandonada, limpieza profunda en curso
(6, 2, 'adult', '{"18":{"statuses":["healthy"]},"17":{"statuses":["healthy"]},"16":{"statuses":["previous-work"]},"15":{"statuses":["healthy"]},"14":{"statuses":["healthy"]},"13":{"statuses":["healthy"]},"12":{"statuses":["healthy"]},"11":{"statuses":["healthy"]},"21":{"statuses":["healthy"]},"22":{"statuses":["healthy"]},"23":{"statuses":["healthy"]},"24":{"statuses":["healthy"]},"25":{"statuses":["healthy"]},"26":{"statuses":["previous-work"]},"27":{"statuses":["healthy"]},"28":{"statuses":["healthy"]},"48":{"statuses":["healthy"]},"47":{"statuses":["healthy"]},"46":{"statuses":["healthy"]},"45":{"statuses":["previous-work"]},"44":{"statuses":["healthy"]},"43":{"statuses":["healthy"]},"42":{"statuses":["healthy"]},"41":{"statuses":["healthy"]},"31":{"statuses":["healthy"]},"32":{"statuses":["healthy"]},"33":{"statuses":["healthy"]},"34":{"statuses":["healthy"]},"35":{"statuses":["healthy"]},"36":{"statuses":["healthy"]},"37":{"statuses":["healthy"]},"38":{"statuses":["healthy"]}}', '2024-12-20 11:00:00', '2024-12-20 11:00:00', 2, 2, true);

-- Carlos (patient_id = 3) - Paciente con ortodoncia, cordal extraída
INSERT INTO odontograms (id, patient_id, dentition_type, teeth_data, created_datetime, last_updated_datetime, created_user, last_updated_user, is_active) VALUES
(7, 3, 'adult', '{"18":{"statuses":["extraction-pending"]},"17":{"statuses":["healthy"]},"16":{"statuses":["healthy"]},"15":{"statuses":["healthy"]},"14":{"statuses":["healthy"]},"13":{"statuses":["healthy"]},"12":{"statuses":["healthy"]},"11":{"statuses":["healthy"]},"21":{"statuses":["healthy"]},"22":{"statuses":["healthy"]},"23":{"statuses":["healthy"]},"24":{"statuses":["healthy"]},"25":{"statuses":["healthy"]},"26":{"statuses":["healthy"]},"27":{"statuses":["healthy"]},"28":{"statuses":["healthy"]},"48":{"statuses":["healthy"]},"47":{"statuses":["healthy"]},"46":{"statuses":["cavity-repair"]},"45":{"statuses":["healthy"]},"44":{"statuses":["healthy"]},"43":{"statuses":["healthy"]},"42":{"statuses":["healthy"]},"41":{"statuses":["healthy"]},"31":{"statuses":["healthy"]},"32":{"statuses":["healthy"]},"33":{"statuses":["healthy"]},"34":{"statuses":["healthy"]},"35":{"statuses":["healthy"]},"36":{"statuses":["healthy"]},"37":{"statuses":["healthy"]},"38":{"statuses":["healthy"]}}', '2024-09-15 14:45:00', '2024-09-15 14:45:00', 2, 2, true),

-- Odontograma post-extracción (septiembre 2024)
(8, 3, 'adult', '{"18":{"statuses":["missing"]},"17":{"statuses":["healthy"]},"16":{"statuses":["healthy"]},"15":{"statuses":["healthy"]},"14":{"statuses":["healthy"]},"13":{"statuses":["healthy"]},"12":{"statuses":["healthy"]},"11":{"statuses":["healthy"]},"21":{"statuses":["healthy"]},"22":{"statuses":["healthy"]},"23":{"statuses":["healthy"]},"24":{"statuses":["healthy"]},"25":{"statuses":["healthy"]},"26":{"statuses":["healthy"]},"27":{"statuses":["healthy"]},"28":{"statuses":["healthy"]},"48":{"statuses":["healthy"]},"47":{"statuses":["healthy"]},"46":{"statuses":["previous-work"]},"45":{"statuses":["healthy"]},"44":{"statuses":["healthy"]},"43":{"statuses":["healthy"]},"42":{"statuses":["healthy"]},"41":{"statuses":["healthy"]},"31":{"statuses":["healthy"]},"32":{"statuses":["healthy"]},"33":{"statuses":["healthy"]},"34":{"statuses":["healthy"]},"35":{"statuses":["healthy"]},"36":{"statuses":["healthy"]},"37":{"statuses":["healthy"]},"38":{"statuses":["healthy"]}}', '2024-09-25 15:30:00', '2024-09-25 15:30:00', 2, 2, true);

-- Laura (patient_id = 4) - Paciente con endodoncia e implante en progreso
INSERT INTO odontograms (id, patient_id, dentition_type, teeth_data, created_datetime, last_updated_datetime, created_user, last_updated_user, is_active) VALUES
(9, 4, 'adult', '{"18":{"statuses":["healthy"]},"17":{"statuses":["healthy"]},"16":{"statuses":["healthy"]},"15":{"statuses":["healthy"]},"14":{"statuses":["healthy"]},"13":{"statuses":["healthy"]},"12":{"statuses":["healthy"]},"11":{"statuses":["healthy"]},"21":{"statuses":["healthy"]},"22":{"statuses":["healthy"]},"23":{"statuses":["healthy"]},"24":{"statuses":["healthy"]},"25":{"statuses":["healthy"]},"26":{"statuses":["healthy"]},"27":{"statuses":["healthy"]},"28":{"statuses":["healthy"]},"48":{"statuses":["healthy"]},"47":{"statuses":["healthy"]},"46":{"statuses":["missing"]},"45":{"statuses":["missing"]},"44":{"statuses":["cavity-repair","crown-pending"]},"43":{"statuses":["healthy"]},"42":{"statuses":["healthy"]},"41":{"statuses":["healthy"]},"31":{"statuses":["healthy"]},"32":{"statuses":["healthy"]},"33":{"statuses":["healthy"]},"34":{"statuses":["healthy"]},"35":{"statuses":["previous-work"]},"36":{"statuses":["healthy"]},"37":{"statuses":["healthy"]},"38":{"statuses":["healthy"]}}', '2024-10-10 11:15:00', '2024-10-10 11:15:00', 2, 2, true),

-- Odontograma post-implante (octubre 2024) - Implante abandonado
(10, 4, 'adult', '{"18":{"statuses":["healthy"]},"17":{"statuses":["healthy"]},"16":{"statuses":["healthy"]},"15":{"statuses":["healthy"]},"14":{"statuses":["healthy"]},"13":{"statuses":["healthy"]},"12":{"statuses":["healthy"]},"11":{"statuses":["healthy"]},"21":{"statuses":["healthy"]},"22":{"statuses":["healthy"]},"23":{"statuses":["healthy"]},"24":{"statuses":["healthy"]},"25":{"statuses":["healthy"]},"26":{"statuses":["healthy"]},"27":{"statuses":["healthy"]},"28":{"statuses":["healthy"]},"48":{"statuses":["healthy"]},"47":{"statuses":["healthy"]},"46":{"statuses":["missing"]},"45":{"statuses":["missing"]},"44":{"statuses":["previous-work"]},"43":{"statuses":["healthy"]},"42":{"statuses":["healthy"]},"41":{"statuses":["healthy"]},"31":{"statuses":["healthy"]},"32":{"statuses":["healthy"]},"33":{"statuses":["healthy"]},"34":{"statuses":["healthy"]},"35":{"statuses":["previous-work"]},"36":{"statuses":["healthy"]},"37":{"statuses":["healthy"]},"38":{"statuses":["healthy"]}}', '2024-11-08 10:00:00', '2024-11-08 10:00:00', 2, 2, true);

-- Pedro (patient_id = 5) - Paciente con tratamiento periodontal y rehabilitación
INSERT INTO odontograms (id, patient_id, dentition_type, teeth_data, created_datetime, last_updated_datetime, created_user, last_updated_user, is_active) VALUES
(11, 5, 'adult', '{"18":{"statuses":["missing"]},"17":{"statuses":["previous-work"]},"16":{"statuses":["previous-work"]},"15":{"statuses":["cavity-repair"]},"14":{"statuses":["healthy"]},"13":{"statuses":["healthy"]},"12":{"statuses":["healthy"]},"11":{"statuses":["healthy"]},"21":{"statuses":["healthy"]},"22":{"statuses":["healthy"]},"23":{"statuses":["healthy"]},"24":{"statuses":["healthy"]},"25":{"statuses":["cavity-repair"]},"26":{"statuses":["previous-work"]},"27":{"statuses":["missing"]},"28":{"statuses":["missing"]},"48":{"statuses":["missing"]},"47":{"statuses":["missing"]},"46":{"statuses":["missing"]},"45":{"statuses":["healthy"]},"44":{"statuses":["healthy"]},"43":{"statuses":["healthy"]},"42":{"statuses":["healthy"]},"41":{"statuses":["healthy"]},"31":{"statuses":["healthy"]},"32":{"statuses":["healthy"]},"33":{"statuses":["healthy"]},"34":{"statuses":["healthy"]},"35":{"statuses":["healthy"]},"36":{"statuses":["missing"]},"37":{"statuses":["missing"]},"38":{"statuses":["missing"]}}', '2024-09-20 09:30:00', '2024-09-20 09:30:00', 2, 2, true),

-- Odontograma post-tratamiento periodontal (diciembre 2024)
(12, 5, 'adult', '{"18":{"statuses":["missing"]},"17":{"statuses":["previous-work"]},"16":{"statuses":["previous-work"]},"15":{"statuses":["previous-work"]},"14":{"statuses":["healthy"]},"13":{"statuses":["healthy"]},"12":{"statuses":["healthy"]},"11":{"statuses":["healthy"]},"21":{"statuses":["healthy"]},"22":{"statuses":["healthy"]},"23":{"statuses":["healthy"]},"24":{"statuses":["healthy"]},"25":{"statuses":["previous-work"]},"26":{"statuses":["previous-work"]},"27":{"statuses":["missing"]},"28":{"statuses":["missing"]},"48":{"statuses":["missing"]},"47":{"statuses":["missing"]},"46":{"statuses":["missing"]},"45":{"statuses":["healthy"]},"44":{"statuses":["healthy"]},"43":{"statuses":["healthy"]},"42":{"statuses":["healthy"]},"41":{"statuses":["healthy"]},"31":{"statuses":["healthy"]},"32":{"statuses":["healthy"]},"33":{"statuses":["healthy"]},"34":{"statuses":["healthy"]},"35":{"statuses":["healthy"]},"36":{"statuses":["missing"]},"37":{"statuses":["missing"]},"38":{"statuses":["missing"]}}', '2024-12-15 17:00:00', '2024-12-15 17:00:00', 2, 2, true);

-- ===============================================
-- INSERTAR CONVERSACIONES DE CHAT
-- ===============================================
-- Campos: id, dentist_id, patient_id, last_message_datetime, last_message_preview, dentist_unread_count, patient_unread_count, created_datetime, last_updated_datetime, is_active
-- 8 Conversaciones: 1 por paciente
INSERT INTO conversations (id, dentist_id, patient_id, last_message_datetime, last_message_preview, dentist_unread_count, patient_unread_count, created_datetime, last_updated_datetime, is_active) VALUES
(1, 1, 1, '2024-12-10 15:30:00', 'Gracias por la información doctora', 0, 0, '2024-09-15 10:00:00', '2024-12-10 15:30:00', true),
(2, 1, 2, '2024-12-08 14:20:00', 'Perfecto, nos vemos el lunes', 0, 0, '2024-08-20 11:00:00', '2024-12-08 14:20:00', true),
(3, 1, 3, '2024-12-05 16:15:00', 'Entendido, seguiré las indicaciones', 0, 1, '2024-09-01 09:00:00', '2024-12-05 16:15:00', true),
(4, 1, 4, '2024-12-12 10:45:00', '¿A qué hora es la próxima cita?', 1, 0, '2024-09-25 14:00:00', '2024-12-12 10:45:00', true),
(5, 1, 5, '2024-12-15 11:30:00', 'Muchas gracias por todo', 0, 0, '2024-08-10 10:00:00', '2024-12-15 11:30:00', true),
(6, 1, 6, '2024-12-01 09:15:00', '¿Puedo cambiar el horario?', 0, 0, '2024-09-05 08:00:00', '2024-12-01 09:15:00', true),
(7, 1, 7, '2024-12-08 13:20:00', 'Sí, está bien', 1, 0, '2024-08-25 15:00:00', '2024-12-08 13:20:00', true),
(8, 1, 8, '2024-12-10 17:00:00', 'Perfecto, confirmado', 0, 0, '2024-09-30 11:00:00', '2024-12-10 17:00:00', true);

-- ===============================================
-- INSERTAR MENSAJES DE CHAT
-- ===============================================
-- Campos: id, conversation_id, sender_id, sender_role, message_text, file_url, file_name, file_type, is_read, created_datetime, is_active
-- 80 Mensajes: 10 por conversación (5 del dentista, 5 del paciente) - Tipo consulta
-- Conversación 1 (Paciente 1)
INSERT INTO chat_messages (id, conversation_id, sender_id, sender_role, message_text, is_read, created_datetime, is_active) VALUES
(1, 1, 3, 'PATIENT', 'Buenos días doctora, tengo una consulta sobre mi tratamiento', false, '2024-09-15 10:00:00', true),
(2, 1, 2, 'DENTIST', 'Hola María Elena, ¿en qué puedo ayudarte?', true, '2024-09-15 10:05:00', true),
(3, 1, 3, 'PATIENT', 'Quería saber cuánto tiempo más durará el tratamiento de sensibilidad', false, '2024-09-15 10:10:00', true),
(4, 1, 2, 'DENTIST', 'El tratamiento debería completarse en aproximadamente 3 semanas más. ¿Cómo estás con la sensibilidad?', true, '2024-09-15 10:12:00', true),
(5, 1, 3, 'PATIENT', 'Mucho mejor, casi no siento molestias', false, '2024-09-15 10:15:00', true),
(6, 1, 2, 'DENTIST', 'Excelente, eso significa que está funcionando bien. Continúa con el gel desensibilizante', true, '2024-11-10 14:00:00', true),
(7, 1, 3, 'PATIENT', 'Perfecto, lo haré. Gracias doctora', false, '2024-11-10 14:05:00', true),
(8, 1, 2, 'DENTIST', 'De nada, cualquier duda estoy aquí', true, '2024-11-10 14:07:00', true),
(9, 1, 3, 'PATIENT', '¿Puedo tomar algo para el dolor si es necesario?', false, '2024-12-10 15:25:00', true),
(10, 1, 2, 'DENTIST', 'Sí, puedes tomar ibuprofeno 400mg cada 8 horas si hay molestias. Pero deberías sentirte mejor ya', true, '2024-12-10 15:30:00', true),
-- Conversación 2 (Paciente 2)
(11, 2, 4, 'PATIENT', 'Hola, tengo una cita programada para el lunes', false, '2024-08-20 11:00:00', true),
(12, 2, 2, 'DENTIST', 'Hola Carlos, sí tienes cita el lunes a las 10:30', true, '2024-08-20 11:05:00', true),
(13, 2, 4, 'PATIENT', 'Perfecto, confirmo que asistiré', false, '2024-11-05 16:00:00', true),
(14, 2, 2, 'DENTIST', 'Excelente, nos vemos el lunes entonces', true, '2024-11-05 16:02:00', true),
(15, 2, 4, 'PATIENT', 'Gracias doctora', false, '2024-11-05 16:03:00', true),
(16, 2, 2, 'DENTIST', 'De nada, cualquier cambio avísame con anticipación', true, '2024-11-18 09:00:00', true),
(17, 2, 4, 'PATIENT', '¿Qué necesito llevar a la cita?', false, '2024-12-08 14:15:00', true),
(18, 2, 2, 'DENTIST', 'Solo trae tu documento de identidad y si tienes alguna radiografía reciente', true, '2024-12-08 14:18:00', true),
(19, 2, 4, 'PATIENT', 'Perfecto, nos vemos el lunes', false, '2024-12-08 14:20:00', true),
(20, 2, 2, 'DENTIST', 'Perfecto, hasta el lunes', true, '2024-12-08 14:20:00', true),
-- Conversación 3 (Paciente 3)
(21, 3, 5, 'PATIENT', 'Buen día, tengo una duda sobre mi tratamiento de ortodoncia', false, '2024-09-01 09:00:00', true),
(22, 3, 2, 'DENTIST', 'Hola Ana, ¿cuál es tu duda?', true, '2024-09-01 09:05:00', true),
(23, 3, 5, 'PATIENT', 'Uno de los brackets se me movió, ¿qué debo hacer?', false, '2024-09-15 10:30:00', true),
(24, 3, 2, 'DENTIST', 'No te preocupes, avísame para programar una cita de urgencia y lo ajustamos', true, '2024-09-15 10:35:00', true),
(25, 3, 5, 'PATIENT', '¿Cuánto tiempo tengo para ir?', false, '2024-09-15 10:40:00', true),
(26, 3, 2, 'DENTIST', 'Puedes venir esta semana cuando tengas tiempo. No es urgente pero conviene ajustarlo pronto', true, '2024-09-15 10:42:00', true),
(27, 3, 5, 'PATIENT', 'Perfecto, iré el viernes entonces', false, '2024-09-15 10:45:00', true),
(28, 3, 2, 'DENTIST', 'Excelente, te espero el viernes', true, '2024-11-18 15:00:00', true),
(29, 3, 5, 'PATIENT', 'Ya tengo cita para el ajuste, muchas gracias', false, '2024-12-05 16:10:00', true),
(30, 3, 2, 'DENTIST', 'Perfecto, cualquier cosa avísame. Entendido, seguiré las indicaciones', true, '2024-12-05 16:15:00', true),
-- Conversación 4 (Paciente 4)
(31, 4, 6, 'PATIENT', 'Hola doctora, quería consultar sobre mi implante', false, '2024-09-25 14:00:00', true),
(32, 4, 2, 'DENTIST', 'Hola Roberto, ¿qué te preocupa del implante?', true, '2024-09-25 14:05:00', true),
(33, 4, 6, 'PATIENT', 'Siento un poco de molestia, ¿es normal?', false, '2024-10-10 11:00:00', true),
(34, 4, 2, 'DENTIST', 'Sí, es normal las primeras semanas. Si es muy intenso o aumenta, avísame', true, '2024-10-10 11:05:00', true),
(35, 4, 6, 'PATIENT', 'Es leve, creo que está bien', false, '2024-10-10 11:10:00', true),
(36, 4, 2, 'DENTIST', 'Perfecto, continúa con las indicaciones que te di y cualquier cambio avísame', true, '2024-10-10 11:12:00', true),
(37, 4, 6, 'PATIENT', '¿A qué hora es la próxima cita?', false, '2024-12-12 10:40:00', true),
(38, 4, 2, 'DENTIST', 'Tu próxima cita es el 8 de enero a las 10:30', true, '2024-12-12 10:42:00', true),
(39, 4, 6, 'PATIENT', 'Perfecto, confirmado. Muchas gracias', false, '2024-12-12 10:45:00', true),
(40, 4, 2, 'DENTIST', 'De nada, cualquier cosa estoy aquí', true, '2024-12-12 10:45:00', true),
-- Conversación 5 (Paciente 5)
(41, 5, 7, 'PATIENT', 'Buenos días, tengo una consulta sobre el blanqueamiento', false, '2024-08-10 10:00:00', true),
(42, 5, 2, 'DENTIST', 'Hola Laura, ¿en qué puedo ayudarte?', true, '2024-08-10 10:05:00', true),
(43, 5, 7, 'PATIENT', 'Quería saber cuánto dura el efecto del blanqueamiento', false, '2024-08-10 10:10:00', true),
(44, 5, 2, 'DENTIST', 'Con buenos cuidados puede durar entre 2 y 3 años. Es importante evitar alimentos que manchen', true, '2024-08-10 10:12:00', true),
(45, 5, 7, 'PATIENT', 'Perfecto, lo tendré en cuenta', false, '2024-08-10 10:15:00', true),
(46, 5, 2, 'DENTIST', 'Excelente, cualquier duda estoy aquí', true, '2024-11-15 14:00:00', true),
(47, 5, 7, 'PATIENT', 'El resultado del blanqueamiento está excelente, muchas gracias', false, '2024-12-15 11:25:00', true),
(48, 5, 2, 'DENTIST', 'Me alegra mucho escuchar eso. Sigue con los cuidados que te indicamos', true, '2024-12-15 11:27:00', true),
(49, 5, 7, 'PATIENT', 'Sí, lo haré. Muchas gracias por todo', false, '2024-12-15 11:30:00', true),
(50, 5, 2, 'DENTIST', 'De nada, fue un placer ayudarte', true, '2024-12-15 11:30:00', true),
-- Conversación 6 (Paciente 6)
(51, 6, 8, 'PATIENT', 'Hola, ¿puedo cambiar el horario de mi cita?', false, '2024-09-05 08:00:00', true),
(52, 6, 2, 'DENTIST', 'Hola Diego, claro. ¿Qué horario prefieres?', true, '2024-09-05 08:05:00', true),
(53, 6, 8, 'PATIENT', 'Podría ser por la tarde en lugar de la mañana', false, '2024-09-05 08:10:00', true),
(54, 6, 2, 'DENTIST', 'Perfecto, te reprogramo para las 15:00. ¿Te funciona?', true, '2024-09-05 08:12:00', true),
(55, 6, 8, 'PATIENT', 'Sí, perfecto. Gracias', false, '2024-09-05 08:15:00', true),
(56, 6, 2, 'DENTIST', 'De nada, nos vemos entonces', true, '2024-11-01 10:00:00', true),
(57, 6, 8, 'PATIENT', '¿Puedo cambiar el horario?', false, '2024-12-01 09:10:00', true),
(58, 6, 2, 'DENTIST', 'Claro Diego, ¿qué horario prefieres ahora?', true, '2024-12-01 09:12:00', true),
(59, 6, 8, 'PATIENT', 'El mismo que tengo está bien, solo confirmaba', false, '2024-12-01 09:15:00', true),
(60, 6, 2, 'DENTIST', 'Perfecto, confirmado entonces', true, '2024-12-01 09:15:00', true),
-- Conversación 7 (Paciente 7)
(61, 7, 9, 'PATIENT', 'Buen día doctora', false, '2024-08-25 15:00:00', true),
(62, 7, 2, 'DENTIST', 'Hola Valentina, ¿en qué puedo ayudarte?', true, '2024-08-25 15:05:00', true),
(63, 7, 9, 'PATIENT', 'Tengo una duda sobre la endodoncia que me hicieron', false, '2024-10-25 11:00:00', true),
(64, 7, 2, 'DENTIST', 'Claro, ¿cuál es tu duda?', true, '2024-10-25 11:05:00', true),
(65, 7, 9, 'PATIENT', '¿Es normal que sienta un poco de sensibilidad?', false, '2024-10-25 11:10:00', true),
(66, 7, 2, 'DENTIST', 'Sí, es normal las primeras semanas. Debería ir disminuyendo', true, '2024-10-25 11:12:00', true),
(67, 7, 9, 'PATIENT', 'Perfecto, entonces esperaré un poco más', false, '2024-10-25 11:15:00', true),
(68, 7, 2, 'DENTIST', 'Sí, cualquier aumento del dolor o molestia intensa avísame', true, '2024-12-08 13:15:00', true),
(69, 7, 9, 'PATIENT', 'Ya está mucho mejor, gracias', false, '2024-12-08 13:18:00', true),
(70, 7, 2, 'DENTIST', 'Me alegra escuchar eso. Sí, está bien', true, '2024-12-08 13:20:00', true),
-- Conversación 8 (Paciente 8)
(71, 8, 10, 'PATIENT', 'Hola, tengo una consulta sobre mi tratamiento', false, '2024-09-30 11:00:00', true),
(72, 8, 2, 'DENTIST', 'Hola Fernando, ¿qué consulta tienes?', true, '2024-09-30 11:05:00', true),
(73, 8, 10, 'PATIENT', 'Quería saber cuántas sesiones faltan del tratamiento periodontal', false, '2024-09-30 11:10:00', true),
(74, 8, 2, 'DENTIST', 'Faltan aproximadamente 2 sesiones más. Estás yendo muy bien', true, '2024-09-30 11:12:00', true),
(75, 8, 10, 'PATIENT', 'Perfecto, gracias por la información', false, '2024-09-30 11:15:00', true),
(76, 8, 2, 'DENTIST', 'De nada, sigue con los cuidados que te indicamos', true, '2024-11-08 16:00:00', true),
(77, 8, 10, 'PATIENT', 'La próxima cita está confirmada, ¿verdad?', false, '2024-12-10 16:55:00', true),
(78, 8, 2, 'DENTIST', 'Sí, está confirmada para el 15 de enero a las 15:00', true, '2024-12-10 16:57:00', true),
(79, 8, 10, 'PATIENT', 'Perfecto, confirmado', false, '2024-12-10 17:00:00', true),
(80, 8, 2, 'DENTIST', 'Excelente, nos vemos entonces', true, '2024-12-10 17:00:00', true);

-- ===============================================
-- NOTAS IMPORTANTES PARA TESTING/DEMOSTRACIÓN
-- ===============================================
-- 
-- ESTRUCTURA DE DATOS PARA DEMOSTRACIÓN:
-- 
-- DENTISTA:
-- - ID: 1, user_id: 2
-- - Nombre: Dra. María González
-- - Especialidad: Odontología General
-- - Todos los pacientes pertenecen a este dentista
--
-- PACIENTES:
-- - 8 Pacientes (IDs: 1-8, user_ids: 3-10)
-- - Todos vinculados al dentista ID: 1
-- - Cada paciente tiene datos completos: tratamientos, recetas, odontogramas, historia clínica, conversaciones y mensajes
--
-- TRATAMIENTOS:
-- - 24 tratamientos totales: 3 por paciente
-- - Cada paciente tiene: 1 completado, 1 abandonado, 1 en curso
-- - Los tratamientos en curso aparecen en el dashboard del paciente
-- - Fechas desde septiembre 2024 hasta diciembre 2024
--
-- CITAS/TURNOS:
-- - 109 citas totales distribuidas:
--   * Noviembre 2025: Turnos pasados (completados, cancelados, ausentes)
--   * Diciembre 2025: Algunos pasados (antes del 15), mayoría futuros (después del 15)
--   * Enero 2026: Todos futuros
-- - Incluye días completos para probar visualización de días ocupados
-- - Todos los estados representados: PROGRAMADO, CONFIRMADO, COMPLETADO, CANCELADO, AUSENTE
--
-- RECETAS:
-- - 80 recetas totales: 10 por paciente
-- - Distribuidas en varios meses (septiembre-diciembre 2024) para probar filtros
-- - Algunas vinculadas a tratamientos/citas, otras independientes
-- - Diferentes fechas de mes y día para probar búsqueda por fecha
--
-- ODONTOGRAMAS:
-- - 32 odontogramas totales: 4 por paciente
-- - Evolución a lo largo del tiempo (desde septiembre 2024)
-- - Representan el estado dental antes y después de tratamientos
--
-- HISTORIA CLÍNICA:
-- - 80 entradas totales: 10 por paciente
-- - Algunas vinculadas a tratamientos/recetas, otras independientes
-- - Descripciones detalladas para probar búsqueda por texto
-- - Fechas distribuidas para probar filtros por fecha/rango
--
-- CONVERSACIONES:
-- - 8 conversaciones: 1 por paciente
-- - Todas con el dentista (ID: 1)
--
-- MENSAJES:
-- - 80 mensajes totales: 10 por conversación
-- - 5 mensajes del dentista y 5 del paciente por conversación
-- - Tipo consulta médica
-- - Fechas desde septiembre hasta diciembre 2024
--
-- RELACIONES COHERENTES:
-- - Todas las relaciones entre tablas son válidas
-- - Foreign keys respetadas
-- - Fechas coherentes (recetas después de citas cuando aplica)
-- - Pacientes con dentista tienen tratamientos, citas, recetas e historia
--
-- ENDPOINTS A PROBAR:
-- - GET /api/core/dentist/{id}/patients - Lista pacientes del dentista
-- - GET /api/core/dentist/available-patients - Lista pacientes sin dentista (6-15)
-- - GET /api/core/patient/{id}/clinical-history - Historia clínica del paciente
-- - GET /api/core/patient/{id}/clinical-history/search?searchText=... - Búsqueda por texto
-- - GET /api/core/patient/{id}/clinical-history/search/date?entryDate=... - Búsqueda por fecha
-- - GET /api/core/patient/{id}/prescriptions - Lista recetas
-- - GET /api/core/patient/{id}/prescriptions/count - Total de recetas (20)
-- - GET /api/core/patient/{id}/treatments - Lista tratamientos
-- - GET /api/core/patient/{id}/appointments - Lista citas
-- - GET /api/core/patient/{id}/appointments/upcoming - Próximas citas
-- - GET /api/core/patient/{id}/appointments/past - Citas pasadas
-- - POST /api/core/dentist/{dentistId}/patients/{patientId}/odontogram - Crear odontograma
-- - GET /api/core/dentist/{dentistId}/patients/{patientId}/odontogram - Listar odontogramas
-- - GET /api/core/dentist/{dentistId}/odontogram/{odontogramId} - Ver odontograma específico
-- - PUT /api/core/dentist/{dentistId}/odontogram/{odontogramId} - Actualizar odontograma
-- - DELETE /api/core/dentist/{dentistId}/odontogram/{odontogramId} - Eliminar odontograma
-- - GET /api/core/dentist/patients/{patientId}/odontogram/count - Contar odontogramas
-- - Y todos los demás endpoints...
--
-- ===============================================
