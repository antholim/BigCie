import { useNavigate } from "react-router-dom";
import "./sidebar.css";

export default function SideBar({ username, email }) {
    const navigate = useNavigate();
    return (
        <nav className="db-sidebar" aria-label="Profile navigation">
            <div className="db-sidebar-user">
                <strong className="db-sidebar-username">{username || "rider"}</strong>
                <span className="db-muted db-sidebar-email">{email || ""}</span>
            </div>

            <ul className="db-sidebar-list">
                <li>
                    <button
                        type="button"
                        className="db-btn db-sidebar-btn"
                        onClick={() => navigate("/profile")}
                    >
                        Profile
                    </button>
                </li>

                <li>
                    <button
                        type="button"
                        className="db-btn db-sidebar-btn"
                        onClick={() => navigate("/trips")}
                    >
                        Trips
                    </button>
                </li>
            </ul>
        </nav>
    );
}