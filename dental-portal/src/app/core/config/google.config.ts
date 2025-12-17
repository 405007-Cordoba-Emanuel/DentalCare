// Note: API_BASE_URL is now handled by ApiConfig service
// This is kept for backward compatibility if needed elsewhere
export const GOOGLE_CONFIG = {
  CLIENT_ID: '830906165893-c7i1u8o134ej796cgoihpm9secct665m.apps.googleusercontent.com',
  API_BASE_URL: 'http://localhost:8081/api' // Fallback for non-Docker environments
};
