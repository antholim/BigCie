import './App.css';
import HomePage from "./components/HomePage";
import LoginPage from "./pages/Login/LoginPage";
import RegisterPage from "./pages/Register/RegisterPage";
import MapDashboard from './pages/MapDashboard/MapDashboard';
import OperatorDashboard from './pages/OperatorDashboard/OperatorDashboard';
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/map" element={<MapDashboard />} />
        <Route path="/operator-dashboard" element={<OperatorDashboard />} />
      </Routes>
    </Router>
  );
}

export default App;
