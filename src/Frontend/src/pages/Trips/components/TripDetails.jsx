import React, { useState, useEffect } from "react";
import { formatDateTime } from "../../../utils/utils";
import FetchingService from "../../../services/FetchingService";

export default function TripDetails({ trip, onClose }) {
  const [eBikeSurcharge, setEBikeSurcharge] = useState(null);
  const [fiveMinRate, setFiveMinRate] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchPricingInfo = async () => {
      try {
        const [surchargeRes, rateRes] = await Promise.all([
          FetchingService.get("api/v1/payments/ebike-surcharge"),
          FetchingService.get("api/v1/payments/5min-rate")
        ]);
        
        console.log("E-bike surcharge:", surchargeRes.data);
        setEBikeSurcharge(surchargeRes.data);
        
        console.log("5-min rate:", rateRes.data);
        setFiveMinRate(rateRes.data);
      } catch (error) {
        console.error("Failed to fetch pricing info:", error);
        // Fallback to default values from Prices constants
        setEBikeSurcharge(2.50);
        setFiveMinRate(1.25);
      } finally {
        setLoading(false);
      }
    };

    fetchPricingInfo();
  }, []);

  const calculateDuration = (start, end) => {
    const startTime = new Date(start).getTime();
    const endTime = new Date(end).getTime();
    return Math.round((endTime - startTime) / (1000 * 60));
  };

  return (
    <div className="trip-details-overlay" 
      onClick={(e) => e.target.className === 'trip-details-overlay' && onClose()}
      style={{
        position: "fixed",
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: "rgba(0,0,0,0.5)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        zIndex: 1000
      }}
    >
      <div className="trip-details-content" style={{
        backgroundColor: "white",
        borderRadius: "12px",
        padding: "24px",
        width: "90%",
        maxWidth: "600px",
        maxHeight: "90vh",
        overflow: "auto"
      }}>
        <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "20px" }}>
          <h2 style={{ margin: 0 }}>Trip Details</h2>
          <button className="db-btn" onClick={onClose}>Close</button>
        </div>

        <div style={{ display: "grid", gap: "16px" }}>
          <div><strong>Trip ID:</strong> {trip.id}</div>
          <div><strong>Rider:</strong> {trip.userId || 'N/A'}</div>
          <div>
            <strong>Start Station:</strong> {trip.bikeStationStart || trip.startStation || 'N/A'}
            <br />
            <span className="db-muted">{formatDateTime(trip.startDate)}</span>
          </div>
          <div>
            <strong>End Station:</strong> {trip.bikeStationEnd || trip.endStation || 'N/A'}
            <br />
            <span className="db-muted">{formatDateTime(trip.endDate)}</span>
          </div>
          <div>
            <strong>Duration:</strong> {calculateDuration(trip.startDate, trip.endDate)} minutes
          </div>
          <div>
            <strong>Bike Type:</strong> {trip.bikeType || 'N/A'}
          </div>

          <div>
            <strong>Cost Breakdown:</strong>
            <div style={{ marginLeft: "20px" }}>
              <div>Pricing Plan: {trip.pricingPlan || 'N/A'}</div>
              {loading ? (
                <div>Loading pricing information...</div>
              ) : (
                <>
                  {trip.pricingPlan === "DAY_PASS" || trip.pricingPlan === "MONTHLY_PASS" ? (
                    <div style={{ color: "#22c55e", fontWeight: "500" }}>
                      ✓ Trip covered by {trip.pricingPlan === "DAY_PASS" ? "daily" : "monthly"} pass
                    </div>
                  ) : (
                    <>
                      <div>5-minute units rate: ${(fiveMinRate || 1.25).toFixed(2)} per 5 minutes</div>
                      {trip.bikeType === "E_BIKE" && eBikeSurcharge > 0 && (
                        <div>E-bike surcharge: ${(eBikeSurcharge || 0).toFixed(2)}</div>
                      )}
                    </>
                  )}
                </>
              )}
              {trip.discountApplied > 0 && (
                <div style={{ marginTop: "12px", padding: "10px", background: "#dbeafe", borderRadius: "6px", borderLeft: "3px solid #0284c7" }}>
                  <div style={{ color: "#0284c7", fontWeight: "500" }}>
                    🎉 Discount Applied: {trip.discountApplied}%
                  </div>
                </div>
              )}
              <div style={{ marginTop: "12px", padding: "12px", background: "#f8fafc", borderRadius: "8px" }}>
                <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "8px" }}>
                  <span>Total Cost:</span>
                  <span style={{ fontWeight: "600" }}>${(trip.cost || 0).toFixed(2)}</span>
                </div>
                {trip.flexDollarsUsed > 0 && (
                  <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "8px", color: "#16a34a" }}>
                    <span>💰 Flex Dollars Used:</span>
                    <span style={{ fontWeight: "600" }}>-${(trip.flexDollarsUsed || 0).toFixed(2)}</span>
                  </div>
                )}
                <div style={{ 
                  display: "flex", 
                  justifyContent: "space-between", 
                  paddingTop: "8px", 
                  borderTop: "2px solid #e2e8f0",
                  fontSize: "16px",
                  fontWeight: "700"
                }}>
                  <span>Amount Charged:</span>
                  <span>${(trip.amountCharged !== undefined ? trip.amountCharged : trip.cost || 0).toFixed(2)}</span>
                </div>
              </div>
            </div>
          </div>

          <div>
            <strong>Timeline:</strong>
            <div style={{ marginTop: "12px", borderLeft: "2px solid #e2e8f0", paddingLeft: "20px" }}>
              <div style={{ marginBottom: "12px" }}>
                <div style={{ fontWeight: "500" }}>Checkout</div>
                <div className="db-muted">{formatDateTime(trip.startDate)}</div>
              </div>
              <div>
                <div style={{ fontWeight: "500" }}>Return</div>
                <div className="db-muted">{formatDateTime(trip.endDate)}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
