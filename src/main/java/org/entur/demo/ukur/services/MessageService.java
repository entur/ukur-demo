/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

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
