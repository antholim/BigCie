import React, { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import FetchingService from "../../services/FetchingService";
import { useAuth } from "../../contexts/AuthContext";
import "../../components/home.css";
import { formatDateTime } from "../../utils/utils";
import SideBar from "../../components/SideBar";

export default function TripsPage() {
  const navigate = useNavigate();
  const { user, isAuthenticated, loading: authLoading } = useAuth();
  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      navigate("/login", { replace: true });
    }
  }, [authLoading, isAuthenticated, navigate]);

  const loadTrips = useCallback(async () => {
    if (!isAuthenticated || !user) return;
    const identifier = user.id ?? user.userId;
    if (!identifier) return;
    setLoading(true);
    setError("");
    try {
      const response = await FetchingService.get("/api/v1/trips/me");
      const data = Array.isArray(response?.data) ? response.data : [];
      const filtered = data.filter((t) => {
        const tripUserId = t.userId ?? t.user_id ?? t.user?.id;
        if (!tripUserId) return false;
        return String(tripUserId).toLowerCase() === String(identifier).toLowerCase();
      });
      setTrips(filtered);
    } catch (err) {
      setError(err.response?.data?.message || err.message || "Unable to load trips right now.");
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, user]);

  useEffect(() => {
    loadTrips();
  }, [loadTrips]);

  if (authLoading) {
    return (
      <div className="db-page" style={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", background: "#f8fafc" }}>
        <p className="db-muted">Loading trips...</p>
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
            <button className="db-btn" type="button" onClick={loadTrips} disabled={loading}>
              {loading ? "Refreshing..." : "Refresh"}
            </button>
          </div>
        </div>
      </header>
      <div style={{ display: "flex", gap: 24, alignItems: "flex-start", paddingTop: 24 }}>
              <SideBar username={user?.username} email={user?.email} />
      <main className="db-container" style={{ paddingTop: 48, paddingBottom: 64 }}>
        <section style={{ marginBottom: 24 }}>
          <h1 style={{ fontSize: 28, fontWeight: 700, marginBottom: 8 }}>Your trips</h1>
          <p className="db-muted" style={{ margin: 0 }}>
            A history of your rides — tap a trip for details.
          </p>
        </section>
      
        <div style={{ display: "grid", gap: 16 }}>
          {loading && <p className="db-muted">Loading trips...</p>}
          {!loading && error && <p style={{ color: "#dc2626" }}>{error}</p>}
          {!loading && !error && trips.length === 0 && (
            <p className="db-muted">No trips found.</p>
          )}

          {!loading && !error && trips.map((trip) => {
            const id = trip.id ?? trip._id;
            return (
              <div key={id} className="db-card" style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <div>
                    <strong style={{ fontSize: 16 }}>{trip.pricingPlan || trip.bikeType || "Trip"}</strong>
                    <div className="db-muted" style={{ fontSize: 13 }}>{trip.bikeType} • {trip.status}</div>
                  </div>
                  <div style={{ textAlign: "right" }}>
                    <div style={{ fontWeight: 700 }}>${(trip.cost ?? 0).toFixed(2)}</div>
                    <div className="db-muted" style={{ fontSize: 13 }}>{(trip.distanceInKm ?? 0).toFixed(2)} km</div>
                  </div>
                </div>

                <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 8 }}>
                  <div>
                    <div style={{ fontWeight: 600, color: "#1e293b", fontSize: 13 }}>From</div>
                    <div className="db-muted" style={{ fontSize: 13 }}>{trip.bikeStationStart || trip.startStation || "—"}</div>
                    <div className="db-muted" style={{ fontSize: 12 }}>{formatDateTime(trip.startDate)}</div>
                  </div>
                  <div>
                    <div style={{ fontWeight: 600, color: "#1e293b", fontSize: 13 }}>To</div>
                    <div className="db-muted" style={{ fontSize: 13 }}>{trip.bikeStationEnd || trip.endStation || "—"}</div>
                    <div className="db-muted" style={{ fontSize: 12 }}>{formatDateTime(trip.endDate)}</div>
                  </div>
                </div>

                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginTop: 8 }}>
                  <div className="db-muted" style={{ fontSize: 13 }}>Bike: {trip.bikeId ? trip.bikeId.slice(0, 8) : "—"}</div>
                  <div style={{ fontSize: 12, padding: "4px 8px", borderRadius: 8, background: trip.status === "COMPLETED" ? "#ecfdf5" : "#fff7ed", color: trip.status === "COMPLETED" ? "#065f46" : "#92400e" }}>
                    {trip.status}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </main>
      </div>
    </div>
  );
}