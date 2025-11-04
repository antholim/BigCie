import { useEffect, useRef, useState, useCallback } from 'react';

// Simple WebSocket hook with auto-reconnect, message buffering and send API.
export default function useWebsocket(url, options = {}) {
  const { reconnect = true, maxMessages = 1000, reconnectMaxDelay = 30000 } = options;
  const wsRef = useRef(null);
  const reconnectTimeoutRef = useRef(null);
  const attemptsRef = useRef(0);
  const sendQueueRef = useRef([]);
  const mountedRef = useRef(false);

  const [connected, setConnected] = useState(false);
  const [messages, setMessages] = useState([]);

  const pushMessage = useCallback(
    (msg) => {
      setMessages((prev) => {
        const next = [msg, ...prev];
        if (next.length > maxMessages) next.length = maxMessages;
        return next;
      });
    },
    [maxMessages]
  );

  const clear = useCallback(() => setMessages([]), []);

  const connect = useCallback(() => {
    if (!url) return;
    try {
      wsRef.current = new WebSocket(url);
    } catch (err) {
      console.error('WebSocket create error', err);
      scheduleReconnect();
      return;
    }

    wsRef.current.onopen = () => {
      attemptsRef.current = 0;
      setConnected(true);
      // flush send queue
      while (sendQueueRef.current.length) {
        try {
          wsRef.current.send(JSON.stringify(sendQueueRef.current.shift()));
        } catch {
          break;
        }
      }
    };

    wsRef.current.onmessage = (evt) => {
      let parsed = null;
      try {
        parsed = JSON.parse(evt.data);
      } catch {
        parsed = { message: evt.data };
      }
      const envelope = {
        raw: evt.data,
        data: parsed,
        receivedAt: Date.now(),
      };
      pushMessage(envelope);
    };

    wsRef.current.onclose = () => {
      setConnected(false);
      wsRef.current = null;
      if (reconnect) scheduleReconnect();
    };

    wsRef.current.onerror = (err) => {
      console.error('WebSocket error', err);
      try {
        wsRef.current.close();
      } catch {
        // ignore close error
      }
    };
  }, [url, reconnect, pushMessage, scheduleReconnect]);

  const scheduleReconnect = useCallback(() => {
    if (!reconnect) return;
    attemptsRef.current += 1;
    const delay = Math.min(1000 * 2 ** Math.min(attemptsRef.current, 8), reconnectMaxDelay);
    if (reconnectTimeoutRef.current) clearTimeout(reconnectTimeoutRef.current);
    reconnectTimeoutRef.current = setTimeout(() => {
      if (!mountedRef.current) return;
      connect();
    }, delay);
  }, [connect, reconnect, reconnectMaxDelay]);

  useEffect(() => {
    mountedRef.current = true;
    connect();
    return () => {
      mountedRef.current = false;
      if (reconnectTimeoutRef.current) clearTimeout(reconnectTimeoutRef.current);
      if (wsRef.current) {
        try {
          wsRef.current.close();
        } catch {
          // ignore close error
        }
      }
    };
  }, [url, connect]);

  const send = useCallback((payload) => {
    if (!payload) return;
    const p = typeof payload === 'string' ? payload : JSON.stringify(payload);
    if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
      wsRef.current.send(p);
    } else {
      try {
        sendQueueRef.current.push(payload);
      } catch {
        // ignore queue push error
      }
    }
  }, []);

  const close = useCallback(() => {
    if (reconnectTimeoutRef.current) clearTimeout(reconnectTimeoutRef.current);
    if (wsRef.current) {
      try {
        wsRef.current.close();
      } catch {
        // ignore close error
      }
    }
  }, []);

  return {
    connected,
    messages,
    send,
    clear,
    close,
  };
}