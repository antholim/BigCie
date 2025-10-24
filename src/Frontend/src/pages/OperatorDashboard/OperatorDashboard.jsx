import React from 'react';
import LogDashboard from './components/LogDashboard';

function OperatorDashboard() {
    // You can configure the WebSocket URL via env variable
    const wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws';
    
    return (
        <div style={{ height: '100vh', padding: '20px' }}>
            <LogDashboard wsUrl={wsUrl} />
        </div>
    );
}

export default OperatorDashboard;