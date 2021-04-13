package com.javarush.task.task30.task3008.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClientGuiModel {
    public  Set<String> getAllUserNames() {
        return Collections.unmodifiableSet(allUserNames);
    }

    private final Set<String> allUserNames = new HashSet<>();

    public String getNewMessage() {
        return newMessage;
    }

    public void setNewMessage(String newMessage) {
        this.newMessage = newMessage;
    }

    private String newMessage;

    public void addUser(String newUserName){
        allUserNames.add(newUserName);
    }
    public void deleteUser(String userName){
        allUserNames.remove(userName);
    }

}
