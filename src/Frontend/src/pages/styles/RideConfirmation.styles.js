const styles = {
  root: {
    minHeight: "100vh",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    padding: "32px",
    background: "linear-gradient(135deg, #f0f4ff 0%, #e8f9f1 100%)",
  },
  card: {
    width: "100%",
    maxWidth: "520px",
    background: "#fff",
    borderRadius: "18px",
    padding: "40px",
    boxShadow: "0 16px 45px rgba(15, 23, 42, 0.16)",
  },
  heading: {
    fontSize: "30px",
    fontWeight: 700,
    marginBottom: "12px",
    color: "#1e293b",
  },
  body: {
    fontSize: "16px",
    lineHeight: 1.6,
    color: "#475569",
    marginBottom: "28px",
  },
  details: {
    borderRadius: "14px",
    background: "#f8fafc",
    padding: "20px",
    marginBottom: "28px",
    display: "flex",
    flexDirection: "column",
    gap: "12px",
  },
  detailRow: {
    display: "flex",
    justifyContent: "space-between",
    fontSize: "15px",
  },
  detailLabel: {
    color: "#64748b",
    fontWeight: 500,
  },
  detailValue: {
    color: "#0f172a",
    fontWeight: 600,
  },
  actions: {
    display: "flex",
    gap: "12px",
    flexWrap: "wrap",
  },
  primaryButton: {
    flex: "1 1 220px",
    padding: "12px 20px",
    borderRadius: "12px",
    border: "none",
    background: "#2563eb",
    color: "#fff",
    fontWeight: 600,
    cursor: "pointer",
  },
  secondaryButton: {
    flex: "1 1 220px",
    padding: "12px 20px",
    borderRadius: "12px",
    border: "1px solid rgba(37, 99, 235, 0.6)",
    background: "transparent",
    color: "#2563eb",
    fontWeight: 600,
    cursor: "pointer",
  },
};

export default styles;
