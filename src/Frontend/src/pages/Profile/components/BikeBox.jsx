import React, { useEffect, useMemo, useState } from "react";
import { formatDateTime, truncateId } from "../../../utils/utils";

const formatBikeTypeLabel = (type) => {
    if (!type) return "Unknown type";
    const normalized = type.toString().toUpperCase();
    if (normalized === "STANDARD") return "Standard";
    if (normalized === "E_BIKE") return "E-bike";
    return normalized.charAt(0) + normalized.slice(1).toLowerCase();
};

const formatDuration = (startTimestamp, now) => {
    if (!startTimestamp) return "—";
    const start = new Date(startTimestamp);
    const startMs = start.getTime();
    if (Number.isNaN(startMs)) return "—";
    const diffMs = Math.max(0, now - startMs);
    const totalSeconds = Math.floor(diffMs / 1000);
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;
    const pad = (value) => value.toString().padStart(2, "0");
    return `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
};

function BikeBox({ bikeRentals = [] }) {
    const [now, setNow] = useState(Date.now());

    useEffect(() => {
        if (bikeRentals.length === 0) return undefined;
        const interval = setInterval(() => setNow(Date.now()), 1000);
        return () => clearInterval(interval);
    }, [bikeRentals.length]);

    const rentalsWithMeta = useMemo(() => {
        return bikeRentals.map((rental) => ({
            ...rental,
            readableType: formatBikeTypeLabel(rental.bikeType),
        }));
    }, [bikeRentals]);

    return (
        <div
            style={{
                border: "1px solid #e2e8f0",
                borderRadius: "12px",
                padding: "16px",
                background: "#fff",
            }}
        >
            <div
                style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    marginBottom: "12px",
                    gap: "12px",
                }}
            >
                <strong>Current Bikes</strong>
            </div>

            {rentalsWithMeta.length === 0 ? (
                <div style={{ color: "#475569", fontSize: 14 }}>No bikes currently rented.</div>
            ) : (
                <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
                    {rentalsWithMeta.map((rental) => (
                        <div
                            key={rental.bikeId ?? rental.rentedAt}
                            style={{
                                border: "1px solid #e2e8f0",
                                borderRadius: 10,
                                padding: "12px",
                                background: "#f8fafc",
                                display: "grid",
                                gridTemplateColumns: "repeat(auto-fit, minmax(160px, 1fr))",
                                gap: 8,
                            }}
                        >
                            <div>
                                <div style={{ fontSize: 12, fontWeight: 600, color: "#1e293b" }}>Bike</div>
                                <div style={{ color: "#0f172a", fontSize: 14 }}>
                                    {truncateId(rental.bikeId ?? "Unknown")}
                                </div>
                            </div>
                            <div>
                                <div style={{ fontSize: 12, fontWeight: 600, color: "#1e293b" }}>Type</div>
                                <div style={{ color: "#0f172a", fontSize: 14 }}>{rental.readableType}</div>
                            </div>
                            <div>
                                <div style={{ fontSize: 12, fontWeight: 600, color: "#1e293b" }}>Rented From</div>
                                <div style={{ color: "#0f172a", fontSize: 14 }}>
                                    {rental.stationName ?? "Unknown station"}
                                </div>
                            </div>
                            <div>
                                <div style={{ fontSize: 12, fontWeight: 600, color: "#1e293b" }}>Rented At</div>
                                <div style={{ color: "#0f172a", fontSize: 14 }}>
                                    {rental.rentedAt ? formatDateTime(rental.rentedAt) : "—"}
                                </div>
                            </div>
                            <div>
                                <div style={{ fontSize: 12, fontWeight: 600, color: "#1e293b" }}>Duration</div>
                                <div style={{ color: "#0f172a", fontSize: 14 }}>
                                    {formatDuration(rental.rentedAt, now)}
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

export default BikeBox;
