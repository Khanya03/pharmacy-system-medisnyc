
import axios from "axios";

const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

const client = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
});

// Attach the JWT (if we have one) to every outgoing request.
client.interceptors.request.use((config) => {
  const token = localStorage.getItem("rx_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// If the token has expired/is invalid, clear the session so the app
// falls back to the login screen instead of showing a broken page.
client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem("rx_token");
      localStorage.removeItem("rx_user");
    }
    return Promise.reject(error);
  }
);

export default client;