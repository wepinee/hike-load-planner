import { FormEvent, useEffect, useState } from 'react';
import { addFood, deleteFood, fetchFood, updateFood } from '../../api/hikes';
import { ApiError } from '../../api/client';
import type { FoodItem } from '../../api/types';
type Props = {
  hikeId: number;
  isOrganizer: boolean;
  onError: (msg: string) => void;
};

export default function FoodTab({ hikeId, isOrganizer, onError }: Props) {
  const [food, setFood] = useState<FoodItem[]>([]);
  const [name, setName] = useState('');
  const [weight, setWeight] = useState('');
  const [calories, setCalories] = useState('');
  const [portions, setPortions] = useState('2');

  async function load() {
    setFood(await fetchFood(hikeId));
  }

  useEffect(() => {
    load().catch((e: Error) => onError(e.message));
  }, [hikeId]);

  async function onAdd(e: FormEvent) {
    e.preventDefault();
    onError('');
    try {
      await addFood(hikeId, {
        name: name.trim(),
        weightPerPortionKg: parseFloat(weight),
        caloriesPerPortion: calories ? parseInt(calories, 10) : undefined,
        portionsPerPersonPerDay: parseFloat(portions),
      });
      setName('');
      setWeight('');
      setCalories('');
      await load();
    } catch (err) {
      onError(err instanceof ApiError ? err.message : 'Ошибка');
    }
  }

  async function onSave(
    item: FoodItem,
    data: { name: string; weightPerPortionKg: number; caloriesPerPortion: number | null; portionsPerPersonPerDay: number },
  ) {
    try {
      await updateFood(hikeId, item.id, data);
      await load();
    } catch (err) {
      onError(err instanceof ApiError ? err.message : 'Ошибка');
    }
  }

  async function onDelete(foodId: number) {
    if (!confirm('Удалить продукт?')) return;
    try {
      await deleteFood(hikeId, foodId);
      await load();
    } catch (err) {
      onError(err instanceof ApiError ? err.message : 'Ошибка');
    }
  }

  return (
    <>
      {isOrganizer && (
        <div className="card mb-3">
          <h2 className="card-title">Добавить продукт</h2>
          <form onSubmit={onAdd} className="form-row">
            <div className="form-field" style={{ flex: 2 }}>
              <label>Название</label>
              <input value={name} onChange={(e) => setName(e.target.value)} required />
            </div>
            <div className="form-field" style={{ flex: 1 }}>
              <label>кг/порция</label>
              <input type="number" step={0.01} min={0.01} value={weight} onChange={(e) => setWeight(e.target.value)} required />
            </div>
            <div className="form-field" style={{ flex: 1 }}>
              <label>ккал</label>
              <input type="number" value={calories} onChange={(e) => setCalories(e.target.value)} />
            </div>
            <div className="form-field" style={{ flex: 1 }}>
              <label>порций/день</label>
              <input type="number" step={0.1} min={0.1} value={portions} onChange={(e) => setPortions(e.target.value)} required />
            </div>
            <button type="submit" className="btn btn-primary">Добавить</button>
          </form>
        </div>
      )}

      <div className="card">
        <table className="table">
          <thead>
            <tr>
              <th>Продукт</th>
              <th className="text-end">кг/порция</th>
              <th className="text-end">ккал</th>
              <th className="text-end">порций/день</th>
              <th className="text-end">Итого кг</th>
              <th className="text-end">Итого ккал</th>
              {isOrganizer && <th className="text-end"></th>}
            </tr>
          </thead>
          <tbody>
            {food.map((f) =>
              isOrganizer ? (
                <FoodEditRow key={f.id} item={f} onSave={onSave} onDelete={onDelete} />
              ) : (
                <tr key={f.id}>
                  <td>{f.name}</td>
                  <td className="text-end">{f.weightPerPortionKg}</td>
                  <td className="text-end">{f.caloriesPerPortion ?? '—'}</td>
                  <td className="text-end">{f.portionsPerPersonPerDay}</td>
                  <td className="text-end">{f.totalWeightKg}</td>
                  <td className="text-end">{f.totalCalories}</td>
                </tr>
              ),
            )}
          </tbody>
        </table>
      </div>
    </>
  );
}

function FoodEditRow({
  item,
  onSave,
  onDelete,
}: {
  item: FoodItem;
  onSave: (item: FoodItem, data: { name: string; weightPerPortionKg: number; caloriesPerPortion: number | null; portionsPerPersonPerDay: number }) => void;
  onDelete: (id: number) => void;
}) {
  const [name, setName] = useState(item.name);
  const [weight, setWeight] = useState(String(item.weightPerPortionKg));
  const [calories, setCalories] = useState(item.caloriesPerPortion != null ? String(item.caloriesPerPortion) : '');
  const [portions, setPortions] = useState(String(item.portionsPerPersonPerDay));

  return (
    <tr>
      <td>
        <input className="input-sm" value={name} onChange={(e) => setName(e.target.value)} />
      </td>
      <td>
        <input className="input-sm text-end" type="number" step={0.01} min={0.01} value={weight} onChange={(e) => setWeight(e.target.value)} />
      </td>
      <td>
        <input className="input-sm text-end" type="number" value={calories} onChange={(e) => setCalories(e.target.value)} />
      </td>
      <td>
        <input className="input-sm text-end" type="number" step={0.1} min={0.1} value={portions} onChange={(e) => setPortions(e.target.value)} />
      </td>
      <td className="text-end text-muted">{item.totalWeightKg}</td>
      <td className="text-end text-muted">{item.totalCalories}</td>
      <td className="text-end text-nowrap">
        <button
          type="button"
          className="btn btn-outline btn-sm"
          onClick={() =>
            onSave(item, {
              name,
              weightPerPortionKg: parseFloat(weight),
              caloriesPerPortion: calories ? parseInt(calories, 10) : null,
              portionsPerPersonPerDay: parseFloat(portions),
            })
          }
        >
          Сохранить
        </button>
        <button type="button" className="btn btn-outline btn-sm btn-danger-outline ms-1" onClick={() => onDelete(item.id)}>
          Удалить
        </button>
      </td>
    </tr>
  );
}
