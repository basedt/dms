import { io } from 'socket.io-client';

export const SqlSocketCreator = () =>
  io(process.env.SOCKET_IO_SERVER, {
    transports: ['websocket'],
  });
