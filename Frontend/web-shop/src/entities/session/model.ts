export interface TokenPair {
  accessToken: string;
  refreshToken: string;
}

export interface UserMe {
  id: number;
  email: string;
  name: string;
  phone: string;
  status: string;
}

export interface AdminMe {
  id: number;
  email: string;
  name: string;
  status: string;
}
