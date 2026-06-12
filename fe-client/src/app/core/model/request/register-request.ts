export interface RegisterRequest {
  username: string;
  password: string;
  fullName: string;
  dob: string;
  gender: boolean | null;
  phone: string;
  address: string;
  guardianName: string;
  guardianPhone: string;
}
