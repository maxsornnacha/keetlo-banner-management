export type User = {
  userCode: string;
  username: string;
  email: string;
  firstname: string | null;
  lastname: string | null;
  avatarUrl: string | null;
  createdAt: string;  // ISO or 'YYYY-MM-DD HH:mm:ss'
  updatedAt: string;  // ISO or 'YYYY-MM-DD HH:mm:ss'
};