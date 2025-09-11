export interface UserInterface{
    idUser: number,
    username: string,
    password: string,
    roleType: "CLIENT"|"PROFESSIONAL"|"ADMIN",
    first_name: string,
    last_name: string,
    address: string,
    number: string,
    email: string,
    avatar?: string;
    mySelf_description?: string
}
mapUserToInterface(user: User): UserInterface {
  return {
    idUser: user.id,
    username: user.username,
    password: user.password,
    roleType: user.role?.name || "CLIENT", // ejemplo
    first_name: user.userInfo?.first_name || "",
    last_name: user.userInfo?.last_name || "",
    address: user.userInfo?.address || "",
    number: user.userInfo?.number || "",
    email: user.email,
    avatar: user.userInfo?.avatar,
    mySelf_description: user.userInfo?.mySelf_description
  };
}

export type LoginInterface = Pick<UserInterface, "username"|"password">
