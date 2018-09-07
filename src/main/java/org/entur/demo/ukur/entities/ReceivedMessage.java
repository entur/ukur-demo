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

package org.entur.demo.ukur.entities;

import java.time.LocalDateTime;

public class ReceivedMessage {
    private String xmlString;
    private MessageTypeEnum type;
    private LocalDateTime received = LocalDateTime.now();
    private String humanReadable;
    private String delay = null;

    public ReceivedMessage(String xmlString) {
        this.xmlString = xmlString;
    }

    public ReceivedMessage() {
    }

    public String getXmlString() {
        return xmlString;
    }

    public void setXmlString(String xmlString) {
        this.xmlString = xmlString;
    }

    public MessageTypeEnum getType() {
        return type;
    }

    public void setType(MessageTypeEnum type) {
        this.type = type;
    }

    public LocalDateTime getReceived() {
        return received;
    }

    public void setReceived(LocalDateTime received) {
        this.received = received;
    }

    public String getHumanReadable() {
        return humanReadable;
    }

    public void setHumanReadable(String humanReadable) {
        this.humanReadable = humanReadable;
    }

    public void setDeliveryDelay(long milliseconds) {
        long millis = milliseconds % 1000;
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / 1000) / 60;
        delay = String.format("%d:%d,%03d", minutes, seconds, millis);
    }

    public String getDeliveryDelay() {
        return delay;
    }

}
