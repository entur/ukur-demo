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
import org.entur.demo.ukur.entities.PushMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLFilterImpl;
import uk.org.siri.siri20.EstimatedVehicleJourney;
import uk.org.siri.siri20.LineRef;
import uk.org.siri.siri20.PtSituationElement;
import uk.org.siri.siri20.Siri;

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
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class MessageService {

    private static final int MAX_SIZE_PER_SUBSCRIPTION = 500;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JAXBContext jaxbContext;
    private HashMap<String, Collection<PushMessage>> messageStore = new HashMap<>();
    private HashMap<String, Collection<PtSituationElement>> ptSituationElements = new HashMap<>();
    private HashMap<String, Collection<EstimatedVehicleJourney>> estimatedVehicleJourneys = new HashMap<>();
    private HashMap<String, LocalDateTime> lastMessageReceived = new HashMap<>();

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

    public void addPushMessage(String subscriptionId, PushMessage pushMessage) {
        CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.DAYS).build();
        Collection<PushMessage> pushMessages = messageStore.computeIfAbsent(subscriptionId, k -> EvictingQueue.create(MAX_SIZE_PER_SUBSCRIPTION));
        pushMessages.add(pushMessage);
        lastMessageReceived.put(subscriptionId, LocalDateTime.now());

        String xmlPayload = pushMessage.getXmlPayload();
        //TODO: Have ukur push the xml element directly (as xml) - and let the server deal with this...
        if (xmlPayload == null) {
            logger.error("Empty XML payload for subscriptionId {}", subscriptionId);
        } else if (xmlPayload.contains("<EstimatedVehicleJourney")) {
            try {
                handle(subscriptionId, unmarshalSiri(xmlPayload, EstimatedVehicleJourney.class));
            } catch (Exception e) {
                logger.error("Could not unmarshall xmlpayload as EstimatedVehicleJourney", e);
            }
        } else if (xmlPayload.contains("<PtSituationElement")) {
            try {
                handle(subscriptionId, unmarshalSiri(xmlPayload, PtSituationElement.class));
            } catch (Exception e) {
                logger.error("Could not unmarshall xmlpayload as PtSituationElement", e);
            }
        } else {
            logger.warn("Unknown xml payload: \n{}", xmlPayload);
        }

    }

    public Collection<PtSituationElement> getPtSituationElements(String id) {
        return ptSituationElements.getOrDefault(id, Collections.emptyList());
    }

    public Collection<EstimatedVehicleJourney> getEstimatedVehicleJourneys(String id) {
        return estimatedVehicleJourneys.getOrDefault(id, Collections.emptyList());
    }

    public Collection<PushMessage> getMessages(String subscriptionId) {
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

    private void handle(String subscriptionId, EstimatedVehicleJourney estimatedVehicleJourney) {
        logger.info("Handles an EstimatedVehicleJourney");
        Collection<EstimatedVehicleJourney> estimatedVehicleJourneys = this.estimatedVehicleJourneys.computeIfAbsent(subscriptionId, k -> EvictingQueue.create(MAX_SIZE_PER_SUBSCRIPTION));
        estimatedVehicleJourneys.add(estimatedVehicleJourney);
        //TODO: behandle meldingen skikkelig:
        /*
        - samle meldinger for en linje
        - automatisk fjerne meldinger som er gamle
        - tolke meldinger på en lett leselig måte
         */
        LineRef lineRef = estimatedVehicleJourney.getLineRef();
    }

    private void handle(String subscriptionId, PtSituationElement ptSituationElement) {
        logger.info("Handles a PtSituationElement");
        Collection<PtSituationElement> ptSituationElements = this.ptSituationElements.computeIfAbsent(subscriptionId, k -> EvictingQueue.create(MAX_SIZE_PER_SUBSCRIPTION));
        ptSituationElements.add(ptSituationElement);
        //TODO: implement
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
