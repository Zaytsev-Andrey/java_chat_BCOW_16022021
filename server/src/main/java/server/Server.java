package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private final int PORT = 8189;
    private ServerSocket server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private List<ClientHandler> clients;
    private AuthService authService;

    public Server() {
        clients = new CopyOnWriteArrayList<>();
        authService = new SimpleAuthServise();

        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started");

            while (true) {
                socket = server.accept();
                System.out.println("Client connected");
                System.out.println("client: " + socket.getRemoteSocketAddress());
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg(ClientHandler sender, String msg){
        String message = String.format("[ %s ]: %s", sender.getNickname(), msg);
        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
    }

    /**
     * Отправляет сообщение клиенту с переданным nikName и отправителю сообщения. В случае если клиента с переданным
     * nikName не существует отправляет клиенту отправителю сообщение с предупреждением.
     * @param sender отправитель сообщения
     * @param nikName nik клиента получателя
     * @param msg текст сообщения
     */
    public void withAddressMsg(ClientHandler sender, String nikName, String msg){
        String message;

        // Ищем клиента с переданным nikName в списке активных клиентов
        ClientHandler recipient = null;
        for (ClientHandler c : clients) {
            if (c.getNickname().equals(nikName)) {
                recipient = c;
                break;
            }
        }

        // Если клиент не найден формируем предупреждение
        // Иначе формируем текст сообщения и отправляем получателю
        if (recipient == null) {
            message = String.format("[SERVER]: Сообщение \"%s\" не было доставлено. Клиент %s не найден", msg, nikName);
        } else {
            message = String.format("[ %s ]: %s", sender.getNickname(), msg);
            recipient.sendMsg(message);
        }

        sender.sendMsg(message);
    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }

    public AuthService getAuthService() {
        return authService;
    }
}
