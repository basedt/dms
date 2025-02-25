import admin from "./pages/admin";
import user from "./pages/user";
import userCenter from "./pages/user.center";
import workspace from "./pages/workspace";
export default {
  ...workspace,
  ...admin,
  ...user,
  ...userCenter,
};
