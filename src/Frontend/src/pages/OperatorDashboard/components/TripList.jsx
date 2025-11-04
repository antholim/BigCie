import React, { useEffect, useState } from 'react';
import { useAuth } from '../../../contexts/AuthContext';
import FetchingService from '../../../services/FetchingService';
import Trip from '../../Trips/components/Trip';
import './TripList.css';

function TripList() {
  const { user } = useAuth();
  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

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

  return (
    <div className="trip-list">
      <h3>All trips</h3>
      {trips.length === 0 ? (
        <div className="empty">No trips found</div>
      ) : (
        <div className="trip-rows">
          {trips.map((t, idx) => (
            <div className="trip-row" key={t.id ?? t.tripId ?? idx}>
              <Trip trip={t} />
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default TripList;
