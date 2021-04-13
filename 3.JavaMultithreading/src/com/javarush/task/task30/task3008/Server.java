package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static class Handler extends Thread{
        private Socket socket;
        public Handler(Socket socket){this.socket = socket;}
        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{
            boolean tru = true;
            String newClient = "";
            while (tru){
                connection.send(new Message(MessageType.NAME_REQUEST, "Введите имя"));
                Message m = connection.receive();
                if(m.getType() != MessageType.USER_NAME){
                    continue;
                }
                else if (m.getData() == null) {}
                else if (m.getData().equals("")){}
                else if (connectionMap.containsKey(m.getData())){}
                else {
                    connectionMap.put(m.getData(), connection);
                    newClient = m.getData();
                    connection.send(new Message(MessageType.NAME_ACCEPTED, "принято"));
                    tru = false;
                }
            }
            return newClient;
        }

        private void notifyUsers(Connection connection, String userName) throws IOException{
            for (Map.Entry<String, Connection> entry : connectionMap.entrySet()
                 ) {
                String name = entry.getKey();
                Message message = new Message(MessageType.USER_ADDED, name);
                if(!userName.equals(name)) {
                    connection.send(message);
                }
            }
        }

        //главный цикл
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            while (true) {
                Message input = connection.receive();
                if (input.getType() == MessageType.TEXT) {
                    Message out_text_message = new Message(MessageType.TEXT, userName + ": " + input.getData());
                    sendBroadcastMessage(out_text_message);
                } else{
                    ConsoleHelper.writeMessage("ошибка");
                }

            }
        }

        //Main method


        @Override
        public void run() {
            String userName="";
            ConsoleHelper.writeMessage(socket.getRemoteSocketAddress().toString() + " соедниение установлено...");
            try (Connection connection = new Connection(socket)){
                userName = serverHandshake(connection);
                notifyUsers(connection, userName);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                serverMainLoop(connection, userName);
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                ConsoleHelper.writeMessage("соединение закрыто");

            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("ошибка при обмене данными");

            }
        }
    }
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(Message message){
        for (Map.Entry<String, Connection> entry : connectionMap.entrySet()
             ) {
            try {
                entry.getValue().send(message);
            }catch (IOException e){
                ConsoleHelper.writeMessage("сообщение не ушло");
            }
        }
    }


    public static void main(String[] args) throws IOException {
        System.out.println("Введите порт: ");
            try(ServerSocket socketServer = new ServerSocket(ConsoleHelper.readInt()))
            {
                ConsoleHelper.writeMessage("сервер запущен");
                while (true){
                Socket client = socketServer.accept();
                Handler handler = new Handler(client);
                handler.start();
                }
            } catch (Exception e){
                System.out.println("Socket error...");
            }
    }
}
