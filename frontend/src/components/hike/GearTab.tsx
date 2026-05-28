import { FormEvent, useEffect, useMemo, useState } from 'react';
import {
  addPersonalGear,
  addSharedGear,
  copyGearFromHike,
  deleteGear,
  fetchCopySources,
  fetchGear,
  fetchParticipants,
  updateGear,
} from '../../api/hikes';
import { ApiError } from '../../api/client';
import type { GearItem, Hike } from '../../api/types';
import { useAuth } from '../../auth/AuthContext';
import { labelItemType } from '../../utils/labels';

type Props = {
  hikeId: number;
  isOrganizer: boolean;
  onError: (msg: string) => void;
  onHikeChange: () => void;
};

function canEditGear(item: GearItem, isOrganizer: boolean, myParticipantId: number | null): boolean {
  if (isOrganizer) {
    return true;
  }
  return item.type === 'PERSONAL' && item.ownerParticipantId === myParticipantId;
}

export default function GearTab({ hikeId, isOrganizer, onError, onHikeChange }: Props) {
  const { user } = useAuth();
  const [gear, setGear] = useState<GearItem[]>([]);
  const [sources, setSources] = useState<Hike[]>([]);
  const [sourceId, setSourceId] = useState('');
  const [newName, setNewName] = useState('');
  const [newWeight, setNewWeight] = useState('');
  const [myParticipantId, setMyParticipantId] = useState<number | null>(null);

  const myGear = useMemo(
    () => gear.filter((g) => g.type === 'PERSONAL' && g.ownerParticipantId === myParticipantId),
    [gear, myParticipantId],
  );

  async function load() {
    setGear(await fetchGear(hikeId));
    const participants = await fetchParticipants(hikeId);
    const email = user?.email?.trim().toLowerCase();
    const me = email
      ? participants.find((p) => p.email?.trim().toLowerCase() === email)
      : undefined;
    setMyParticipantId(me?.id ?? null);

    if (isOrganizer) {
      const page = await fetchCopySources(hikeId);
      setSources(page.content);
    }
  }

  useEffect(() => {
    load().catch((e: Error) => onError(e.message));
  }, [hikeId, isOrganizer, user?.email]);

  async function onAddShared(e: FormEvent) {
    e.preventDefault();
    onError('');
    try {
      await addSharedGear(hikeId, newName.trim(), parseFloat(newWeight));
      setNewName('');
      setNewWeight('');
      await load();
      onHikeChange();
    } catch (err) {
      onError(err instanceof ApiError ? err.message : 'Ошибка');
    }
  }

  async function onAddPersonal(e: FormEvent) {
    e.preventDefault();
    onError('');
    try {
      await addPersonalGear(hikeId, newName.trim(), parseFloat(newWeight), myParticipantId);
      setNewName('');
      setNewWeight('');
      await load();
      onHikeChange();
    } catch (err) {
      onError(err instanceof ApiError ? err.message : 'Ошибка');
    }
  }

  async function onCopy(e: FormEvent) {
    e.preventDefault();
    if (!sourceId) return;
    try {
      await copyGearFromHike(hikeId, Number(sourceId));
      await load();
      onHikeChange();
    } catch (err) {
      onError(err instanceof ApiError ? err.message : 'Ошибка копирования');
    }
  }

  async function onSave(item: GearItem, name: string, weightKg: number) {
    try {
      await updateGear(hikeId, item.id, name, weightKg);
      await load();
      onHikeChange();
    } catch (err) {
      onError(err instanceof ApiError ? err.message : 'Ошибка');
    }
  }

  async function onDelete(gearId: number) {
    if (!confirm('Удалить предмет?')) return;
    try {
      await deleteGear(hikeId, gearId);
      await load();
      onHikeChange();
    } catch (err) {
      onError(err instanceof ApiError ? err.message : 'Ошибка');
    }
  }

  const showActionsColumn = isOrganizer || myParticipantId != null;

  return (
    <>
      {isOrganizer && (
        <>
          <div className="card mb-3">
            <h2 className="card-title">Добавить общее снаряжение</h2>
            <form onSubmit={onAddShared} className="form-row">
              <div className="form-field form-field--grow-2">
                <label>Название</label>
                <input value={newName} onChange={(e) => setNewName(e.target.value)} required />
              </div>
              <div className="form-field form-field--grow-1">
                <label>Вес, кг</label>
                <input
                  type="number"
                  step={0.01}
                  min={0.01}
                  value={newWeight}
                  onChange={(e) => setNewWeight(e.target.value)}
                  required
                />
              </div>
              <button type="submit" className="btn btn-primary">
                Добавить
              </button>
            </form>
          </div>

          <div className="card mb-3">
            <h2 className="card-title">Скопировать из прошлого похода</h2>
            <p className="text-muted mb-3" style={{ marginTop: 0, fontSize: '0.9rem' }}>
              Копируются название и вес. Личные вещи остаются личными, если тот же участник есть в этом походе (по email); иначе
              становятся общими.
            </p>
            <form onSubmit={onCopy} className="form-row">
              <div className="form-field form-field--grow-2">
                <select value={sourceId} onChange={(e) => setSourceId(e.target.value)} required>
                  <option value="">Выберите поход</option>
                  {sources.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.name} ({s.startDate})
                    </option>
                  ))}
                </select>
              </div>
              <button type="submit" className="btn btn-outline">
                Копировать
              </button>
            </form>
          </div>
        </>
      )}

      {!isOrganizer && myParticipantId != null && (
        <div className="card mb-3">
          <h2 className="card-title">Добавить личное снаряжение</h2>
          <p className="text-muted mb-3" style={{ marginTop: 0, fontSize: '0.9rem' }}>
            Укажите вещи, которые несёте только вы (спальник, коврик и т.д.). Общее снаряжение добавляет организатор.
          </p>
          <form onSubmit={onAddPersonal} className="form-row">
            <div className="form-field form-field--grow-2">
              <label>Название</label>
              <input value={newName} onChange={(e) => setNewName(e.target.value)} required placeholder="Спальник" />
            </div>
            <div className="form-field form-field--grow-1">
              <label>Вес, кг</label>
              <input
                type="number"
                step={0.01}
                min={0.01}
                value={newWeight}
                onChange={(e) => setNewWeight(e.target.value)}
                required
              />
            </div>
            <button type="submit" className="btn btn-primary">
              Добавить
            </button>
          </form>
        </div>
      )}

      {!isOrganizer && myParticipantId == null && (
        <div className="alert mb-3">
          Чтобы добавлять личное снаряжение, организатор должен указать в списке участников ваш email (
          {user?.email ?? 'как в аккаунте'}), совпадающий с email входа.
        </div>
      )}

      {!isOrganizer && myGear.length > 0 && (
        <div className="card mb-3 card--flat">
          <h2 className="card-title">Моё личное снаряжение</h2>
          <p className="text-muted" style={{ marginTop: 0, fontSize: '0.9rem' }}>
            {myGear.length} предм., {myGear.reduce((s, g) => s + g.weightKg, 0).toFixed(1)} кг
          </p>
        </div>
      )}

      <div className="card">
        <div className="table-wrap">
          <table className="table table--gear">
            <thead>
              <tr>
                <th>Название</th>
                <th className="text-end">Вес, кг</th>
                <th>Тип</th>
                {showActionsColumn && <th className="text-end"></th>}
              </tr>
            </thead>
            <tbody>
              {gear.map((g) =>
                canEditGear(g, isOrganizer, myParticipantId) ? (
                  <GearEditRow key={g.id} item={g} onSave={onSave} onDelete={onDelete} />
                ) : (
                  <tr key={g.id}>
                    <td>{g.name}</td>
                    <td className="text-end">{g.weightKg}</td>
                    <td>
                      <span className={`badge ${g.type === 'PERSONAL' ? '' : 'badge--ok'}`}>
                        {labelItemType(g.type)}
                      </span>
                    </td>
                    {showActionsColumn && <td aria-hidden="true" />}
                  </tr>
                ),
              )}
              {gear.length === 0 && (
                <tr className="table-empty">
                  <td colSpan={showActionsColumn ? 4 : 3} className="text-center">
                    {isOrganizer ? 'Нет снаряжения' : 'Добавьте личное снаряжение выше'}
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

function GearEditRow({
  item,
  onSave,
  onDelete,
}: {
  item: GearItem;
  onSave: (item: GearItem, name: string, weight: number) => void;
  onDelete: (id: number) => void;
}) {
  const [name, setName] = useState(item.name);
  const [weightKg, setWeightKg] = useState(String(item.weightKg));

  return (
    <tr>
      <td>
        <input className="input-sm" value={name} onChange={(e) => setName(e.target.value)} />
      </td>
      <td>
        <input
          className="input-sm text-end"
          type="number"
          step={0.01}
          min={0.01}
          value={weightKg}
          onChange={(e) => setWeightKg(e.target.value)}
        />
      </td>
      <td>
        <span className={`badge ${item.type === 'PERSONAL' ? '' : 'badge--ok'}`}>{labelItemType(item.type)}</span>
      </td>
      <td className="table-actions">
        <div className="table-actions__inner">
          <button type="button" className="btn btn-outline btn-sm" onClick={() => onSave(item, name, parseFloat(weightKg))}>
            Сохранить
          </button>
          <button type="button" className="btn btn-outline btn-sm btn-danger-outline" onClick={() => onDelete(item.id)}>
            Удалить
          </button>
        </div>
      </td>
    </tr>
  );
}
