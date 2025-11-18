import { createContext, useEffect, useRef, useState } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

export const WebSocketContext = createContext(null);
const wsUrl = import.meta.env.VITE_WS_URL || "ws://localhost:8080/ws";
export const WebSocketProvider = ({ children }) => {
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
      client.subscribe('/topic/user-events', (message) => {
        const receivedAt = new Date().getTime();
        const data = JSON.parse(message.body);
        alert("Your new loyalty tier is: " + data);
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

    return (
        <WebSocketContext.Provider value={{ messages, connected }}>
            {children}
        </WebSocketContext.Provider>
    );
};
