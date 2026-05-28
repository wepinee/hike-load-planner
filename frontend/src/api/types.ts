export type AuthResponse = {
  userId: number;
  email: string;
  displayName: string;
  accessToken?: string;
  tokenType?: string;
};

export type Hike = {
  id: number;
  name: string;
  startDate: string;
  durationDays: number;
  status: string;
  startLat?: number | null;
  startLon?: number | null;
  participantCount: number;
  totalGearWeightKg: number;
  organizerUserId: number;
};

export type Page<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
};

export type ProblemDetail = {
  detail?: string;
  title?: string;
  errors?: Record<string, string>;
  properties?: {
    errors?: Record<string, string>;
  };
};

export type Gender = 'MALE' | 'FEMALE';
export type ParticipantRole = 'ORGANIZER' | 'PARTICIPANT';
export type ItemType = 'SHARED' | 'PERSONAL';
export type AssignmentType = 'GEAR_SHARED' | 'GEAR_PERSONAL' | 'FOOD';

export type Participant = {
  id: number;
  name: string;
  email: string | null;
  gender: Gender;
  role: ParticipantRole;
  maxWeightKg: number;
};

export type GearItem = {
  id: number;
  name: string;
  weightKg: number;
  type: ItemType;
  ownerParticipantId: number | null;
};

export type FoodItem = {
  id: number;
  name: string;
  weightPerPortionKg: number;
  caloriesPerPortion: number | null;
  portionsPerPersonPerDay: number;
  totalWeightKg: number;
  totalCalories: number;
};

export type LoadPlan = {
  id: number;
  createdAt: string;
  version: number;
  assignments: AssignmentLine[];
  summary: ParticipantLoadSummary[];
};

export type AssignmentLine = {
  id: number;
  participantId: number;
  participantName: string;
  assignmentType: AssignmentType;
  itemName: string;
  gearItemId: number | null;
  foodItemId: number | null;
  effectiveWeightKg: number;
};

export type ParticipantLoadSummary = {
  participantId: number;
  participantName: string;
  totalWeightKg: number;
  maxWeightKg: number;
  withinLimit: boolean;
};
