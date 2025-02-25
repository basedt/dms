import { io } from "socket.io-client";

export const SqlSocketCreator = () => io("http://localhost:8366/sql", {
  transports: ["websocket"],
});
