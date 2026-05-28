import { FormEvent, useEffect, useState } from 'react';
import { addParticipant, deleteParticipant, fetchParticipants } from '../../api/hikes';
import { ApiError } from '../../api/client';
import type { Gender, Participant } from '../../api/types';
import { labelGender, labelRole } from '../../utils/labels';

type Props = {
  hikeId: number;
  isOrganizer: boolean;
  onError: (msg: string) => void;
};

export default function ParticipantsTab({ hikeId, isOrganizer, onError }: Props) {
  const [list, setList] = useState<Participant[]>([]);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [gender, setGender] = useState<Gender>('MALE');
  const [maxWeightKg, setMaxWeightKg] = useState(15);

  async function load() {
    setList(await fetchParticipants(hikeId));
  }

  useEffect(() => {
    load().catch((e: Error) => onError(e.message));
  }, [hikeId]);

  async function onAdd(e: FormEvent) {
    e.preventDefault();
    onError('');
    try {
      await addParticipant(hikeId, { name, email: email || undefined, gender, maxWeightKg });
      setName('');
      setEmail('');
      await load();
    } catch (err) {
      onError(err instanceof ApiError ? err.message : 'Ошибка');
    }
  }

  async function onRemove(participantId: number) {
    if (!confirm('Удалить участника?')) return;
    try {
      await deleteParticipant(hikeId, participantId);
      await load();
    } catch (err) {
      onError(err instanceof ApiError ? err.message : 'Ошибка');
    }
  }

  return (
    <>
      {isOrganizer && (
        <div className="card mb-3">
          <h2 className="card-title">Добавить участника</h2>
          <form onSubmit={onAdd} className="form-row">
            <div className="form-field" style={{ flex: 2 }}>
              <label>Имя</label>
              <input value={name} onChange={(e) => setName(e.target.value)} required />
            </div>
            <div className="form-field" style={{ flex: 2 }}>
              <label>Email</label>
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
            </div>
            <div className="form-field" style={{ flex: 1 }}>
              <label>Пол</label>
              <select value={gender} onChange={(e) => setGender(e.target.value as Gender)}>
                <option value="MALE">Мужской</option>
                <option value="FEMALE">Женский</option>
              </select>
            </div>
            <div className="form-field" style={{ flex: '0 0 100px' }}>
              <label>Лимит, кг</label>
              <input type="number" step={0.1} min={0.1} value={maxWeightKg} onChange={(e) => setMaxWeightKg(Number(e.target.value))} required />
            </div>
            <button type="submit" className="btn btn-primary">Добавить</button>
          </form>
        </div>
      )}

      <div className="card">
        <table className="table">
          <thead>
            <tr>
              <th>Имя</th>
              <th>Email</th>
              <th>Пол</th>
              <th>Лимит, кг</th>
              <th>Роль</th>
              {isOrganizer && <th></th>}
            </tr>
          </thead>
          <tbody>
            {list.map((p) => (
              <tr key={p.id}>
                <td>{p.name}</td>
                <td>{p.email ?? '—'}</td>
                <td>{labelGender(p.gender)}</td>
                <td>{p.maxWeightKg}</td>
                <td>
                  <span className={`badge ${p.role === 'ORGANIZER' ? 'badge--ok' : ''}`}>{labelRole(p.role)}</span>
                </td>
                {isOrganizer && (
                  <td className="text-end">
                    {p.role !== 'ORGANIZER' && (
                      <button type="button" className="btn btn-outline btn-sm btn-danger-outline" onClick={() => onRemove(p.id)}>
                        Удалить
                      </button>
                    )}
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
