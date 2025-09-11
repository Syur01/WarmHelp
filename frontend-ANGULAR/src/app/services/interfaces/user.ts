export interface User {
  id: number;
  username: string;
  userInfo?: {
    avatar?: string;
    // agrega aquí otros campos que tengas en UserInfo
  };
}
