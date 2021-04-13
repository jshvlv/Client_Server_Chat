package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class BotClient extends Client{
    public class BotSocketThread extends SocketThread{
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if(message.contains(":")){
                String nameUser = message.split(":")[0];
                String messageData = message.split(":")[1].trim();
                SimpleDateFormat df;
                //ConsoleHelper.writeMessage(name + " имя пидораса");
                String forUser = "Информация для " + nameUser + ": ";
                switch (messageData){
                    case "дата" : df = new SimpleDateFormat("d.MM.YYYY");
                    break;
                    case "день" : df = new SimpleDateFormat("d");
                    break;
                    case "месяц" : df = new SimpleDateFormat("MMMM");
                    break;
                    case "год" : df = new SimpleDateFormat("YYYY");
                    break;
                    case "время" : df = new SimpleDateFormat("H:mm:ss");
                    break;
                    case "час" : df = new SimpleDateFormat("H");
                    break;
                    case "минуты" : df = new SimpleDateFormat("m");
                    break;
                    case "секунды" : df = new SimpleDateFormat("s");
                    break;
                    default: df = null;
                }
                if(df!=null){
                    sendTextMessage(forUser + df.format(GregorianCalendar.getInstance().getTime()));
                }
            }
        }
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return "date_bot_"  + (int)(Math.random()*100);
    }

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }
}
