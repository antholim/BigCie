import React from 'react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import LogDashboard from './components/LogDashboard';
import TripList from './components/TripList';
import { useAuth } from '../../contexts/AuthContext';
import FetchingService from '../../services/FetchingService';

function OperatorDashboard() {
    const wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws';
    const navigate = useNavigate();
    const { user, isAuthenticated, loading } = useAuth();
    const [isRebalancing, setIsRebalancing] = useState(false);
    const [rebalanceMessage, setRebalanceMessage] = useState('');

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

    const handleRebalanceBikes = async () => {
        setIsRebalancing(true);
        setRebalanceMessage('');
        try {
            await FetchingService.post('api/v1/stations/rebalance-bikes', {});
            setRebalanceMessage('✅ Bikes rebalanced successfully!');
            setTimeout(() => setRebalanceMessage(''), 3000);
        } catch (error) {
            console.error('Error rebalancing bikes:', error);
            setRebalanceMessage('❌ Failed to rebalance bikes');
            setTimeout(() => setRebalanceMessage(''), 3000);
        } finally {
            setIsRebalancing(false);
        }
    };

    // While auth is being verified, avoid rendering the dashboard
    if (loading) return null;

    // If not operator, the effect above will redirect; render nothing here as a guard
    if (!isAuthenticated || !isOperator) return null;

    return (
        // We need to add a box with all the trips using the /api/v1/trips/getAll endpoint
        <div style={{ height: '100vh', padding: '20px', display: 'flex', flexDirection: 'column' }}>
            {/* Rebalance button at the top */}
            <div style={{ marginBottom: '15px', display: 'flex', gap: '10px', alignItems: 'center' }}>
                <button
                    onClick={() => navigate('/')}
                    style={{
                        padding: '10px 20px',
                        backgroundColor: '#2196F3',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        cursor: 'pointer',
                        fontSize: '14px',
                        fontWeight: 'bold',
                        transition: 'opacity 0.2s'
                    }}
                >
                    ← Go Back
                </button>
                <button
                    onClick={handleRebalanceBikes}
                    disabled={isRebalancing}
                    style={{
                        padding: '10px 20px',
                        backgroundColor: '#4CAF50',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        cursor: isRebalancing ? 'not-allowed' : 'pointer',
                        fontSize: '14px',
                        fontWeight: 'bold',
                        opacity: isRebalancing ? 0.6 : 1,
                        transition: 'opacity 0.2s'
                    }}
                >
                    {isRebalancing ? '⏳ Rebalancing...' : '🔄 Rebalance Bikes'}
                </button>
                {rebalanceMessage && (
                    <span style={{ fontSize: '14px', fontWeight: '500' }}>
                        {rebalanceMessage}
                    </span>
                )}
            </div>
            {/* Make the inner grid fill the available height so children can scroll */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 420px', gap: '20px', alignItems: 'stretch', flex: 1, minHeight: 0 }}>
                {/* Each column gets its own scroll container so content can scroll independently */}
                <div style={{ height: '100%', overflow: 'auto', minHeight: 0 }}>
                    <LogDashboard wsUrl={wsUrl} />
                </div>
                <div style={{ height: '100%', overflow: 'auto', minHeight: 0 }}>
                    <TripList />
                </div>
            </div>
        </div>
    );
}

export default OperatorDashboard;