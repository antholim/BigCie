import React, { useMemo } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import styles from "./styles/RideConfirmation.styles";

export default function RideConfirmation() {
  const navigate = useNavigate();
  const location = useLocation();

  const { plan, rideId, message, startedAt } = location.state ?? {};

  const formattedStartedAt = useMemo(() => {
    if (!startedAt) return null;
    try {
      return new Date(startedAt).toLocaleString();
    } catch (err) {
      return startedAt;
    }
  }, [startedAt]);

  return (
    <div style={styles.root}>
      <div style={styles.card}>
        <h1 style={styles.heading}>Ride Ready</h1>
        <p style={styles.body}>
          {message ?? "Your ride is ready to begin. Grab a bike and enjoy the journey!"}
        </p>
        <div style={styles.details}>
          <Detail label="Plan" value={plan ?? "Pay-As-You-Go"} />
          {rideId && <Detail label="Ride ID" value={rideId} />}
          {formattedStartedAt && <Detail label="Start Time" value={formattedStartedAt} />}
        </div>
        <div style={styles.actions}>
          <button style={styles.primaryButton} onClick={() => navigate("/map")}>Open operator map</button>
          <button style={styles.secondaryButton} onClick={() => navigate("/")}>Back to home</button>
        </div>
      </div>
    </div>
  );
}

function Detail({ label, value }) {
  return (
    <div style={styles.detailRow}>
      <span style={styles.detailLabel}>{label}</span>
      <span style={styles.detailValue}>{value}</span>
    </div>
  );
}
