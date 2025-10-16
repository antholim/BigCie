import React, { useEffect, useRef, useState, useCallback } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import styles from "./styles/MapDashboard.styles";
import FetchingService from "../../services/FetchingService";


export default function MapDashboard() {
    const mapRef = useRef(null);
    const leafletRef = useRef(null);
    const markerRef = useRef(null);
    const stationMarkersRef = useRef([]);
    const searchInputRef = useRef(null);

    const [loading, setLoading] = useState(true);
    const [center, setCenter] = useState({ lat: 45.497326, lng: -73.5816037 }); // default
    const [zoom, setZoom] = useState(13);
    const [stations, setStations] = useState([]);
    const [mapReady, setMapReady] = useState(false);
    const [selectedStation, setSelectedStation] = useState(null);
    const [stationMenuOpen, setStationMenuOpen] = useState(false);

    // Create station markers only after the map is initialized
    useEffect(() => {
        if (!mapReady || !leafletRef.current) return;

        let cancelled = false;

        const loadStationsAndRender = async () => {
            try {
                const resp = await FetchingService.get("/api/v1/stations");
                const data = resp?.data ?? [];
                if (cancelled) return;
                setStations(data);

                // remove any existing markers
                stationMarkersRef.current.forEach((m) => m.remove());

                // create markers using either lat/lng or latitude/longitude fields
                stationMarkersRef.current = data.map((s) => {
                    const lat = Number(s.lat ?? s.latitude ?? 0);
                    const lng = Number(s.lng ?? s.longitude ?? 0);
                    const name = s.name ?? s.address ?? `Station ${s.id ?? ''}`;
                    const m = L.marker([lat, lng]).addTo(leafletRef.current);
                    const popupHtml = `<strong>${name}</strong><br/>Lat: ${lat.toFixed(5)}<br/>Lng: ${lng.toFixed(5)}`;
                    m.bindPopup(popupHtml);
                    // attach reference to station id for later lookup
                    m.stationId = s.id;
                    return m;
                });
                console.log(resp)
            } catch (err) {
                console.error("Error fetching bike stations:", err);
            }
        };

        loadStationsAndRender();

        // cleanup when component unmounts or before next run
        return () => {
            cancelled = true;
            stationMarkersRef.current.forEach((m) => m.remove());
            stationMarkersRef.current = [];
        };
    }, [mapReady]);

    useEffect(() => {
        setLoading(true);
        // initialize map using the `center` state (default is Montreal)
        leafletRef.current = L.map(mapRef.current).setView([center.lat, center.lng], zoom);
        // Ensure the DOM container accepts pointer events
        if (mapRef.current) {
            mapRef.current.style.pointerEvents = 'auto';
            mapRef.current.style.position = 'relative';
            mapRef.current.style.zIndex = '0';
        }

        L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(leafletRef.current);

        // marker
        markerRef.current = L.marker([center.lat, center.lng], { draggable: true }).addTo(leafletRef.current);
        markerRef.current.bindPopup("A pretty CSS popup.<br> Easily customizable.", { maxWidth: 480 }).openPopup();

        markerRef.current.on("dragend", () => {
            const pos = markerRef.current.getLatLng();
            const newCenter = { lat: pos.lat, lng: pos.lng };
            setCenter(newCenter);
            leafletRef.current.panTo([newCenter.lat, newCenter.lng]);
        });

        // Add station markers with popups
        if (mapReady) {
            stationMarkersRef.current.forEach((m) => m.remove());
            stationMarkersRef.current = stations.map((s) => {
                const m = L.marker([s.lat, s.lng]).addTo(leafletRef.current);
                const popupHtml = `<strong>${s.name}</strong><br/>Lat: ${s.lat.toFixed(5)}<br/>Lng: ${s.lng.toFixed(5)}`;
                m.bindPopup(popupHtml);
                return m;
            });
        }
        // Leaflet sometimes needs a resize call when used in flex layouts
        setTimeout(() => {
            try {
                leafletRef.current.invalidateSize();
            } catch (e) {
                // ignore
            }
        }, 200);

        setLoading(false);
        setMapReady(true);

        return () => {
            if (leafletRef.current) {
                // remove station markers
                stationMarkersRef.current.forEach((m) => m.remove());
                stationMarkersRef.current = [];
                setMapReady(false);

                leafletRef.current.remove();
                leafletRef.current = null;
            }
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const goToMyLocation = useCallback(() => {
        if (!navigator.geolocation) return alert("Geolocation not supported");
        navigator.geolocation.getCurrentPosition(
            (pos) => {
                const newCenter = { lat: pos.coords.latitude, lng: pos.coords.longitude };
                setCenter(newCenter);
                setZoom(15);
                if (leafletRef.current) {
                    leafletRef.current.setView([newCenter.lat, newCenter.lng], 15);
                }
                if (markerRef.current) markerRef.current.setLatLng([newCenter.lat, newCenter.lng]);
            },
            () => alert("Unable to retrieve your location")
        );
    }, []);

    // Center map on a given station and open its popup
    const centerOnStation = useCallback((station) => {
        const lat = Number(station.lat ?? station.latitude ?? 0);
        const lng = Number(station.lng ?? station.longitude ?? 0);
        if (!leafletRef.current) return;
        leafletRef.current.setView([lat, lng], 16);
        // find marker for this station and open popup
        const m = stationMarkersRef.current.find((mk) => mk.stationId === station.id);
        if (m) {
            m.openPopup();
        } else {
            // create a temporary marker and open popup
            const tmp = L.marker([lat, lng]).addTo(leafletRef.current);
            const popupHtml = `<strong>${station.name ?? station.address}</strong><br/>Lat: ${lat.toFixed(5)}<br/>Lng: ${lng.toFixed(5)}`;
                    m.bindPopup(popupHtml, { maxWidth: 420 });
            setTimeout(() => tmp.remove(), 5000);
        }
        // open station action menu
        setSelectedStation(station);
        setStationMenuOpen(true);
    }, []);

    const closeStationMenu = () => {
        setStationMenuOpen(false);
        setSelectedStation(null);
    };

    const performStationAction = async (action) => {
        if (!selectedStation) return;
        try {
            // Assumes backend supports POST /api/v1/stations/:id/{action}
            const path = `/api/v1/stations/${selectedStation.id}/${action}`;
            const resp = await FetchingService.post(path);
            // Simple feedback; you can wire this into context/state/UI
            alert(`${action} successful`);
            // optionally refresh stations
            try {
                const r = await FetchingService.get('/api/v1/stations');
                setStations(r.data || []);
            } catch (e) {
                // ignore refresh error
            }
            closeStationMenu();
        } catch (err) {
            console.error(`${action} failed`, err);
            alert(`${action} failed: ${err?.message ?? err}`);
        }
    };

    const zoomIn = useCallback(() => {
        const next = Math.min(21, zoom + 1);
        setZoom(next);
        if (leafletRef.current) leafletRef.current.setZoom(next);
    }, [zoom]);

    const zoomOut = useCallback(() => {
        const next = Math.max(1, zoom - 1);
        setZoom(next);
        if (leafletRef.current) leafletRef.current.setZoom(next);
    }, [zoom]);

    const handleSearch = async () => {
        const q = searchInputRef.current.value;
        if (!q) return;
        // Use Nominatim for simple geocoding
        try {
            const resp = await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(q)}`);
            const data = await resp.json();
            if (data && data.length > 0) {
                const loc = data[0];
                const newCenter = { lat: parseFloat(loc.lat), lng: parseFloat(loc.lon) };
                setCenter(newCenter);
                setZoom(15);
                if (leafletRef.current) {
                    leafletRef.current.setView([newCenter.lat, newCenter.lng], 15);
                }
                if (markerRef.current) markerRef.current.setLatLng([newCenter.lat, newCenter.lng]);
            } else {
                alert("No results found");
            }
        } catch (err) {
            console.error(err);
            alert("Search failed");
        }
    };

    return (
        <div style={styles.root}>
            <header style={styles.header}>
                <h1 style={{ margin: 0, fontSize: 18 }}>Map Dashboard</h1>
            </header>

            <div style={styles.body}>
                <aside style={styles.sidebar}>
                    <div style={styles.panel}>
                        <button style={styles.button} onClick={goToMyLocation}>
                            My Location
                        </button>
                        <div style={{ marginTop: 12 }}>
                            <div style={{ display: 'flex', gap: 8 }}>
                                <input
                                    ref={searchInputRef}
                                    placeholder="Search places..."
                                    style={styles.input}
                                />
                                <button style={styles.button} onClick={handleSearch}>Search</button>
                            </div>
                        </div>
                        <div style={{ marginTop: 12 }}>
                            <h3 style={{ margin: '12px 0 8px', fontSize: 14 }}>Bike Stations</h3>
                            <div style={{ maxHeight: 300, overflowY: 'auto' }}>
                                {stations && stations.length > 0 ? (
                                    stations.map((s) => {
                                        const lat = Number(s.lat ?? s.latitude ?? 0);
                                        const lng = Number(s.lng ?? s.longitude ?? 0);
                                        return (
                                            <div
                                                key={s.id}
                                                onClick={() => centerOnStation(s)}
                                                style={{
                                                    padding: '8px',
                                                    borderRadius: 6,
                                                    cursor: 'pointer',
                                                    background: '#fff',
                                                    marginBottom: 8,
                                                    boxShadow: '0 0 0 1px rgba(0,0,0,0.04)'
                                                }}
                                            >
                                                <div style={{ fontWeight: 600 }}>{s.name ?? s.address ?? 'Unnamed'}</div>
                                                <div style={{ fontSize: 12, color: '#555' }}>{lat.toFixed(5)}, {lng.toFixed(5)}</div>
                                                <div style={{ fontSize: 12, color: '#666' }}>Bikes: {s.numberOfBikesDocked ?? s.bikes?.length ?? 0}</div>
                                            </div>
                                        );
                                    })
                                ) : (
                                    <div style={{ color: '#666', fontSize: 13 }}>No stations</div>
                                )}
                            </div>
                        </div>
                    </div>
                </aside>

                <main style={styles.content}>
                    <div style={styles.mapContainer}>
                        {loading && (
                            <div style={styles.loadingOverlay}>Loading map…</div>
                        )}
                        <div ref={mapRef} style={styles.map} />
                    </div>
                </main>
            </div>

            {/* Station action modal */}
            {stationMenuOpen && selectedStation && (
                <div style={{ position: 'fixed', left: 0, top: 0, right: 0, bottom: 0, zIndex: 9999, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <div onClick={closeStationMenu} style={{ position: 'absolute', left: 0, top: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.35)' }} />
                    <div style={{ position: 'relative', background: '#fff', padding: 20, borderRadius: 12, boxShadow: '0 12px 36px rgba(0,0,0,0.18)', width: 560, maxWidth: 'calc(100% - 48px)' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <div>
                                <div style={{ fontWeight: 800, fontSize: 18 }}>{selectedStation.name ?? selectedStation.address}</div>
                                <div style={{ fontSize: 13, color: '#666', marginTop: 4 }}>ID: {selectedStation.id}</div>
                            </div>
                            <button onClick={closeStationMenu} style={{ background: 'transparent', border: 'none', cursor: 'pointer', fontSize: 18 }}>✕</button>
                        </div>
                        <div style={{ marginTop: 16, display: 'flex', gap: 12, alignItems: 'center' }}>
                            <button style={{ ...styles.button, padding: '10px 18px', fontSize: 15 }} onClick={() => performStationAction('rent')}>Rent</button>
                            <button style={{ ...styles.button, padding: '10px 18px', fontSize: 15 }} onClick={() => performStationAction('reserve')}>Reserve</button>
                            <button style={{ ...styles.button, padding: '10px 18px', fontSize: 15 }} onClick={() => performStationAction('dock')}>Dock</button>
                        </div>
                        <div style={{ marginTop: 14, color: '#444' }}>
                            <div style={{ fontSize: 14 }}>Location</div>
                            <div style={{ fontSize: 13, color: '#666' }}>{(selectedStation.latitude ?? selectedStation.lat ?? selectedStation.longitude ?? selectedStation.lng) ? `${Number(selectedStation.latitude ?? selectedStation.lat ?? 0).toFixed(5)}, ${Number(selectedStation.longitude ?? selectedStation.lng ?? 0).toFixed(5)}` : 'Unknown'}</div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

