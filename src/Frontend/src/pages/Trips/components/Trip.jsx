import { E_BIKE_CHARGE } from "../../../constants/constants";
import { formatDateTime } from "../../../utils/utils";

function Trip({ trip, onSelect }) {
    return (
        <div 
            key={trip.id} 
            className="db-card" 
            onClick={onSelect}
            style={{ 
                display: "flex", 
                flexDirection: "column", 
                gap: 8,
                cursor: "pointer",
                transition: "transform 0.1s ease-in-out",
                ':hover': {
                    transform: "translateY(-1px)"
                }
            }}
        >
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div>
                    <strong style={{ fontSize: 16 }}>{trip.pricingPlan || trip.bikeType || "Trip"}</strong>
                    <div className="db-muted" style={{ fontSize: 13 }}>{trip.bikeType} • {trip.status}</div>
                </div>
                <div style={{ textAlign: "right" }}>
                    {trip.flexDollarsUsed > 0 ? (
                        <>
                            <div style={{ fontSize: 12, color: "#16a34a", textDecoration: "line-through" }}>
                                ${(trip.cost ?? 0).toFixed(2)}
                            </div>
                            <div style={{ fontWeight: 700, display: "flex", alignItems: "center", gap: 4, justifyContent: "flex-end" }}>
                                ${(trip.amountCharged ?? 0).toFixed(2)}
                                <span style={{ fontSize: 11, background: "#dcfce7", color: "#166534", padding: "2px 6px", borderRadius: 4, fontWeight: 600 }}>
                                    💰 FLEX
                                </span>
                            </div>
                        </>
                    ) : (
                        <div style={{ fontWeight: 700 }}>${(trip.cost ?? 0).toFixed(2)}</div>
                    )}
                    <div className="db-muted" style={{ fontSize: 13 }}>{(trip.distanceInKm ?? 0).toFixed(2)} km</div>
                </div>
            </div>

            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 8 }}>
                <div>
                    <div style={{ fontWeight: 600, color: "#1e293b", fontSize: 13 }}>From</div>
                    <div className="db-muted" style={{ fontSize: 13 }}>{trip.bikeStationStart || trip.startStation || "—"}</div>
                    <div className="db-muted" style={{ fontSize: 12 }}>{formatDateTime(trip.startDate)}</div>
                </div>
                <div>
                    <div style={{ fontWeight: 600, color: "#1e293b", fontSize: 13 }}>To</div>
                    <div className="db-muted" style={{ fontSize: 13 }}>{trip.bikeStationEnd || trip.endStation || "—"}</div>
                    <div className="db-muted" style={{ fontSize: 12 }}>{formatDateTime(trip.endDate)}</div>
                </div>
            </div>

            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginTop: 8 }}>
                <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
                    <div className="db-muted" style={{ fontSize: 13 }}>Bike: {trip.bikeId ? trip.bikeId.slice(0, 8) : "—"}</div>
                    <div className="db-muted" style={{ fontSize: 13 }}>
                        Type: {trip.bikeType ?? trip.type ?? "—"} • E-Bike Charge {trip.bikeType === "E_BIKE" && trip.cost === 0 ? "Covered by subscription" : `$${trip.eBikeCharge?.toFixed(2) ?? E_BIKE_CHARGE}`}
                    </div>
                    <div className="db-muted" style={{ fontSize: 12 }}>Trip ID: {trip.id ?? "—"}</div>
                </div>
                <div style={{ fontSize: 12, padding: "4px 8px", borderRadius: 8, background: trip.status === "COMPLETED" ? "#ecfdf5" : "#fff7ed", color: trip.status === "COMPLETED" ? "#065f46" : "#92400e" }}>
                    {trip.status}
                </div>
            </div>
            <div
                className="db-muted"
                style={{
                    fontSize: 13,
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                }}
            >
                <span>Payment: {trip.paymentInfo?.cardType ?? "—"}</span>
                <span>•••• {trip.paymentInfo?.lastFourCreditCardNumber ?? "1234"}</span>
            </div>


        </div>
    )
} export default Trip;