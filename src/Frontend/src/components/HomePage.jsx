import React from "react";
import "./home.css";

export default function DowntownBikeLanding() {
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
            <button className="db-btn ghost">Log in</button>
            <a className="db-btn primary" href="#download">Get the app</a>
          </div>
        </div>
      </header>

      {/* Hero */}
      <section className="db-hero">
        <div className="db-container db-grid-2">
          <div>
            <h1>Ride the downtown core, <span className="accent">your way</span>.</h1>
            <p className="lede">
              Unlock a bike in seconds, beat the traffic, and park steps from your destination.
              Pay per ride or save with flexible passes.
            </p>
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

          <div>
            <div className="db-card glow">
              <div className="db-card-head">
                <h3>Quicktrip Estimator</h3>
                <p>Estimate a short ride and pick a plan.</p>
              </div>
              <div className="db-card-body">
                <label className="db-field">
                  <span>Start</span>
                  <input placeholder="e.g., Central Station" />
                </label>
                <label className="db-field">
                  <span>Destination</span>
                  <input placeholder="e.g., Old Port" />
                </label>
                <div className="db-grid-2 gap">
                  <label className="db-field">
                    <span>Minutes</span>
                    <input type="number" placeholder="12" />
                  </label>
                  <label className="db-field">
                    <span>Distance (km)</span>
                    <input type="number" placeholder="3.2" />
                  </label>
                </div>
                <button className="db-btn primary full">Estimate</button>

                <div className="db-suggest">
                  <div className="db-suggest-top">
                    <span>Suggested</span>
                    <span className="tag">best value</span>
                  </div>
                  <p className="db-suggest-title">Day Pass • $7.99</p>
                  <p className="db-muted">Unlimited 30-min rides, 24 hours.</p>
                </div>
              </div>
            </div>
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
            <PriceCard name="Pay-As-You-Go" price="$1.25 / 5 min" bullets={["Best for quick hops","No commitment","Available 24/7"]} cta="Start a ride" />
            <PriceCard name="Day Pass" price="$7.99 / day" bullets={["Unlimited 30-min rides","Perfect for tourists","Includes classic bikes"]} cta="Buy day pass" featured />
            <PriceCard name="Monthly" price="$19 / month" bullets={["Commute & save","60-min classic rides","Member rewards"]} cta="Become a member" />
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
              <a href="#download" className="db-btn primary">Get the app</a>
              <a href="#" className="db-btn">View full map</a>
            </div>
          </div>
          <div className="db-map-placeholder">Map placeholder</div>
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

function PriceCard({ name, price, bullets, cta, featured }) {
  return (
    <div className={`db-card ${featured ? "featured" : ""}`}>
      {featured && <div className="db-badge">Popular</div>}
      <h3>{name}</h3>
      <p className="db-price">{price}</p>
      <ul className="db-list">
        {bullets.map((b, i) => <li key={i}><span className="dot" />{b}</li>)}
      </ul>
      <button className="db-btn primary full">{cta}</button>
    </div>
  );
}
