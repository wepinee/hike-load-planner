import { FormEvent, useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { ApiError } from '../api/client';
import { apiFieldErrors, formatApiError, validateRegister } from '../utils/validation';

export default function RegisterPage() {
  const { user, register } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  if (user) {
    return <Navigate to="/" replace />;
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');

    const clientErrors = validateRegister(email, displayName, password);
    if (Object.keys(clientErrors).length > 0) {
      setFieldErrors(clientErrors);
      return;
    }
    setFieldErrors({});

    try {
      await register(email.trim(), password, displayName.trim());
      navigate('/');
    } catch (err) {
      if (err instanceof ApiError) {
        const fromApi = apiFieldErrors(err);
        if (Object.keys(fromApi).length > 0) {
          setFieldErrors(fromApi);
        }
        setError(formatApiError(err));
      } else {
        setError('Ошибка регистрации');
      }
    }
  }

  function fieldClass(field: string) {
    return `form-field${fieldErrors[field] ? ' form-field--invalid' : ''}`;
  }

  return (
    <div className="auth-page">
      <div className="card auth-card">
        <h1 className="auth-card__title">Регистрация</h1>
        <p className="auth-card__subtitle">Новый аккаунт организатора или участника</p>
        {error && <div className="error-box">{error}</div>}
        <form onSubmit={onSubmit} className="form-stack" noValidate>
          <div className={fieldClass('email')}>
            <label>Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => {
                setEmail(e.target.value);
                if (fieldErrors.email) {
                  setFieldErrors((prev) => {
                    const next = { ...prev };
                    delete next.email;
                    return next;
                  });
                }
              }}
              autoComplete="email"
            />
            {fieldErrors.email && <span className="field-error">{fieldErrors.email}</span>}
          </div>
          <div className={fieldClass('displayName')}>
            <label>Имя</label>
            <input
              value={displayName}
              onChange={(e) => {
                setDisplayName(e.target.value);
                if (fieldErrors.displayName) {
                  setFieldErrors((prev) => {
                    const next = { ...prev };
                    delete next.displayName;
                    return next;
                  });
                }
              }}
              minLength={2}
              maxLength={100}
              autoComplete="name"
            />
            {fieldErrors.displayName && <span className="field-error">{fieldErrors.displayName}</span>}
          </div>
          <div className={fieldClass('password')}>
            <label>Пароль</label>
            <input
              type="password"
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                if (fieldErrors.password) {
                  setFieldErrors((prev) => {
                    const next = { ...prev };
                    delete next.password;
                    return next;
                  });
                }
              }}
              minLength={4}
              maxLength={100}
              autoComplete="new-password"
            />
            {fieldErrors.password && <span className="field-error">{fieldErrors.password}</span>}
          </div>
          <button type="submit" className="btn btn-primary btn-block">
            Зарегистрироваться
          </button>
        </form>
        <p className="auth-card__footer">
          <Link to="/login">Уже есть аккаунт</Link>
        </p>
      </div>
    </div>
  );
}
