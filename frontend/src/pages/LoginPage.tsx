import { FormEvent, useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { ApiError } from '../api/client';

export default function LoginPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('organizer@hike.local');
  const [password, setPassword] = useState('demo');
  const [error, setError] = useState('');

  if (user) {
    return <Navigate to="/" replace />;
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    try {
      await login(email, password);
      navigate('/');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Ошибка входа');
    }
  }

  return (
    <div className="auth-page">
      <div className="card auth-card">
        <h1 className="auth-card__title">Вход</h1>
        <p className="auth-card__subtitle">Планирование нагрузки в походе</p>
        {error && <div className="error-box">{error}</div>}
        <form onSubmit={onSubmit} className="form-stack">
          <div className="form-field">
            <label>Email</label>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </div>
          <div className="form-field">
            <label>Пароль</label>
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </div>
          <button type="submit" className="btn btn-primary btn-block">
            Войти
          </button>
        </form>
        <p className="auth-card__footer">
          <Link to="/register">Создать аккаунт</Link>
        </p>
        <p className="auth-card__hint">Демо: organizer@hike.local / demo</p>
      </div>
    </div>
  );
}
