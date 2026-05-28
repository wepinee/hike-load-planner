import { FormEvent, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { createHike, deleteHike, fetchHikes } from '../api/hikes';
import { ApiError } from '../api/client';
import type { Hike } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { labelHikeStatus } from '../utils/labels';
import { apiFieldErrors, formatApiError, todayIsoDate, validateCreateHike } from '../utils/validation';

export default function HikesPage() {
  const { user } = useAuth();
  const [hikes, setHikes] = useState<Hike[]>([]);
  const [name, setName] = useState('');
  const [startDate, setStartDate] = useState('');
  const [durationDays, setDurationDays] = useState(3);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const minStartDate = todayIsoDate();

  async function load() {
    const page = await fetchHikes();
    setHikes(page.content);
  }

  useEffect(() => {
    load().catch((e: Error) => setError(e.message));
  }, []);

  async function onCreate(e: FormEvent) {
    e.preventDefault();
    setError('');

    const clientErrors = validateCreateHike(name, startDate, durationDays);
    if (Object.keys(clientErrors).length > 0) {
      setFieldErrors(clientErrors);
      setError(Object.values(clientErrors).join('. '));
      return;
    }
    setFieldErrors({});

    try {
      await createHike({ name: name.trim(), startDate, durationDays });
      setName('');
      setStartDate('');
      await load();
    } catch (err) {
      if (err instanceof ApiError) {
        const fromApi = apiFieldErrors(err);
        if (Object.keys(fromApi).length > 0) {
          setFieldErrors(fromApi);
        }
        setError(formatApiError(err));
      } else {
        setError('Ошибка');
      }
    }
  }

  async function onDelete(id: number) {
    if (!confirm('Удалить поход и все данные?')) return;
    try {
      await deleteHike(id);
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? formatApiError(err) : 'Ошибка удаления');
    }
  }

  function invalidClass(field: string) {
    return fieldErrors[field] ? ' form-field--invalid' : '';
  }

  return (
    <>
      <h1 className="page-title">Мои походы</h1>
      <p className="page-subtitle">Создавайте походы, добавляйте участников и распределяйте нагрузку</p>
      {error && <div className="error-box">{error}</div>}

      <div className="card card--flat">
        <h2 className="card-title">Создать поход</h2>
        <form onSubmit={onCreate} className="form-row" noValidate>
          <div className={`form-field form-field--grow-2${invalidClass('name')}`}>
            <label>Название</label>
            <input
              value={name}
              onChange={(e) => {
                setName(e.target.value);
                if (fieldErrors.name) {
                  setFieldErrors((prev) => {
                    const next = { ...prev };
                    delete next.name;
                    return next;
                  });
                }
              }}
              placeholder="Например, Алтай, июль"
            />
            {fieldErrors.name && <span className="field-error">{fieldErrors.name}</span>}
          </div>
          <div className={`form-field form-field--grow-1${invalidClass('startDate')}`}>
            <label>Дата старта</label>
            <input
              type="date"
              value={startDate}
              min={minStartDate}
              onChange={(e) => {
                setStartDate(e.target.value);
                if (fieldErrors.startDate) {
                  setFieldErrors((prev) => {
                    const next = { ...prev };
                    delete next.startDate;
                    return next;
                  });
                }
              }}
            />
            {fieldErrors.startDate && <span className="field-error">{fieldErrors.startDate}</span>}
          </div>
          <div className={`form-field form-field--narrow${invalidClass('durationDays')}`}>
            <label>Дней</label>
            <input
              type="number"
              min={1}
              value={durationDays}
              onChange={(e) => {
                setDurationDays(Number(e.target.value));
                if (fieldErrors.durationDays) {
                  setFieldErrors((prev) => {
                    const next = { ...prev };
                    delete next.durationDays;
                    return next;
                  });
                }
              }}
            />
            {fieldErrors.durationDays && <span className="field-error">{fieldErrors.durationDays}</span>}
          </div>
          <button type="submit" className="btn btn-primary">
            Создать
          </button>
        </form>
      </div>

      <div className="card">
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Название</th>
                <th>Дата</th>
                <th>Дней</th>
                <th>Статус</th>
                <th>Участников</th>
                <th>Снаряжение, кг</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {hikes.map((h) => (
                <tr key={h.id}>
                  <td>
                    <Link to={`/hikes/${h.id}`}>{h.name}</Link>
                  </td>
                  <td>{h.startDate}</td>
                  <td>{h.durationDays}</td>
                  <td>
                    <span className="badge badge--ok">{labelHikeStatus(h.status)}</span>
                  </td>
                  <td>{h.participantCount}</td>
                  <td>{h.totalGearWeightKg.toFixed(1)}</td>
                  <td className="text-end">
                    <Link to={`/hikes/${h.id}`} className="btn btn-outline btn-sm">
                      Открыть
                    </Link>
                    {h.organizerUserId === user?.userId && (
                      <button
                        type="button"
                        className="btn btn-outline btn-sm btn-danger-outline ms-1"
                        onClick={() => onDelete(h.id)}
                      >
                        Удалить
                      </button>
                    )}
                  </td>
                </tr>
              ))}
              {hikes.length === 0 && (
                <tr className="table-empty">
                  <td colSpan={7} className="text-center">
                    Нет походов — создайте первый выше
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </>
  );
}
