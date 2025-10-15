import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../../components/home.css";

export default function LoginPage() {
  const [form, setForm] = useState({ usernameOrEmail: "", password: "" });
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
      const res = await fetch("/api/v1/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(form),
      });
      if (!res.ok) {
        const data = await res.json();
        throw new Error(data.message || "Login failed");
      }
      // Optionally handle login response (e.g., save token)
      navigate("/");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="db-page" style={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center" }}>
      <form className="db-card" style={{ padding: 32, minWidth: 320 }} onSubmit={handleSubmit}>
        <h2 style={{ marginBottom: 24 }}>Login</h2>
        <div className="db-field">
          <span>Username or Email</span>
          <input
            name="usernameOrEmail"
            type="text"
            value={form.usernameOrEmail}
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
      </form>
    </div>
  );
}
