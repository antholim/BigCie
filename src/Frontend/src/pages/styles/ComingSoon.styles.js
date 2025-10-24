const styles = {
  root: {
    minHeight: "100vh",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    background: "linear-gradient(135deg, #EEF2FF 0%, #E0F2F1 100%)",
    padding: "24px",
  },
  card: {
    maxWidth: "460px",
    width: "100%",
    background: "#fff",
    borderRadius: "16px",
    padding: "40px",
    boxShadow: "0 16px 45px rgba(15, 23, 42, 0.12)",
    textAlign: "center",
  },
  heading: {
    fontSize: "28px",
    fontWeight: 700,
    marginBottom: "16px",
    color: "#1e1b4b",
  },
  body: {
    fontSize: "16px",
    lineHeight: 1.6,
    marginBottom: "32px",
    color: "#475569",
  },
  button: {
    padding: "12px 24px",
    borderRadius: "9999px",
    border: "none",
    background: "#2563eb",
    color: "#fff",
    fontWeight: 600,
    cursor: "pointer",
    transition: "transform 0.15s ease, box-shadow 0.15s ease",
  },
};

export default styles;
