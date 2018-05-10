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

import com.google.common.collect.EvictingQueue;
import org.entur.demo.ukur.entities.MessageTypeEnum;
import org.entur.demo.ukur.entities.ReceivedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.org.siri.siri20.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class MessageService {

    public static final int MAX_SIZE_PER_SUBSCRIPTION = 100;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    private final JAXBContext jaxbContext;
    private HashMap<String, Collection<ReceivedMessage>> messageStore = new HashMap<>();
    private HashMap<String, LocalDateTime> lastMessageReceived = new HashMap<>();

    @Autowired
    public MessageService() {
        try {
            jaxbContext = JAXBContext.newInstance(Siri.class);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not initialize a JAXBContext", e);
        }
    }

    public LocalDateTime getLastMessageReceived(String subscriptionId) {
        return lastMessageReceived.get(subscriptionId);
    }

    public int getMessageCount(String subscriptionId) {
        return getMessages(subscriptionId).size();
    }

    public void addPushMessage(String subscriptionId, EstimatedVehicleJourney estimatedVehicleJourney) {
        Collection<ReceivedMessage> pushMessages = getReceivedMessages(subscriptionId);
        ReceivedMessage message = new ReceivedMessage(toString(estimatedVehicleJourney));
        pushMessages.add(message);
        message.setType(MessageTypeEnum.ET);
        lastMessageReceived.put(subscriptionId, message.getReceived());
        message.setEstimatedVehicleJourney(estimatedVehicleJourney);
        message.setHumanReadable(makeHumanReadable(estimatedVehicleJourney));
    }

    public void addPushMessage(String subscriptionId, PtSituationElement ptSituationElement) {
        Collection<ReceivedMessage> pushMessages = getReceivedMessages(subscriptionId);
        ReceivedMessage message = new ReceivedMessage(toString(ptSituationElement));
        pushMessages.add(message);
        message.setType(MessageTypeEnum.SX);
        message.setPtSituationElement(ptSituationElement);
        message.setHumanReadable(makeHumanReadable(ptSituationElement));
    }

    public Collection<ReceivedMessage> getMessages(String subscriptionId) {
        Collection<ReceivedMessage> messages = messageStore.getOrDefault(subscriptionId, Collections.emptyList());
        ArrayList<ReceivedMessage> list = new ArrayList<>(messages);
        list.sort(Comparator.comparing(ReceivedMessage::getReceived).reversed());
        return list;
    }

    public void removeMessages(String id) {
        messageStore.remove(id);
        lastMessageReceived.remove(id);
    }

    public void clearAll() {
        messageStore.clear();
        lastMessageReceived.clear();
    }

    private Collection<ReceivedMessage> getReceivedMessages(String subscriptionId) {
        return messageStore.computeIfAbsent(subscriptionId, k -> EvictingQueue.create(MAX_SIZE_PER_SUBSCRIPTION));
    }

    private String toString(Object siriElement) {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter writer = new StringWriter();
            marshaller.marshal(siriElement, writer);
            return writer.getBuffer().toString();
        } catch (JAXBException e) {
            logger.error("Could not marshall", e);
        }
        return null;
    }


    private String makeHumanReadable(PtSituationElement ptSituationElement) {
        //TODO: Implement properly
        List<DefaultedTextStructure> descriptions = ptSituationElement.getDescriptions();
        if (descriptions == null || descriptions.isEmpty()) {
            return null;
        }
        if (descriptions.size() == 1) {
            return descriptions.get(0).getValue();
        }
        String norwegian = null;
        String last = null;
        for (DefaultedTextStructure description : descriptions) {
            if ("EN".equalsIgnoreCase(description.getLang())) {
                return description.getValue();
            } else if ("NO".equalsIgnoreCase(description.getLang())) {
                norwegian = description.getValue();
            }
            last = description.getValue();
        }
        if (norwegian == null) {
            return last;
        }
        return norwegian;
    }

    private String makeHumanReadable(EstimatedVehicleJourney estimatedVehicleJourney) {

        StringBuilder result = new StringBuilder();

        LineRef lineRef = estimatedVehicleJourney.getLineRef();
        String line = (lineRef == null) ? "null" : lineRef.getValue();
        if (line.startsWith("NSB:Line:")) {
            result.append("Line ").append(line.substring(9));
        } else {
            result.append(line);
        }

        DirectionRefStructure directionRef = estimatedVehicleJourney.getDirectionRef();
        if (directionRef != null) {
            //other providers than BaneNOR has different content in direction so this is not universal...
            result.append(" towards ").append(directionRef.getValue());
        }

        RecordedCall recordedCall = null;
        if (estimatedVehicleJourney.getRecordedCalls() != null && estimatedVehicleJourney.getRecordedCalls().getRecordedCalls() != null) {
            int noRecordedCalls = estimatedVehicleJourney.getRecordedCalls().getRecordedCalls().size();
            if (noRecordedCalls == 1) {
                recordedCall = estimatedVehicleJourney.getRecordedCalls().getRecordedCalls().get(0);
            }
            if (noRecordedCalls > 1) {
                //Should not happen, but we could use the subscriptions from-list to find a relevant call...
                logger.warn("Expected zero or one recorded calls, got {}", noRecordedCalls);
            }
        }

        EstimatedCall fromCall = null;
        EstimatedCall toCall = null;
        if (estimatedVehicleJourney.getEstimatedCalls() != null && estimatedVehicleJourney.getEstimatedCalls().getEstimatedCalls() != null) {
            List<EstimatedCall> estimatedCalls = estimatedVehicleJourney.getEstimatedCalls().getEstimatedCalls();
            if (estimatedCalls.size() == 2) {
                fromCall = estimatedCalls.get(0);
                toCall = estimatedCalls.get(1);
            } else if (estimatedCalls.size() == 1) {
                toCall = estimatedCalls.get(0);
            } else {
                //Should not happen, but we could find relevant calls by using the subscriptions to and from lists
                logger.warn("Expected one or two estimated calls, got {}", estimatedCalls.size());
            }
        }
        //This logic is somewhat naive and not very robust for future changes...
        if (recordedCall != null) {
            result.append(" from ").append(getName(recordedCall.getStopPointNames()));
            String aimedDeparture = recordedCall.getAimedDepartureTime() != null ? recordedCall.getAimedDepartureTime().format(formatter) : null;
            if (aimedDeparture != null) {
                result.append(" ").append(aimedDeparture);
            }
            if (Boolean.TRUE.equals(recordedCall.isCancellation())) {
                result.append(" was cancelled");
            } else {
                String actualDeparture = recordedCall.getActualDepartureTime() != null ? recordedCall.getActualDepartureTime().format(formatter) : null;
                if (actualDeparture != null && !actualDeparture.equals(aimedDeparture)) {
                    result.append(" departed ").append(actualDeparture);
                }
            }

        } else if (fromCall != null) {
            result.append(" from ").append(getName(fromCall.getStopPointNames()));
            ZonedDateTime aimedDepartureTime = fromCall.getAimedDepartureTime();
            if (aimedDepartureTime != null) {
                result.append(" ").append(aimedDepartureTime.format(formatter));
            }
            if (Boolean.TRUE.equals(fromCall.isCancellation())) {
                result.append(" is cancelled");
            } else {
                switch (fromCall.getDepartureStatus()) {
                    case CANCELLED:
                        result.append(" is cancelled");
                        break;
                    case DELAYED:
                        result.append(" is delayed");
                        ZonedDateTime expectedDepartureTime = fromCall.getExpectedDepartureTime();
                        if (expectedDepartureTime != null) {
                            result.append(" and expected to depart ").append(expectedDepartureTime.format(formatter));
                        }
                        break;
                }
            }
        }
        if (toCall != null) {
            result.append(" to ").append(getName(toCall.getStopPointNames()));
            ZonedDateTime aimedArrivalTime = toCall.getAimedArrivalTime();
            if (aimedArrivalTime != null) {
                result.append(" with aimed arrival ").append(aimedArrivalTime.format(formatter));
            }
            if (Boolean.TRUE.equals(toCall.isCancellation())) {
                result.append(" is cancelled");
            } else {
                switch (toCall.getArrivalStatus()) {
                    case CANCELLED:
                        result.append(" is cancelled");
                        break;
                    case DELAYED:
                        result.append(" is delayed");
                        ZonedDateTime expectedArrivalTime = toCall.getExpectedArrivalTime();
                        if (expectedArrivalTime != null) {
                            result.append(" and expected to arrive ").append(expectedArrivalTime.format(formatter));
                        }
                        break;
                }
            }
        }

        return result.toString();
    }

    private String getName(List<NaturalLanguageStringStructure> stopPointNames) {
        return  (stopPointNames == null || stopPointNames.isEmpty()) ? "?" : stopPointNames.get(0).getValue();
    }

}


