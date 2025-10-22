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
- ✅ **5 Pacientes vinculados** (IDs: 201-205)
- ✅ **5 Pacientes disponibles** (IDs: 206-210)

### **Microservicio Core (`be-core/src/main/resources/data.sql`)**
- ✅ **5 Dentistas** vinculados a usuarios
- ✅ **5 Pacientes** ya vinculados a dentistas
- ✅ **5 Tratamientos** en diferentes estados
- ✅ **5 Citas médicas** programadas
- ✅ **5 Entradas de historial** médico
- ✅ **5 Recetas** médicas

## 🎯 Datos de Prueba Incluidos

### **Usuarios Disponibles para Testing:**
```bash
# Administrador
Email: admin@dentalcare.com
Password: 123456

# Dentista
Email: maria.gonzalez@dentalcare.com
Password: 123456

# Paciente Disponible (aparece en endpoint de disponibles)
Email: diego.torres@email.com
Password: 123456
```

### **Relaciones Coherentes:**
- ✅ **Pacientes vinculados**: user_id 201-205 tienen dentista asignado
- ✅ **Pacientes disponibles**: user_id 206-210 NO tienen dentista asignado
- ✅ **Tratamientos activos**: En diferentes estados (completed, in_progress)
- ✅ **Citas programadas**: Para fechas futuras
- ✅ **Historial médico**: Entradas realistas

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
```

## 🧪 Endpoints para Probar

### **1. Usuarios Pacientes Disponibles**
```bash
GET /api/dentists/available-patients
# Devuelve usuarios 206-210 (sin dentista vinculado)
```

### **2. Vincular Paciente**
```bash
POST /api/dentists/{dentistId}/patients
{
  "userId": 206,
  "dni": "12345678",
  "birthDate": "1995-01-20"
}
```

### **3. Listar Pacientes de un Dentista**
```bash
GET /api/dentists/{dentistId}/patients
# Devuelve pacientes ya vinculados
```

### **4. Crear Cita**
```bash
POST /api/dentists/{dentistId}/appointments
{
  "patientId": 1,
  "startDateTime": "2024-12-25T10:00:00",
  "endDateTime": "2024-12-25T10:30:00",
  "reason": "Control de rutina"
}
```

## 🔄 Limpieza de Datos

### **El seed data incluye limpieza automática:**
```sql
-- Se ejecuta al inicio para datos frescos
TRUNCATE TABLE appointments;
TRUNCATE TABLE patients;
TRUNCATE TABLE dentists;
-- etc...
```

### **Para limpiar manualmente:**
```bash
# Opción 1: Reiniciar aplicaciones
# Opción 2: Ejecutar TRUNCATE en DataGrip
# Opción 3: Eliminar archivos data.sql temporalmente
```

## 💡 Ventajas del Seed Data

### **✅ Para ti:**
- **Automático**: Se carga solo al iniciar
- **Consistente**: Siempre los mismos datos
- **Reproducible**: Tu colega tendrá exactamente los mismos datos

### **✅ Para tu colega:**
- **Sin configuración**: Solo arranca la app
- **Sin trabajo manual**: No necesita llenar datos en DataGrip
- **Datos coherentes**: Relaciones correctas entre tablas

### **✅ Para el equipo:**
- **Testing uniforme**: Todos tienen los mismos datos
- **Desarrollo ágil**: No pierdes tiempo creando datos
- **Demo listo**: Datos realistas para presentaciones

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

## 🎉 ¡Listo para probar!

Con el seed data configurado, puedes:
1. **Arrancar las aplicaciones**
2. **Probar todos los endpoints**
3. **Ver relaciones coherentes**
4. **Desarrollar sin preocuparte por datos**

¡Tu colega también tendrá exactamente los mismos datos sin configuración adicional!
