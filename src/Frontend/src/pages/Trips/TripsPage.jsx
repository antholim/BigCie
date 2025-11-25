import React, { useCallback, useEffect, useMemo, useState } from "react";
import { dateValidation } from "../../utils/utils";
import { useNavigate } from "react-router-dom";
import FetchingService from "../../services/FetchingService";
import { useAuth } from "../../contexts/AuthContext";
import "../../components/home.css";
import SideBar from "../../components/SideBar";
import Trip from "./components/Trip";
import TripDetails from "./components/TripDetails";

const TRIPS_PER_PAGE = 5;

export default function TripsPage() {
  const navigate = useNavigate();
  const { user, isAuthenticated, loading: authLoading } = useAuth();
  const [trips, setTrips] = useState([]);
  const [planFilter, setPlanFilter] = useState("ALL");
  const [searchQuery, setSearchQuery] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [bikeTypeFilter, setBikeTypeFilter] = useState("ALL");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [selectedTrip, setSelectedTrip] = useState(null);
  const [currentPage, setCurrentPage] = useState(1);

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
      // sort by most recent trip date (startDate or fallback to createdAt) descending
      const getTripTimestamp = (trip) => {
        const d = trip.startDate ?? trip.start_date ?? trip.start ?? trip.createdAt ?? trip.created_at;
        const time = d ? new Date(d).getTime() : 0;
        return isNaN(time) ? 0 : time;
      };
      filtered.sort((a, b) => getTripTimestamp(b) - getTripTimestamp(a));
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

  const filteredTrips = useMemo(() => {
    const q = String(searchQuery || "").trim().toLowerCase();
    const start = startDate ? new Date(startDate + "T00:00:00") : null;
    const end = endDate ? new Date(endDate + "T23:59:59.999") : null;

    return trips.filter((trip) => {
      if (planFilter !== "ALL") {
        const plan = trip.pricingPlan ?? trip.pricing_plan ?? trip.pricing;
        if (String(plan) !== planFilter) return false;
      }

      if (bikeTypeFilter !== "ALL") {
        const ttype = trip.bikeType ?? trip.type ?? trip.bike_type ?? "";
        if (String(ttype) !== bikeTypeFilter) return false;
      }

      if (start || end) {
        const sd = trip.startDate ?? trip.start_date ?? trip.start ?? trip.createdAt ?? trip.created_at;
        if (!sd) return false;
        const tripDate = new Date(sd);
        if (isNaN(tripDate)) return false;
        if (start && tripDate < start) return false;
        if (end && tripDate > end) return false;
      }

      if (q.length > 0) {
        const idStr = String(trip.id ?? trip._id ?? "");
        return idStr.includes(q);
      }

      return true;
    });
  }, [trips, planFilter, searchQuery, startDate, endDate, bikeTypeFilter]);

  useEffect(() => {
    setCurrentPage(1);
  }, [planFilter, searchQuery, startDate, endDate, bikeTypeFilter]);

  useEffect(() => {
    const maxPage = Math.max(1, Math.ceil(filteredTrips.length / TRIPS_PER_PAGE));
    setCurrentPage((prev) => Math.min(prev, maxPage));
  }, [filteredTrips]);

  const totalPages = Math.max(1, Math.ceil(filteredTrips.length / TRIPS_PER_PAGE));
  const paginationStart = Math.max(0, (currentPage - 1) * TRIPS_PER_PAGE);
  const paginatedTrips = filteredTrips.slice(paginationStart, paginationStart + TRIPS_PER_PAGE);
  const pageSummaryStart = filteredTrips.length === 0 ? 0 : paginationStart + 1;
  const pageSummaryEnd = Math.min(paginationStart + TRIPS_PER_PAGE, filteredTrips.length);

  if (authLoading) {
    return (
      <div className="db-page" style={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", background: "#f8fafc" }}>
        <p className="db-muted">Loading trips...</p>
      </div>
    );
  }

  if (!isAuthenticated) return null;

  // derive available bike types from loaded trips
  const bikeTypes = Array.from(new Set(trips.map((t) => (t.bikeType ?? t.type ?? t.bike_type ?? "")).filter(Boolean)));
  const isDateRangeValid = dateValidation(startDate, endDate);

  return (
    <div className="db-page" style={{ minHeight: "100vh", background: "#f8fafc" }}>
      <header className="db-header" style={{ position: "sticky", top: 0, zIndex: 5 }}>
        <div className="db-container db-flex-between">
          <div className="db-brand">
            <div className="db-logo">dYs�</div>
            <span>BigCie</span>
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
        {/* Search + Pricing plan filter */}
        <div style={{ marginBottom: 16, display: "flex", alignItems: "center", gap: 12, flexWrap: 'wrap' }}>
          <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
            <input
              type="text"
              aria-label="Search by trip ID"
              placeholder="Search by trip ID"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={{ padding: '8px 10px', borderRadius: 8, border: '1px solid #e2e8f0', minWidth: 220 }}
            />
          

            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
              <label style={{ fontSize: 13, color: '#0f172a' }}>From</label>
              <input
                type="date"
                aria-label="Start date"
                value={startDate}
                onChange={(e) => {
                  setStartDate(e.target.value);
                  console.log(e.target.value)
                }}
                style={{ padding: '6px 8px', borderRadius: 8, border: '1px solid #e2e8f0' }}
              />
              <label style={{ fontSize: 13, color: '#0f172a' }}>To</label>
              <input
                type="date"
                aria-label="End date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                style={{ padding: '6px 8px', borderRadius: 8, border: '1px solid #e2e8f0' }}
              />
              <button className="db-btn" type="button" aria-label="Clear filters" onClick={() => { setSearchQuery(""); setStartDate(""); setEndDate(""); setBikeTypeFilter("ALL"); setPlanFilter("ALL"); }}>Clear</button>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <div style={{ fontWeight: 600, color: "#0f172a" }}>Show</div>
            <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
            <input
              type="radio"
              name="plan"
              value="ALL"
              checked={planFilter === "ALL"}
              onChange={() => setPlanFilter("ALL")}
            />
            <span className="db-muted">All</span>
          </label>
          <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
            <input
              type="radio"
              name="plan"
              value="SINGLE_RIDE"
              checked={planFilter === "SINGLE_RIDE"}
              onChange={() => setPlanFilter("SINGLE_RIDE")}
            />
            <span className="db-muted">Single ride</span>
          </label>
          <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
            <input
              type="radio"
              name="plan"
              value="DAY_PASS"
              checked={planFilter === "DAY_PASS"}
              onChange={() => setPlanFilter("DAY_PASS")}
            />
            <span className="db-muted">Day pass</span>
          </label>
          <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
            <input
              type="radio"
              name="plan"
              value="MONTHLY_PASS"
              checked={planFilter === "MONTHLY_PASS"}
              onChange={() => setPlanFilter("MONTHLY_PASS")}
            />
            <span className="db-muted">Monthly pass</span>
          </label>
            {/* Bike type selector */}
            <label style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <div style={{ fontSize: 13, color: '#0f172a' }}>Type</div>
              <select
                value={bikeTypeFilter}
                onChange={(e) => setBikeTypeFilter(e.target.value)}
                style={{ padding: '6px 8px', borderRadius: 8, border: '1px solid #e2e8f0' }}
              >
                <option value="ALL">All</option>
                {bikeTypes.map((bt) => (
                  <option key={bt} value={bt}>{bt}</option>
                ))}
              </select>
            </label>
          </div>
        </div>
        {isDateRangeValid && (
          <div style={{ display: "grid", gap: 16 }}>
            {loading && <p className="db-muted">Loading trips...</p>}
            {!loading && error && <p style={{ color: "#dc2626" }}>{error}</p>}

            {!loading && !error && (
              filteredTrips.length === 0 ? (
                <p className="db-muted">No trips found.</p>
              ) : (
                <>
                  {paginatedTrips.map((trip) => (
                    <Trip
                      key={trip.id ?? trip._id}
                      trip={trip}
                      onSelect={() => setSelectedTrip(trip)}
                    />
                  ))}
                  <div
                    style={{
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "space-between",
                      marginTop: 8,
                      flexWrap: "wrap",
                      gap: 12,
                    }}
                  >
                    <span className="db-muted">
                      Showing {pageSummaryStart}-{pageSummaryEnd} of {filteredTrips.length} trips
                    </span>
                    {filteredTrips.length > TRIPS_PER_PAGE && (
                      <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
                        <button
                          type="button"
                          className="db-btn"
                          onClick={() => setCurrentPage((prev) => Math.max(1, prev - 1))}
                          disabled={currentPage === 1}
                          style={{ padding: "6px 12px" }}
                        >
                          Previous
                        </button>
                        <span className="db-muted">Page {currentPage} of {totalPages}</span>
                        <button
                          type="button"
                          className="db-btn"
                          onClick={() => setCurrentPage((prev) => Math.min(totalPages, prev + 1))}
                          disabled={currentPage === totalPages}
                          style={{ padding: "6px 12px" }}
                        >
                          Next
                        </button>
                      </div>
                    )}
                  </div>
                </>
              )
            )}
          </div>
        )}
        {isDateRangeValid || <div style={{ color: "#dc2626", marginTop: 12 }}>Please ensure the date range is valid.</div>}
        {selectedTrip && (
          <TripDetails
            trip={selectedTrip}
            onClose={() => setSelectedTrip(null)}
          />
        )}
      </main>
      </div>
    </div>
  );
}
