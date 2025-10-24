import React, { createContext, useContext, useState } from "react";

const BikeContext = createContext({
  bikeIds: [],
  selectedBikeId: null,
  setBikeIds: () => {},
  setSelectedBikeId: () => {},
});

export function BikeProvider({ children, value }) {
  // allow overriding via `value` for tests or embedding
  const [bikeIds, setBikeIds] = useState(value?.bikeIds ?? []);
  const [selectedBikeId, setSelectedBikeId] = useState(value?.selectedBikeId ?? null);

  const ctx = value ?? { bikeIds, setBikeIds, selectedBikeId, setSelectedBikeId };

  return <BikeContext.Provider value={ctx}>{children}</BikeContext.Provider>;
}

export function useBike() {
  return useContext(BikeContext);
}

export default BikeContext;
