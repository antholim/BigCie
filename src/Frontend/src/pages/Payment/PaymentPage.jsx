import React, { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import FetchingService from "../../services/FetchingService";
import { useAuth } from "../../contexts/AuthContext";
import "../../components/home.css";
import { formatDateTime } from "../../utils/utils";
import SideBar from "../../components/SideBar";
import PaymentInfo from "./components/PaymentInfo";
import PaymentPlan from "./components/PaymentPlan";

export default function PaymentPage() {
  const navigate = useNavigate();
  const { user, isAuthenticated, loading: authLoading } = useAuth();
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
    const [paymentInfos, setPaymentInfos] = useState({
      creditCardNumber: "",
      cardExpiry: "",
      cardHolderName: "",
      cardType: "",
      cvv: "",
    });
    const [paymentLoading, setPaymentLoading] = useState(false);
    const [paymentError, setPaymentError] = useState("");
    const [paymentSuccess, setPaymentSuccess] = useState("");

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      navigate("/login", { replace: true });
    }
  }, [authLoading, isAuthenticated, navigate]);

  const loadPayments = useCallback(async () => {
    if (!isAuthenticated || !user) return;
    const identifier = user.id ?? user.userId;
    if (!identifier) return;
    setLoading(true);
    setError("");
    try {
      const response = await FetchingService.get("/api/v1/payments/me");
      console.log("Payments response:", response);
      const data = Array.isArray(response?.data) ? response.data : [];
      setPayments(data);
    } catch (err) {
      setError(err.response?.data?.message || err.message || "Unable to load payments right now.");
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, user]);

  useEffect(() => {
    loadPayments();
  }, [loadPayments]);

    const updatePayment = (field, value) =>
    setPaymentInfos(prev => ({ ...prev, [field]: value }));

  const handleAddPayment = async (e) => {
    e?.preventDefault?.();
    setPaymentError("");
    setPaymentSuccess("");
    // Basic validation
    if (!paymentInfos.creditCardNumber || !paymentInfos.cardExpiry || !paymentInfos.cardHolderName || !paymentInfos.cardType || !paymentInfos.cvv) {
      setPaymentError("Please complete all payment fields.");
      return;
    }
    setPaymentLoading(true);
    try {
      // NOTE: CVV should not be stored in plaintext. Backend should tokenize or encrypt sensitive details.
      const payload = {
        creditCardNumber: paymentInfos.creditCardNumber,
        cardExpiry: paymentInfos.cardExpiry,
        cardHolderName: paymentInfos.cardHolderName,
        cardType: paymentInfos.cardType,
        cvv: paymentInfos.cvv,
      };
      await FetchingService.post("/api/v1/payments/add-payment", payload);
      setPaymentSuccess("Payment method added successfully.");
      // Clear sensitive fields (especially cvv)
      setPaymentInfos({
        creditCardNumber: "",
        cardExpiry: "",
        cardHolderName: "",
        cardType: "",
        cvv: "",
      });
    } catch (err) {
      console.error("Failed to add payment method:", err);
      setPaymentError(
        err.response?.data?.message || err.message || "Failed to add payment method. Please try again."
      );
    } finally {
      setPaymentLoading(false);
    }
  };
  if (authLoading) {
    return (
      <div className="db-page" style={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", background: "#f8fafc" }}>
        <p className="db-muted">Loading payments...</p>
      </div>
    );
  }

  if (!isAuthenticated) return null;

  return (
    <div className="db-page" style={{ minHeight: "100vh", background: "#f8fafc" }}>
      <header className="db-header" style={{ position: "sticky", top: 0, zIndex: 5 }}>
        <div className="db-container db-flex-between">
          <div className="db-brand">
            <div className="db-logo">dYs�</div>
            <span>DowntownBike</span>
          </div>
          <div className="db-actions" style={{ gap: "12px" }}>
            <button className="db-btn" type="button" onClick={() => navigate(-1)}>Back</button>
            <button className="db-btn" type="button" onClick={loadPayments} disabled={loading}>
              {loading ? "Refreshing..." : "Refresh"}
            </button>
          </div>
        </div>
      </header>
      <div style={{ display: "flex", gap: 24, alignItems: "flex-start", paddingTop: 24 }}>
              <SideBar username={user?.username} email={user?.email} />
      
      <main className="db-container" style={{ paddingTop: 48, paddingBottom: 64 }}>
        <PaymentPlan/>
    
                      <div className="db-card" style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
                <div className="db-flex-between" style={{ alignItems: "center" }}>
                  <div>
                    <h2 style={{ marginBottom: "4px" }}>Add payment method</h2>
                    <p className="db-muted" style={{ margin: 0 }}>
                      Add a credit card to quickly pay for rentals. Sensitive data should be tokenized by backend.
                    </p>
                  </div>
                </div>

                <form onSubmit={handleAddPayment} style={{ display: "grid", gap: "8px" }}>
                  <label style={{ fontSize: "13px", fontWeight: 600, color: "#0f172a" }}>
                    Card holder name
                    <input
                      type="text"
                      value={paymentInfos.cardHolderName}
                      onChange={(e) => updatePayment("cardHolderName", e.target.value)}
                      placeholder="Full name"
                      style={{ width: "100%", padding: "8px", marginTop: "6px", borderRadius: 6, border: "1px solid #e2e8f0" }}
                    />
                  </label>

                  <label style={{ fontSize: "13px", fontWeight: 600, color: "#0f172a" }}>
                    Card number
                    <input
                      type="text"
                      inputMode="numeric"
                      value={paymentInfos.creditCardNumber}
                      onChange={(e) => updatePayment("creditCardNumber", e.target.value)}
                      placeholder="4242 4242 4242 4242"
                      style={{ width: "100%", padding: "8px", marginTop: "6px", borderRadius: 6, border: "1px solid #e2e8f0" }}
                    />
                  </label>

                  <div style={{ display: "flex", gap: "8px" }}>
                    <label style={{ flex: 1, fontSize: "13px", fontWeight: 600, color: "#0f172a" }}>
                      Expiry (MM/YY)
                      <input
                        type="text"
                        value={paymentInfos.cardExpiry}
                        onChange={(e) => updatePayment("cardExpiry", e.target.value)}
                        placeholder="MM/YY"
                        style={{ width: "100%", padding: "8px", marginTop: "6px", borderRadius: 6, border: "1px solid #e2e8f0" }}
                      />
                    </label>

                    <label style={{ flex: 1, fontSize: "13px", fontWeight: 600, color: "#0f172a" }}>
                      CVV
                      <input
                        type="password"
                        value={paymentInfos.cvv}
                        onChange={(e) => updatePayment("cvv", e.target.value)}
                        placeholder="123"
                        style={{ width: "100%", padding: "8px", marginTop: "6px", borderRadius: 6, border: "1px solid #e2e8f0" }}
                      />
                    </label>
                  </div>

                  <label style={{ fontSize: "13px", fontWeight: 600, color: "#0f172a" }}>
                    Card type
                    <select
                      value={paymentInfos.cardType}
                      onChange={(e) => updatePayment("cardType", e.target.value)}
                      style={{ width: "100%", padding: "8px", marginTop: "6px", borderRadius: 6, border: "1px solid #e2e8f0" }}
                    >
                      <option value="">Select card type</option>
                      <option value="VISA">VISA</option>
                      <option value="MASTERCARD">Mastercard</option>
                      <option value="AMEX">Amex</option>
                      <option value="DISCOVER">Discover</option>
                    </select>
                  </label>

                  {paymentError && <p style={{ color: "#dc2626", margin: 0 }}>{paymentError}</p>}
                  {paymentSuccess && <p style={{ color: "#16a34a", margin: 0 }}>{paymentSuccess}</p>}

                  <div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}>
                    <button
                      type="button"
                      className="db-btn"
                      onClick={() =>
                        setPaymentInfos({
                          creditCardNumber: "",
                          cardExpiry: "",
                          cardHolderName: "",
                          cardType: "",
                          cvv: "",
                        })
                      }
                      disabled={paymentLoading}
                    >
                      Clear
                    </button>
                    <button
                      type="submit"
                      className="db-btn primary"
                      disabled={paymentLoading}
                      style={{ minWidth: 120 }}
                    >
                      {paymentLoading ? "Adding..." : "Add payment"}
                    </button>
                  </div>
                </form>
              </div>
        <section style={{ marginBottom: 24 }}>
          <h1 style={{ fontSize: 28, fontWeight: 700, marginBottom: 8 }}>Your payment information</h1>
          <p className="db-muted" style={{ margin: 0 }}>
            Review your past payment information below.
          </p>
        </section>
      
        <div style={{ display: "grid", gap: 16 }}>
          {loading && <p className="db-muted">Loading payment information...</p>}
          {!loading && error && <p style={{ color: "#dc2626" }}>{error}</p>}
          {!loading && !error && payments.length === 0 && (
            <p className="db-muted">No payment information found.</p>
          )}

          {!loading && !error && payments.map((payment) => {
            return <PaymentInfo paymentInfo={payment} />
          })}
        </div>
      </main>
      </div>
    </div>
  );
}

