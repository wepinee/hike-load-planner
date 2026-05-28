import type { AssignmentType, Gender, Hike, ItemType, ParticipantRole } from '../api/types';

export const HIKE_STATUS_LABELS: Record<string, string> = {
  DRAFT: 'Черновик',
  PLANNING: 'Планирование',
  READY: 'Готов',
  COMPLETED: 'Завершён',
};

export const ROLE_LABELS: Record<ParticipantRole, string> = {
  ORGANIZER: 'Организатор',
  PARTICIPANT: 'Участник',
};

export const ITEM_TYPE_LABELS: Record<ItemType, string> = {
  SHARED: 'Общее',
  PERSONAL: 'Личное',
};

export const ASSIGNMENT_TYPE_LABELS: Record<AssignmentType, string> = {
  GEAR_SHARED: 'Общее снаряжение',
  GEAR_PERSONAL: 'Личное снаряжение',
  FOOD: 'Питание',
};

export const GENDER_LABELS: Record<Gender, string> = {
  MALE: 'Мужской',
  FEMALE: 'Женский',
};

export function labelHikeStatus(status: Hike['status'] | string): string {
  return HIKE_STATUS_LABELS[status] ?? status;
}

export function labelRole(role: ParticipantRole | string): string {
  return ROLE_LABELS[role as ParticipantRole] ?? role;
}

export function labelItemType(type: ItemType | string): string {
  return ITEM_TYPE_LABELS[type as ItemType] ?? type;
}

export function labelAssignmentType(type: AssignmentType | string): string {
  return ASSIGNMENT_TYPE_LABELS[type as AssignmentType] ?? type;
}

export function labelGender(gender: Gender | string): string {
  return GENDER_LABELS[gender as Gender] ?? gender;
}
