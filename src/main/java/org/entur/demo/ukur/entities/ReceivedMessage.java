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

import uk.org.siri.siri20.EstimatedVehicleJourney;
import uk.org.siri.siri20.PtSituationElement;

import java.time.LocalDateTime;

public class ReceivedMessage {
    private String xmlString;
    private MessageTypeEnum type;
    private LocalDateTime received = LocalDateTime.now();
    private EstimatedVehicleJourney estimatedVehicleJourney;
    private PtSituationElement ptSituationElement;
    private String humanReadable;

    public ReceivedMessage(String xmlString) {
        this.xmlString = xmlString;
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

    public EstimatedVehicleJourney getEstimatedVehicleJourney() {
        return estimatedVehicleJourney;
    }

    public void setEstimatedVehicleJourney(EstimatedVehicleJourney estimatedVehicleJourney) {
        this.estimatedVehicleJourney = estimatedVehicleJourney;
    }

    public PtSituationElement getPtSituationElement() {
        return ptSituationElement;
    }

    public void setPtSituationElement(PtSituationElement ptSituationElement) {
        this.ptSituationElement = ptSituationElement;
    }

    public String getHumanReadable() {
        return humanReadable;
    }

    public void setHumanReadable(String humanReadable) {
        this.humanReadable = humanReadable;
    }
}
