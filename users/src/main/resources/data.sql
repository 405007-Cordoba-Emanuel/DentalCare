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
(1, 'Carlos', 'Administrador', 'admin@dentalcare.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'ADMIN', '+54-11-1234-5678', 'Av. Corrientes 1234, CABA', '1980-05-15', true, NOW(), NOW());

-- ===============================================
-- INSERTAR USUARIOS DENTISTAS
-- ===============================================
INSERT INTO users (id, first_name, last_name, email, password, role, phone, address, birth_date, is_active, created_at, updated_at) VALUES
(101, 'Dr. María', 'González', 'maria.gonzalez@dentalcare.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'DENTIST', '+54-11-2345-6789', 'Av. Santa Fe 2345, CABA', '1975-08-20', true, NOW(), NOW()),
(102, 'Dr. Juan Carlos', 'López', 'juan.lopez@dentalcare.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'DENTIST', '+54-11-3456-7890', 'Av. Córdoba 3456, CABA', '1982-12-10', true, NOW(), NOW()),
(103, 'Dra. Ana', 'Martínez', 'ana.martinez@dentalcare.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'DENTIST', '+54-11-4567-8901', 'Av. Rivadavia 4567, CABA', '1978-03-25', true, NOW(), NOW()),
(104, 'Dr. Roberto', 'Fernández', 'roberto.fernandez@dentalcare.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'DENTIST', '+54-11-5678-9012', 'Av. Belgrano 5678, CABA', '1985-07-18', true, NOW(), NOW()),
(105, 'Dra. Laura', 'Rodríguez', 'laura.rodriguez@dentalcare.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'DENTIST', '+54-11-6789-0123', 'Av. San Martín 6789, CABA', '1981-11-30', true, NOW(), NOW());

-- ===============================================
-- INSERTAR USUARIOS PACIENTES (YA VINCULADOS)
-- ===============================================
INSERT INTO users (id, first_name, last_name, email, password, role, phone, address, birth_date, is_active, created_at, updated_at) VALUES
(201, 'María Elena', 'Pérez', 'maria.perez@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-1111-1111', 'Av. 9 de Julio 1111, CABA', '1985-03-15', true, NOW(), NOW()),
(202, 'Carlos Alberto', 'García', 'carlos.garcia@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-2222-2222', 'Av. Callao 2222, CABA', '1990-07-22', true, NOW(), NOW()),
(203, 'Ana Beatriz', 'Silva', 'ana.silva@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-3333-3333', 'Av. Las Heras 3333, CABA', '1978-11-08', true, NOW(), NOW()),
(204, 'Roberto Carlos', 'Morales', 'roberto.morales@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-4444-4444', 'Av. Scalabrini Ortiz 4444, CABA', '1992-05-12', true, NOW(), NOW()),
(205, 'Laura Patricia', 'Vargas', 'laura.vargas@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-5555-5555', 'Av. Corrientes 5555, CABA', '1988-09-30', true, NOW(), NOW());

-- ===============================================
-- INSERTAR USUARIOS PACIENTES DISPONIBLES
-- ===============================================
-- Estos usuarios NO están vinculados a ningún dentista
-- Aparecerán en el endpoint GET /api/dentists/available-patients
INSERT INTO users (id, first_name, last_name, email, password, role, phone, address, birth_date, is_active, created_at, updated_at) VALUES
(206, 'Diego Alejandro', 'Torres', 'diego.torres@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-6666-6666', 'Av. Santa Fe 6666, CABA', '1995-01-20', true, NOW(), NOW()),
(207, 'Valentina', 'Jiménez', 'valentina.jimenez@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-7777-7777', 'Av. Córdoba 7777, CABA', '1993-04-12', true, NOW(), NOW()),
(208, 'Fernando', 'Herrera', 'fernando.herrera@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-8888-8888', 'Av. Rivadavia 8888, CABA', '1987-08-25', true, NOW(), NOW()),
(209, 'Camila', 'Ruiz', 'camila.ruiz@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-9999-9999', 'Av. Belgrano 9999, CABA', '1991-12-03', true, NOW(), NOW()),
(210, 'Sebastián', 'Mendoza', 'sebastian.mendoza@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-0000-0000', 'Av. San Martín 0000, CABA', '1989-06-17', true, NOW(), NOW()),
(211, 'Sofía', 'Morales', 'sofia.morales@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-1111-2222', 'Av. Corrientes 1111, CABA', '1994-03-15', true, NOW(), NOW()),
(212, 'Lucas', 'González', 'lucas.gonzalez@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-2222-3333', 'Av. Santa Fe 2222, CABA', '1992-07-22', true, NOW(), NOW()),
(213, 'Martina', 'López', 'martina.lopez@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-3333-4444', 'Av. Córdoba 3333, CABA', '1990-11-08', true, NOW(), NOW()),
(214, 'Nicolás', 'Fernández', 'nicolas.fernandez@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-4444-5555', 'Av. Rivadavia 4444, CABA', '1996-05-12', true, NOW(), NOW()),
(215, 'Isabella', 'Rodríguez', 'isabella.rodriguez@email.com', '$2a$10$er6wTpFKwFMcBtstML5IEORN6jqufbsZHsDFQOUgsJ99k.3iKDSXO', 'PATIENT', '+54-11-5555-6666', 'Av. Belgrano 5555, CABA', '1993-09-30', true, NOW(), NOW());

-- ===============================================
-- NOTAS IMPORTANTES PARA TESTING
-- ===============================================
-- 
-- ⚠️ IMPORTANTE: Los hashes de contraseña deben generarse usando BCryptPasswordEncoder
-- Para generar un hash nuevo para "123456", ejecuta:
-- java -cp "target/classes:target/dependency/*" dental.core.users.util.PasswordHashGenerator
-- 
-- USUARIOS ADMINISTRADORES:
-- - ID: 1
-- - Email: admin@dentalcare.com
-- - Password: 123456 (hasheada con BCrypt)
--
-- USUARIOS DENTISTAS:
-- - IDs: 101, 102, 103, 104, 105
-- - Todos están activos y pueden recibir pacientes
-- - Passwords: 123456 (hasheada)
--
-- USUARIOS PACIENTES YA VINCULADOS:
-- - IDs: 201, 202, 203, 204, 205
-- - Estos usuarios SÍ aparecen en la tabla patients del be-core
-- - NO aparecerán en el endpoint de usuarios disponibles
--
-- USUARIOS PACIENTES DISPONIBLES:
-- - IDs: 206, 207, 208, 209, 210, 211, 212, 213, 214, 215
-- - Estos usuarios están como pacientes DISPONIBLES en el be-core (sin dentista asignado)
-- - SÍ aparecerán en el endpoint GET /api/dentists/available-patients
--
-- CREDENCIALES PARA TESTING:
-- - Email: admin@dentalcare.com, Password: 123456 (ADMIN)
-- - Email: maria.gonzalez@dentalcare.com, Password: 123456 (DENTIST)
-- - Email: diego.torres@email.com, Password: 123456 (PATIENT disponible)
--
-- ===============================================
