import React from "react";

function CurrentPlanDisplay({ currentPlan }) {
  const planConfig = {
    SINGLE_RIDE: {
      name: "Single Ride",
      description: "Pay as you go",
      icon: "🎯",
      color: "#3b82f6",
    },
    DAY_PASS: {
      name: "Day Pass",
      description: "Unlimited rides for 24 hours",
      icon: "☀️",
      color: "#f59e0b",
    },
    MONTHLY_PASS: {
      name: "Monthly Pass",
      description: "Unlimited rides for 30 days",
      icon: "📅",
      color: "#8b5cf6",
    },
  };

  const planData = currentPlan ? (planConfig[currentPlan] || {
    name: "Unknown Plan",
    description: "Plan type not recognized",
    icon: "❓",
    color: "#6b7280",
  }) : {
    name: "No Plan Selected",
    description: "Select or create a plan to get started",
    icon: "📋",
    color: "#94a3b8",
  };

  return (
    <div className="db-card" style={{
      display: "flex",
      flexDirection: "column",
      gap: 16,
      background: `linear-gradient(135deg, ${planData.color}10 0%, ${planData.color}05 100%)`,
      borderLeft: `4px solid ${planData.color}`,
    }}>
      <div style={{ display: "flex", alignItems: "flex-start", gap: 12 }}>
        <div style={{
          fontSize: 40,
          lineHeight: 1,
          marginTop: 4
        }}>
          {planData.icon}
        </div>
        <div style={{ flex: 1 }}>
          <h2 style={{ margin: 0, marginBottom: 4, fontSize: 20, fontWeight: 700, color: planData.color }}>
            {planData.name}
          </h2>
          <p className="db-muted" style={{ margin: 0, fontSize: 13 }}>
            {planData.description}
          </p>
        </div>
      </div>

      <div style={{
        padding: 12,
        borderRadius: 8,
        background: "#ffffff",
        border: `1px solid ${planData.color}20`,
      }}>
        <p style={{ margin: 0, fontSize: 12, color: "#475569" }}>
          <strong style={{ color: "#0f172a" }}>Current Plan:</strong> {currentPlan || "Not specified"}
        </p>
      </div>

      <div style={{
        display: "flex",
        gap: 8,
        flexWrap: "wrap",
      }}>
        <button
          className="db-btn"
          type="button"
          disabled
          style={{
            fontSize: 13,
            padding: "6px 12px",
            background: planData.color,
            color: "#fff",
            border: "none",
          }}
        >
          {currentPlan ? "Current Plan" : "No Plan"}
        </button>
        <button
          className="db-btn"
          type="button"
          disabled
          style={{
            fontSize: 13,
            padding: "6px 12px",
          }}
        >
          Change Plan
        </button>
      </div>
    </div>
  );
}

export default CurrentPlanDisplay;
