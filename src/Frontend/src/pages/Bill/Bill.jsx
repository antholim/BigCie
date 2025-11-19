import React, { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import FetchingService from "../../services/FetchingService";
import { useAuth } from "../../contexts/AuthContext";
import "../../components/home.css";
import { formatDateTime } from "../../utils/utils";
import SideBar from "../../components/SideBar";
import CurrentPlanDisplay from "./components/CurrentPlanDisplay";

export default function BillPage() {
  const navigate = useNavigate();
  const { user, isAuthenticated, loading: authLoading } = useAuth();
  const [currentPlan, setCurrentPlan] = useState(null);
  const [bills, setBills] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      navigate("/login", { replace: true });
    }
  }, [authLoading, isAuthenticated, navigate]);

  const loadBillingData = useCallback(async () => {
    if (!isAuthenticated || !user) return;
    setLoading(true);
    setError("");
    try {
      // Fetch current plan
      const planResponse = await FetchingService.get("/api/v1/payments/current-plan");
      console.log("Current plan response:", planResponse);
      setCurrentPlan(planResponse.data?.pricingPlan || null);

      // Fetch bills list from billing-info endpoint
      const billsResponse = await FetchingService.get("/api/v1/payments/billing-info");
      console.log("Bills response:", billsResponse);
      
      const billsList = Array.isArray(billsResponse.data) ? billsResponse.data : [];
      console.log("Bills list:", billsList);
      setBills(billsList);
      
      if (billsList.length === 0) {
        setError("No bills found for this user");
      }
    } catch (err) {
      console.error("Error loading billing data:", err);
      setBills([]);
      setError(err.response?.data?.message || err.message || "Unable to load bills.");
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, user]);

  useEffect(() => {
    loadBillingData();
  }, [loadBillingData]);

  if (authLoading) {
    return (
      <div className="db-page" style={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", background: "#f8fafc" }}>
        <p className="db-muted">Loading billing information...</p>
      </div>
    );
  }

  if (!isAuthenticated) return null;

  return (
    <div className="db-page" style={{ minHeight: "100vh", background: "#f8fafc" }}>
      <header className="db-header" style={{ position: "sticky", top: 0, zIndex: 5 }}>
        <div className="db-container db-flex-between">
          <div className="db-brand">
            <div className="db-logo">dYs•</div>
            <span>DowntownBike</span>
          </div>
          <div className="db-actions" style={{ gap: "12px" }}>
            <button className="db-btn" type="button" onClick={() => navigate(-1)}>Back</button>
            <button className="db-btn" type="button" onClick={loadBillingData} disabled={loading}>
              {loading ? "Refreshing..." : "Refresh"}
            </button>
          </div>
        </div>
      </header>

      <div style={{ display: "flex", gap: 24, alignItems: "flex-start", paddingTop: 24 }}>
        <SideBar username={user?.username} email={user?.email} />

        <main className="db-container" style={{ paddingTop: 48, paddingBottom: 64, flex: 1 }}>
          <section style={{ marginBottom: 32 }}>
            <h1 style={{ fontSize: 28, fontWeight: 700, marginBottom: 8 }}>Billing & Plans</h1>
            <p className="db-muted" style={{ margin: 0 }}>
              Review your current subscription plan and billing information.
            </p>
          </section>

          {loading && (
            <div style={{ display: "flex", justifyContent: "center", padding: "48px 16px" }}>
              <p className="db-muted">Loading your billing information...</p>
            </div>
          )}

          {!loading && error && (
            <div className="db-card" style={{ background: "#fef2f2", borderLeft: "4px solid #dc2626", display: "flex", flexDirection: "column", gap: 12 }}>
              <h3 style={{ color: "#991b1b", marginBottom: 0 }}>Error loading billing data</h3>
              <p style={{ color: "#7f1d1d", margin: 0 }}>{error}</p>
              <button className="db-btn" type="button" onClick={loadBillingData} style={{ alignSelf: "flex-start" }}>
                Try again
              </button>
            </div>
          )}

          {!loading && !error && (
            <div style={{ display: "grid", gap: 24, gridTemplateColumns: "repeat(auto-fit, minmax(320px, 1fr))" }}>
              {/* Current Plan Card */}
              <CurrentPlanDisplay currentPlan={currentPlan} />

              {/* Bills List */}
              {bills.length > 0 ? (
                bills.slice().reverse().map((bill) => (
                  <div key={bill.id} className="db-card" style={{ display: "flex", flexDirection: "column", gap: 12 }}>
                    <div>
                      <h2 style={{ marginBottom: 4, fontSize: 18, fontWeight: 600 }}>Bill</h2>
                      <p className="db-muted" style={{ margin: 0, fontSize: 13 }}>
                        Bill ID: {bill.id}
                      </p>
                    </div>

                    <dl style={{ display: "grid", gridTemplateColumns: "auto 1fr", rowGap: 12, columnGap: 16, margin: 0 }}>
                      <dt style={{ fontWeight: 600, color: "#0f172a" }}>Total Cost</dt>
                      <dd style={{ margin: 0, color: "#475569" }}>
                        ${(bill.cost || 0).toFixed(2)}
                      </dd>

                      {bill.flexDollarsUsed > 0 && (
                        <>
                          <dt style={{ fontWeight: 600, color: "#16a34a" }}>Flex Dollars Used</dt>
                          <dd style={{ margin: 0, color: "#16a34a" }}>
                            -${(bill.flexDollarsUsed || 0).toFixed(2)}
                          </dd>
                        </>
                      )}

                      <dt style={{ fontWeight: 600, color: "#0f172a" }}>Amount Charged</dt>
                      <dd style={{ margin: 0, color: "#0f172a", fontWeight: 700 }}>
                        ${(bill.amountCharged !== undefined ? bill.amountCharged : bill.cost || 0).toFixed(2)}
                      </dd>

                      {bill.billingDate && (
                        <>
                          <dt style={{ fontWeight: 600, color: "#0f172a" }}>Billing Date</dt>
                          <dd style={{ margin: 0, color: "#475569" }}>
                            {formatDateTime(bill.billingDate)}
                          </dd>
                        </>
                      )}

                      {bill.paymentInfo && (
                        <>
                          <dt style={{ fontWeight: 600, color: "#0f172a" }}>Payment Method</dt>
                          <dd style={{ margin: 0, color: "#475569" }}>
                            {bill.paymentInfo.cardType && `${bill.paymentInfo.cardType} `}
                            {bill.paymentInfo.last4 && `****${bill.paymentInfo.last4}`}
                            {bill.paymentInfo.expiryDate && ` (Exp: ${bill.paymentInfo.expiryDate})`}
                          </dd>
                        </>
                      )}

                      {bill._billClass && (
                        <>
                          <dt style={{ fontWeight: 600, color: "#0f172a" }}>Bill Type</dt>
                          <dd style={{ margin: 0, color: "#475569" }}>
                            {bill._billClass}
                          </dd>
                        </>
                      )}
                    </dl>
                  </div>
                ))
              ) : (
                <div className="db-card" style={{ gridColumn: "1 / -1" }}>
                  <p className="db-muted">No bills found.</p>
                </div>
              )}
            </div>
          )}

          {/* Help section */}
          <section style={{ marginTop: 48 }}>
            <h2 style={{ fontSize: 18, fontWeight: 600, marginBottom: 12 }}>Need help?</h2>
            <div style={{ display: "grid", gap: 12, gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))" }}>
              <div className="db-card" style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                <h3 style={{ marginBottom: 0, fontSize: 14, fontWeight: 600 }}>Update payment method</h3>
                <p className="db-muted" style={{ margin: 0, fontSize: 13 }}>
                  Manage your payment methods and billing settings.
                </p>
                <button
                  className="db-btn"
                  type="button"
                  onClick={() => navigate("/payment")}
                  style={{ alignSelf: "flex-start", marginTop: 8 }}
                >
                  Go to Payments
                </button>
              </div>

              <div className="db-card" style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                <h3 style={{ marginBottom: 0, fontSize: 14, fontWeight: 600 }}>View your trips</h3>
                <p className="db-muted" style={{ margin: 0, fontSize: 13 }}>
                  Check your ride history and usage details.
                </p>
                <button
                  className="db-btn"
                  type="button"
                  onClick={() => navigate("/trips")}
                  style={{ alignSelf: "flex-start", marginTop: 8 }}
                >
                  Go to Trips
                </button>
              </div>

              <div className="db-card" style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                <h3 style={{ marginBottom: 0, fontSize: 14, fontWeight: 600 }}>Contact support</h3>
                <p className="db-muted" style={{ margin: 0, fontSize: 13 }}>
                  Have questions about your bill? Reach out to our support team.
                </p>
                <button
                  className="db-btn"
                  type="button"
                  disabled
                  style={{ alignSelf: "flex-start", marginTop: 8 }}
                >
                  Contact Support
                </button>
              </div>
            </div>
          </section>
        </main>
      </div>
    </div>
  );
}
