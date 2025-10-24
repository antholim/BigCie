import React, { useEffect, useRef } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

const DEFAULT_CENTER = [45.5019, -73.5674]; // Downtown Montreal demo

export default function MapPreview({ height = 260, className, style = {} }) {
  const mapRef = useRef(null);
  const leafletInstanceRef = useRef(null);

  useEffect(() => {
    if (!mapRef.current) {
      return undefined;
    }

    leafletInstanceRef.current = L.map(mapRef.current, {
      center: DEFAULT_CENTER,
      zoom: 13,
      zoomControl: false,
      attributionControl: false,
    });

    L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png").addTo(leafletInstanceRef.current);

    L.circle(DEFAULT_CENTER, {
      radius: 800,
      color: "#2563eb",
      weight: 2,
      fillColor: "#93c5fd",
      fillOpacity: 0.2,
    }).addTo(leafletInstanceRef.current);

    L.marker(DEFAULT_CENTER).addTo(leafletInstanceRef.current).bindPopup("Downtown core");

    return () => {
      if (leafletInstanceRef.current) {
        leafletInstanceRef.current.remove();
        leafletInstanceRef.current = null;
      }
    };
  }, []);

  useEffect(() => {
    if (!leafletInstanceRef.current) return;
    const timeout = setTimeout(() => {
      leafletInstanceRef.current.invalidateSize();
    }, 0);
    return () => clearTimeout(timeout);
  }, [height]);

  const computedStyle = {
    width: "100%",
    height: typeof height === "number" ? `${height}px` : height,
    borderRadius: "16px",
    overflow: "hidden",
    boxShadow: "0 10px 30px rgba(15, 23, 42, 0.18)",
    ...style,
  };

  return <div ref={mapRef} className={className} style={computedStyle} />;
}
