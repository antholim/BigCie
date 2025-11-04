import React, { useEffect, useState } from "react";

function PaymentPlan({ paymentInfo, value: controlledValue, onChange }) {
    // component supports controlled or uncontrolled usage
    const [plan, setPlan] = useState(controlledValue ?? "SINGLE_RIDE");

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

    return (
        <div className="db-card" style={{ display: "flex", flexDirection: "column", gap: 12 }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div>
                    <strong style={{ fontSize: 16 }}>Choose plan</strong>
                    <div className="db-muted" style={{ fontSize: 13 }}>Current plan {plan}</div>
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