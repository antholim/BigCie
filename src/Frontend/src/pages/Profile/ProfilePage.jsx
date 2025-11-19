// ...existing code...
import React, { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import FetchingService from "../../services/FetchingService";
import { useAuth } from "../../contexts/AuthContext";
import MapPreview from "../../components/MapPreview";
import "../../components/home.css";
import BikeBox from "./components/BikeBox";
import { truncateId, formatDateTime } from "../../utils/utils";
import SideBar from "../../components/SideBar";

export default function ProfilePage() {
  const navigate = useNavigate();
  const { user, isAuthenticated, loading: authLoading, logout } = useAuth();
  const isOperator = user?.userType === 'OPERATOR' || user?.type === 'OPERATOR';
  const [reservations, setReservations] = useState([]);
  const [reservationsLoading, setReservationsLoading] = useState(false);
  const [bikeRentals, setBikeRentals] = useState([]);
  const [_bikesLoading, setBikesLoading] = useState(false);
  const [_bikesError, setBikesError] = useState("");
  const [reservationsError, setReservationsError] = useState("");
  const [isMapExpanded, setIsMapExpanded] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [unreservingIds, setUnreservingIds] = useState(new Set());
  const [userProfileInformation, setUserProfileInformation] = useState(null);
  const [showLoyaltyModal, setShowLoyaltyModal] = useState(false);


  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      navigate("/login", { replace: true });
    }
  }, [authLoading, isAuthenticated, navigate]);
  const loadUserProfile = useCallback(async () => {
    if (!isAuthenticated || !user) return;
    const identifier = user.id ?? user.userId;
    if (!identifier) return;

    try {
      const response = await FetchingService.get("/api/v1/user/my-profile");
      setUserProfileInformation(response.data);
    } catch (err) {
      console.error("Failed to load user profile information:", err);
    }
  })
  const loadReservations = useCallback(async () => {
    if (!isAuthenticated || !user) return;
    const identifier = user.id ?? user.userId;
    if (!identifier) return;
    setReservationsLoading(true);
    setReservationsError("");
    try {
      const response = await FetchingService.get("/api/v1/stations/reservations/me");
      const data = Array.isArray(response?.data) ? response.data : [];
      console.log(data)
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


  const loadBikes = useCallback(async () => {
    if (!isAuthenticated || !user) return;
    const identifier = user.id ?? user.userId;
    if (!identifier) return;
    setBikesLoading(true);
    setBikesError("");
    try {
      const [bikeIdsResp, tripsResp] = await Promise.all([
        FetchingService.get("/api/v1/bikes/me"),
        FetchingService.get("/api/v1/trips/me"),
      ]);

      const bikeIds = Array.isArray(bikeIdsResp?.data) ? bikeIdsResp.data : [];
      const trips = Array.isArray(tripsResp?.data) ? tripsResp.data : [];
      const normalizedUserId = String(identifier).toLowerCase();

      const ongoingTrips = trips.filter((trip) => {
        const tripUserId = trip.userId ?? trip.user_id ?? trip.user?.id;
        const status = (trip.status ?? trip.tripStatus ?? "").toString().toUpperCase();
        return tripUserId && String(tripUserId).toLowerCase() === normalizedUserId && status === "ONGOING";
      });

      const tripByBikeId = new Map(
        ongoingTrips
          .map((trip) => {
            const bikeId = trip.bikeId ?? trip.bike_id;
            return bikeId ? [String(bikeId).toLowerCase(), trip] : null;
          })
          .filter(Boolean)
      );

      const seenBikeIds = new Set();
      const rentals = bikeIds.map((rawId) => {
        const lookupKey = String(rawId).toLowerCase();
        const trip = tripByBikeId.get(lookupKey);
        seenBikeIds.add(lookupKey);
        return {
          bikeId: rawId,
          bikeType: trip?.bikeType ?? trip?.type ?? null,
          stationName: trip?.bikeStationStart ?? trip?.startStation ?? "Unknown station",
          rentedAt: trip?.startDate ?? trip?.start_date ?? null,
        };
      });

      ongoingTrips.forEach((trip) => {
        const bikeId = trip.bikeId ?? trip.bike_id;
        if (!bikeId) return;
        const lookupKey = String(bikeId).toLowerCase();
        if (seenBikeIds.has(lookupKey)) return;
        rentals.push({
          bikeId,
          bikeType: trip.bikeType ?? trip.type ?? null,
          stationName: trip.bikeStationStart ?? trip.startStation ?? "Unknown station",
          rentedAt: trip.startDate ?? trip.start_date ?? null,
        });
      });

      setBikeRentals(rentals);
    } catch (err) {
      setBikesError(
        err?.response?.data?.message || err?.message || "Unable to load bikes right now."
      );
    } finally {
      setBikesLoading(false);
    }
  }, [isAuthenticated, user]);

  const handleLogout = async () => {
    setIsLoggingOut(true);
    try {
      await logout();
      navigate("/", { replace: true });
    } catch {
      // Ignore logout network errors; user is forced to sign in again regardless.
    } finally {
      setIsLoggingOut(false);
    }
  };

  const handleUnreserve = async (reservationId) => {
    setUnreservingIds(prev => new Set(prev).add(reservationId));
    try {
      await FetchingService.delete(`/api/v1/stations/reservations/${reservationId}`);
      // Refresh reservations after successful cancellation
      await loadReservations();
    } catch (err) {
      console.error("Failed to unreserve:", err);
      // Optionally show an error message to the user
      setReservationsError(
        err.response?.data?.message || err.message || "Failed to cancel reservation. Please try again."
      );
    } finally {
      setUnreservingIds(prev => {
        const newSet = new Set(prev);
        newSet.delete(reservationId);
        return newSet;
      });
    }
  };

  const renderTierBadge = (tier) => {
    const t = String(tier ?? '').toUpperCase();
    const map = {
      GOLD: { label: 'Gold', color: '#f59e0b' },
      SILVER: { label: 'Silver', color: '#9ca3af' },
      BRONZE: { label: 'Bronze', color: '#a16207' },
      DEFAULT: { label: 'Default', color: '#cbd5e1' },
    };
    const info = map[t] || map.DEFAULT;
    return (
      <span style={{
        display: 'inline-block',
        padding: '6px 10px',
        borderRadius: 999,
        backgroundColor: info.color,
        color: '#031024',
        fontWeight: 700,
        fontSize: 13,
        marginLeft: 8,
      }}>{info.label}</span>
    );
  };

  // Close modal on ESC
  useEffect(() => {
    const onKey = (e) => {
      if (e.key === 'Escape') setShowLoyaltyModal(false);
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, []);

useEffect(() => {
  if (!isAuthenticated || !user) return;

  loadReservations();
  loadBikes();
  loadUserProfile();
}, [isAuthenticated, user]);

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
            {isOperator && (
              <button
                className="db-btn"
                type="button"
                onClick={() => navigate('/operator-dashboard')}
              >
                Operator Dashboard
              </button>
            )}
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

      {/* Page layout with left sidebar + main content */}
      <div style={{ display: "flex", gap: 24, alignItems: "flex-start", paddingTop: 24 }}>
        {/* Sidebar */}
        <SideBar username={user?.username} email={user?.email} />
        {/* <nav
          aria-label="Profile navigation"
          style={{
            width: 220,
            minHeight: "calc(100vh - 88px)",
            padding: "16px",
            borderRadius: 12,
            background: "#ffffff",
            boxShadow: "0 1px 2px rgba(16,24,40,0.04)",
            border: "1px solid #e6eef8",
            position: "sticky",
            top: 88,
            alignSelf: "flex-start",
          }}
        >
          <div style={{ marginBottom: 12 }}>
            <strong style={{ display: "block", fontSize: 16 }}>{user?.username || "rider"}</strong>
            <span className="db-muted" style={{ fontSize: 13 }}>{user?.email || ""}</span>
          </div>
          <ul style={{ listStyle: "none", padding: 0, margin: 0, display: "grid", gap: 8 }}>
            <li>
              <button
                type="button"
                className="db-btn"
                style={{ width: "100%", justifyContent: "flex-start" }}
                onClick={() => navigate("/profile")}
              >
                Profile
              </button>
            </li>
            <li>
              <button
                type="button"
                className="db-btn"
                style={{ width: "100%", justifyContent: "flex-start" }}
                onClick={() => navigate("/trips")}
              >
                Trips
              </button>
            </li>
          </ul>
        </nav> */}

        {/* Main content area */}
        <main className="db-container" style={{ paddingTop: "48px", paddingBottom: "64px", flex: 1 }}>
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
            <div className="db-card" style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
              <div className="db-flex-between" style={{ alignItems: "center" }}>
                <div>
                  <h2 style={{ marginBottom: "4px" }}>Loyalty Program</h2>
                  <p className="db-muted" style={{ margin: 0 }}>
                    Track your current tier and benefits.
                  </p>
                </div>
              </div>

              <div
                style={{
                  background: "#f9fafb",
                  border: "1px solid #e2e8f0",
                  padding: "16px",
                  borderRadius: "12px",
                }}
              >
                <h3 style={{ margin: "0 0 8px", fontSize: "20px", display: 'flex', alignItems: 'center' }}>
                  {userProfileInformation?.loyaltyTier ?? "DEFAULT"}
                  {renderTierBadge(userProfileInformation?.loyaltyTier)}
                </h3>

                <p className="db-muted" style={{ marginTop: 0 }}>
                  You're currently enjoying the benefits associated with the <strong>{userProfileInformation?.loyaltyTier ?? "DEFAULT"}</strong> tier.
                </p>

                {(userProfileInformation?.loyaltyTier === "SILVER" || userProfileInformation?.loyaltyTier === "GOLD") && (
                  <div style={{ marginTop: "12px", padding: "12px", background: "#ecfdf5", borderRadius: "8px", border: "1px solid #a7f3d0" }}>
                    <p style={{ margin: 0, fontWeight: 600 }}>
                      🎉 Congrats! You're in a premium tier.
                    </p>
                  </div>
                )}

                <div style={{ marginTop: 12 }}>
                  <button
                    className="db-btn"
                    type="button"
                    onClick={() => setShowLoyaltyModal(true)}
                  >
                    View benefits & requirements
                  </button>
                </div>
              </div>
            </div>
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
                    {reservations.map((reservation) => {
                      const reservationId = reservation.id ?? reservation._id;
                      const isUnreserving = unreservingIds.has(reservationId);

                      return (
                        <div
                          key={reservationId}
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
                            <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                              <span style={{ fontSize: "12px", color: "#475569" }}>
                                {formatDateTime(reservation.startTime)}
                              </span>
                              <button
                                className="db-btn"
                                type="button"
                                onClick={() => handleUnreserve(reservationId)}
                                disabled={isUnreserving}
                                style={{
                                  fontSize: "12px",
                                  padding: "4px 8px",
                                  backgroundColor: "#dc2626",
                                  color: "#fff",
                                  border: "none",
                                  borderRadius: "6px",
                                  cursor: isUnreserving ? "not-allowed" : "pointer",
                                  opacity: isUnreserving ? 0.6 : 1,
                                }}
                              >
                                {isUnreserving ? "Unreserving..." : "Unreserve"}
                              </button>
                            </div>
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
                      );
                    })}
                  </div>
                )}
              </div>

            </div>
            <div style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
              <BikeBox bikeRentals={bikeRentals} />
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

      {showLoyaltyModal && (
        <div style={{ position: 'fixed', left: 0, top: 0, right: 0, bottom: 0, zIndex: 9999, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <div onClick={() => setShowLoyaltyModal(false)} style={{ position: 'absolute', left: 0, top: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.35)' }} />
          <div role="dialog" aria-modal="true" style={{ position: 'relative', background: '#fff', padding: 20, borderRadius: 12, boxShadow: '0 12px 36px rgba(0,0,0,0.18)', width: 720, maxWidth: 'calc(100% - 48px)', maxHeight: '80vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <div style={{ fontSize: 18, fontWeight: 800 }}>Loyalty Program — Benefits & Requirements</div>
                <div style={{ fontSize: 13, color: '#666', marginTop: 4 }}>Tier: <strong>{userProfileInformation?.loyaltyTier ?? 'DEFAULT'}</strong>{renderTierBadge(userProfileInformation?.loyaltyTier)}</div>
              </div>
              <button onClick={() => setShowLoyaltyModal(false)} style={{ background: 'transparent', border: 'none', cursor: 'pointer', fontSize: 18 }}>✕</button>
            </div>

            <div style={{ marginTop: 16, lineHeight: 1.45 }}>
              <h3 style={{ marginBottom: 6 }}>Bronze tier</h3>
              <ul>
                <li><strong>BR-001:</strong> Rider has to have no missed reservations within the last year.</li>
                <li><strong>BR-002:</strong> Rider returned all bikes that they ever took successfully.</li>
                <li><strong>BR-003:</strong> Rider has surpassed 10 trips in the last year.</li>
                <li><strong>BR-004:</strong> Rider gets 5% discount on trips.</li>
              </ul>

              <h3 style={{ marginBottom: 6, marginTop: 12 }}>Silver tier</h3>
              <ul>
                <li><strong>SL-001:</strong> Rider covers Bronze tier eligibility.</li>
                <li><strong>SL-002:</strong> Rider has to have at least 5 reservations of bikes that were successfully claimed within the last year.</li>
                <li><strong>SL-003:</strong> Rider has surpassed 5 trips per month for the last three months.</li>
                <li><strong>SL-004:</strong> Rider gets a 10% discount on trips and an extra 2-minute reservation hold.</li>
              </ul>

              <h3 style={{ marginBottom: 6, marginTop: 12 }}>Gold tier</h3>
              <ul>
                <li><strong>GL-001:</strong> Rider covers Silver tier eligibility.</li>
                <li><strong>GL-002:</strong> Rider surpasses 5 trips every week for the last 3 months.</li>
                <li><strong>GL-003:</strong> Rider gets a 15% discount on trips and an extra 5-minute reservation hold.</li>
              </ul>
            </div>

            <div style={{ marginTop: 16, display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
              <button className="db-btn" onClick={() => setShowLoyaltyModal(false)}>Close</button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}
