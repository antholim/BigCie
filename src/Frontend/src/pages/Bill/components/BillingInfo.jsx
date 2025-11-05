import React from "react";

function BillingInfo({ billingInfo }) {
  const billingId = billingInfo.id ?? "Not available";
  const userId = billingInfo.userId ?? "Not available";

  return (
    <div className="db-card" style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      <div>
        <h2 style={{ marginBottom: 4, fontSize: 18, fontWeight: 600 }}>Billing Information</h2>
        <p className="db-muted" style={{ margin: 0, fontSize: 13 }}>
          Your bill details and associated information.
        </p>
      </div>

      <dl style={{ display: "grid", gridTemplateColumns: "auto 1fr", rowGap: 12, columnGap: 16, margin: 0 }}>
        <dt style={{ fontWeight: 600, color: "#0f172a" }}>Bill ID</dt>
        <dd style={{ margin: 0, color: "#475569", wordBreak: "break-all" }}>
          {billingId}
        </dd>

        <dt style={{ fontWeight: 600, color: "#0f172a" }}>User ID</dt>
        <dd style={{ margin: 0, color: "#475569", wordBreak: "break-all" }}>
          {userId}
        </dd>
      </dl>
    </div>
  );
}

export default BillingInfo;
