import React, { createContext, useContext, useState, useEffect } from "react";
import FetchingService from "../services/FetchingService";

const AuthContext = createContext(null);

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
      setUser(res.data || null);
    } catch (err) {
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  const login = async (credentials) => {
    // credentials: { usernameOrEmail, password }
    const res = await FetchingService.post("/api/v1/login", credentials);
    // server should set httpOnly cookie; we rely on /api/v1/me to return user
    await checkAuth();
    return res;
  };

  const logout = async () => {
    try {
      await FetchingService.post("/api/v1/logout");
    } finally {
      setUser(null);
    }
  };

  useEffect(() => {
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
