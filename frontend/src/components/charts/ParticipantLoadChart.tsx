import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import type { ParticipantLoadSummary } from '../../api/types';

type Props = {
  summary: ParticipantLoadSummary[];
};

export default function ParticipantLoadChart({ summary }: Props) {
  if (summary.length === 0) {
    return null;
  }

  const data = summary.map((s) => ({
    name: s.participantName,
    weight: Number(s.totalWeightKg.toFixed(2)),
    limit: Number(s.maxWeightKg.toFixed(2)),
    withinLimit: s.withinLimit,
  }));

  return (
    <div className="chart-panel chart-panel--bar">
      <ResponsiveContainer width="100%" height={280}>
        <BarChart data={data} margin={{ top: 12, right: 16, left: 8, bottom: 8 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#e2e8df" />
          <XAxis dataKey="name" tick={{ fontSize: 12 }} />
          <YAxis unit=" кг" tick={{ fontSize: 12 }} />
          <Tooltip
            formatter={(value: number, name: string) => [
              `${value} кг`,
              name === 'weight' ? 'Нагрузка' : 'Лимит',
            ]}
            labelFormatter={(label) => `Участник: ${label}`}
          />
          <Legend formatter={(value) => (value === 'weight' ? 'Нагрузка' : 'Лимит')} />
          <Bar dataKey="weight" name="weight" radius={[6, 6, 0, 0]}>
            {data.map((entry) => (
              <Cell key={entry.name} fill={entry.withinLimit ? '#2d6a4f' : '#b42318'} />
            ))}
          </Bar>
          <Bar dataKey="limit" name="limit" fill="#95b8a8" radius={[6, 6, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
