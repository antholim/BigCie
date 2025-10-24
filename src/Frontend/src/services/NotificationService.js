import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const DEFAULT_BASE_URL = "http://localhost:8080";
const OPERATOR_TOPIC = "/topic/operator-events";

const resolveBaseUrl = () => {
  const envUrl = import.meta.env.VITE_WS_BASE_URL ?? import.meta.env.VITE_API_BASE_URL;
  if (envUrl) return envUrl.replace(/\/+$/, "");

  if (typeof window !== "undefined") {
    const protocol = window.location.protocol === "https:" ? "https:" : "http:";
    const hostname = window.location.hostname || "localhost";
    const port = import.meta.env.VITE_API_PORT ?? "8080";
    return `${protocol}//${hostname}:${port}`;
  }

  return DEFAULT_BASE_URL;
};

class NotificationService {
  constructor() {
    this.client = null;
    this.tripEventHandlers = new Set();
    this.tripSubscription = null;
    this.connected = false;
    this.pendingMessages = [];
  }

  subscribeToTripEvents(handler) {
    this.tripEventHandlers.add(handler);
    this.ensureClient();
    return () => {
      this.tripEventHandlers.delete(handler);
      if (this.tripEventHandlers.size === 0) {
        this.teardownSubscription();
      }
    };
  }

  ensureClient() {
    if (this.client) {
      if (!this.client.active) {
        this.client.activate();
      }
      return;
    }

    const baseUrl = resolveBaseUrl();
    this.client = new Client({
      webSocketFactory: () => new SockJS(`${baseUrl}/ws`),
      reconnectDelay: 5000,
      debug: (msg) => {
        if (import.meta.env.DEV) {
          console.debug(`[NotificationService] ${msg}`);
        }
      },
    });

    this.client.onConnect = () => {
      this.connected = true;
      this.tripSubscription = this.client.subscribe(OPERATOR_TOPIC, (message) => {
        try {
          const payload = JSON.parse(message.body);
          this.dispatchTripEvent(payload);
        } catch (err) {
          console.error("Failed to parse trip event payload", err);
        }
      });

      while (this.pendingMessages.length > 0) {
        const payload = this.pendingMessages.shift();
        this.dispatchTripEvent(payload);
      }
    };

    this.client.onStompError = (frame) => {
      console.error("STOMP error", frame.headers["message"], frame.body);
    };

    this.client.onWebSocketClose = () => {
      this.connected = false;
    };

    this.client.activate();
  }

  dispatchTripEvent(event) {
    if (!this.connected) {
      this.pendingMessages.push(event);
      return;
    }

    this.tripEventHandlers.forEach((handler) => {
      try {
        handler(event);
      } catch (err) {
        console.error("Trip event handler threw", err);
      }
    });
  }

  teardownSubscription() {
    if (this.tripSubscription) {
      try {
        this.tripSubscription.unsubscribe();
      } catch (err) {
        console.warn("Failed to unsubscribe trip events", err);
      } finally {
        this.tripSubscription = null;
      }
    }

    if (this.client) {
      this.client.deactivate();
      this.client = null;
      this.connected = false;
    }
  }
}

const notificationService = new NotificationService();
export default notificationService;
