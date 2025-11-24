import { createContext, useEffect, useRef, useState } from "react";
import { Client } from "@stomp/stompjs";
import { useAuth } from "./AuthContext"; 

export const WebSocketContext = createContext(null);

const getWebSocketURL = () => {
  // Use environment variable if available (recommended for production)
  if (import.meta.env.VITE_WS_URL) {
    return import.meta.env.VITE_WS_URL;
  }
  
  // Fallback: dynamically construct WebSocket URL from current location
  if (typeof window !== "undefined") {
    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
    const host = window.location.host;
    return `${protocol}//${host}/ws`;
  }
  
  return "ws://localhost:8080/ws";
};

export const WebSocketProvider = ({ children }) => {
  const { user, isAuthenticated, loading } = useAuth();
  const [connected, setConnected] = useState(false);
  const [messages, setMessages] = useState([]);
  const scrollRef = useRef(null);
  const stompClient = useRef(null);
  const wsUrl = getWebSocketURL();

useEffect(() => {
  if (!isAuthenticated || loading) return; // ← prevent connecting before user exists
  const client = new Client({
    brokerURL: wsUrl,
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  });

  client.onConnect = () => {
    client.subscribe('/topic/user-events', (message) => {
      const data = JSON.parse(message.body);

      if (user.id === data.userId) {
        alert("Your new loyalty tier is: " + data.loyaltyTier);
      }
    });
  };

  client.activate();

  return () => client.deactivate();
}, [wsUrl, isAuthenticated]); // 👈 important


    return (
        <WebSocketContext.Provider value={{ messages, connected }}>
            {children}
        </WebSocketContext.Provider>
    );
};
