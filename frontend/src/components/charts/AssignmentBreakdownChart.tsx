import { Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';
import type { AssignmentLine } from '../../api/types';
import { labelAssignmentType } from '../../utils/labels';

const COLORS = ['#2d6a4f', '#40916c', '#1d4ed8'];

const PIE_MARGIN = { top: 8, right: 8, bottom: 36, left: 8 };

type Props = {
  assignments: AssignmentLine[];
};

export default function AssignmentBreakdownChart({ assignments }: Props) {
  const byType = assignments.reduce<Record<string, number>>((acc, a) => {
    acc[a.assignmentType] = (acc[a.assignmentType] ?? 0) + a.effectiveWeightKg;
    return acc;
  }, {});

  const data = Object.entries(byType).map(([type, value]) => ({
    name: labelAssignmentType(type),
    value: Number(value.toFixed(2)),
    type,
  }));

  if (data.length === 0) {
    return null;
  }

  return (
    <div className="chart-panel chart-panel--pie">
      <ResponsiveContainer width="100%" height={240}>
        <PieChart margin={PIE_MARGIN}>
          <Pie
            data={data}
            dataKey="value"
            nameKey="name"
            cx="50%"
            cy="45%"
            outerRadius={72}
            label={false}
          >
            {data.map((entry, i) => (
              <Cell key={entry.type} fill={COLORS[i % COLORS.length]} />
            ))}
          </Pie>
          <Tooltip formatter={(v: number) => [`${v} кг`, 'Вес']} />
          <Legend verticalAlign="bottom" align="center" layout="horizontal" />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}
