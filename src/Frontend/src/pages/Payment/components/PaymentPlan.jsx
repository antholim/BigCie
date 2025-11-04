import React, { useEffect, useState, useCallback, use } from "react";
import FetchingService from "../../../services/FetchingService";


function PaymentPlan({ value: controlledValue, onChange }) {
    // component supports controlled or uncontrolled usage
    const [plan, setPlan] = useState(controlledValue ?? "SINGLE_RIDE");
    const [currentPlan, setCurrentPlan] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    useEffect(() => {
        if (controlledValue !== undefined && controlledValue !== plan) {
            setPlan(controlledValue);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [controlledValue]);

    const handleChange = (next) => {
        if (controlledValue === undefined) setPlan(next);
        onChange?.(next);
    };
    const loadCurrentPlan = useCallback(async () => {
        try {
            const response = await FetchingService.get("/api/v1/payments/current-plan");
            console.log("Payments response:", response);
            setCurrentPlan(response.data.pricingPlan);
        } catch (err) {
            setError(err.response?.data?.message || err.message || "Unable to load current plan right now.");
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadCurrentPlan();
    }, [loadCurrentPlan]);

    return (
        <div className="db-card" style={{ display: "flex", flexDirection: "column", gap: 12 }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div>
                    <strong style={{ fontSize: 16 }}>Choose plan</strong>
                    <div className="db-muted" style={{ fontSize: 13 }}>Current plan {currentPlan}</div>
                </div>
            </div>

            <div style={{ display: "flex", gap: 12, alignItems: "center", flexWrap: "wrap" }}>
                <label style={{ display: "flex", alignItems: "center", gap: 8 }}>
                    <input
                        type="radio"
                        name="pricingPlan"
                        value="SINGLE_RIDE"
                        checked={plan === "SINGLE_RIDE"}
                        onChange={() => handleChange("SINGLE_RIDE")}
                    />
                    <span>Single ride</span>
                </label>

                <label style={{ display: "flex", alignItems: "center", gap: 8 }}>
                    <input
                        type="radio"
                        name="pricingPlan"
                        value="DAY_PASS"
                        checked={plan === "DAY_PASS"}
                        onChange={() => handleChange("DAY_PASS")}
                    />
                    <span>Day pass</span>
                </label>

                <label style={{ display: "flex", alignItems: "center", gap: 8 }}>
                    <input
                        type="radio"
                        name="pricingPlan"
                        value="MONTHLY_PASS"
                        checked={plan === "MONTHLY_PASS"}
                        onChange={() => handleChange("MONTHLY_PASS")}
                    />
                    <span>Monthly pass</span>
                </label>
            </div>

            <div className="db-muted" style={{ fontSize: 13 }}>
                Selected: <strong style={{ color: "#0f172a" }}>{plan}</strong>
            </div>
        </div>
    );
}

export default PaymentPlan;