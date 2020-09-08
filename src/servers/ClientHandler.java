package servers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    DataInputStream inputStreamNet;
    DataOutputStream outputStreamNet;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            inputStreamNet = new DataInputStream(socket.getInputStream());
            outputStreamNet = new DataOutputStream(socket.getOutputStream());

            Thread threadReadMsgFromNet = new Thread(() -> {
                try {
                    while (true){

                        String msg = inputStreamNet.readUTF();

                        if (msg.equals("/end")){

                            outputStreamNet.writeUTF(msg);
                            break;
                        }
                        server.broadcastMsg(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Client disconnect");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            threadReadMsgFromNet.setDaemon(true);
            threadReadMsgFromNet.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void sendMsg(String msg){
        try {
            outputStreamNet.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
