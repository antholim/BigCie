/* eslint-disable react-refresh/only-export-components */
import React, { createContext, useContext, useState, useEffect } from "react";
import FetchingService from "../services/FetchingService";

const AuthContext = createContext(null);
const STORAGE_KEY = "bigcie.auth.user";
const VIEW_MODE_KEY = "bigcie.auth.viewMode";

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
  } catch {
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

const readStoredViewMode = () => {
  if (typeof window === "undefined") return null;
  return window.sessionStorage.getItem(VIEW_MODE_KEY);
};

const persistViewMode = (mode) => {
  if (typeof window === "undefined") return;
  if (!mode) {
    window.sessionStorage.removeItem(VIEW_MODE_KEY);
    return;
  }
  window.sessionStorage.setItem(VIEW_MODE_KEY, mode);
};

export function useAuth() {
  return useContext(AuthContext);
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [viewMode, setViewMode] = useState(null);

  const checkAuth = async () => {
    setLoading(true);
    try {
      const res = await FetchingService.get("/api/v1/me");
      const normalized = normalizeUser(res.data);
      setUser(normalized);
      persistUser(normalized);
      
      // Initialize view mode for dual-role users
      if (normalized?.userType === "DUAL_ROLE") {
        const storedMode = readStoredViewMode() || "RIDER";
        setViewMode(storedMode);
        persistViewMode(storedMode);
      } else if (normalized?.userType === "OPERATOR") {
        setViewMode("OPERATOR");
        persistViewMode("OPERATOR");
      } else if (normalized?.userType === "RIDER") {
        setViewMode("RIDER");
        persistViewMode("RIDER");
      }
      return normalized;
    } catch {
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
    
    // Initialize view mode for dual-role users
    if (normalized?.userType === "DUAL_ROLE") {
      setViewMode("RIDER"); // Default to rider view
      persistViewMode("RIDER");
    } else if (normalized?.userType === "OPERATOR") {
      setViewMode("OPERATOR");
      persistViewMode("OPERATOR");
    } else if (normalized?.userType === "RIDER") {
      setViewMode("RIDER");
      persistViewMode("RIDER");
    }
    
    return res;
  };

  const logout = async () => {
    try {
      await FetchingService.post("/api/v1/logout");
    } catch {
      // Backend may not expose a logout endpoint yet; ignore network errors.
    } finally {
      setUser(null);
      setViewMode(null);
      persistUser(null);
      persistViewMode(null);
    }
  };

  const toggleViewMode = (newMode) => {
    if (user?.userType === "DUAL_ROLE") {
      setViewMode(newMode);
      persistViewMode(newMode);
    }
  };

  useEffect(() => {
    const stored = readStoredUser();
    if (stored) {
      setUser(stored);
      
      // Initialize view mode for dual-role users
      if (stored?.userType === "DUAL_ROLE") {
        const storedMode = readStoredViewMode() || "RIDER";
        setViewMode(storedMode);
      } else if (stored?.userType === "OPERATOR") {
        setViewMode("OPERATOR");
      } else if (stored?.userType === "RIDER") {
        setViewMode("RIDER");
      }
      
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
    viewMode,
    toggleViewMode,
    canToggleRole: user?.userType === "DUAL_ROLE",
    isOperatorView: viewMode === "OPERATOR",
    isRiderView: viewMode === "RIDER",
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
