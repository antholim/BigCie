function PaymentInfo({ paymentInfo }) {
    const id = paymentInfo.paymentInfoId ?? paymentInfo.id;
    const lastFour = paymentInfo.lastFourCreditCardNumber ?? paymentInfo.lastFour ?? "----";
    const expiry = paymentInfo.cardExpiry ?? "--/--";
    const holder = paymentInfo.cardHolderName ?? paymentInfo.cardHolder ?? "";
    const type = paymentInfo.cardType ?? paymentInfo.card_brand ?? "Card";

    return (
        <div key={id} className="db-card" style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div>
                    <strong style={{ fontSize: 16 }}>{type}</strong>
                    <div className="db-muted" style={{ fontSize: 13 }}>{holder}</div>
                </div>

                <div style={{ textAlign: "right" }}>
                    <div style={{ fontWeight: 700 }}>•••• {lastFour}</div>
                    <div className="db-muted" style={{ fontSize: 13 }}>{expiry}</div>
                </div>
            </div>

            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginTop: 8 }}>
                <div className="db-muted" style={{ fontSize: 13 }}>ID: {id ?? "—"}</div>
                <div style={{ display: "flex", gap: 8 }}>
                    {/* Placeholder for actions: edit / remove. Implement handlers in parent if needed. */}
                    <button type="button" className="db-btn" style={{ padding: "6px 10px" }} disabled>
                        Edit
                    </button>
                    <button type="button" className="db-btn" style={{ padding: "6px 10px", background: "#ef4444", color: "#fff", border: "none" }} disabled>
                        Remove
                    </button>
                </div>
            </div>
        </div>
    );
}

export default PaymentInfo;