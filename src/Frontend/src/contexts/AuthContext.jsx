import React, { createContext, useContext, useState, useEffect } from "react";
import FetchingService from "../services/FetchingService";

const AuthContext = createContext(null);
const STORAGE_KEY = "bigcie.auth.user";

const normalizeUser = (raw) => {
  if (!raw) return null;
  const id = raw.id ?? raw.userId ?? null;
  return {
    id,
    userId: raw.userId ?? id ?? null,
    username: raw.username ?? raw.name ?? "",
    email: raw.email ?? "",
    userType: raw.userType ?? raw.type ?? null,
    accessToken: raw.accessToken,
    refreshToken: raw.refreshToken,
  };
};

const readStoredUser = () => {
  if (typeof window === "undefined") return null;
  const raw = window.sessionStorage.getItem(STORAGE_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch (err) {
    window.sessionStorage.removeItem(STORAGE_KEY);
    return null;
  }
};

const persistUser = (value) => {
  if (typeof window === "undefined") return;
  if (!value) {
    window.sessionStorage.removeItem(STORAGE_KEY);
    return;
  }
  window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify(value));
};

export function useAuth() {
  return useContext(AuthContext);
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const checkAuth = async () => {
    setLoading(true);
    try {
      const res = await FetchingService.get("/api/v1/me");
      const normalized = normalizeUser(res.data);
      setUser(normalized);
      persistUser(normalized);
      return normalized;
    } catch (err) {
      const stored = readStoredUser();
      setUser(stored);
      if (!stored) {
        persistUser(null);
      }
      return stored;
    } finally {
      setLoading(false);
    }
  };

  const login = async (credentials) => {
    const res = await FetchingService.post("/api/v1/login", credentials);
    const normalized = normalizeUser(res.data);
    setUser(normalized);
    persistUser(normalized);
    return res;
  };

  const logout = async () => {
    try {
      await FetchingService.post("/api/v1/logout");
    } catch (err) {
      // Backend may not expose a logout endpoint yet; ignore network errors.
    } finally {
      setUser(null);
      persistUser(null);
    }
  };

  useEffect(() => {
    const stored = readStoredUser();
    if (stored) {
      setUser(stored);
      setLoading(false);
      return;
    }
    checkAuth();
  }, []);

  const value = {
    user,
    isAuthenticated: !!user,
    loading,
    checkAuth,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
