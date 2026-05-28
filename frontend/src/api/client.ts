import type { ProblemDetail } from './types';

const TOKEN_KEY = 'hike_access_token';

export function getStoredToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setStoredToken(token: string | null): void {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}

export class ApiError extends Error {
  status: number;
  body: ProblemDetail;

  constructor(status: number, body: ProblemDetail) {
    super(body.detail ?? body.title ?? 'Ошибка API');
    this.status = status;
    this.body = body;
  }
}

export async function api<T>(
  path: string,
  options: RequestInit = {},
  auth = true,
): Promise<T> {
  const headers = new Headers(options.headers);
  if (!headers.has('Content-Type') && options.body) {
    headers.set('Content-Type', 'application/json');
  }
  if (auth) {
    const token = getStoredToken();
    if (token) {
      headers.set('Authorization', `Bearer ${token}`);
    }
  }

  const response = await fetch(`/api${path}`, { ...options, headers });

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  const data = text ? (JSON.parse(text) as unknown) : null;

  if (!response.ok) {
    throw new ApiError(response.status, (data as ProblemDetail) ?? { detail: response.statusText });
  }

  return data as T;
}

/** Скачивание файла (PDF и т.д.) с авторизацией */
export async function fetchBinary(path: string): Promise<Blob> {
  const headers = new Headers();
  const token = getStoredToken();
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`/api${path}`, { headers });

  if (!response.ok) {
    const text = await response.text();
    let data: ProblemDetail | null = null;
    try {
      data = text ? (JSON.parse(text) as ProblemDetail) : null;
    } catch {
      data = { detail: response.statusText };
    }
    throw new ApiError(response.status, data ?? { detail: response.statusText });
  }

  return response.blob();
}

export function saveBlob(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}
