-- ===============================================
-- SEED DATA PARA USERS MICROSERVICE - SOLO TESTING
-- ===============================================
-- IMPORTANTE: Este archivo es solo para desarrollo/testing
-- Se ejecuta automáticamente al iniciar la aplicación
-- Los datos son ficticios pero realistas para pruebas

-- ===============================================
-- LIMPIAR DATOS EXISTENTES (para testing limpio)
-- ===============================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- ===============================================
-- INSERTAR USUARIOS ADMINISTRADORES
-- ===============================================
INSERT INTO users (id, first_name, last_name, email, password, role, phone, address, birth_date, is_active, created_at, updated_at) VALUES
(1, 'Carlos', 'Administrador', 'admin@dentalcare.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'ADMIN', '+54-11-1234-5678', 'Av. Corrientes 1234, CABA', '1980-05-15', true, '2024-01-10 09:00:00', '2024-01-10 09:00:00');

-- ===============================================
-- INSERTAR USUARIOS DENTISTAS
-- ===============================================
INSERT INTO users (id, first_name, last_name, email, password, role, phone, address, birth_date, is_active, created_at, updated_at) VALUES
(2, 'María', 'González', 'maria.gonzalez@dentalcare.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'DENTIST', '+54-11-2345-6789', 'Av. Santa Fe 2345, CABA', '1982-08-20', true, '2024-01-15 10:00:00', '2024-01-15 10:00:00');

-- ===============================================
-- INSERTAR USUARIOS PACIENTES
-- ===============================================
-- 8 Pacientes todos vinculados al mismo dentista (ID 2)
INSERT INTO users (id, first_name, last_name, email, password, role, phone, address, birth_date, is_active, created_at, updated_at) VALUES
(3, 'María Elena', 'Pérez', 'maria.perez@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-1111-1111', 'Av. 9 de Julio 1111, CABA', '1985-03-15', true, '2024-02-10 11:00:00', '2024-02-10 11:00:00'),
(4, 'Carlos Alberto', 'García', 'carlos.garcia@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-2222-2222', 'Av. Callao 2222, CABA', '1990-07-22', true, '2024-02-15 14:00:00', '2024-02-15 14:00:00'),
(5, 'Ana Beatriz', 'Silva', 'ana.silva@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-3333-3333', 'Av. Las Heras 3333, CABA', '1978-11-08', true, '2024-03-05 10:00:00', '2024-03-05 10:00:00'),
(6, 'Roberto Carlos', 'Morales', 'roberto.morales@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-4444-4444', 'Av. Scalabrini Ortiz 4444, CABA', '1992-05-12', true, '2024-03-20 15:00:00', '2024-03-20 15:00:00'),
(7, 'Laura Patricia', 'Vargas', 'laura.vargas@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-5555-5555', 'Av. Corrientes 5555, CABA', '1988-09-30', true, '2024-04-10 09:00:00', '2024-04-10 09:00:00'),
(8, 'Diego Alejandro', 'Torres', 'diego.torres@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-6666-6666', 'Av. Santa Fe 6666, CABA', '1995-01-20', true, '2024-05-05 11:00:00', '2024-05-05 11:00:00'),
(9, 'Valentina', 'Jiménez', 'valentina.jimenez@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-7777-7777', 'Av. Córdoba 7777, CABA', '1993-04-12', true, '2024-05-18 13:00:00', '2024-05-18 13:00:00'),
(10, 'Fernando', 'Herrera', 'fernando.herrera@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-8888-8888', 'Av. Rivadavia 8888, CABA', '1987-08-25', true, '2024-06-12 16:00:00', '2024-06-12 16:00:00');

-- ===============================================
-- NOTAS IMPORTANTES PARA TESTING
-- ===============================================
-- 
-- ⚠️ IMPORTANTE: Los hashes de contraseña deben generarse usando BCryptPasswordEncoder
-- Todos los usuarios usan la contraseña: 123456 (hasheada con BCrypt)
-- 
-- ESTRUCTURA DE USUARIOS:
-- - 1 Administrador (ID: 1)
-- - 1 Dentista (ID: 2)
-- - 8 Pacientes (IDs: 3-10) - Todos vinculados al mismo dentista
--
-- CREDENCIALES PARA DEMOSTRACIÓN:
-- 
-- ADMINISTRADOR:
-- - Email: admin@dentalcare.com
-- - Password: 123456
-- - Role: ADMIN
--
-- DENTISTA:
-- - Email: maria.gonzalez@dentalcare.com
-- - Password: 123456
-- - Role: DENTIST
-- - Nombre: Dra. María González
--
-- PACIENTES (todos con password: 123456):
-- 1. Email: maria.perez@email.com - María Elena Pérez
-- 2. Email: carlos.garcia@email.com - Carlos Alberto García
-- 3. Email: ana.silva@email.com - Ana Beatriz Silva
-- 4. Email: roberto.morales@email.com - Roberto Carlos Morales
-- 5. Email: laura.vargas@email.com - Laura Patricia Vargas
-- 6. Email: diego.torres@email.com - Diego Alejandro Torres
-- 7. Email: valentina.jimenez@email.com - Valentina Jiménez
-- 8. Email: fernando.herrera@email.com - Fernando Herrera
--
-- ===============================================
