-- ===============================================
-- SEED DATA PARA DENTAL CARE - SOLO TESTING
-- ===============================================
-- IMPORTANTE: Este archivo es solo para desarrollo/testing
-- Se ejecuta automáticamente al iniciar la aplicación
-- Los datos son ficticios pero realistas para pruebas
--
-- ESTRUCTURA DE DATOS:
-- - 5 Dentistas (user_id: 101-105)
-- - 15 Pacientes: 5 con dentista asignado (201-205), 10 disponibles sin dentista (206-215)
-- - 12 Tratamientos (varios estados: completado, en progreso)
-- - 20 Citas (diferentes estados: PROGRAMADO, CONFIRMADO, COMPLETADO, CANCELADO)
-- - 25 Entradas de Historia Clínica (con y sin receta/tratamiento asociado)
-- - 20 Recetas (incluyendo recetas del mes actual para KPI)
--
-- ===============================================

-- ===============================================
-- LIMPIAR DATOS EXISTENTES (para testing limpio)
-- ===============================================
SET FOREIGN_KEY_CHECKS = 0;

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
(1, 101, 'DENT-001-MED', 'Odontología General', true),
(2, 102, 'DENT-002-ORT', 'Ortodoncia', true),
(3, 103, 'DENT-003-END', 'Endodoncia', true),
(4, 104, 'DENT-004-PER', 'Periodoncia', true),
(5, 105, 'DENT-005-CIR', 'Cirugía Oral', true);

-- ===============================================
-- INSERTAR PACIENTES
-- ===============================================
-- Campos: id, user_id, dentist_id, dni, active
-- Pacientes con dentista asignado
INSERT INTO patients (id, user_id, dentist_id, dni, active) VALUES
(1, 201, 1, '12345678', true),
(2, 202, 1, '23456789', true),
(3, 203, 2, '34567890', true),
(4, 204, 3, '45678901', true),
(5, 205, 4, '56789012', true);

-- Pacientes disponibles (sin dentista asignado) - Aparecen en available-patients
INSERT INTO patients (id, user_id, dentist_id, dni, active) VALUES
(6, 206, NULL, '67890123', true),
(7, 207, NULL, '78901234', true),
(8, 208, NULL, '89012345', true),
(9, 209, NULL, '90123456', true),
(10, 210, NULL, '01234567', true),
(11, 211, NULL, '11223344', true),
(12, 212, NULL, '22334455', true),
(13, 213, NULL, '33445566', true),
(14, 214, NULL, '44556677', true),
(15, 215, NULL, '55667788', true);

-- ===============================================
-- INSERTAR TRATAMIENTOS
-- ===============================================
-- Campos: id, patient_id, dentist_id, name, description, start_date, estimated_end_date, actual_end_date, status, total_sessions, completed_sessions, notes, active
INSERT INTO treatments (id, patient_id, dentist_id, name, description, start_date, estimated_end_date, actual_end_date, status, total_sessions, completed_sessions, notes, active) VALUES
-- Tratamientos completados
(1, 1, 1, 'Limpieza Dental', 'Limpieza profesional y profilaxis dental', '2024-01-15', '2024-01-15', '2024-01-15', 'completado', 1, 1, 'Paciente con buena higiene oral', true),
(2, 2, 1, 'Empaste Dental', 'Restauración de caries en molar superior derecho', '2024-02-01', '2024-02-01', '2024-02-01', 'completado', 1, 1, 'Caries moderada, restauración exitosa con resina compuesta', true),
(6, 1, 1, 'Blanqueamiento Dental', 'Tratamiento de blanqueamiento con peróxido de carbamida', '2024-03-01', '2024-03-15', '2024-03-15', 'completado', 3, 3, 'Resultado excelente, paciente muy satisfecho. Color mejorado en 4 tonos', true),
(8, 3, 2, 'Extracción de Cordal', 'Extracción quirúrgica de muela del juicio superior derecha impactada', '2024-02-15', '2024-02-15', '2024-02-15', 'completado', 1, 1, 'Extracción exitosa, sin complicaciones. Cicatrización normal', true),

-- Tratamientos en progreso
(3, 3, 2, 'Tratamiento de Ortodoncia', 'Corrección de maloclusión clase II con brackets metálicos', '2024-01-10', '2025-06-30', NULL, 'en progreso', 24, 8, 'Progreso satisfactorio, paciente colaborador. Alineación mejorando notablemente', true),
(4, 4, 3, 'Endodoncia', 'Tratamiento de conducto radicular en premolar inferior derecho', '2024-03-05', '2024-03-19', NULL, 'en progreso', 3, 1, 'Primera sesión completada exitosamente. Necrosis pulpar confirmada. Próxima sesión en 7 días', true),
(5, 5, 4, 'Tratamiento Periodontal', 'Limpieza profunda y curetaje para tratamiento de periodontitis', '2024-02-20', '2024-03-20', NULL, 'en progreso', 4, 2, 'Mejora notable en salud gingival. Reducción de bolsones de 6mm a 3mm', true),
(7, 2, 1, 'Corona Dental', 'Colocación de corona de porcelana en molar tratado endodónticamente', '2024-03-10', '2024-04-10', NULL, 'en progreso', 2, 1, 'Preparación del diente completada exitosamente. Próxima sesión para colocación de corona definitiva', true),
(9, 4, 3, 'Implante Dental', 'Colocación de implante de titanio en zona posterior mandibular', '2024-04-01', '2024-07-01', NULL, 'en progreso', 6, 2, 'Primera fase completada exitosamente. Implante colocado con buen anclaje óseo. Esperando osteointegración (3-4 meses)', true),
(10, 5, 4, 'Rehabilitación Oral', 'Tratamiento integral de rehabilitación oral con prótesis parcial removible', '2024-01-20', '2024-06-20', NULL, 'en progreso', 12, 6, 'Progreso satisfactorio. Paciente muy comprometido con el tratamiento. Prótesis parcial funcionando bien', true),
(11, 1, 1, 'Tratamiento de Sensibilidad', 'Tratamiento para sensibilidad dental con aplicación de flúor y sellantes', '2024-11-01', '2024-12-15', NULL, 'en progreso', 3, 2, 'Mejora notable en la sensibilidad. Dos sesiones completadas exitosamente', true),
(12, 2, 1, 'Rehabilitación Estética', 'Mejora estética con carillas de porcelana en sector anterior', '2024-10-15', '2025-01-15', NULL, 'en progreso', 4, 2, 'Dos carillas colocadas exitosamente. Buen resultado estético', true);

-- ===============================================
-- INSERTAR CITAS MÉDICAS
-- ===============================================
-- Campos: id, patient_id, dentist_id, start_datetime, end_datetime, status, reason, notes, active
-- Estados: PROGRAMADO, CONFIRMADO, COMPLETADO, CANCELADO, NO_SHOW
INSERT INTO appointments (id, patient_id, dentist_id, start_datetime, end_datetime, status, reason, notes, active) VALUES
-- Citas pasadas completadas
(1, 1, 1, '2024-01-15 09:00:00', '2024-01-15 09:30:00', 'COMPLETADO', 'Limpieza dental', 'Limpieza profesional completada exitosamente', true),
(2, 2, 1, '2024-02-01 10:00:00', '2024-02-01 10:45:00', 'COMPLETADO', 'Empaste dental', 'Restauración de caries completada', true),
(3, 3, 2, '2024-01-10 14:00:00', '2024-01-10 14:45:00', 'COMPLETADO', 'Inicio ortodoncia', 'Brackets colocados exitosamente', true),

-- Citas futuras programadas
(4, 1, 1, '2024-12-30 10:30:00', '2024-12-30 11:00:00', 'PROGRAMADO', 'Control post-blanqueamiento', 'Revisar resultados del blanqueamiento realizado en marzo', true),
(5, 2, 1, '2025-01-02 16:00:00', '2025-01-02 17:00:00', 'PROGRAMADO', 'Consulta inicial', 'Nueva paciente, evaluación completa', true),
(6, 3, 2, '2025-01-05 15:30:00', '2025-01-05 16:00:00', 'PROGRAMADO', 'Ajuste de ortodoncia', 'Control mensual de brackets. Ajuste de arcos', true),
(7, 4, 3, '2025-01-08 11:00:00', '2025-01-08 12:00:00', 'PROGRAMADO', 'Continuación endodoncia', 'Segunda sesión del tratamiento de conducto', true),
(8, 5, 4, '2025-01-10 16:00:00', '2025-01-10 17:00:00', 'PROGRAMADO', 'Seguimiento periodontal', 'Tercera sesión de limpieza profunda', true),

-- Citas confirmadas
(9, 1, 1, '2024-12-25 08:30:00', '2024-12-25 09:15:00', 'CONFIRMADO', 'Colocación de corona', 'Finalización del tratamiento de corona para paciente 2', true),
(10, 2, 1, '2024-12-26 11:00:00', '2024-12-26 12:00:00', 'CONFIRMADO', 'Consulta de urgencia', 'Dolor intenso en molar. Paciente llamó esta mañana', true),

-- Citas canceladas (para probar diferentes estados)
(11, 3, 2, '2024-12-15 14:00:00', '2024-12-15 14:30:00', 'CANCELADO', 'Control ortodoncia', 'Paciente canceló por motivo personal', true),
(12, 4, 3, '2024-12-18 09:45:00', '2024-12-18 11:15:00', 'CANCELADO', 'Continuación implante', 'Pospuesta por razones médicas. Reprogramada', true),

-- Más citas del mes actual (diciembre 2024) para probar endpoints
(13, 1, 1, '2024-12-20 09:00:00', '2024-12-20 09:30:00', 'PROGRAMADO', 'Control post-tratamiento', 'Revisar evolución del empaste realizado', true),
(14, 2, 1, '2024-12-21 10:00:00', '2024-12-21 10:45:00', 'PROGRAMADO', 'Consulta general', 'Revisión de rutina', true),
(15, 3, 2, '2024-12-22 14:00:00', '2024-12-22 14:30:00', 'PROGRAMADO', 'Ajuste de ortodoncia', 'Control mensual de brackets', true),
(16, 4, 3, '2024-12-23 11:00:00', '2024-12-23 12:00:00', 'PROGRAMADO', 'Continuación endodoncia', 'Segunda sesión del tratamiento', true),
(17, 5, 4, '2024-12-24 16:00:00', '2024-12-24 17:00:00', 'PROGRAMADO', 'Seguimiento periodontal', 'Tercera sesión de limpieza profunda', true),
(18, 1, 1, '2024-12-27 10:00:00', '2024-12-27 10:30:00', 'PROGRAMADO', 'Limpieza dental', 'Limpieza de rutina semestral', true),
(19, 2, 1, '2024-12-28 11:00:00', '2024-12-28 11:45:00', 'PROGRAMADO', 'Control corona', 'Revisión de corona colocada', true),
(20, 3, 2, '2024-12-29 15:30:00', '2024-12-29 16:00:00', 'PROGRAMADO', 'Control ortodoncia', 'Ajuste de brackets mensual', true);

-- ===============================================
-- INSERTAR RECETAS MÉDICAS
-- ===============================================
-- Campos: id, patient_id, dentist_id, prescription_date, observations, medications, active
INSERT INTO prescriptions (id, patient_id, dentist_id, prescription_date, observations, medications, active) VALUES
-- Recetas históricas (antes de diciembre)
(1, 1, 1, '2024-01-15', 'Analgésico post-limpieza dental. Tomar solo si hay molestias moderadas.', 'Ibuprofeno 400mg - Cada 8 horas por 3 días si hay molestias\nParacetamol 500mg - Cada 8 horas si el dolor persiste', true),
(2, 2, 1, '2024-02-01', 'Tratamiento post-empaste. Completar todo el tratamiento antibiótico según indicación.', 'Amoxicilina 500mg - Cada 8 horas por 7 días\nIbuprofeno 400mg - Cada 8 horas por 3 días', true),
(3, 3, 2, '2024-01-10', 'Alivio de molestias por brackets durante los primeros días del tratamiento.', 'Cera para brackets - Aplicar sobre brackets que causen molestias\nEnjuague bucal con clorhexidina 0.12% - 2 veces al día', true),
(4, 4, 3, '2024-03-05', 'Tratamiento post-endodoncia. Importante completar todo el antibiótico para evitar infecciones.', 'Ibuprofeno 400mg - Cada 8 horas por 5 días\nClindamicina 300mg - Cada 8 horas por 7 días\nAtención: Tomar con alimentos para evitar molestias gástricas', true),
(5, 5, 4, '2024-02-20', 'Antiséptico bucal para mantenimiento post-tratamiento periodontal.', 'Clorhexidina 0.12% - Enjuague bucal 2 veces al día por 2 semanas\nUsar después del cepillado, no enjuagar con agua después', true),
(6, 1, 1, '2024-03-01', 'Tratamiento para sensibilidad dental post-blanqueamiento.', 'Gel desensibilizante con flúor - Aplicar 2 veces al día por 2 semanas\nPasta dental desensibilizante - Usar como pasta habitual durante el tratamiento', true),
(7, 2, 1, '2024-03-10', 'Profilaxis post-preparación de corona. Controlar posible infección.', 'Ibuprofeno 400mg - Cada 8 horas por 5 días si hay dolor\nAmoxicilina 500mg - Cada 8 horas por 7 días', true),
(8, 3, 2, '2024-02-15', 'Tratamiento post-extracción quirúrgica de cordal. Importante seguir indicaciones.', 'Ibuprofeno 400mg - Cada 8 horas por 5 días\nClindamicina 300mg - Cada 8 horas por 7 días\nCompresas frías las primeras 24 horas para reducir inflamación', true),
(9, 4, 3, '2024-04-01', 'Profilaxis post-colocación de implante. Importante seguir al pie de la letra para éxito del implante.', 'Amoxicilina 500mg - Cada 8 horas por 7 días\nIbuprofeno 400mg - Cada 8 horas por 5 días\nClorhexidina 0.12% - Enjuague 2 veces al día por 2 semanas\nNO fumar ni beber alcohol durante el tratamiento', true),
(10, 5, 4, '2024-01-20', 'Suplementación para fortalecimiento óseo previo a rehabilitación oral.', 'Calcio 600mg - 2 comprimidos al día\nVitamina D 2000 UI - 1 cápsula al día\nTomar por 3 meses antes de iniciar rehabilitación completa', true),

-- Recetas del mes actual (diciembre 2024) - Para probar KPI de recetas del mes
(11, 1, 1, '2024-12-01', 'Limpieza dental de rutina. Mantenimiento de higiene oral.', 'Enjuague bucal con flúor - Usar cada noche después del cepillado\nHilo dental - Usar una vez al día preferentemente antes de dormir', true),
(12, 1, 1, '2024-12-10', 'Control post-blanqueamiento. Mantenimiento de resultados obtenidos.', 'Pasta dental desensibilizante - Usar 2 veces al día\nEvitar alimentos y bebidas que manchen los dientes (café, té, vino tinto)', true),
(13, 1, 1, '2024-12-18', 'Consulta de urgencia por absceso dental. Tratamiento antibiótico intensivo.', 'Amoxicilina 500mg - Cada 8 horas por 7 días\nIbuprofeno 400mg - Cada 8 horas por 5 días\nClorhexidina 0.12% - Enjuague 3 veces al día por 1 semana', true),
(14, 2, 1, '2024-12-15', 'Post-tratamiento corona dental. Analgésico y antibiótico preventivo.', 'Ibuprofeno 400mg - Cada 8 horas por 3 días si hay molestias\nAmoxicilina 500mg - Cada 8 horas por 5 días', true),
(15, 3, 2, '2024-12-05', 'Ajuste de ortodoncia. Alivio de molestias post-ajuste de brackets.', 'Paracetamol 500mg - Cada 8 horas por 2 días\nCera ortodóncica - Aplicar sobre brackets que causen molestias según necesidad', true),
(16, 3, 2, '2024-12-22', 'Control de ortodoncia. Mantenimiento de higiene oral durante tratamiento.', 'Enjuague bucal con clorhexidina 0.12% - 2 veces al día\nCepillo interdental - Usar diariamente para limpiar alrededor de brackets', true),
(17, 4, 3, '2024-12-12', 'Control post-endodoncia. Mantenimiento y cuidado del diente tratado.', 'Ibuprofeno 400mg - Cada 8 horas por 3 días si hay sensibilidad\nEnjuague bucal con flúor - Usar diariamente para fortalecer el diente', true),
(18, 5, 4, '2024-12-15', 'Seguimiento periodontal. Mantenimiento de salud gingival.', 'Clorhexidina 0.12% - Enjuague 2 veces al día por 2 semanas\nCepillo de cerdas suaves - Usar exclusivamente durante el tratamiento', true),
(19, 1, 1, '2024-12-27', 'Limpieza dental de rutina. Continuar con higiene oral adecuada.', 'Enjuague bucal con flúor - Usar cada noche\nHilo dental - Usar una vez al día', true),
(20, 2, 1, '2024-12-28', 'Consulta general. Revisión de salud bucal completa.', 'Pasta dental con flúor - Continuar uso habitual\nEnjuague bucal anticaries - Usar como complemento diario', true);

-- ===============================================
-- INSERTAR HISTORIA CLÍNICA
-- ===============================================
-- Campos: id, patient_id, dentist_id, entry_date, description, prescription_id, treatment_id, file_url, file_name, file_type, active
-- Nota: entry_date se puede omitir y se asignará automáticamente, pero aquí la incluimos para datos de prueba consistentes
INSERT INTO medical_history (id, patient_id, dentist_id, entry_date, description, prescription_id, treatment_id, active) VALUES
-- Entradas vinculadas a tratamientos (sesiones de tratamiento)
(1, 1, 1, '2024-01-15', 'Limpieza dental profesional completada exitosamente. Paciente con buena higiene oral, mínima placa bacteriana detectada. Se realizó profilaxis dental completa y aplicación tópica de flúor. Paciente sin molestias post-tratamiento.', NULL, 1, true),
(2, 2, 1, '2024-02-01', 'Restauración de caries en molar 16 (superior derecho). Caries moderada sin compromiso pulpar confirmada mediante radiografía. Empaste realizado con resina compuesta de alta calidad. Oclusión verificada. Paciente sin molestias post-tratamiento.', NULL, 2, true),
(3, 3, 2, '2024-01-10', 'Inicio de tratamiento ortodóncico con brackets metálicos convencionales. Maloclusión clase II con apiñamiento moderado diagnosticado. Colocación de brackets en arcada superior e inferior. Progreso esperado en 18-24 meses. Paciente informado sobre cuidados y alimentación.', NULL, 3, true),
(4, 4, 3, '2024-03-05', 'Inicio de endodoncia en premolar 44 (inferior derecho). Dolor intenso con necrosis pulpar confirmada mediante pruebas de vitalidad y radiografía. Primera sesión completada exitosamente: limpieza y desinfección del conducto. Próxima sesión programada en 7 días.', NULL, 4, true),
(5, 5, 4, '2024-02-20', 'Evaluación periodontal inicial completa. Bolsones de 4-6mm con sangrado al sondaje detectados. Diagnóstico: periodontitis moderada. Inicio de tratamiento no quirúrgico: raspado y alisado radicular en cuadrante superior derecho. Paciente informado sobre importancia de higiene oral.', NULL, 5, true),
(6, 1, 1, '2024-03-01', 'Inicio de blanqueamiento dental con peróxido de carbamida al 16%. Coloración amarillenta generalizada en dientes anteriores. Buena salud dental general sin caries activas. Primera sesión completada con resultados prometedores. Instrucciones de uso de férulas domiciliarias proporcionadas.', NULL, 6, true),
(7, 2, 1, '2024-03-10', 'Preparación para corona dental de porcelana. Diente tratado endodónticamente (molar 16) requiere protección con corona. Preparación del muñón completada exitosamente. Impresión tomada para laboratorio. Próxima cita programada para colocación de corona definitiva en 2 semanas.', NULL, 7, true),
(8, 3, 2, '2024-02-15', 'Extracción quirúrgica de muela del juicio superior derecha (cordal) impactada. Procedimiento realizado bajo anestesia local. Extracción completada sin complicaciones. Hemostasia obtenida. Radiografía post-operatoria muestra extracción completa. Instrucciones post-operatorias proporcionadas.', NULL, 8, true),
(9, 4, 3, '2024-04-01', 'Primera fase de colocación de implante dental de titanio en zona posterior mandibular (región de premolares). Implante de 4.5mm de diámetro colocado con buen anclaje óseo primario confirmado. Sutura realizada. Se espera osteointegración en 3-4 meses. Próxima fase: colocación de pilar y corona.', NULL, 9, true),
(10, 5, 4, '2024-01-20', 'Evaluación inicial para rehabilitación oral integral. Pérdida múltiple de dientes posteriores en ambos maxilares. Plan de tratamiento establecido: prótesis parcial removible. Impresiones preliminares tomadas. Paciente informado sobre opciones de tratamiento y tiempos estimados.', NULL, 10, true),

-- Entradas de control y seguimiento (sin tratamiento asociado, algunas con receta)
(11, 1, 1, '2024-12-01', 'Control post-blanqueamiento realizado en marzo. Resultados excelentes mantenidos, paciente muy satisfecho con el resultado obtenido. Dientes con aspecto saludable y brillante. Sin sensibilidad residual. Higiene oral adecuada. Recomendación de control semestral.', NULL, NULL, true),
(12, 1, 1, '2024-12-05', 'Consulta de rutina. Revisión general completa de la salud bucal. Paciente mantiene excelente higiene oral. Sin caries nuevas detectadas. Encías saludables sin signos de inflamación. Radiografías de control sin alteraciones. Recomendación de mantener rutina de cepillado actual.', NULL, NULL, true),
(13, 2, 1, '2024-12-10', 'Finalización de tratamiento de corona dental. Corona de porcelana colocada exitosamente en molar 16. Ajuste oclusal verificado. Paciente sin molestias. Oclusión correcta confirmada. Se programó control en 6 meses para verificar estado de la corona y tejidos circundantes.', NULL, NULL, true),
(14, 3, 2, '2024-12-08', 'Ajuste de brackets mensual. Progreso satisfactorio en la corrección de la maloclusión. Alineación dental mejorando notablemente según plan de tratamiento. Cambio de arcos realizado. Paciente colaborador, sigue indicaciones correctamente. Próximo ajuste programado en 4 semanas.', NULL, NULL, true),
(15, 4, 3, '2024-12-12', 'Control post-endodoncia. Diente tratado asintomático, sin dolor ni sensibilidad a la percusión. Radiografía de control muestra buena obturación del conducto radicular. Sin signos de patología periapical. Tratamiento exitoso. Paciente informado sobre importancia de corona protectora.', NULL, NULL, true),
(16, 5, 4, '2024-12-15', 'Seguimiento periodontal. Mejora significativa en la salud gingival desde inicio del tratamiento. Reducción de bolsones de 4-6mm a 2-3mm confirmada mediante sondaje. Paciente mantiene buena higiene oral. Continuar con enjuague de clorhexidina según indicación. Próxima sesión en 1 mes.', NULL, NULL, true),
(17, 1, 1, '2024-12-18', 'Consulta de urgencia por dolor intenso en molar superior izquierdo. Diagnóstico: absceso dental periapical. Tratamiento realizado: drenaje del absceso, limpieza de la zona afectada. Prescripción de antibióticos y analgésicos. Control programado en 3 días para evaluar evolución. Paciente informado sobre signos de alarma.', 13, NULL, true),
(18, 2, 1, '2024-12-20', 'Limpieza dental de rutina semestral. Buena salud bucal general. Sin problemas detectados. Profilaxis dental realizada. Aplicación de flúor tópico. Paciente mantiene adecuada higiene oral. Recomendación de mantener hábitos actuales y retornar en 6 meses para próximo control.', NULL, NULL, true),
(19, 3, 2, '2024-12-22', 'Control de ortodoncia de rutina. Brackets funcionando correctamente, sin despegues ni fracturas. Paciente colaborador con el tratamiento, sigue todas las indicaciones. Progreso según lo esperado según plan de tratamiento. Ajuste menor de arcos realizado. Paciente motivado.', NULL, NULL, true),
(20, 4, 3, '2024-12-25', 'Seguimiento de implante dental. Radiografía de control muestra buena osteointegración del implante colocado en abril. Implante estable, sin signos de rechazo ni movilidad. Tejidos blandos circundantes saludables. Próxima fase (colocación de pilar y corona) programada para dentro de 1 mes.', NULL, 9, true),

-- Más entradas para tener variedad de datos (algunas con receta, algunas sin)
(21, 1, 1, '2024-11-15', 'Consulta de seguimiento. Revisión de sensibilidad dental post-blanqueamiento. Mejora notable en la sensibilidad. Segunda sesión de aplicación de gel desensibilizante realizada. Paciente reporta mayor confort. Continuar con tratamiento domiciliario.', NULL, 11, true),
(22, 2, 1, '2024-11-20', 'Evaluación estética. Planificación de rehabilitación estética con carillas de porcelana. Estudio fotográfico realizado. Impresiones para mock-up tomadas. Paciente informado sobre proceso y expectativas realistas.', NULL, 12, true),
(23, 3, 2, '2024-11-10', 'Control ortodoncia. Progreso normal del tratamiento. Alineación dental mejorando progresivamente. Cambio de elásticos realizado. Paciente sigue indicaciones correctamente. Motivación del paciente excelente.', NULL, NULL, true),
(24, 4, 3, '2024-11-25', 'Consulta de urgencia por dolor post-endodoncia. Evaluación realizada: normal post-tratamiento. Dolor leve esperado. Prescripción de analgésicos. Control en 1 semana programado si persisten molestias.', NULL, NULL, true),
(25, 5, 4, '2024-11-30', 'Seguimiento rehabilitación oral. Prótesis parcial removible funcionando correctamente. Ajuste menor realizado para mejorar adaptación. Paciente satisfecho con resultado. Instrucciones de cuidado y limpieza reforzadas.', NULL, NULL, true);

-- ===============================================
-- NOTAS IMPORTANTES PARA TESTING
-- ===============================================
--
-- DENTISTAS DISPONIBLES:
-- - ID: 1-5, user_id: 101-105
-- - Todos activos, especialidades variadas
--
-- PACIENTES CON DENTISTA ASIGNADO (para probar endpoints de dentista):
-- - ID: 1-5, user_id: 201-205
-- - Todos tienen dentista asignado y datos completos
--
-- PACIENTES DISPONIBLES (sin dentista):
-- - ID: 6-15, user_id: 206-215
-- - Aparecen en GET /api/core/dentist/available-patients
-- - Pueden ser asignados con PUT /api/core/patient/{patientId}/assign-dentist/{dentistId}
--
-- TRATAMIENTOS:
-- - 4 completados (ID: 1, 2, 6, 8)
-- - 8 en progreso (ID: 3, 4, 5, 7, 9, 10, 11, 12)
-- - Vinculados a pacientes 1-5 (todos con dentista asignado)
--
-- CITAS:
-- - Estados variados: PROGRAMADO, CONFIRMADO, COMPLETADO, CANCELADO
-- - 20 citas totales, distribuidas en diferentes fechas
-- - Para probar filtros por estado y fechas
--
-- RECETAS:
-- - 10 recetas históricas (enero-noviembre 2024)
-- - 10 recetas del mes actual (diciembre 2024) - Para probar KPI
-- - Total: 20 recetas
-- - Vinculadas a pacientes 1-5
--
-- HISTORIA CLÍNICA:
-- - 25 entradas totales
-- - 10 vinculadas a tratamientos (sesiones)
-- - 5 vinculadas a recetas
-- - 10 entradas independientes (solo texto)
-- - Varias fechas para probar búsquedas por fecha/rango
-- - Descripciones detalladas para probar búsqueda por texto
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
-- - Y todos los demás endpoints...
--
-- ===============================================
