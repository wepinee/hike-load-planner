import { ApiError } from '../api/client';
import type { ProblemDetail } from '../api/types';

const FIELD_LABELS: Record<string, string> = {
  email: 'Email',
  displayName: 'Имя',
  password: 'Пароль',
  name: 'Название',
  startDate: 'Дата старта',
  durationDays: 'Длительность',
};

export function todayIsoDate(): string {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

export function isPastDate(isoDate: string): boolean {
  if (!isoDate) {
    return false;
  }
  return isoDate < todayIsoDate();
}

export function validateRegister(email: string, displayName: string, password: string): Record<string, string> {
  const errors: Record<string, string> = {};
  const trimmedEmail = email.trim();
  const trimmedName = displayName.trim();

  if (!trimmedEmail) {
    errors.email = 'Укажите email';
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmedEmail)) {
    errors.email = 'Некорректный email';
  }

  if (!trimmedName) {
    errors.displayName = 'Укажите имя';
  } else if (trimmedName.length < 2) {
    errors.displayName = 'Имя должно быть не короче 2 символов';
  } else if (trimmedName.length > 100) {
    errors.displayName = 'Имя не длиннее 100 символов';
  }

  if (!password) {
    errors.password = 'Укажите пароль';
  } else if (password.length < 4) {
    errors.password = 'Пароль должен быть не короче 4 символов';
  } else if (password.length > 100) {
    errors.password = 'Пароль не длиннее 100 символов';
  }

  return errors;
}

export function validateCreateHike(name: string, startDate: string, durationDays: number): Record<string, string> {
  const errors: Record<string, string> = {};
  const trimmedName = name.trim();

  if (!trimmedName) {
    errors.name = 'Укажите название похода';
  }

  if (!startDate) {
    errors.startDate = 'Укажите дату старта';
  } else if (isPastDate(startDate)) {
    errors.startDate = 'Дата не может быть в прошлом';
  }

  if (!Number.isFinite(durationDays) || durationDays < 1) {
    errors.durationDays = 'Длительность должна быть не меньше 1 дня';
  }

  return errors;
}

function getFieldErrors(body: ProblemDetail): Record<string, string> | undefined {
  if (body.errors && Object.keys(body.errors).length > 0) {
    return body.errors;
  }
  const nested = body.properties?.errors;
  if (nested && Object.keys(nested).length > 0) {
    return nested;
  }
  return undefined;
}

export function formatApiError(err: ApiError): string {
  const fieldErrors = getFieldErrors(err.body);
  if (fieldErrors) {
    return Object.entries(fieldErrors)
      .map(([field, msg]) => {
        const label = FIELD_LABELS[field] ?? field;
        return `${label}: ${msg}`;
      })
      .join('. ');
  }
  return err.message;
}

export function apiFieldErrors(err: ApiError): Record<string, string> {
  return getFieldErrors(err.body) ?? {};
}
