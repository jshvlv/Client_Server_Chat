package com.javarush.task.task30.task3008.client;
import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    protected String getServerAddress(){
        System.out.println("Input server address:");
        return ConsoleHelper.readString();
    }
    protected int getServerPort(){
        System.out.println("Input server port:");
        return ConsoleHelper.readInt();
    }
    protected String getUserName(){
        System.out.println("What's your name?");
        return ConsoleHelper.readString();
    }
    protected boolean shouldSendTextFromConsole(){
        return true;
    }
    protected SocketThread getSocketThread(){
        return new SocketThread();
    }
    protected void sendTextMessage(String text){
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e){
            ConsoleHelper.writeMessage("ошибка отправки...");
            clientConnected = false;
        }
    }

    public void run() {
        try {
            SocketThread socketThread = getSocketThread();
            socketThread.setDaemon(true);
            socketThread.start();
            synchronized (this) {
                this.wait();
            }
            if(clientConnected) ConsoleHelper.writeMessage("Соединение установлено.\n" +
                    "Для выхода наберите команду 'exit'.");
            else ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
            while (clientConnected){
                String text = ConsoleHelper.readString();
                if(text.equals("exit")){
                    clientConnected = false;
                }
                if(shouldSendTextFromConsole()){
                    sendTextMessage(text);
                }
            }
        }catch (InterruptedException e){
            ConsoleHelper.writeMessage("Программа оставновлена");
        }
    }
    public class SocketThread extends Thread{
        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }
        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage(userName + " - added to the chat");
        }
        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage(userName + " left the chat");
        }
        protected void notifyConnectionStatusChanged(boolean clientConnected){
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        //16
        protected void clientHandshake() throws IOException, ClassNotFoundException{
            while (true){
                Message message = connection.receive();
                if(message.getType() == MessageType.NAME_REQUEST){
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                } else if(message.getType() == MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    break;
                } else {
                    throw new IOException("Unexpected MessageType");
                }

            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            while (true) {
                Message serverMessage = connection.receive();
                if (serverMessage.getType() == MessageType.TEXT) processIncomingMessage(serverMessage.getData());
                else if (serverMessage.getType() == MessageType.USER_ADDED)
                    informAboutAddingNewUser(serverMessage.getData());
                else if (serverMessage.getType() == MessageType.USER_REMOVED)
                    informAboutDeletingNewUser(serverMessage.getData());
                else throw new IOException("Unexpected MessageType");
            }
        }

        //17 main main

        @Override
        public void run() {
            String address = getServerAddress();
            int port = getServerPort();
            try {
                Socket socket = new Socket(address, port);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
