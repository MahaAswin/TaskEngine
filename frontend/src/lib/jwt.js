import { jwtDecode } from 'jwt-decode';

/**
 * Reads role claim from access token (for UI). Server remains source of truth via /users/me.
 */
export function decodeRoleFromAccessToken(accessToken) {
  if (!accessToken) return null;
  try {
    const payload = jwtDecode(accessToken);
    return payload.role ?? null;
  } catch {
    return null;
  }
}
