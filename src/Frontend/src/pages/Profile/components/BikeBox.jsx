import React, { useState } from "react"
import { formatDateTime, truncateId } from "../../../utils/utils";

function BikeBox({bikeIdList}) {
    console.log(bikeIdList);
    return (
        <div
            key={bikeIdList[0]}
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
                    marginBottom: "8px",
                    gap: "12px",
                }}
            >
                <strong>Current Bikes</strong>
                <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                </div>
            </div>
            <dl
                style={{
                    display: "grid",
                    gridTemplateColumns: "auto 1fr",
                    rowGap: "6px",
                    columnGap: "12px",
                    margin: 0,
                }}
            >
                {bikeIdList.length === 0 && (
                    <dd style={{ margin: 0, color: "#475569" }}>No bikes found.</dd>
                )}
                {bikeIdList.map((bikeId) => {
                    return (
                        <>
                            <dt style={{ fontWeight: 600, color: "#1e293b" }}>Bike</dt>
                            <dd style={{ margin: 0, color: "#475569" }}>
                                {truncateId(bikeId)}
                            </dd>
                        </>
                    );
                })}
            </dl>
        </div>
    )
} export default BikeBox;