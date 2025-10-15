import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import FetchingService from "../../services/FetchingService";
import "../../components/home.css";

export default function RegisterPage() {
  const [form, setForm] = useState({ username: "", email: "", password: "" });
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
      await FetchingService.post("/api/v1/register", form);
      navigate("/login");
    } catch (err) {
      setError(
        err.response?.data?.message || err.message || "Registration failed"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="db-page" style={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center" }}>
      <form className="db-card" style={{ padding: 32, minWidth: 320 }} onSubmit={handleSubmit}>
        <h2 style={{ marginBottom: 24 }}>Register</h2>
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
          <span>Email</span>
          <input
            name="email"
            type="email"
            value={form.email}
            onChange={handleChange}
            required
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
          {loading ? "Registering..." : "Register"}
        </button>
      </form>
    </div>
  );
}
