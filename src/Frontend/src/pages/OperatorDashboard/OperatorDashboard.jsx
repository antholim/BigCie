import React from 'react';
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import LogDashboard from './components/LogDashboard';
import TripList from './components/TripList';
import { useAuth } from '../../contexts/AuthContext';

function OperatorDashboard() {
    const wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws';
    const navigate = useNavigate();
    const { user, isAuthenticated, loading } = useAuth();

    // Determine operator role
    const isOperator = user?.userType === 'OPERATOR' || user?.type === 'OPERATOR';

    useEffect(() => {
        // Wait for auth check to finish before redirecting
        if (loading) return;
        if (!isAuthenticated || !isOperator) {
            // If user is not logged in or not an operator, send them to the home page
            navigate('/');
        }
    }, [loading, isAuthenticated, isOperator, navigate]);

    // While auth is being verified, avoid rendering the dashboard
    if (loading) return null;

    // If not operator, the effect above will redirect; render nothing here as a guard
    if (!isAuthenticated || !isOperator) return null;

    return (
        // We need to add a box with all the trips using the /api/v1/trips/getAll endpoint
        <div style={{ height: '100vh', padding: '20px' }}>
            {/* Make the inner grid fill the available height so children can scroll */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 420px', gap: '20px', alignItems: 'stretch', height: '100%' }}>
                {/* Each column gets its own scroll container so content can scroll independently */}
                <div style={{ height: '100%', overflow: 'auto' }}>
                    <LogDashboard wsUrl={wsUrl} />
                </div>
                <div style={{ height: '100%', overflow: 'auto' }}>
                    <TripList />
                </div>
            </div>
        </div>
    );
}

export default OperatorDashboard;