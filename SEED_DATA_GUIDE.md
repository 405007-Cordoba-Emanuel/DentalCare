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
- ✅ **1 Dentista** (ID: 2)
- ✅ **8 Pacientes** (IDs: 3-10) - Todos vinculados al mismo dentista

### **Microservicio Core (`be-core/src/main/resources/data.sql`)**
- ✅ **1 Dentista** vinculado a usuario (ID: 1, user_id: 2)
- ✅ **8 Pacientes** todos vinculados al mismo dentista (IDs: 1-8, user_id: 3-10)
- ✅ **24 Tratamientos**: 3 por paciente
  - 1 completado por paciente
  - 1 abandonado por paciente
  - 1 en curso por paciente (aparece en dashboard del paciente)
- ✅ **109 Citas/Turnos médicos**:
  - Distribuidas en noviembre (pasados), diciembre y enero (futuros)
  - Incluye días completos para probar visualización
  - Todos los estados: PROGRAMADO, CONFIRMADO, COMPLETADO, CANCELADO, AUSENTE
- ✅ **80 Recetas médicas**: 10 por paciente
  - Algunas vinculadas a tratamientos/citas
  - Otras independientes
  - Distribuidas en varios meses (septiembre-diciembre) para probar filtros
- ✅ **32 Odontogramas**: 4 por paciente
  - Evolución a lo largo del tiempo
- ✅ **80 Entradas de Historia Clínica**: 10 por paciente
  - Algunas vinculadas a tratamientos/recetas
  - Otras independientes
  - Para probar búsqueda y filtrado
- ✅ **8 Conversaciones de Chat**
- ✅ **80 Mensajes de Chat**: 10 por conversación (tipo consulta)

## 🎯 Datos de Prueba Incluidos

### **Usuarios Disponibles para Demostración:**
```bash
# ============================================
# ADMINISTRADOR
# ============================================
Email: admin@dentalcare.com
Password: 123456
User ID: 1, Role: ADMIN
Nombre: Carlos Administrador

# ============================================
# DENTISTA
# ============================================
Email: maria.gonzalez@dentalcare.com
Password: 123456
Dentist ID: 1, User ID: 2
Nombre: Dra. María González
Especialidad: Odontología General
Matrícula: DENT-001-MED

# ============================================
# PACIENTES (todos vinculados al dentista)
# ============================================
# Paciente 1
Email: maria.perez@email.com
Password: 123456
Patient ID: 1, User ID: 3, Dentist ID: 1
Nombre: María Elena Pérez
DNI: 12345678

# Paciente 2
Email: carlos.garcia@email.com
Password: 123456
Patient ID: 2, User ID: 4, Dentist ID: 1
Nombre: Carlos Alberto García
DNI: 23456789

# Paciente 3
Email: ana.silva@email.com
Password: 123456
Patient ID: 3, User ID: 5, Dentist ID: 1
Nombre: Ana Beatriz Silva
DNI: 34567890

# Paciente 4
Email: roberto.morales@email.com
Password: 123456
Patient ID: 4, User ID: 6, Dentist ID: 1
Nombre: Roberto Carlos Morales
DNI: 45678901

# Paciente 5
Email: laura.vargas@email.com
Password: 123456
Patient ID: 5, User ID: 7, Dentist ID: 1
Nombre: Laura Patricia Vargas
DNI: 56789012

# Paciente 6
Email: diego.torres@email.com
Password: 123456
Patient ID: 6, User ID: 8, Dentist ID: 1
Nombre: Diego Alejandro Torres
DNI: 67890123

# Paciente 7
Email: valentina.jimenez@email.com
Password: 123456
Patient ID: 7, User ID: 9, Dentist ID: 1
Nombre: Valentina Jiménez
DNI: 78901234

# Paciente 8
Email: fernando.herrera@email.com
Password: 123456
Patient ID: 8, User ID: 10, Dentist ID: 1
Nombre: Fernando Herrera
DNI: 89012345
```

**⚠️ IMPORTANTE:** 
- Todos los usuarios (admin, dentista y pacientes) usan la misma contraseña de prueba: `123456`
- **TODOS los emails y contraseñas listados arriba coinciden exactamente con los datos en `users/src/main/resources/data.sql`**
- Si tienes problemas de login, verifica que estés usando el email exacto (respeta mayúsculas/minúsculas)

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
