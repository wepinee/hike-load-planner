import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { deleteHike, fetchHike } from '../api/hikes';
import { ApiError } from '../api/client';
import type { Hike } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import ParticipantsTab from '../components/hike/ParticipantsTab';
import GearTab from '../components/hike/GearTab';
import FoodTab from '../components/hike/FoodTab';
import LoadPlanTab from '../components/hike/LoadPlanTab';
import MyLoadTab from '../components/hike/MyLoadTab';
import { labelHikeStatus } from '../utils/labels';

const TABS = [
  { id: 'participants', label: 'Участники' },
  { id: 'gear', label: 'Снаряжение' },
  { id: 'food', label: 'Питание' },
  { id: 'loadplan', label: 'Раскладка' },
  { id: 'my', label: 'Моя раскладка' },
] as const;

export type TabId = (typeof TABS)[number]['id'];

export default function HikeDetailPage() {
  const { id } = useParams();
  const hikeId = Number(id);
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const { user } = useAuth();
  const [hike, setHike] = useState<Hike | null>(null);
  const [error, setError] = useState('');
  const tab = (searchParams.get('tab') as TabId) || 'gear';

  const isOrganizer = hike != null && user != null && hike.organizerUserId === user.userId;

  const reloadHike = useCallback(async () => {
    const data = await fetchHike(hikeId);
    setHike(data);
  }, [hikeId]);

  useEffect(() => {
    if (!hikeId || Number.isNaN(hikeId)) {
      setError('Некорректный id похода');
      return;
    }
    reloadHike().catch((e: Error) => setError(e.message));
  }, [hikeId, reloadHike]);

  function setTab(next: TabId) {
    setSearchParams({ tab: next });
  }

  async function onDeleteHike() {
    if (!confirm('Удалить поход и все данные?')) return;
    try {
      await deleteHike(hikeId);
      navigate('/');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Ошибка удаления');
    }
  }

  if (error && !hike) {
    return (
      <div>
        <p className="error">{error}</p>
        <Link to="/">К списку</Link>
      </div>
    );
  }

  if (!hike) {
    return <div className="loading">Загрузка похода…</div>;
  }

  return (
    <>
      <Link to="/" className="back-link">
        &larr; К списку походов
      </Link>

      <div className="page-header">
        <div>
          <h1 className="page-title">{hike.name}</h1>
          <div className="meta-row">
            <span className="meta-chip">
              Старт: <strong>{hike.startDate}</strong>
            </span>
            <span className="meta-chip">
              Длительность: <strong>{hike.durationDays} дн.</strong>
            </span>
            <span className="meta-chip">
              Статус: <strong>{labelHikeStatus(hike.status)}</strong>
            </span>
          </div>
        </div>
        {isOrganizer && (
          <button type="button" className="btn btn-outline btn-danger-outline" onClick={onDeleteHike}>
            Удалить поход
          </button>
        )}
      </div>

      {error && <div className="error-box">{error}</div>}

      <nav className="tabs">
        {TABS.map((t) => (
          <button
            key={t.id}
            type="button"
            className={`tabs__item${tab === t.id ? ' tabs__item--active' : ''}`}
            onClick={() => setTab(t.id)}
          >
            {t.label}
          </button>
        ))}
      </nav>

      {tab === 'participants' && (
        <ParticipantsTab hikeId={hikeId} isOrganizer={isOrganizer} onError={setError} />
      )}
      {tab === 'gear' && (
        <GearTab hikeId={hikeId} isOrganizer={isOrganizer} onError={setError} onHikeChange={reloadHike} />
      )}
      {tab === 'food' && <FoodTab hikeId={hikeId} isOrganizer={isOrganizer} onError={setError} />}
      {tab === 'loadplan' && (
        <LoadPlanTab hikeId={hikeId} isOrganizer={isOrganizer} onError={setError} />
      )}
      {tab === 'my' && <MyLoadTab hikeId={hikeId} onError={setError} />}
    </>
  );
}
