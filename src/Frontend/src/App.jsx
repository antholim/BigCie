import './App.css';
import HomePage from "./components/HomePage";
import LoginPage from "./pages/Login/LoginPage";
import RegisterPage from "./pages/Register/RegisterPage";
import MapDashboard from './pages/MapDashboard/MapDashboard';
import ComingSoon from "./pages/ComingSoon";
import RideConfirmation from "./pages/RideConfirmation";
import OperatorDashboard from './pages/OperatorDashboard/OperatorDashboard';
import ProfilePage from "./pages/Profile/ProfilePage";
import TripsPage from "./pages/Trips/TripsPage";
import PaymentPage from "./pages/Payment/PaymentPage";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/map" element={<MapDashboard />} />
        <Route path="/coming-soon" element={<ComingSoon />} />
        <Route path="/ride-confirmation" element={<RideConfirmation />} />
        <Route path="/operator-dashboard" element={<OperatorDashboard />} />
        <Route path="/trips" element={<TripsPage />} />
        <Route path="/payment" element={<PaymentPage />} />
      </Routes>
    </Router>
  );
}

export default App;
