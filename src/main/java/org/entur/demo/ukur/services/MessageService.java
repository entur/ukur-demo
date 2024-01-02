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
import com.google.common.collect.Queues;
import org.apache.commons.lang3.StringUtils;
import org.entur.demo.ukur.entities.MessageTypeEnum;
import org.entur.demo.ukur.entities.ReceivedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.org.siri.siri20.*;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static uk.org.siri.siri20.CallStatusEnumeration.CANCELLED;
import static uk.org.siri.siri20.CallStatusEnumeration.DELAYED;

@Service
public class MessageService {

    public static final int MAX_SIZE_PER_SUBSCRIPTION = 100;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
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

    public void addPushMessage(String subscriptionId, Object receivedPushMessage) {
        ReceivedMessage message = new ReceivedMessage(toString(receivedPushMessage));
        if (receivedPushMessage instanceof Siri) {
            Siri siri = (Siri) receivedPushMessage;
            receivedPushMessage = extractPushMessage(siri);
            if (siri.getServiceDelivery() != null && siri.getServiceDelivery().getResponseTimestamp() != null) {
                ZonedDateTime responseTimestamp = siri.getServiceDelivery().getResponseTimestamp();
                long delay = ChronoUnit.MILLIS.between(responseTimestamp, ZonedDateTime.now());
                logger.info("siri message delay: {} ms", delay);
                message.setDeliveryDelay(delay);
            }
        }

        if (receivedPushMessage instanceof EstimatedVehicleJourney) {
            message.setType(MessageTypeEnum.ET);
            message.setHumanReadable(makeHumanReadable((EstimatedVehicleJourney) receivedPushMessage));
        } else if (receivedPushMessage instanceof PtSituationElement) {
            message.setType(MessageTypeEnum.SX);
            message.setHumanReadable(makeHumanReadable((PtSituationElement) receivedPushMessage));
        } else if (receivedPushMessage instanceof HeartbeatNotificationStructure) {
            message.setType(MessageTypeEnum.Heartbeat);
            message.setHumanReadable("Periodic heartbeat from server according to the subscriptions HeartbeatInterval");
        } else if (receivedPushMessage instanceof SubscriptionTerminatedNotificationStructure) {
            message.setType(MessageTypeEnum.Terminated);
            message.setHumanReadable("Subscription terminated by server due to InitialTerminationTime");
        }
        Collection<ReceivedMessage> pushMessages = getReceivedMessages(subscriptionId);
        pushMessages.add(message);
        lastMessageReceived.put(subscriptionId, message.getReceived());
    }

    private Object extractPushMessage(Siri siri) {
        ServiceDelivery serviceDelivery = siri.getServiceDelivery();
        if (serviceDelivery != null) {
            if (serviceDelivery.getSituationExchangeDeliveries().size() == 1) {
                SituationExchangeDeliveryStructure situationExchangeDeliveryStructure = serviceDelivery.getSituationExchangeDeliveries().get(0);
                List<PtSituationElement> ptSituationElements = situationExchangeDeliveryStructure.getSituations().getPtSituationElements();
                if (ptSituationElements.size() != 1) {
                    throw new IllegalArgumentException("Got ptSituationElements.size() PtSituationElements, expects only 1");
                }
                return ptSituationElements.get(0);
            } else if (serviceDelivery.getSituationExchangeDeliveries().size() > 1) {
                throw new IllegalArgumentException("Got more than one SituationExchangeDelivery");
            }

            if (serviceDelivery.getEstimatedTimetableDeliveries().size() == 1) {
                EstimatedTimetableDeliveryStructure estimatedTimetableDeliveryStructure = serviceDelivery.getEstimatedTimetableDeliveries().get(0);
                if (estimatedTimetableDeliveryStructure.getEstimatedJourneyVersionFrames().size() != 1) {
                    throw new IllegalArgumentException("Got " + estimatedTimetableDeliveryStructure.getEstimatedJourneyVersionFrames().size() + " EstimatedVersionFrames, expects only 1");
                }
                EstimatedVersionFrameStructure estimatedVersionFrameStructure = estimatedTimetableDeliveryStructure.getEstimatedJourneyVersionFrames().get(0);
                if (estimatedVersionFrameStructure.getEstimatedVehicleJourneies().size() != 1) {
                    throw new IllegalArgumentException("Got " + estimatedVersionFrameStructure.getEstimatedVehicleJourneies().size() + " EstimatedVehicleJourneys, expects only 1");
                }
                return estimatedVersionFrameStructure.getEstimatedVehicleJourneies().get(0);
            } else if (serviceDelivery.getEstimatedTimetableDeliveries().size() > 1) {
                throw new IllegalArgumentException("Got more than one EstimatedTimetableDelivery");
            }
        }

        if (siri.getHeartbeatNotification() != null) {
            return siri.getHeartbeatNotification();
        }

        if (siri.getSubscriptionTerminatedNotification() != null) {
            return siri.getSubscriptionTerminatedNotification();
        }

        throw new IllegalArgumentException("Requires one PtSituationElement, one EstimatedVehicleJourney, a HeartbeatNotification or a SubscriptionTerminatedNotification - but got none of them");
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
        return messageStore.computeIfAbsent(subscriptionId, k -> Queues.synchronizedQueue(EvictingQueue.create(MAX_SIZE_PER_SUBSCRIPTION)));
    }

    private String toString(Object siriElement) {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            StringWriter writer = new StringWriter();
            marshaller.marshal(siriElement, writer);
            return writer.getBuffer().toString();
        } catch (JAXBException e) {
            logger.error("Could not marshall", e);
        }
        return null;
    }


    private String makeHumanReadable(PtSituationElement ptSituationElement) {
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
        result.append((lineRef == null) ? "Line NULL" : lineRef.getValue());

        DirectionRefStructure directionRef = estimatedVehicleJourney.getDirectionRef();
        if (directionRef != null && StringUtils.isAlphaSpace(directionRef.getValue())) { //Sometimes direction is only a number and pointless to show
            result.append(" towards ").append(directionRef.getValue());
        }

        RecordedCall recordedCall = null;
        if (estimatedVehicleJourney.getRecordedCalls() != null && estimatedVehicleJourney.getRecordedCalls().getRecordedCalls() != null) {
            int noRecordedCalls = estimatedVehicleJourney.getRecordedCalls().getRecordedCalls().size();
            if (noRecordedCalls == 1) {
                recordedCall = estimatedVehicleJourney.getRecordedCalls().getRecordedCalls().get(0);
            }
        }
        boolean addedFromToText = false;
        if (estimatedVehicleJourney.getEstimatedCalls() != null && estimatedVehicleJourney.getEstimatedCalls().getEstimatedCalls() != null) {
            List<EstimatedCall> estimatedCalls = estimatedVehicleJourney.getEstimatedCalls().getEstimatedCalls();
            EstimatedCall fromCall = null;
            EstimatedCall toCall = null;
            if (estimatedCalls.size() == 2) {
                fromCall = estimatedCalls.get(0);
                toCall = estimatedCalls.get(1);
            } else if (estimatedCalls.size() == 1) {
                toCall = estimatedCalls.get(0);
            }
            if (toCall != null) {
                addedFromToText = true;
                result.append(findFromToText(recordedCall, fromCall, toCall));
            }
        }
        if (!addedFromToText) {
            result.append(" has deviations");
        }

        return result.toString();
    }

    private String  findFromToText(RecordedCall recordedCall, EstimatedCall fromCall, EstimatedCall toCall) {
        StringBuilder result = new StringBuilder();

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
            if (Boolean.TRUE.equals(fromCall.isCancellation()) || fromCall.getDepartureStatus() == CANCELLED) {
                result.append(" is cancelled");
            } else if (fromCall.getDepartureStatus() == DELAYED || isDelayed(fromCall.getAimedDepartureTime(), fromCall.getExpectedDepartureTime())) {
                result.append(" is delayed");
                ZonedDateTime expectedDepartureTime = fromCall.getExpectedDepartureTime();
                if (expectedDepartureTime != null) {
                    result.append(" and expected to depart ").append(expectedDepartureTime.format(formatter));
                }
            } else {
                result.append(" is on time");
            }
            appendAnyTrackChange(result, fromCall);
        }

        if (toCall != null) {
            result.append(" to ").append(getName(toCall.getStopPointNames()));
            ZonedDateTime aimedArrivalTime = toCall.getAimedArrivalTime();
            if (aimedArrivalTime != null) {
                result.append(" with aimed arrival ").append(aimedArrivalTime.format(formatter));
            }
            if (Boolean.TRUE.equals(toCall.isCancellation()) || toCall.getArrivalStatus() == CANCELLED) {
                result.append(" is cancelled");
            } else if(toCall.getArrivalStatus() == DELAYED || isDelayed(toCall.getAimedArrivalTime(), toCall.getExpectedArrivalTime())) {
                result.append(" is delayed");
                ZonedDateTime expectedArrivalTime = toCall.getExpectedArrivalTime();
                if (expectedArrivalTime != null) {
                    result.append(" and expected to arrive ").append(expectedArrivalTime.format(formatter));
                }
            }
            appendAnyTrackChange(result, toCall);
        }
        return result.toString();
    }

    private void appendAnyTrackChange(StringBuilder result, EstimatedCall call) {
        StopAssignmentStructure stopAssignment = call.getArrivalStopAssignment();
        if (stopAssignment == null) {
            stopAssignment = call.getDepartureStopAssignment();
        }
        if (stopAssignment != null && stopAssignment.getAimedQuayRef() != null && stopAssignment.getExpectedQuayRef() != null) {
            String aimed = stopAssignment.getAimedQuayRef().getValue();
            String expected = stopAssignment.getExpectedQuayRef().getValue();
            if (StringUtils.isNotBlank(aimed) && !StringUtils.equals(aimed, expected)) {
                result.append(" with new track ");
                String platformName = getPlatformName(call);
                if (StringUtils.isNotBlank(platformName)) {
                        result.append("platform ").append(platformName);
                } else {
                    result.append(expected);
                }
            }
        }
    }

    private String getPlatformName(EstimatedCall call) {
        if (call.getArrivalPlatformName() != null) return call.getArrivalPlatformName().getValue();
        if (call.getDeparturePlatformName() != null) return call.getDeparturePlatformName().getValue();
        return null;
    }

    private boolean isDelayed(ZonedDateTime aimed, ZonedDateTime expected) {
        if (aimed != null && expected != null) {
            return expected.isAfter(aimed);
        }
        return false;
    }

    private String getName(List<NaturalLanguageStringStructure> stopPointNames) {
        return  (stopPointNames == null || stopPointNames.isEmpty()) ? "?" : stopPointNames.get(0).getValue();
    }

}


