import React, { useState, useEffect } from "react";
import FetchingService from "../../../services/FetchingService";

export default function FlexDollarBalance({ refreshTrigger }) {
  const [balance, setBalance] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    loadBalance();
  }, [refreshTrigger]);

  const loadBalance = async () => {
    setLoading(true);
    setError("");
    try {
      const response = await FetchingService.get("/api/v1/payments/flex-dollars");
      setBalance(response.data);
    } catch (err) {
      console.error("Failed to load flex dollar balance:", err);
      setError(err.response?.data?.message || "Failed to load balance");
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="db-card" style={{ background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)", color: "white" }}>
        <p style={{ margin: 0, color: "white" }}>Loading flex dollars...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="db-card" style={{ background: "#fef2f2", borderLeft: "4px solid #dc2626" }}>
        <h3 style={{ color: "#991b1b", marginBottom: 8 }}>Error</h3>
        <p style={{ color: "#7f1d1d", margin: 0 }}>{error}</p>
      </div>
    );
  }

  return (
    <div 
      className="db-card" 
      style={{ 
        background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)", 
        color: "white",
        display: "flex",
        flexDirection: "column",
        gap: 12
      }}
    >
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
        <div>
          <h2 style={{ marginBottom: 4, color: "white", fontSize: 16, fontWeight: 600 }}>
            💰 Flex Dollars
          </h2>
          <p style={{ margin: 0, fontSize: 13, opacity: 0.9 }}>
            Your balance
          </p>
        </div>
        <button
          className="db-btn"
          type="button"
          onClick={loadBalance}
          style={{
            background: "rgba(255,255,255,0.2)",
            color: "white",
            border: "1px solid rgba(255,255,255,0.3)",
            fontSize: 12,
            padding: "4px 12px"
          }}
        >
          Refresh
        </button>
      </div>

      <div style={{ fontSize: 36, fontWeight: 700, color: "white" }}>
        ${(balance || 0).toFixed(2)}
      </div>

      <div 
        style={{ 
          background: "rgba(255,255,255,0.15)", 
          padding: 12, 
          borderRadius: 8,
          fontSize: 13
        }}
      >
        <p style={{ margin: 0, marginBottom: 6, fontWeight: 600 }}>How it works:</p>
        <ul style={{ margin: 0, paddingLeft: 20 }}>
          <li>Earn $5 by docking at stations below 25% capacity</li>
          <li>Automatically applied to trips and plans</li>
          <li>Never expires</li>
        </ul>
      </div>
    </div>
  );
}
