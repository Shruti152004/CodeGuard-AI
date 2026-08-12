import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Shield, LayoutDashboard, GitFork, LogOut, Bell, Wifi, WifiOff } from 'lucide-react';

interface LayoutProps {
  children: React.ReactNode;
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const [wsConnected, setWsConnected] = useState(false);
  const [notifications, setNotifications] = useState<string[]>([]);
  const username = localStorage.getItem('username') || 'Developer';

  useEffect(() => {
    const ws = new WebSocket('ws://localhost:3000');

    ws.onopen = () => {
      setWsConnected(true);
      console.log('Connected to Node-Notification WebSocket server');
    };

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        if (data.message) {
          setNotifications((prev) => [data.message, ...prev.slice(0, 4)]);
        }
      } catch (e) {
        console.error('Error parsing notification event data', e);
      }
    };

    ws.onclose = () => {
      setWsConnected(false);
      console.log('Disconnected from Node-Notification WebSocket server');
    };

    return () => {
      ws.close();
    };
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('username');
    navigate('/login');
  };

  const menuItems = [
    { name: 'Dashboard', path: '/', icon: <LayoutDashboard size={20} /> },
    { name: 'Repositories', path: '/repositories', icon: <GitFork size={20} /> },
  ];

  return (
    <div className="app-layout">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-logo">
          <Shield size={28} color="#66fcf1" />
          <span>CodeGuard <span>AI</span></span>
        </div>
        <nav style={{ flex: 1 }}>
          <ul className="sidebar-menu">
            {menuItems.map((item) => (
              <li
                key={item.name}
                className={`menu-item ${location.pathname === item.path ? 'active' : ''}`}
                onClick={() => navigate(item.path)}
              >
                {item.icon}
                <span>{item.name}</span>
              </li>
            ))}
          </ul>
        </nav>
        <div className="menu-item" onClick={handleLogout} style={{ marginTop: 'auto', color: '#ff4a4a' }}>
          <LogOut size={20} />
          <span>Sign Out</span>
        </div>
      </aside>

      {/* Main Panel */}
      <div className="main-content">
        <header className="main-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
            <h2>Developer Portal</h2>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '13px', color: wsConnected ? '#2ecc71' : '#ff4a4a' }}>
              {wsConnected ? <Wifi size={16} /> : <WifiOff size={16} />}
              <span>{wsConnected ? 'Live Connection Active' : 'Disconnected'}</span>
            </div>
          </div>
          
          <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
            {/* Notifications Popover */}
            <div style={{ position: 'relative', cursor: 'pointer' }}>
              <Bell size={22} style={{ color: notifications.length > 0 ? '#66fcf1' : 'inherit' }} />
              {notifications.length > 0 && (
                <div style={{
                  position: 'absolute',
                  top: '-15px',
                  right: '-10px',
                  width: '320px',
                  background: '#1f2833',
                  border: '1px solid #2e303a',
                  borderRadius: '8px',
                  padding: '12px',
                  boxShadow: '0 4px 12px rgba(0,0,0,0.5)',
                  zIndex: 100
                }}>
                  <h4 style={{ color: '#fff', fontSize: '14px', marginBottom: '8px', borderBottom: '1px solid #2e303a', paddingBottom: '4px' }}>Live Notifications</h4>
                  <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '6px', fontSize: '12px' }}>
                    {notifications.map((note, index) => (
                      <li key={index} style={{ borderBottom: '1px solid #2e303a', paddingBottom: '4px' }}>📢 {note}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
            <div style={{ fontSize: '15px', fontWeight: 600 }}>Welcome, {username}</div>
          </div>
        </header>

        {/* Content View */}
        <main className="page-container">
          {children}
        </main>
      </div>
    </div>
  );
};

export default Layout;
