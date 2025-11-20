import React, { useEffect, useState } from 'react';
import { useAuth } from '../../../contexts/AuthContext';
import FetchingService from '../../../services/FetchingService';
import Trip from '../../Trips/components/Trip';
import TripDetails from '../../Trips/components/TripDetails';
import './TripList.css';

function TripList() {
  const { user, isOperatorView, viewMode } = useAuth();
  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [bikeTypeFilter, setBikeTypeFilter] = useState("ALL");
  const [planFilter, setPlanFilter] = useState("ALL");
  const [selectedTrip, setSelectedTrip] = useState(null);

  useEffect(() => {
    let mounted = true;

    async function fetchTrips() {
      setLoading(true);
      setError(null);
      try {
        const token = user?.token || user?.accessToken || user?.jwt;
        const headers = token ? { Authorization: `Bearer ${token}` } : undefined;

        // Use the project's FetchingService (axios instance)
        const res = await FetchingService.get('/api/v1/trips/getAll', { headers });
        const data = res?.data;

        // Support both array response or { trips: [...] }
        const list = Array.isArray(data) ? data : data?.trips ?? [];
        if (mounted) setTrips(list);
      } catch (err) {
        // axios error shape
        const message = err?.response ? `${err.response.status} ${err.response.statusText}` : err.message;
        if (mounted) setError(message || 'Failed to load trips');
      } finally {
        if (mounted) setLoading(false);
      }
    }

    fetchTrips();
    return () => {
      mounted = false;
    };
  }, [user]);

  if (loading) return <div className="trip-list">Loading trips...</div>;
  if (error) return <div className="trip-list error">Error: {error}</div>;

  // Derive available bike types from loaded trips
  const bikeTypes = Array.from(new Set(trips.map((t) => (t.bikeType ?? t.type ?? t.bike_type ?? "")).filter(Boolean)));

  // Apply combined filters
  const q = String(searchQuery || "").trim().toLowerCase();
  const start = startDate ? new Date(startDate + "T00:00:00") : null;
  const end = endDate ? new Date(endDate + "T23:59:59.999") : null;

  const filteredTrips = trips.filter((trip) => {
    // If in RIDER view and user is DUAL_ROLE, only show their own trips
    if (viewMode === 'RIDER' && user?.userType === 'DUAL_ROLE') {
      const tripUserId = String(trip.userId ?? trip.user_id ?? trip.user?.id ?? "");
      const currentUserId = String(user?.id ?? user?.userId ?? "");
      if (tripUserId !== currentUserId) return false;
    }

    // plan filter
    if (planFilter !== "ALL") {
      const plan = trip.pricingPlan ?? trip.pricing_plan ?? trip.pricing;
      if (String(plan) !== planFilter) return false;
    }
    // bike type filter
    if (bikeTypeFilter !== "ALL") {
      const ttype = trip.bikeType ?? trip.type ?? trip.bike_type ?? "";
      if (String(ttype) !== bikeTypeFilter) return false;
    }
    // date range filter (based on trip start date)
    if (start || end) {
      const sd = trip.startDate ?? trip.start_date ?? trip.start ?? trip.createdAt ?? trip.created_at;
      if (!sd) return false;
      const tripDate = new Date(sd);
      if (isNaN(tripDate)) return false;
      if (start && tripDate < start) return false;
      if (end && tripDate > end) return false;
    }
    // search by trip id or user id (partial match)
    if (q.length > 0) {
      const idStr = String(trip.id ?? trip._id ?? "");
      const userIdStr = String(trip.userId ?? trip.user_id ?? trip.user?.id ?? "");
      return idStr.includes(q) || userIdStr.includes(q);
    }
    return true;
  });

  return (
    <div className="trip-list">
      <h3>{viewMode === 'RIDER' && user?.userType === 'DUAL_ROLE' ? 'My trips' : 'All trips'}</h3>
      
      {/* Search and Filter Controls */}
      <div style={{ marginBottom: 12, display: 'flex', flexDirection: 'column', gap: 8 }}>
        {/* Search input */}
        <input
          type="text"
          aria-label="Search by trip ID or user ID"
          placeholder="Search by trip ID or user ID"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          style={{ padding: '6px 8px', borderRadius: 4, border: '1px solid #e2e8f0', fontSize: 12 }}
        />

        {/* Date range filters */}
        <div style={{ display: 'flex', gap: 8, alignItems: 'center', fontSize: 12 }}>
          <label style={{ fontSize: 12, color: '#0f172a' }}>From</label>
          <input
            type="date"
            aria-label="Start date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            style={{ padding: '4px 6px', borderRadius: 4, border: '1px solid #e2e8f0', fontSize: 12 }}
          />
          <label style={{ fontSize: 12, color: '#0f172a' }}>To</label>
          <input
            type="date"
            aria-label="End date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            style={{ padding: '4px 6px', borderRadius: 4, border: '1px solid #e2e8f0', fontSize: 12 }}
          />
        </div>

        {/* Plan and Type filters */}
        <div style={{ display: 'flex', gap: 8, fontSize: 12, flexWrap: 'wrap' }}>
          <label style={{ display: "flex", alignItems: "center", gap: 4 }}>
            <input
              type="radio"
              name="plan"
              value="ALL"
              checked={planFilter === "ALL"}
              onChange={() => setPlanFilter("ALL")}
            />
            <span>All plans</span>
          </label>
          <label style={{ display: "flex", alignItems: "center", gap: 4 }}>
            <input
              type="radio"
              name="plan"
              value="SINGLE_RIDE"
              checked={planFilter === "SINGLE_RIDE"}
              onChange={() => setPlanFilter("SINGLE_RIDE")}
            />
            <span>Single ride</span>
          </label>
          <label style={{ display: "flex", alignItems: "center", gap: 4 }}>
            <input
              type="radio"
              name="plan"
              value="DAY_PASS"
              checked={planFilter === "DAY_PASS"}
              onChange={() => setPlanFilter("DAY_PASS")}
            />
            <span>Day pass</span>
          </label>
          <label style={{ display: "flex", alignItems: "center", gap: 4 }}>
            <input
              type="radio"
              name="plan"
              value="MONTHLY_PASS"
              checked={planFilter === "MONTHLY_PASS"}
              onChange={() => setPlanFilter("MONTHLY_PASS")}
            />
            <span>Monthly pass</span>
          </label>
        </div>

        {/* Bike type selector */}
        <label style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12 }}>
          <span>Type</span>
          <select
            value={bikeTypeFilter}
            onChange={(e) => setBikeTypeFilter(e.target.value)}
            style={{ padding: '4px 6px', borderRadius: 4, border: '1px solid #e2e8f0', fontSize: 12 }}
          >
            <option value="ALL">All</option>
            {bikeTypes.map((bt) => (
              <option key={bt} value={bt}>{bt}</option>
            ))}
          </select>
        </label>

        {/* Clear button */}
        <button 
          type="button" 
          onClick={() => { 
            setSearchQuery(""); 
            setStartDate(""); 
            setEndDate(""); 
            setBikeTypeFilter("ALL"); 
            setPlanFilter("ALL"); 
          }}
          style={{
            padding: '6px 12px',
            backgroundColor: '#e2e8f0',
            border: '1px solid #cbd5e1',
            borderRadius: 4,
            cursor: 'pointer',
            fontSize: 12,
            fontWeight: 500,
            alignSelf: 'flex-start'
          }}
        >
          Clear Filters
        </button>
      </div>

      {/* Trips display */}
      {filteredTrips.length === 0 ? (
        <div className="empty">No trips found</div>
      ) : (
        <div className="trip-rows">
          {filteredTrips.map((t, idx) => (
            <div className="trip-row" key={t.id ?? t.tripId ?? idx} onClick={() => setSelectedTrip(t)} style={{ cursor: 'pointer' }}>
              <Trip trip={t} />
            </div>
          ))}
        </div>
      )}

      {/* Trip Details Modal */}
      {selectedTrip && (
        <TripDetails
          trip={selectedTrip}
          onClose={() => setSelectedTrip(null)}
        />
      )}
    </div>
  );
}

export default TripList;
