import { useEffect, useState } from 'react';
import { fetchMyLoadPlan } from '../../api/hikes';
import { ApiError } from '../../api/client';
import type { LoadPlan } from '../../api/types';
import AssignmentBreakdownChart from '../charts/AssignmentBreakdownChart';
import ParticipantLoadChart from '../charts/ParticipantLoadChart';
import { AssignmentsTable } from './LoadPlanTab';

type Props = {
  hikeId: number;
  onError: (msg: string) => void;
};

export default function MyLoadTab({ hikeId, onError }: Props) {
  const [plan, setPlan] = useState<LoadPlan | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    fetchMyLoadPlan(hikeId)
      .then(setPlan)
      .catch((err) => {
        if (err instanceof ApiError && (err.status === 404 || err.status === 400)) {
          setPlan(null);
        } else {
          onError(err instanceof Error ? err.message : 'Ошибка');
        }
      })
      .finally(() => setLoading(false));
  }, [hikeId]);

  if (loading) {
    return <p>Загрузка…</p>;
  }

  if (!plan || plan.assignments.length === 0) {
    return <div className="alert">Для вас пока нет назначений. Сформируйте раскладку (организатор).</div>;
  }

  const summary = plan.summary[0];

  return (
    <>
      {summary && (
        <div className="card mb-3">
          <p style={{ marginTop: 0 }}>
            Итого: <strong>{summary.totalWeightKg.toFixed(1)}</strong> кг из {summary.maxWeightKg.toFixed(1)} кг
            {!summary.withinLimit && <span className="error"> — превышен лимит</span>}
          </p>
          <ParticipantLoadChart summary={plan.summary} />
        </div>
      )}
      {plan.assignments.length > 0 && (
        <div className="card mb-3">
          <h2 className="card-title">Моя нагрузка по категориям</h2>
          <AssignmentBreakdownChart assignments={plan.assignments} />
        </div>
      )}
      <div className="card">
        <h2 className="card-title">Мои назначения</h2>
        <AssignmentsTable assignments={plan.assignments} />
      </div>
    </>
  );
}
