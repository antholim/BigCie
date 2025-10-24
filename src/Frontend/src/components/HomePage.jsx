import React, { useEffect, useState } from "react";
import "./home.css";
import { useNavigate } from "react-router-dom";
import MapPreview from "./MapPreview";
import FetchingService from "../services/FetchingService";
import { useAuth } from "../contexts/AuthContext";

export default function DowntownBikeLanding() {
  const navigate = useNavigate();
  const { user, isAuthenticated, loading: authLoading } = useAuth();
  const [startingRide, setStartingRide] = useState(false);
  const [returningRide, setReturningRide] = useState(false);
  const [activeRideIds, setActiveRideIds] = useState(() => {
    if (typeof window === "undefined") return [];
    try {
      const stored = sessionStorage.getItem("activeRideIds");
      return stored ? JSON.parse(stored) : [];
    } catch (err) {
      return [];
    }
  });
  const [lastMessage, setLastMessage] = useState(null);

  useEffect(() => {
    if (typeof window !== "undefined") {
      sessionStorage.setItem("activeRideIds", JSON.stringify(activeRideIds));
    }
  }, [activeRideIds]);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      setActiveRideIds([]);
      setLastMessage(null);
      if (typeof window !== "undefined") {
        sessionStorage.removeItem("activeRideIds");
      }
    }
  }, [authLoading, isAuthenticated]);

  const requireAuth = () => {
    if (!isAuthenticated) {
      navigate("/login");
      return false;
    }
    return true;
  };

  const handleStartRide = async () => {
    if (!requireAuth()) return;
    if (startingRide) return;
    setStartingRide(true);
    try {
      const plan = "Pay-As-You-Go";
      const response = await FetchingService.post("/api/v1/rides/start", { plan });
      const rideData = response?.data ?? {};
      const rideId = rideData.rideId
        ?? (typeof crypto !== "undefined" && crypto.randomUUID
          ? crypto.randomUUID()
          : `ride-${Date.now()}`);
      setActiveRideIds((prev) => [...prev, rideId]);
      setLastMessage(rideData.message ?? "Ride can begin");
      navigate("/ride-confirmation", {
        state: {
          plan,
          rideId,
          message: rideData.message,
          startedAt: rideData.startedAt,
        },
      });
    } catch (err) {
      console.error("Failed to start ride", err);
      alert("Unable to start ride right now. Please try again in a moment.");
    } finally {
      setStartingRide(false);
    }
  };

  const handleReturnRide = async () => {
    if (!requireAuth()) return;
    if (returningRide || activeRideIds.length === 0) return;
    setReturningRide(true);
    try {
      const rideId = activeRideIds[activeRideIds.length - 1];
      await FetchingService.post("/api/v1/rides/return", { rideId });
      setActiveRideIds((prev) => prev.slice(0, -1));
      setLastMessage("Bike successfully returned");
    } catch (err) {
      console.error("Failed to return bike", err);
      alert("Unable to return bike right now. Please try again in a moment.");
    } finally {
      setReturningRide(false);
    }
  };

  return (
    <div className="db-page">
      {/* Top bar */}
      <div className="db-topbar">
        <div className="db-container db-flex-between">
          <p>🚲 Fall promo: 15% off monthly passes this week.</p>
          <a href="#pricing">See pricing</a>
        </div>
      </div>

      {/* Header */}
      <header className="db-header">
        <div className="db-container db-flex-between">
          <div className="db-brand">
            <div className="db-logo">🚲</div>
            <span>DowntownBike</span>
          </div>
          <nav className="db-nav">
            <a href="#how">How it works</a>
            <a href="#pricing">Pricing</a>
            <a href="#map">Stations</a>
            <a href="#faq">FAQ</a>
          </nav>
          <div className="db-actions">
            <button className="db-btn primary" onClick={() => navigate("/login")}>Log in</button>
            <button className="db-btn" onClick={() => navigate("/coming-soon")}>Get the app</button>
          </div>
        </div>
      </header>

      {/* Hero */}
      <section className="db-hero">
        <div className="db-container">
          <div>
            <h1>Ride the downtown core, <span className="accent">your way</span>.</h1>
            <p className="lede">
              Unlock a bike in seconds, beat the traffic, and park steps from your destination.
              Pay per ride or save with flexible passes.
            </p>
            <div className="db-auth-panel">
              {authLoading ? (
                <p className="db-muted">Checking your account status…</p>
              ) : isAuthenticated ? (
                <>
                  <p className="db-muted">
                    Welcome back{user?.username ? `, ${user.username}` : ""}! You currently have
                    <strong> {activeRideIds.length} </strong>
                    {activeRideIds.length === 1 ? "bike" : "bikes"} out.
                  </p>
                  {lastMessage && <p className="db-note">{lastMessage}</p>}
                  <div className="db-row" style={{ gap: 12, flexWrap: "wrap" }}>
                    <button
                      className="db-btn primary"
                      onClick={handleStartRide}
                      disabled={startingRide}
                      style={{ opacity: startingRide ? 0.7 : 1 }}
                    >
                      {startingRide ? "Booking…" : "Book trip"}
                    </button>
                    <button
                      className="db-btn"
                      onClick={handleReturnRide}
                      disabled={returningRide || activeRideIds.length === 0}
                      style={{
                        opacity: returningRide || activeRideIds.length === 0 ? 0.6 : 1,
                        cursor: returningRide || activeRideIds.length === 0 ? "not-allowed" : "pointer",
                      }}
                    >
                      {returningRide ? "Processing…" : "Return bike"}
                    </button>
                  </div>
                </>
              ) : (
                <p className="db-muted">
                  Log in to start a ride or manage your trips.
                </p>
              )}
            </div>
            <div className="db-row">
              <a href="#pricing" className="db-btn primary">See pricing</a>
              <a href="#how" className="db-btn">How it works</a>
            </div>
            <ul className="db-bullets">
              <li><span className="dot" />24/7 access</li>
              <li><span className="dot" />100+ stations</li>
              <li><span className="dot" />Tap to unlock</li>
            </ul>
          </div>
        </div>
      </section>

      {/* How it works */}
      <section id="how" className="db-section">
        <div className="db-container">
          <h2>How it works</h2>
          <div className="db-cards-3">
            <StepCard step="1" title="Find & unlock" desc="Use the app to find a nearby station and scan to unlock." />
            <StepCard step="2" title="Ride" desc="Cruise the bike lanes. E-bikes give you an extra boost uphill." />
            <StepCard step="3" title="Park & lock" desc="Dock at any station. End your trip in the app." />
          </div>
        </div>
      </section>

      {/* Pricing */}
      <section id="pricing" className="db-section muted">
        <div className="db-container">
          <div className="db-flex-between db-align-end">
            <h2>Flexible pricing</h2>
            <span className="db-muted">Taxes may apply • E-bike fees may differ</span>
          </div>
          <div className="db-cards-3">
            <PriceCard
              name="Pay-As-You-Go"
              price="$1.25 / 5 min"
              bullets={["Best for quick hops","No commitment","Available 24/7"]}
              cta={startingRide ? "Starting..." : "Start a ride"}
              onAction={handleStartRide}
              disabled={startingRide}
            />
            <PriceCard
              name="Day Pass"
              price="$7.99 / day"
              bullets={["Unlimited 30-min rides","Perfect for tourists","Includes classic bikes"]}
              cta="Buy day pass"
              featured
              onAction={() => navigate("/coming-soon")}
            />
            <PriceCard
              name="Monthly"
              price="$19 / month"
              bullets={["Commute & save","60-min classic rides","Member rewards"]}
              cta="Become a member"
              onAction={() => navigate("/coming-soon")}
            />
          </div>
          <p className="db-muted small">* Example prices for demo purposes. Replace with your real fares and terms.</p>
        </div>
      </section>

      {/* Map teaser */}
      <section id="map" className="db-section">
        <div className="db-container db-grid-2">
          <div>
            <h2>Stations across the core</h2>
            <p className="db-muted">
              Find a bike near you and dock at any station in the network. Integrates with Google or Mapbox — add your API key and swap in a live map.
            </p>
            <ul className="db-bullets">
              <li><span className="dot" />Real-time availability</li>
              <li><span className="dot" />E-bike friendly docks</li>
              <li><span className="dot" />Accessible stations</li>
            </ul>
            <div className="db-row">
              <button className="db-btn primary" onClick={() => navigate("/coming-soon")}>Get the app</button>
              <button className="db-btn" onClick={() => navigate("/map")}>View full map</button>
            </div>
          </div>
          <div className="db-map-placeholder">
            <MapPreview />
          </div>
        </div>
      </section>

      {/* FAQ */}
      <section id="faq" className="db-section muted">
        <div className="db-container">
          <h2>FAQ</h2>
          <div className="db-accordion">
            {[
              { q: "Do I need the app to ride?", a: "Yes. The app lets you find bikes, unlock, and end trips. A kiosk flow can be added for tourists."},
              { q: "What about e-bikes?", a: "E-bikes provide a pedal-assist boost. You can allow e-bike upgrades with per-minute pricing or a small unlock fee."},
              { q: "Where can I park?", a: "Dock at any station in the service area. Consider adding a geofenced ‘end ride’ policy in your app."},
              { q: "Are helmets required?", a: "Follow local regulations. Surface safety tips, helmet guidance, and bike-lane maps in your app."},
            ].map((item, i) => (
              <details key={i} className="db-details">
                <summary>{item.q}</summary>
                <div className="db-details-body">{item.a}</div>
              </details>
            ))}
          </div>
        </div>
      </section>

      {/* Download */}
      <section id="download" className="db-section">
        <div className="db-container">
          <div className="db-download">
            <div>
              <h3>Start riding in minutes</h3>
              <p className="db-muted">Create an account, add a card, and scan to unlock. That’s it.</p>
              <div className="db-row">
                <a className="db-store" href="#">App Store</a>
                <a className="db-store" href="#">Google Play</a>
              </div>
            </div>
            <div className="db-phones">
              <div className="db-phone"><div>Unlock<br/><span>Scan & go</span></div></div>
              <div className="db-phone"><div>Ride<br/><span>Live timer</span></div></div>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="db-footer">
        <div className="db-container db-footer-grid">
          <div>
            <div className="db-brand">
              <div className="db-logo">🚲</div>
              <span>DowntownBike</span>
            </div>
            <p className="db-muted">
              A demo landing page for a city bike rental service built with React.
            </p>
          </div>
          <div>
            <h4>Company</h4>
            <ul>
              <li><a href="#">About</a></li>
              <li><a href="#">Careers</a></li>
              <li><a href="#">Press</a></li>
            </ul>
          </div>
          <div>
            <h4>Riding</h4>
            <ul>
              <li><a href="#how">How it works</a></li>
              <li><a href="#pricing">Pricing</a></li>
              <li><a href="#map">Service area</a></li>
            </ul>
          </div>
          <div>
            <h4>Support</h4>
            <ul>
              <li><a href="#faq">Help & FAQ</a></li>
              <li><a href="#">Safety</a></li>
              <li><a href="#">Terms & Privacy</a></li>
            </ul>
          </div>
        </div>
        <div className="db-footer-bottom">© {new Date().getFullYear()} DowntownBike. Demo content.</div>
      </footer>
    </div>
  );
}

function StepCard({ step, title, desc }) {
  return (
    <div className="db-card">
      <div className="db-step">
        <div className="db-step-num">{step}</div>
        <h3>{title}</h3>
      </div>
      <p className="db-muted">{desc}</p>
    </div>
  );
}

function PriceCard({ name, price, bullets, cta, featured, onAction = () => {}, disabled = false }) {
  return (
    <div className={`db-card ${featured ? "featured" : ""}`}>
      {featured && <div className="db-badge">Popular</div>}
      <h3>{name}</h3>
      <p className="db-price">{price}</p>
      <ul className="db-list">
        {bullets.map((b, i) => <li key={i}><span className="dot" />{b}</li>)}
      </ul>
      <button
        className="db-btn primary full"
        onClick={onAction}
        disabled={disabled}
        style={{ opacity: disabled ? 0.7 : 1, cursor: disabled ? "not-allowed" : "pointer" }}
      >
        {cta}
      </button>
    </div>
  );
}
