# 🌱 Guía de Seed Data - Dental Care

## ¿Qué es el Seed Data?

El **seed data** es un conjunto de datos de prueba que se carga automáticamente en las bases de datos cuando inicias las aplicaciones. Es como tener un "kit de datos de prueba" que se ejecuta solo.

## 🚀 ¿Cómo funciona?

### **Configuración automática:**
```properties
# En application.properties
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true
spring.sql.init.continue-on-error=true
```

### **Flujo automático:**
1. **Arrancas la aplicación** → Spring Boot inicia
2. **JPA crea las tablas** → Estructura de base de datos
3. **Se ejecuta data.sql** → Datos de prueba se cargan
4. **¡Listo para probar!** → Endpoints funcionando con datos

## 📁 Archivos de Seed Data

### **Microservicio Users (`users/src/main/resources/data.sql`)**
- ✅ **1 Administrador** (admin@dentalcare.com)
- ✅ **5 Dentistas** (IDs: 101-105)
- ✅ **15 Pacientes** (IDs: 201-215)
  - 5 vinculados a dentistas (201-205)
  - 10 disponibles sin dentista (206-215)

### **Microservicio Core (`be-core/src/main/resources/data.sql`)**
- ✅ **5 Dentistas** vinculados a usuarios (IDs: 1-5, user_id: 101-105)
- ✅ **15 Pacientes**:
  - 5 ya vinculados a dentistas (IDs: 1-5, user_id: 201-205)
  - 10 disponibles sin dentista (IDs: 6-15, user_id: 206-215)
- ✅ **12 Tratamientos**:
  - 4 completados
  - 8 en progreso
- ✅ **20 Citas médicas**:
  - Estados variados: PROGRAMADO, CONFIRMADO, COMPLETADO, CANCELADO
  - Distribuidas en diferentes fechas
- ✅ **25 Entradas de Historia Clínica**:
  - 10 vinculadas a tratamientos (sesiones)
  - 5 vinculadas a recetas
  - 10 entradas independientes
  - Descripciones detalladas para búsqueda
- ✅ **20 Recetas médicas**:
  - 10 históricas (enero-noviembre 2024)
  - 10 del mes actual (diciembre 2024) - Para probar KPIs

## 🎯 Datos de Prueba Incluidos

### **Usuarios Disponibles para Testing:**
```bash
# Administrador
Email: admin@dentalcare.com
Password: 123456
User ID: 1, Role: ADMIN

# Dentista 1
Email: maria.gonzalez@dentalcare.com
Password: 123456
Dentist ID: 1, User ID: 101, Nombre: Dr. María González

# Dentista 2
Email: juan.lopez@dentalcare.com
Password: 123456
Dentist ID: 2, User ID: 102, Nombre: Dr. Juan Carlos López

# Dentista 3
Email: ana.martinez@dentalcare.com
Password: 123456
Dentist ID: 3, User ID: 103, Nombre: Dra. Ana Martínez

# Dentista 4
Email: roberto.fernandez@dentalcare.com
Password: 123456
Dentist ID: 4, User ID: 104, Nombre: Dr. Roberto Fernández

# Dentista 5
Email: laura.rodriguez@dentalcare.com
Password: 123456
Dentist ID: 5, User ID: 105, Nombre: Dra. Laura Rodríguez

# Paciente Vinculado 1 (con dentista asignado)
Email: maria.perez@email.com
Password: 123456
Patient ID: 1, User ID: 201, Dentist ID: 1
Nombre: María Elena Pérez

# Paciente Vinculado 2 (con dentista asignado)
Email: carlos.garcia@email.com
Password: 123456
Patient ID: 2, User ID: 202, Dentist ID: 1
Nombre: Carlos Alberto García

# Paciente Vinculado 3 (con dentista asignado)
Email: ana.silva@email.com
Password: 123456
Patient ID: 3, User ID: 203, Dentist ID: 2
Nombre: Ana Beatriz Silva

# Paciente Vinculado 4 (con dentista asignado)
Email: roberto.morales@email.com
Password: 123456
Patient ID: 4, User ID: 204, Dentist ID: 3
Nombre: Roberto Carlos Morales

# Paciente Vinculado 5 (con dentista asignado)
Email: laura.vargas@email.com
Password: 123456
Patient ID: 5, User ID: 205, Dentist ID: 4
Nombre: Laura Patricia Vargas

# Paciente Disponible 1 (aparece en endpoint de disponibles)
Email: diego.torres@email.com
Password: 123456
Patient ID: 6, User ID: 206, Dentist ID: NULL
Nombre: Diego Alejandro Torres

# Paciente Disponible 2 (aparece en endpoint de disponibles)
Email: valentina.jimenez@email.com
Password: 123456
Patient ID: 7, User ID: 207, Dentist ID: NULL
Nombre: Valentina Jiménez

# Paciente Disponible 3
Email: fernando.herrera@email.com
Password: 123456
Patient ID: 8, User ID: 208, Dentist ID: NULL

# Paciente Disponible 4
Email: camila.ruiz@email.com
Password: 123456
Patient ID: 9, User ID: 209, Dentist ID: NULL

# Paciente Disponible 5
Email: sebastian.mendoza@email.com
Password: 123456
Patient ID: 10, User ID: 210, Dentist ID: NULL

# Paciente Disponible 6
Email: sofia.morales@email.com
Password: 123456
Patient ID: 11, User ID: 211, Dentist ID: NULL

# Paciente Disponible 7
Email: lucas.gonzalez@email.com
Password: 123456
Patient ID: 12, User ID: 212, Dentist ID: NULL

# Paciente Disponible 8
Email: martina.lopez@email.com
Password: 123456
Patient ID: 13, User ID: 213, Dentist ID: NULL

# Paciente Disponible 9
Email: nicolas.fernandez@email.com
Password: 123456
Patient ID: 14, User ID: 214, Dentist ID: NULL

# Paciente Disponible 10
Email: isabella.rodriguez@email.com
Password: 123456
Patient ID: 15, User ID: 215, Dentist ID: NULL
```

**⚠️ IMPORTANTE:** 
- Todos los usuarios (admin, dentistas y pacientes) usan la misma contraseña de prueba: `123456`
- **TODOS los emails y contraseñas listados arriba coinciden exactamente con los datos en `users/src/main/resources/data.sql`**
- Si tienes problemas de login, verifica que estés usando el email exacto (respeta mayúsculas/minúsculas)

### **Estructura de Datos:**

#### **Dentistas:**
| ID | User ID | Matrícula | Especialidad |
|----|---------|-----------|--------------|
| 1 | 101 | DENT-001-MED | Odontología General |
| 2 | 102 | DENT-002-ORT | Ortodoncia |
| 3 | 103 | DENT-003-END | Endodoncia |
| 4 | 104 | DENT-004-PER | Periodoncia |
| 5 | 105 | DENT-005-CIR | Cirugía Oral |

#### **Pacientes:**
- **Con dentista asignado** (IDs: 1-5): Tienen tratamientos, citas, recetas e historia clínica completa
- **Disponibles sin dentista** (IDs: 6-15): Aparecen en `GET /api/core/dentist/available-patients`

#### **Tratamientos:**
- **Completados**: 4 tratamientos finalizados
- **En progreso**: 8 tratamientos activos
- Vinculados a pacientes 1-5 (todos con dentista asignado)

#### **Citas:**
- **20 citas totales**
- Estados: PROGRAMADO (mayoría), CONFIRMADO, COMPLETADO, CANCELADO
- Distribuidas en diferentes fechas para probar filtros

#### **Recetas:**
- **10 recetas históricas** (enero-noviembre 2024)
- **10 recetas del mes actual** (diciembre 2024) - Para probar KPI de recetas del mes
- Vinculadas a pacientes 1-5

#### **Historia Clínica:**
- **25 entradas totales**
- Variedad de tipos:
  - Vinculadas a tratamientos (sesiones)
  - Vinculadas a recetas
  - Entradas independientes
- Descripciones detalladas para probar búsqueda por texto
- Fechas variadas para probar búsqueda por fecha/rango

### **Relaciones Coherentes:**
- ✅ **Pacientes vinculados**: user_id 201-205 tienen dentista asignado
- ✅ **Pacientes disponibles**: user_id 206-215 NO tienen dentista asignado
- ✅ **Tratamientos activos**: En diferentes estados (completado, en progreso)
- ✅ **Citas programadas**: Para fechas pasadas y futuras
- ✅ **Historia clínica**: Entradas realistas con diferentes vínculos
- ✅ **Recetas del mes**: 10 recetas de diciembre 2024 para probar KPIs
- ✅ **Foreign keys**: Todas las relaciones son válidas

## 🔧 ¿Cómo usar el Seed Data?

### **Opción 1: Automático (Recomendado)**
```bash
# 1. Arrancar microservicio de usuarios
cd users
./mvnw spring-boot:run

# 2. Arrancar microservicio core
cd be-core
./mvnw spring-boot:run

# ¡Los datos se cargan automáticamente!
```

### **Opción 2: Limpiar y recargar**
```bash
# Si quieres datos frescos, simplemente reinicia las aplicaciones
# El seed data se ejecuta automáticamente cada vez
# El data.sql incluye TRUNCATE automático al inicio
```

## 🧪 Endpoints para Probar

### **1. Pacientes Disponibles**
```bash
GET /api/core/dentist/available-patients
# Devuelve pacientes 6-15 (sin dentista vinculado)
# Cada paciente incluye: id, userId, firstName, lastName, email, phone, address, dni, active
```

### **2. Asignar Dentista a Paciente**
```bash
PUT /api/core/patient/{patientId}/assign-dentist/{dentistId}
# Ejemplo: PUT /api/core/patient/6/assign-dentist/1
# Asigna el paciente 6 al dentista 1
```

### **3. Listar Pacientes de un Dentista**
```bash
GET /api/core/dentist/{id}/patients
# Devuelve pacientes ya vinculados al dentista
# Ejemplo: GET /api/core/dentist/1/patients
```

### **4. Historia Clínica del Paciente**
```bash
GET /api/core/patient/{id}/clinical-history
# Lista todas las entradas de historia clínica del paciente
# Ejemplo: GET /api/core/patient/1/clinical-history

GET /api/core/patient/{id}/clinical-history/search?searchText=limpieza
# Busca entradas por texto en la descripción

GET /api/core/patient/{id}/clinical-history/search/date?entryDate=2024-12-01
# Busca entradas por fecha específica

GET /api/core/patient/{id}/clinical-history/search/date-range?startDate=2024-12-01&endDate=2024-12-31
# Busca entradas por rango de fechas
```

### **5. Recetas del Paciente**
```bash
GET /api/core/patient/{id}/prescriptions
# Lista todas las recetas del paciente
# Ejemplo: GET /api/core/patient/1/prescriptions

GET /api/core/patient/{id}/prescriptions/count
# Cuenta total de recetas del paciente
# Ejemplo: Para paciente 1 retorna 6 (recetas totales)

GET /api/core/patient/{id}/prescriptions/{prescriptionId}
# Obtiene detalle de una receta específica
```

### **6. Tratamientos del Paciente**
```bash
GET /api/core/patient/{id}/treatments
# Lista todos los tratamientos del paciente

GET /api/core/patient/{id}/treatments/{treatmentId}
# Obtiene detalle completo de un tratamiento con todas sus sesiones
```

### **7. Citas del Paciente**
```bash
GET /api/core/patient/{id}/appointments
# Lista todas las citas del paciente

GET /api/core/patient/{id}/appointments/upcoming
# Lista solo las citas futuras del paciente

GET /api/core/patient/{id}/appointments/count?status=PROGRAMADO
# Cuenta citas por estado
```

### **8. Crear Entrada en Historia Clínica (Dentista)**
```bash
POST /api/core/dentist/{id}/patients/{patientId}/clinical-history
Content-Type: multipart/form-data

description: "Consulta de rutina. Paciente en buen estado."
entryDate: "2024-12-30" (opcional, se asigna automáticamente si no se envía)
prescriptionId: 11 (opcional)
treatmentId: 11 (opcional)
file: [archivo opcional - imagen o PDF]
```

### **9. Crear Cita**
```bash
POST /api/core/dentist/{dentistId}/appointments
{
  "patientId": 1,
  "startDateTime": "2025-01-15T10:00:00",
  "endDateTime": "2025-01-15T10:30:00",
  "reason": "Control de rutina",
  "notes": "Revisión semestral"
}
```

## 📊 Ejemplos de Datos para Probar

### **Paciente 1 (user_id: 201)**
- **Tratamientos**: 3 (1 completado, 2 en progreso)
- **Citas**: 5 citas (varios estados)
- **Recetas**: 6 recetas (3 históricas, 3 del mes actual)
- **Historia Clínica**: 6 entradas (2 vinculadas a tratamientos, 1 a receta, 3 independientes)

### **Paciente 2 (user_id: 202)**
- **Tratamientos**: 3 (2 completados, 1 en progreso)
- **Citas**: 4 citas
- **Recetas**: 4 recetas (2 históricas, 2 del mes actual)
- **Historia Clínica**: 5 entradas

### **Paciente 6 (user_id: 206) - Disponible**
- **Tratamientos**: 0
- **Citas**: 0
- **Recetas**: 0
- **Historia Clínica**: 0
- Aparece en `available-patients` para ser asignado

## 🔄 Limpieza de Datos

### **El seed data incluye limpieza automática:**
```sql
-- Se ejecuta al inicio para datos frescos
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE appointments;
TRUNCATE TABLE medical_history;
TRUNCATE TABLE prescriptions;
TRUNCATE TABLE treatments;
TRUNCATE TABLE patients;
TRUNCATE TABLE dentists;
SET FOREIGN_KEY_CHECKS = 1;
```

### **Para limpiar manualmente:**
```bash
# Opción 1: Reiniciar aplicaciones (recomendado)
# Opción 2: Ejecutar TRUNCATE en DataGrip
# Opción 3: Cambiar spring.sql.init.mode=never temporalmente
```

## 💡 Ventajas del Seed Data

### **✅ Para ti:**
- **Automático**: Se carga solo al iniciar
- **Consistente**: Siempre los mismos datos
- **Reproducible**: Tu colega tendrá exactamente los mismos datos
- **Completo**: Datos suficientes para probar todos los endpoints

### **✅ Para tu colega:**
- **Sin configuración**: Solo arranca la app
- **Sin trabajo manual**: No necesita llenar datos en DataGrip
- **Datos coherentes**: Relaciones correctas entre tablas
- **Listo para probar**: Todos los endpoints tienen datos

### **✅ Para el equipo:**
- **Testing uniforme**: Todos tienen los mismos datos
- **Desarrollo ágil**: No pierdes tiempo creando datos
- **Demo listo**: Datos realistas para presentaciones
- **KPIs funcionando**: Datos del mes actual para pruebas

## 🚨 Importante

### **Solo para desarrollo/testing:**
- ❌ **NO usar en producción**
- ❌ **NO subir a bases de datos reales**
- ✅ **Solo para desarrollo local**
- ✅ **Fácil de eliminar después**

### **Eliminación futura:**
```properties
# Cuando quieras usar bases de datos reales, cambiar a:
spring.sql.init.mode=never
```

## 📝 Notas sobre los Datos

### **Pacientes Disponibles:**
- Los pacientes con IDs 6-15 (user_id 206-215) **NO tienen dentista asignado**
- Aparecen en el endpoint `GET /api/core/dentist/available-patients`
- Pueden ser asignados usando `PUT /api/core/patient/{patientId}/assign-dentist/{dentistId}`

### **Recetas del Mes:**
- Hay 10 recetas de diciembre 2024 para probar el KPI de recetas del mes
- El endpoint `GET /api/core/patient/{id}/prescriptions/count` puede filtrar por mes

### **Búsquedas en Historia Clínica:**
- Las descripciones son detalladas para facilitar la búsqueda por texto
- Hay entradas en diferentes fechas para probar búsquedas por fecha/rango
- Ejemplo de búsqueda: `searchText=limpieza` encontrará varias entradas

### **Estados de Tratamientos:**
- **Completado**: Tratamiento finalizado
- **En progreso**: Tratamiento activo, en desarrollo

### **Estados de Citas:**
- **PROGRAMADO**: Cita programada, pendiente de confirmación
- **CONFIRMADO**: Cita confirmada por el paciente
- **COMPLETADO**: Cita ya realizada
- **CANCELADO**: Cita cancelada

## 🎉 ¡Listo para probar!

Con el seed data configurado, puedes:
1. **Arrancar las aplicaciones** y los datos se cargan automáticamente
2. **Probar todos los endpoints** con datos realistas
3. **Ver relaciones coherentes** entre tablas
4. **Probar búsquedas** en historia clínica (texto, fecha, rango)
5. **Probar KPIs** de recetas del mes
6. **Desarrollar sin preocuparte por datos**

¡Tu colega también tendrá exactamente los mismos datos sin configuración adicional!
