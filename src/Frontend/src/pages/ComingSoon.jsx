import React from "react";
import { useNavigate } from "react-router-dom";

import styles from "./styles/ComingSoon.styles";

export default function ComingSoon() {
  const navigate = useNavigate();

  return (
    <div style={styles.root}>
      <div style={styles.card}>
        <h1 style={styles.heading}>Coming Soon</h1>
        <p style={styles.body}>
          We&apos;re working hard on the mobile app experience. Check back soon for the official launch!
        </p>
        <button style={styles.button} onClick={() => navigate(-1)}>
          Go back
        </button>
      </div>
    </div>
  );
}
