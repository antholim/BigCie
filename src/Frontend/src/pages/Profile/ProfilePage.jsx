import React, { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import FetchingService from "../../services/FetchingService";
import { useAuth } from "../../contexts/AuthContext";
import MapPreview from "../../components/MapPreview";
import "../../components/home.css";

export default function ProfilePage() {
  const navigate = useNavigate();
  const { user, isAuthenticated, loading: authLoading, logout } = useAuth();
  const [reservations, setReservations] = useState([]);
  const [reservationsLoading, setReservationsLoading] = useState(false);
  const [reservationsError, setReservationsError] = useState("");
  const [isMapExpanded, setIsMapExpanded] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      navigate("/login", { replace: true });
    }
  }, [authLoading, isAuthenticated, navigate]);

  const loadReservations = useCallback(async () => {
    if (!isAuthenticated || !user) return;
    const identifier = user.id ?? user.userId;
    if (!identifier) return;
    setReservationsLoading(true);
    setReservationsError("");
    try {
      const response = await FetchingService.get("/api/v1/stations/reservations");
      const data = Array.isArray(response?.data) ? response.data : [];
      const filtered = data.filter((entry) => {
        const reservationUserId = entry.userId ?? entry.user?.id ?? entry.user_id;
        if (!reservationUserId) return false;
        return String(reservationUserId).toLowerCase() === String(identifier).toLowerCase();
      });
      setReservations(filtered);
    } catch (err) {
      setReservationsError(
        err.response?.data?.message || err.message || "Unable to load reservations right now."
      );
    } finally {
      setReservationsLoading(false);
    }
  }, [isAuthenticated, user]);

  useEffect(() => {
    loadReservations();
  }, [loadReservations]);

  const handleLogout = async () => {
    setIsLoggingOut(true);
    try {
      await logout();
      navigate("/", { replace: true });
    } catch (err) {
      // Ignore logout network errors; user is forced to sign in again regardless.
    } finally {
      setIsLoggingOut(false);
    }
  };

  const formatDateTime = (value) => {
    if (!value) return "N/A";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString();
  };

  const truncateId = (value) => {
    if (!value) return "N/A";
    const str = String(value);
    return `${str.slice(0, 8)}...`;
  };

  const hasReservations = reservations.length > 0;

  if (authLoading) {
    return (
      <div className="db-page" style={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", background: "#f8fafc" }}>
        <p className="db-muted" style={{ fontSize: "16px" }}>Loading your profile...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="db-page" style={{ minHeight: "100vh", background: "#f8fafc" }}>
      <header className="db-header" style={{ position: "sticky", top: 0, zIndex: 5 }}>
        <div className="db-container db-flex-between">
          <div className="db-brand">
            <div className="db-logo">dYs�</div>
            <span>DowntownBike</span>
          </div>
          <div className="db-actions" style={{ gap: "12px" }}>
            <button className="db-btn" type="button" onClick={() => navigate("/")}>
              Back to landing
            </button>
            <button
              className="db-btn primary"
              type="button"
              onClick={handleLogout}
              disabled={isLoggingOut}
            >
              {isLoggingOut ? "Logging out..." : "Log out"}
            </button>
          </div>
        </div>
      </header>

      <main className="db-container" style={{ paddingTop: "48px", paddingBottom: "64px" }}>
        <section style={{ marginBottom: "32px" }}>
          <h1 style={{ fontSize: "28px", fontWeight: 700, marginBottom: "8px" }}>
            Welcome back, {user?.username || "rider"}!
          </h1>
          <p className="db-muted" style={{ maxWidth: "640px" }}>
            Manage your reservations and explore the service map. Your login now brings you straight
            to this personalized view with quick access to everything you need.
          </p>
        </section>

        <div
          style={{
            display: "grid",
            gap: "24px",
            gridTemplateColumns: "repeat(auto-fit, minmax(320px, 1fr))",
            alignItems: "stretch",
          }}
        >
          <div className="db-card" style={{ display: "flex", flexDirection: "column" }}>
            <div className="db-flex-between" style={{ marginBottom: "16px", gap: "12px" }}>
              <div>
                <h2 style={{ marginBottom: "4px" }}>Bike reservations</h2>
                <p className="db-muted" style={{ margin: 0 }}>
                  View upcoming bookings tied to your account.
                </p>
              </div>
              <button
                className="db-btn"
                type="button"
                onClick={loadReservations}
                disabled={reservationsLoading}
              >
                {reservationsLoading ? "Refreshing..." : "Refresh"}
              </button>
            </div>

            <div style={{ flex: 1, overflowY: "auto" }}>
              {reservationsLoading && (
                <p className="db-muted" style={{ margin: 0 }}>
                  Loading your reservations...
                </p>
              )}
              {!reservationsLoading && reservationsError && (
                <p style={{ color: "#dc2626", margin: 0 }}>{reservationsError}</p>
              )}
              {!reservationsLoading && !reservationsError && !hasReservations && (
                <p className="db-muted" style={{ margin: 0 }}>
                  You do not have any reservations at the moment.
                </p>
              )}
              {!reservationsLoading && !reservationsError && hasReservations && (
                <div style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
                  {reservations.map((reservation) => (
                    <div
                      key={reservation.id ?? reservation._id}
                      style={{
                        border: "1px solid #e2e8f0",
                        borderRadius: "12px",
                        padding: "16px",
                        background: "#fff",
                      }}
                    >
                      <div
                        style={{
                          display: "flex",
                          justifyContent: "space-between",
                          alignItems: "center",
                          marginBottom: "8px",
                          gap: "12px",
                        }}
                      >
                        <strong>Reservation</strong>
                        <span style={{ fontSize: "12px", color: "#475569" }}>
                          {formatDateTime(reservation.startTime)}
                        </span>
                      </div>
                      <dl
                        style={{
                          display: "grid",
                          gridTemplateColumns: "auto 1fr",
                          rowGap: "6px",
                          columnGap: "12px",
                          margin: 0,
                        }}
                      >
                        <dt style={{ fontWeight: 600, color: "#1e293b" }}>Station</dt>
                        <dd style={{ margin: 0, color: "#475569" }}>
                          {truncateId(reservation.bikeStationId)}
                        </dd>
                        <dt style={{ fontWeight: 600, color: "#1e293b" }}>Bike</dt>
                        <dd style={{ margin: 0, color: "#475569" }}>
                          {truncateId(reservation.bikeId)}
                        </dd>
                        <dt style={{ fontWeight: 600, color: "#1e293b" }}>Expires</dt>
                        <dd style={{ margin: 0, color: "#475569" }}>
                          {formatDateTime(reservation.expiry)}
                        </dd>
                      </dl>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          <div className="db-card" style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
            <div className="db-flex-between" style={{ alignItems: "center" }}>
              <div>
                <h2 style={{ marginBottom: "4px" }}>Service map</h2>
                <p className="db-muted" style={{ margin: 0 }}>
                  Explore available stations and plan your ride.
                </p>
              </div>
              <button
                className="db-btn"
                type="button"
                onClick={() => setIsMapExpanded((prev) => !prev)}
              >
                {isMapExpanded ? "Collapse map" : "Expand map"}
              </button>
            </div>
            <MapPreview
              height={isMapExpanded ? 420 : 260}
              style={{
                transition: "height 0.3s ease",
              }}
            />
            <p className="db-muted" style={{ margin: 0 }}>
              Need more detail? Use the expand toggle or head to the full map view.
            </p>
            <button
              className="db-btn primary"
              type="button"
              onClick={() => navigate("/map")}
            >
              Open full map
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}
