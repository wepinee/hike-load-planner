import { api, fetchBinary, saveBlob } from './client';
import type {
  FoodItem,
  GearItem,
  Hike,
  ItemType,
  LoadPlan,
  Page,
  Participant,
} from './types';

export function fetchHikes(size = 50) {
  return api<Page<Hike>>(`/hikes?size=${size}`);
}

export function fetchHike(id: number) {
  return api<Hike>(`/hikes/${id}`);
}

export function createHike(body: {
  name: string;
  startDate: string;
  durationDays: number;
}) {
  return api<Hike>('/hikes', {
    method: 'POST',
    body: JSON.stringify({ ...body, startLat: null, startLon: null }),
  });
}

export function deleteHike(id: number) {
  return api<void>(`/hikes/${id}`, { method: 'DELETE' });
}

export function fetchCopySources(hikeId: number) {
  return api<Page<Hike>>(`/hikes/${hikeId}/copy-sources?size=20`);
}

export function copyGearFromHike(targetId: number, sourceId: number) {
  return api<Hike>(`/hikes/${targetId}/gear/copy-from/${sourceId}`, { method: 'POST' });
}

export function fetchParticipants(hikeId: number) {
  return api<Participant[]>(`/hikes/${hikeId}/participants`);
}

export function addParticipant(
  hikeId: number,
  body: { name: string; email?: string; gender: string; maxWeightKg: number },
) {
  return api<Participant>(`/hikes/${hikeId}/participants`, {
    method: 'POST',
    body: JSON.stringify(body),
  });
}

export function deleteParticipant(hikeId: number, participantId: number) {
  return api<void>(`/hikes/${hikeId}/participants/${participantId}`, { method: 'DELETE' });
}

export function fetchGear(hikeId: number) {
  return api<GearItem[]>(`/hikes/${hikeId}/gear`);
}

export function addSharedGear(hikeId: number, name: string, weightKg: number) {
  return api<GearItem>(`/hikes/${hikeId}/gear/shared`, {
    method: 'POST',
    body: JSON.stringify({ name, weightKg, type: 'SHARED' as ItemType }),
  });
}

export function addPersonalGear(
  hikeId: number,
  name: string,
  weightKg: number,
  ownerParticipantId?: number | null,
) {
  return api<GearItem>(`/hikes/${hikeId}/gear/personal`, {
    method: 'POST',
    body: JSON.stringify({
      name,
      weightKg,
      type: 'PERSONAL' as ItemType,
      ownerParticipantId: ownerParticipantId ?? null,
    }),
  });
}

export function updateGear(hikeId: number, gearId: number, name: string, weightKg: number) {
  return api<GearItem>(`/hikes/${hikeId}/gear/${gearId}`, {
    method: 'PUT',
    body: JSON.stringify({ name, weightKg }),
  });
}

export function deleteGear(hikeId: number, gearId: number) {
  return api<void>(`/hikes/${hikeId}/gear/${gearId}`, { method: 'DELETE' });
}

export function fetchFood(hikeId: number) {
  return api<FoodItem[]>(`/hikes/${hikeId}/food`);
}

export function addFood(
  hikeId: number,
  body: {
    name: string;
    weightPerPortionKg: number;
    caloriesPerPortion?: number;
    portionsPerPersonPerDay: number;
  },
) {
  return api<FoodItem>(`/hikes/${hikeId}/food`, {
    method: 'POST',
    body: JSON.stringify(body),
  });
}

export function updateFood(
  hikeId: number,
  foodId: number,
  body: {
    name: string;
    weightPerPortionKg: number;
    caloriesPerPortion: number | null;
    portionsPerPersonPerDay: number;
  },
) {
  return api<FoodItem>(`/hikes/${hikeId}/food/${foodId}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  });
}

export function deleteFood(hikeId: number, foodId: number) {
  return api<void>(`/hikes/${hikeId}/food/${foodId}`, { method: 'DELETE' });
}

export function generateLoadPlan(hikeId: number) {
  return api<LoadPlan>(`/hikes/${hikeId}/load-plan/generate`, { method: 'POST' });
}

export function fetchLoadPlan(hikeId: number) {
  return api<LoadPlan>(`/hikes/${hikeId}/load-plan`);
}

export function fetchMyLoadPlan(hikeId: number) {
  return api<LoadPlan>(`/hikes/${hikeId}/load-plan/my`);
}

export function reassignGear(
  hikeId: number,
  assignmentId: number,
  toParticipantId: number,
) {
  return api<LoadPlan>(`/hikes/${hikeId}/load-plan/reassign`, {
    method: 'PUT',
    body: JSON.stringify({ assignmentId, toParticipantId }),
  });
}

export function fetchLoadPlanExport(hikeId: number) {
  return api<unknown>(`/hikes/${hikeId}/load-plan/export`);
}

export async function downloadLoadPlanPdf(hikeId: number): Promise<void> {
  const blob = await fetchBinary(`/hikes/${hikeId}/load-plan/export/pdf`);
  saveBlob(blob, `load-plan-${hikeId}.pdf`);
}
