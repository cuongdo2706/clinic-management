export interface UpdatePatientProfileRequest {
  code?: string | null;
  fullName: string;
  dob: string;
  gender: boolean;
  phone: string;
  address: string;
  guardianName: string;
  guardianPhone: string;
  version: number;
}
