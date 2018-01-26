package org.entur.demo.ukur.services;

import org.entur.demo.ukur.entities.PushMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
public class MessageService {

    private HashMap<String, List<PushMessage>> messageStore = new HashMap<>();
    private HashMap<String, LocalDateTime> lastMessageReceived = new HashMap<>();

    public LocalDateTime getLastMessageReceived(String subscriptionId) {
        return lastMessageReceived.get(subscriptionId);
    }

    public int getMessageCount(String subscriptionId) {
        return getMessages(subscriptionId).size();
    }

    public void addPushMessage(String subscriptionId, PushMessage pushMessage) {
        List<PushMessage> pushMessages = messageStore.computeIfAbsent(subscriptionId, k -> new ArrayList<>());
        pushMessages.add(pushMessage);
        lastMessageReceived.put(subscriptionId, LocalDateTime.now());
    }

    public List<PushMessage> getMessages(String subscriptionId) {
        return messageStore.getOrDefault(subscriptionId, Collections.emptyList());
    }

    public void removeMessages(String id) {
        messageStore.remove(id);
        lastMessageReceived.remove(id);
    }
}
