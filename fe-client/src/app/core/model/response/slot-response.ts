export interface SlotDetailResponse {
  time: string;
  startAt: string;
  endAt: string;
  available: boolean;
  reason: string | null;
}

export interface SlotResponse {
  slotDetails: SlotDetailResponse[];
  slots: string[];
  durationMinutes: number;
  slotStepMinutes: number;
}
