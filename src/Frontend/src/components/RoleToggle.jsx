import React from "react";
import { useAuth } from "../contexts/AuthContext";
import "./RoleToggle.css";

export default function RoleToggle() {
  const { canToggleRole, viewMode, toggleViewMode } = useAuth();

  if (!canToggleRole) {
    return null;
  }

  const isOperatorView = viewMode === "OPERATOR";

  return (
    <div className="role-toggle">
      <span className="role-toggle-label">View as:</span>
      <button
        className={`role-toggle-btn ${isOperatorView ? "active" : ""}`}
        onClick={() => toggleViewMode("OPERATOR")}
        title="Switch to operator view to manage all trips and rebalance bikes"
      >
        👷 Operator
      </button>
      <button
        className={`role-toggle-btn ${!isOperatorView ? "active" : ""}`}
        onClick={() => toggleViewMode("RIDER")}
        title="Switch to rider view to book trips with operator discount"
      >
        🚴 Rider
      </button>
    </div>
  );
}
