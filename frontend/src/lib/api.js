const defaultUrls = ['http://localhost:8080/api', 'http://localhost:8000/api', 'http://localhost:8001/api'];
const configuredUrl = import.meta.env.VITE_API_BASE_URL;
const baseUrls = configuredUrl ? [configuredUrl, ...defaultUrls.filter((url) => url !== configuredUrl)] : defaultUrls;

async function request(path, { method = 'GET', body, token } = {}) {
  let lastNetworkError = null;
  for (const baseUrl of baseUrls) {
    try {
      const response = await fetch(`${baseUrl}${path}`, {
        method,
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        ...(body ? { body: JSON.stringify(body) } : {}),
      });

      if (!response.ok) {
        let message = `Request failed with ${response.status}`;
        try {
          const errorBody = await response.json();
          message = errorBody.message || message;
        } catch {
          // Ignore invalid JSON body.
        }
        throw new Error(message);
      }

      if (response.status === 204) {
        return null;
      }
      return response.json();
    } catch (error) {
      if (error instanceof TypeError) {
        lastNetworkError = error;
        continue;
      }
      throw error;
    }
  }
  throw new Error(lastNetworkError?.message || 'Unable to connect to backend API');
}

export const api = {
  get: (path, token) => request(path, { token }),
  post: (path, body, token) => request(path, { method: 'POST', body, token }),
  put: (path, body, token) => request(path, { method: 'PUT', body, token }),
  delete: (path, token) => request(path, { method: 'DELETE', token }),
};
