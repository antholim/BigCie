import React, { useEffect, useState } from 'react';

export default function Toast({ message, duration = 3000, onClose, icon = '🎉' }) {
    const [isVisible, setIsVisible] = useState(true);
    const [isExiting, setIsExiting] = useState(false);

    useEffect(() => {
        const timer = setTimeout(() => {
            setIsExiting(true);
            setTimeout(() => {
                setIsVisible(false);
                onClose?.();
            }, 300); // Match animation duration
        }, duration);

        return () => clearTimeout(timer);
    }, [duration, onClose]);

    if (!isVisible) return null;

    return (
        <div
            style={{
                position: 'fixed',
                top: 20,
                right: 20,
                zIndex: 10000,
                animation: isExiting ? 'slideOut 0.3s ease-out forwards' : 'slideIn 0.3s ease-out',
            }}
        >
            <style>
                {`
                    @keyframes slideIn {
                        from {
                            transform: translateX(400px);
                            opacity: 0;
                        }
                        to {
                            transform: translateX(0);
                            opacity: 1;
                        }
                    }
                    @keyframes slideOut {
                        from {
                            transform: translateX(0);
                            opacity: 1;
                        }
                        to {
                            transform: translateX(400px);
                            opacity: 0;
                        }
                    }
                `}
            </style>
            <div
                style={{
                    background: 'linear-gradient(135deg, #9333ea 0%, #7e22ce 100%)',
                    color: 'white',
                    padding: '16px 24px',
                    borderRadius: 12,
                    boxShadow: '0 10px 40px rgba(147, 51, 234, 0.3), 0 4px 12px rgba(0, 0, 0, 0.2)',
                    display: 'flex',
                    alignItems: 'center',
                    gap: 12,
                    minWidth: 300,
                    maxWidth: 400,
                }}
            >
                <div style={{ fontSize: 28 }}>{icon}</div>
                <div style={{ flex: 1, fontSize: 15, fontWeight: 500 }}>{message}</div>
            </div>
        </div>
    );
}
