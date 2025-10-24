import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import FetchingService from "../../services/FetchingService";

// Ensure axios sends cookies with requests
// axios.defaults.withCredentials = true; // This line is no longer needed
import "../../components/home.css";

export default function LoginPage() {
  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    try {
      const res = await FetchingService.post("/api/v1/login", form);
      // Optionally handle login response (e.g., save token)
      navigate("/");
    } catch (err) {
      setError(
        err.response?.data?.message || err.message || "Login failed"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="db-page" style={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center" }}>
      <form className="db-card" style={{ padding: 32, minWidth: 320 }} onSubmit={handleSubmit}>
        <h2 style={{ marginBottom: 24 }}>Login</h2>
        <div className="db-field">
          <span>Username</span>
          <input
            name="username"
            type="text"
            value={form.username}
            onChange={handleChange}
            required
            autoFocus
          />
        </div>
        <div className="db-field" style={{ marginTop: 16 }}>
          <span>Password</span>
          <input
            name="password"
            type="password"
            value={form.password}
            onChange={handleChange}
            required
          />
        </div>
        {error && <div className="db-muted" style={{ color: "#c00", marginTop: 12 }}>{error}</div>}
        <button className="db-btn primary full" type="submit" style={{ marginTop: 24 }} disabled={loading}>
          {loading ? "Logging in..." : "Login"}
        </button>
        <p style={{ marginTop: 16, fontSize: 14, color: "#475569" }}>
          Don't have an account?{" "}
          <button
            type="button"
            onClick={() => navigate("/register")}
            style={{ border: "none", background: "none", color: "#2563eb", cursor: "pointer", fontWeight: 600 }}
          >
            Create one
          </button>
        </p>
     </form>
   </div>
 );
}
