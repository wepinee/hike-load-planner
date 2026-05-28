import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { api, setStoredToken, getStoredToken } from '../api/client';
import type { AuthResponse } from '../api/types';

type AuthState = {
  user: AuthResponse | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, displayName: string) => Promise<void>;
  logout: () => void;
  refreshUser: () => Promise<void>;
};

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const refreshUser = useCallback(async () => {
    const token = getStoredToken();
    if (!token) {
      setUser(null);
      return;
    }
    try {
      const me = await api<AuthResponse>('/auth/me');
      setUser(me);
    } catch {
      setStoredToken(null);
      setUser(null);
    }
  }, []);

  useEffect(() => {
    refreshUser().finally(() => setLoading(false));
  }, [refreshUser]);

  const login = useCallback(async (email: string, password: string) => {
    const res = await api<AuthResponse>(
      '/auth/login',
      { method: 'POST', body: JSON.stringify({ email, password }) },
      false,
    );
    if (!res.accessToken) {
      throw new Error('Сервер не вернул токен');
    }
    setStoredToken(res.accessToken);
    setUser(res);
  }, []);

  const register = useCallback(async (email: string, password: string, displayName: string) => {
    const res = await api<AuthResponse>(
      '/auth/register',
      { method: 'POST', body: JSON.stringify({ email, password, displayName }) },
      false,
    );
    if (!res.accessToken) {
      throw new Error('Сервер не вернул токен');
    }
    setStoredToken(res.accessToken);
    setUser(res);
  }, []);

  const logout = useCallback(() => {
    api('/auth/logout', { method: 'POST' }).catch(() => undefined);
    setStoredToken(null);
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({ user, loading, login, register, logout, refreshUser }),
    [user, loading, login, register, logout, refreshUser],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth вне AuthProvider');
  }
  return ctx;
}
