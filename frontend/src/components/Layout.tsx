import { Outlet, Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function Layout() {
  const { user, logout } = useAuth();

  return (
    <>
      <nav className="navbar">
        <Link to="/" className="navbar__brand">
          Раскладка в походе
        </Link>
        <div className="navbar__actions">
          <span className="navbar__user">{user?.displayName}</span>
          <button type="button" className="btn btn-sm btn-ghost-light" onClick={logout}>
            Выйти
          </button>
        </div>
      </nav>
      <main className="container">
        <Outlet />
      </main>
    </>
  );
}
