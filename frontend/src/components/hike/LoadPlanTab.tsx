import { useEffect, useState } from 'react';
import {
  fetchLoadPlan,
  downloadLoadPlanPdf,
  fetchLoadPlanExport,
  fetchParticipants,
  generateLoadPlan,
  reassignGear,
} from '../../api/hikes';
import { ApiError, saveBlob } from '../../api/client';
import type { AssignmentLine, LoadPlan, Participant } from '../../api/types';
import AssignmentBreakdownChart from '../charts/AssignmentBreakdownChart';
import ParticipantLoadChart from '../charts/ParticipantLoadChart';
import { labelAssignmentType } from '../../utils/labels';

type Props = {
  hikeId: number;
  isOrganizer: boolean;
  onError: (msg: string) => void;
};

export default function LoadPlanTab({ hikeId, isOrganizer, onError }: Props) {
  const [plan, setPlan] = useState<LoadPlan | null>(null);
  const [participants, setParticipants] = useState<Participant[]>([]);
  const [loading, setLoading] = useState(true);
  const [reassignId, setReassignId] = useState<number | ''>('');
  const [reassignTo, setReassignTo] = useState<number | ''>('');

  async function load() {
    setLoading(true);
    try {
      setPlan(await fetchLoadPlan(hikeId));
    } catch (err) {
      if (err instanceof ApiError && err.status === 404) {
        setPlan(null);
      } else {
        onError(err instanceof Error ? err.message : 'Ошибка');
      }
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    fetchParticipants(hikeId)
      .then(setParticipants)
      .catch((e: Error) => onError(e.message));
  }, [hikeId]);

  async function onGenerate() {
    onError('');
    try {
      setPlan(await generateLoadPlan(hikeId));
    } catch (err) {
      onError(err instanceof ApiError ? err.message : 'Ошибка формирования');
    }
  }

  async function onExportJson() {
    try {
      const data = await fetchLoadPlanExport(hikeId);
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
      saveBlob(blob, `load-plan-${hikeId}.json`);
    } catch (err) {
      onError(err instanceof ApiError ? err.message : 'Ошибка экспорта');
    }
  }

  async function onExportPdf() {
    onError('');
    try {
      await downloadLoadPlanPdf(hikeId);
    } catch (err) {
      onError(err instanceof ApiError ? err.message : 'Ошибка экспорта PDF');
    }
  }

  async function onReassign() {
    if (!reassignId || !reassignTo) return;
    try {
      setPlan(await reassignGear(hikeId, Number(reassignId), Number(reassignTo)));
      setReassignId('');
      setReassignTo('');
    } catch (err) {
      onError(err instanceof ApiError ? err.message : 'Ошибка переназначения');
    }
  }

  const sharedAssignments = plan?.assignments.filter((a) => a.assignmentType === 'GEAR_SHARED') ?? [];

  if (loading) {
    return <p>Загрузка…</p>;
  }

  return (
    <>
      {isOrganizer && (
        <div className="toolbar">
          <button type="button" className="btn btn-primary" onClick={onGenerate}>
            Сформировать раскладку
          </button>
          <button type="button" className="btn btn-outline" onClick={onExportJson}>
            Экспорт JSON
          </button>
          <button type="button" className="btn btn-outline" onClick={onExportPdf} disabled={!plan}>
            Экспорт PDF
          </button>
        </div>
      )}

      {!plan && <div className="alert">Раскладка ещё не сформирована</div>}

      {plan && (
        <>
          <p className="text-muted">
            Версия {plan.version}, создана {new Date(plan.createdAt).toLocaleString('ru-RU')}
          </p>

          <div className="charts-grid">
            <div className="card card--flat">
              <h2 className="card-title">Нагрузка по участникам</h2>
              <ParticipantLoadChart summary={plan.summary} />
            </div>
            <div className="card card--flat">
              <h2 className="card-title">Состав нагрузки</h2>
              <AssignmentBreakdownChart assignments={plan.assignments} />
            </div>
          </div>

          <div className="card mb-3">
            <h2 className="card-title">Таблица нагрузки</h2>
            <div className="table-wrap">
            <table className="table table--load">
              <colgroup>
                <col className="col-name" />
                <col className="col-num" />
                <col className="col-num" />
                <col className="col-status" />
              </colgroup>
              <thead>
                <tr>
                  <th>Участник</th>
                  <th className="text-end">Вес, кг</th>
                  <th className="text-end">Лимит</th>
                  <th className="text-center">OK</th>
                </tr>
              </thead>
              <tbody>
                {plan.summary.map((s) => (
                  <tr key={s.participantId}>
                    <td>{s.participantName}</td>
                    <td className="text-end">{s.totalWeightKg.toFixed(1)}</td>
                    <td className="text-end">{s.maxWeightKg.toFixed(1)}</td>
                    <td className="text-center">
                      <span className={`badge ${s.withinLimit ? 'badge--ok' : 'badge--bad'}`}>
                        {s.withinLimit ? 'да' : 'нет'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            </div>
          </div>

          {isOrganizer && sharedAssignments.length > 0 && (
            <div className="card mb-3">
              <h2 className="card-title">Переназначить общее снаряжение</h2>
              <div className="form-row">
                <div className="form-field" style={{ flex: 2 }}>
                  <select value={reassignId} onChange={(e) => setReassignId(e.target.value ? Number(e.target.value) : '')}>
                    <option value="">Назначение</option>
                    {sharedAssignments.map((a) => (
                      <option key={a.id} value={a.id}>
                        {a.itemName} → {a.participantName} ({a.effectiveWeightKg.toFixed(1)} кг)
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-field" style={{ flex: 1 }}>
                  <select value={reassignTo} onChange={(e) => setReassignTo(e.target.value ? Number(e.target.value) : '')}>
                    <option value="">Кому</option>
                    {participants.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.name}
                      </option>
                    ))}
                  </select>
                </div>
                <button type="button" className="btn btn-outline" onClick={onReassign}>
                  Переназначить
                </button>
              </div>
            </div>
          )}

          <div className="card">
            <h2 className="card-title">Назначения</h2>
            <AssignmentsTable assignments={plan.assignments} />
          </div>
        </>
      )}
    </>
  );
}

export function AssignmentsTable({ assignments }: { assignments: AssignmentLine[] }) {
  return (
    <div className="table-wrap">
      <table className="table table-sm table--assignments">
        <colgroup>
          <col className="col-name" />
          <col className="col-item" />
          <col className="col-type" />
          <col className="col-num" />
        </colgroup>
        <thead>
          <tr>
            <th>Участник</th>
            <th>Предмет</th>
            <th>Тип</th>
            <th className="text-end">кг</th>
          </tr>
        </thead>
        <tbody>
          {assignments.map((a) => (
            <tr key={a.id}>
              <td>{a.participantName}</td>
              <td>{a.itemName}</td>
              <td>
                <span className="badge badge--table">{labelAssignmentType(a.assignmentType)}</span>
              </td>
              <td className="text-end">{a.effectiveWeightKg.toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
