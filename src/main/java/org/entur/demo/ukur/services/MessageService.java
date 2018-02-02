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

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.EvictingQueue;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import org.entur.demo.ukur.entities.MessageTypeEnum;
import org.entur.demo.ukur.entities.PushMessage;
import org.entur.demo.ukur.entities.ReceivedMessage;
import org.entur.demo.ukur.entities.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLFilterImpl;
import uk.org.siri.siri20.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class MessageService {

    public static final int MAX_SIZE_PER_SUBSCRIPTION = 100;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    private final JAXBContext jaxbContext;
    private HashMap<String, Collection<ReceivedMessage>> messageStore = new HashMap<>();
    private HashMap<String, LocalDateTime> lastMessageReceived = new HashMap<>();
    private SubscriptionService subscriptionService;

    @Autowired
    public MessageService(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
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

    public void addPushMessage(String subscriptionId, PushMessage pushMessage) {
        CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.DAYS).build();
        Collection<ReceivedMessage> pushMessages = messageStore.computeIfAbsent(subscriptionId, k -> EvictingQueue.create(MAX_SIZE_PER_SUBSCRIPTION));
        ReceivedMessage message = new ReceivedMessage(pushMessage.getXmlPayload());
        pushMessages.add(message);
        lastMessageReceived.put(subscriptionId, message.getReceived());

        String xmlPayload = pushMessage.getXmlPayload();
        //TODO: Have ukur push the xml element directly (as application/xml) - and let the server deal with this...
        if (xmlPayload == null) {
            logger.error("Empty XML payload for subscriptionId {}", subscriptionId);
        } else if (xmlPayload.contains("<EstimatedVehicleJourney")) {
            message.setType(MessageTypeEnum.ET);
            try {
                EstimatedVehicleJourney estimatedVehicleJourney = unmarshalSiri(xmlPayload, EstimatedVehicleJourney.class);
                message.setEstimatedVehicleJourney(estimatedVehicleJourney);
                message.setHumanReadable(makeHumanReadable(estimatedVehicleJourney, subscriptionId));
            } catch (Exception e) {
                logger.error("Could not unmarshall xmlpayload as EstimatedVehicleJourney", e);
            }
        } else if (xmlPayload.contains("<PtSituationElement")) {
            message.setType(MessageTypeEnum.SX);
            try {
                PtSituationElement ptSituationElement = unmarshalSiri(xmlPayload, PtSituationElement.class);
                message.setPtSituationElement(ptSituationElement);
                message.setHumanReadable(makeHumanReadable(ptSituationElement, subscriptionId));
            } catch (Exception e) {
                logger.error("Could not unmarshall xmlpayload as PtSituationElement", e);
            }
        } else {
            message.setType(MessageTypeEnum.UNKNOWN);
            logger.warn("Unknown xml payload: \n{}", xmlPayload);
        }

    }

    public Collection<ReceivedMessage> getMessages(String subscriptionId) {
        return messageStore.getOrDefault(subscriptionId, Collections.emptyList());
    }

    public void removeMessages(String id) {
        messageStore.remove(id);
        lastMessageReceived.remove(id);
    }

    @SuppressWarnings({"unused", "unchecked"})
    private <T> T unmarshalSiri(String xmlString, Class<T> elementType) throws JAXBException, ParserConfigurationException, SAXException, IOException {
        XMLFilter filter = new SiriNamespaceFilter();
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();
        XMLReader xr = sp.getXMLReader();
        filter.setParent(xr);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        UnmarshallerHandler unmarshallerHandler = jaxbUnmarshaller.getUnmarshallerHandler();
        filter.setContentHandler(unmarshallerHandler);
        InputSource xml = new InputSource(new StringReader(xmlString));
        filter.parse(xml);
        return (T) unmarshallerHandler.getResult();
    }

    private String makeHumanReadable(PtSituationElement ptSituationElement, String subscriptionId) {
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
            //Use last since that seems to be where the english text is
            //TODO: because of namespace issues lang is not unmarshalled... Will be fixed when ukur sends proper xml.
            return last;
        }
        return norwegian;
    }

    private String makeHumanReadable(EstimatedVehicleJourney estimatedVehicleJourney, String subscriptionId) {

        Subscription subscription = subscriptionService.get(subscriptionId);

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

        if (estimatedVehicleJourney.getEstimatedCalls() != null) {
            List<EstimatedCall> estimatedCalls = estimatedVehicleJourney.getEstimatedCalls().getEstimatedCalls();
            if (estimatedCalls == null || estimatedCalls.size() != 1) {
                logger.warn("Did not receive one estimated call as expected - instead got this: {}", estimatedCalls);
                result.append(" is delayed/cancelled (does not have enough data to extract details)");
            } else {
                EstimatedCall estimatedCall = estimatedCalls.get(0);
                StopPointRef stopPointRef = estimatedCall.getStopPointRef();
                List<NaturalLanguageStringStructure> stopPointNames = estimatedCall.getStopPointNames();
                String name = (stopPointNames == null || stopPointNames.isEmpty()) ? "?" : stopPointNames.get(0).getValue();
                if (isDepartingStop(subscription, stopPointRef)) {
                    result.append(" from ").append(name);
                    ZonedDateTime aimedDepartureTime = estimatedCall.getAimedDepartureTime();
                    if (aimedDepartureTime != null) {
                        result.append(" with aimed departure ").append(aimedDepartureTime.format(formatter));
                    }
                    if (Boolean.TRUE.equals(estimatedCall.isCancellation())) {
                        result.append(" is cancelled");
                    } else {
                        switch (estimatedCall.getDepartureStatus()) {
                            case CANCELLED:
                                result.append(" is cancelled");
                                break;
                            case DELAYED:
                                result.append(" is delayed");
                                ZonedDateTime expectedDepartureTime = estimatedCall.getExpectedDepartureTime();
                                if (expectedDepartureTime != null) {
                                    result.append(" and expected to depart ").append(expectedDepartureTime.format(formatter));
                                }
                                break;
                        }
                    }
                } else {
                    result.append(" to ").append(name);
                    ZonedDateTime aimedArrivalTime = estimatedCall.getAimedArrivalTime();
                    if (aimedArrivalTime != null) {
                        result.append(" with aimed arrival ").append(aimedArrivalTime.format(formatter));
                    }
                    if (Boolean.TRUE.equals(estimatedCall.isCancellation())) {
                        result.append(" is cancelled");
                    } else {
                        switch (estimatedCall.getArrivalStatus()) {
                            case CANCELLED:
                                result.append(" is cancelled");
                                break;
                            case DELAYED:
                                result.append(" is delayed");
                                ZonedDateTime expectedArrivalTime = estimatedCall.getExpectedArrivalTime();
                                if (expectedArrivalTime != null) {
                                    result.append(" and expected to arrive ").append(expectedArrivalTime.format(formatter));
                                }
                                break;
                        }
                    }
                }
            }
        }
        return result.toString();
    }

    private boolean isDepartingStop(Subscription subscription, StopPointRef stopPointRef) {
        if (stopPointRef == null) {
            logger.error("Got null as StopPointRef...");
        } else if (StringUtils.isBlank(stopPointRef.getValue())) {
            logger.error("Got empty StopPointRef...");
        } else {
            String stop = stopPointRef.getValue().trim();
            ArrayList<String> fromStopPoints = subscription.getFromStopPoints();
            for (String fromStopPoint : fromStopPoints) {
                if (stop.equalsIgnoreCase(StringUtils.trim(fromStopPoint))) {
                    return true;
                }
            }
        }
        return false;
    }


    private class SiriNamespaceFilter extends XMLFilterImpl {
        private static final String NAMESPACE = "http://www.siri.org.uk/siri";
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(NAMESPACE, localName, qName);
        }
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(NAMESPACE, localName, qName, attributes);
        }
    }
}


