

import './App.css';
import HomePage from "./components/HomePage";
import LoginPage from "./pages/Login/LoginPage";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

function App() {
  return (
    <Router>
      <Routes>
  <Route path="/" element={<HomePage />} />
  <Route path="/login" element={<LoginPage />} />
      </Routes>
    </Router>
  );
}

export default App;
