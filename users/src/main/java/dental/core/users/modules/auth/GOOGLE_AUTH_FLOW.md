# Flujo de Autorización de Google Calendar

## Descripción

Este documento describe cómo funciona el flujo de autorización de Google Calendar después del login con Google.

## Endpoints Disponibles

### 1. Login con Google
**POST** `/api/auth/google/login`

Este endpoint ahora incluye información sobre la autorización de Google Calendar en la respuesta.

**Respuesta:**
```json
{
  "token": "jwt_token_here",
  "name": "Nombre del Usuario",
  "email": "usuario@example.com",
  "picture": "https://example.com/picture.jpg",
  "googleCalendarAuthorized": false,
  "authorizationUrl": "https://accounts.google.com/o/oauth2/v2/auth?...",
  "message": "Para usar las funciones de Google Calendar, necesitas autorizar el acceso adicional."
}
```

### 2. Verificar Autorización
**GET** `/api/auth/google/check-authorization?userEmail=usuario@example.com`

Verifica si el usuario tiene autorización para Google Calendar.

**Respuesta:**
```json
{
  "isAuthorized": false,
  "authorizationUrl": "https://accounts.google.com/o/oauth2/v2/auth?...",
  "message": "Usuario no autorizado para Google Calendar. Se requiere autorización adicional.",
  "userEmail": "usuario@example.com",
  "scopes": "openid profile email https://www.googleapis.com/auth/calendar.events"
}
```

### 3. Solicitar Autorización de Calendar
**GET** `/api/auth/google/request-calendar-authorization`

Genera una URL específica para solicitar autorización de Google Calendar.

**Respuesta:**
```json
{
  "authorizationUrl": "https://accounts.google.com/o/oauth2/v2/auth?...",
  "message": "URL de autorización para Google Calendar generada",
  "scopes": "openid profile email https://www.googleapis.com/auth/calendar.events"
}
```

### 4. Callback de OAuth
**GET** `/api/auth/google/callback?code=authorization_code`

Maneja el callback después de la autorización de Google.

## Flujo de Uso

### Escenario 1: Usuario se logea por primera vez
1. Usuario hace login con Google
2. Sistema verifica si tiene autorización para Calendar
3. Si no la tiene, se incluye `authorizationUrl` en la respuesta
4. Frontend puede mostrar un botón para autorizar Calendar
5. Usuario hace clic en el botón y es redirigido a Google
6. Después de autorizar, Google redirige al callback
7. Sistema almacena los tokens y actualiza el estado de autorización

### Escenario 2: Usuario ya autorizado
1. Usuario hace login con Google
2. Sistema verifica autorización y encuentra tokens válidos
3. Respuesta incluye `googleCalendarAuthorized: true`
4. Usuario puede usar funciones de Calendar inmediatamente

### Escenario 3: Verificación posterior
1. Usuario ya logueado quiere verificar su autorización
2. Frontend llama a `/check-authorization`
3. Sistema verifica tokens y los renueva si es necesario
4. Retorna estado actual de autorización

## Implementación en Frontend

```javascript
// Ejemplo de uso en el frontend
async function handleGoogleLogin(googleToken) {
  const response = await fetch('/api/auth/google/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ idToken: googleToken })
  });
  
  const authData = await response.json();
  
  if (!authData.googleCalendarAuthorized) {
    // Mostrar botón de autorización
    showCalendarAuthorizationButton(authData.authorizationUrl);
  } else {
    // Usuario ya autorizado, puede usar Calendar
    enableCalendarFeatures();
  }
}

function showCalendarAuthorizationButton(authorizationUrl) {
  const button = document.createElement('button');
  button.textContent = 'Autorizar Google Calendar';
  button.onclick = () => window.location.href = authorizationUrl;
  document.body.appendChild(button);
}
```

## Estados de Autorización

- **No autorizado**: Usuario no tiene tokens de Google o tokens expirados
- **Autorizado**: Usuario tiene tokens válidos para Google Calendar
- **Pendiente de renovación**: Tokens expirados pero refresh token válido

## Scopes Requeridos

- `openid`: Para autenticación
- `profile`: Para información del perfil
- `email`: Para email del usuario
- `https://www.googleapis.com/auth/calendar.events`: Para acceso a eventos del calendario

## Notas Importantes

1. Los tokens se almacenan en la base de datos del usuario
2. Los refresh tokens se usan automáticamente para renovar access tokens expirados
3. La verificación de autorización incluye renovación automática de tokens
4. El sistema maneja errores de autorización de forma graceful
5. Los scopes se solicitan de forma incremental para mejor UX
