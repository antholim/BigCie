import React, { useState, useCallback, useRef, useEffect } from 'react';
import './logDashboard.css';
import { Client } from '@stomp/stompjs';

const formatTimestamp = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString('en-US', {
    hour12: false,
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    fractionalSecondDigits: 3
  });
};

const guessLogType = (message) => {
  if (typeof message !== 'object' || !message) return 'info';
  if (message.level) return message.level.toLowerCase();
  if (message.type) return message.type.toLowerCase();
  if (message.error || message.err) return 'error';
  if (message.warn || message.warning) return 'warn';
  if (message.debug) return 'debug';
  return 'info';
};

const formatEventMessage = (data) => {
  if (!data || !data.type) return JSON.stringify(data);
  
  switch (data.type) {
    case 'TRIP_STARTED':
      return `Trip started - Bike ${data.bikeId.slice(0,8)} - Rider ${data.riderId.slice(0,8)} - Station ${data.stationId.slice(0,8)}`;
    default:
      return JSON.stringify(data);
  }
};

const LogEntry = React.memo(({ entry }) => {
  const { data, receivedAt } = entry;
  const type = guessLogType(data);
  const message = formatEventMessage(data);

  return (
    <div className="log-entry">
      <span className="timestamp">{formatTimestamp(receivedAt)}</span>
      <span className={`type ${type}`}>{type}</span>
      <span className="message">{message}</span>
    </div>
  );
});

export default function LogDashboard({ wsUrl = 'ws://localhost:8080/ws' }) {
  const [filter, setFilter] = useState('');
  const [isPaused, setIsPaused] = useState(false);
  const [connected, setConnected] = useState(false);
  const [messages, setMessages] = useState([]);
  const scrollRef = useRef(null);
  const stompClient = useRef(null);

  useEffect(() => {
    const client = new Client({
      brokerURL: wsUrl,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      setConnected(true);
      client.subscribe('/topic/operator-events', (message) => {
        const receivedAt = new Date().getTime();
        const data = JSON.parse(message.body);
        setMessages(prev => [...prev, { data, receivedAt }]);
      });
    };

    client.onDisconnect = () => {
      setConnected(false);
    };

    client.activate();
    stompClient.current = client;

    return () => {
      if (client.connected) {
        client.deactivate();
      }
    };
  }, [wsUrl]);

  const clear = useCallback(() => {
    setMessages([]);
  }, []);

  const filteredMessages = messages.filter(msg => {
    if (!filter) return true;
    try {
      return JSON.stringify(msg.data).toLowerCase().includes(filter.toLowerCase());
    } catch {
      // ignore serialization errors and filter out malformed messages
      return false;
    }
  });

  const scrollToBottom = useCallback(() => {
    if (scrollRef.current && !isPaused) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [isPaused]);

  useEffect(() => {
    
    scrollToBottom();
  }, [filteredMessages, scrollToBottom]);

  return (
    <div className="log-dashboard">
      <div className="log-controls">
        <input
          type="text"
          placeholder="Filter logs..."
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
        />
        <button onClick={() => setIsPaused(!isPaused)}>
          {isPaused ? '▶️ Resume' : '⏸️ Pause'}
        </button>
        <button onClick={clear}>🗑️ Clear</button>
        <div className={`connection-status ${connected ? 'connected' : 'disconnected'}`}>
          {connected ? 'Connected' : 'Disconnected'}
        </div>
      </div>
      <div className="log-stream" ref={scrollRef}>
        {filteredMessages.map((msg, idx) => (
          <LogEntry key={msg.receivedAt + idx} entry={msg} />
        ))}
      </div>
    </div>
  );
}