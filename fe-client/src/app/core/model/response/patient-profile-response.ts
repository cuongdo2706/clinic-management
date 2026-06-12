export interface PatientProfileResponse {
  id: number;
  code: string;
  fullName: string;
  phone: string | null;
  dob: string | null;
  gender: boolean | null;
  address: string | null;
  guardianName: string | null;
  guardianPhone: string | null;
  version: number;
}
