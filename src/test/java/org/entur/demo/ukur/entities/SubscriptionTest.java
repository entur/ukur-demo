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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.siri.siri20.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class SubscriptionTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testJSON() throws IOException {
        Subscription s = new Subscription();
        s.setInitialTerminationTime(ZonedDateTime.now().plusWeeks(1));
        s.setHeartbeatInterval("PT10M");
        s.setName("My Test Subscription");
        s.setType(SubscriptionTypeEnum.ALL);
        s.setPushAddress("http://someserver:someport/push");
        s.setUseSiriSubscriptionModel(true);
        s.addCodespace("BNR");
        s.addLineRef("NSB:Line:L14");
        s.addLineRef("NSB:Line:L13");
        s.addLineRef("NSB:Line:L12");
        String json = s.toJSON();
        logger.info("JSON\n{}", json);
        assertNotNull(json);
        ObjectMapper mapper = new ObjectMapper();

        //ensure it is proper json by unmarshalling it:
        Subscription jsonSubscription = mapper.readValue(json, Subscription.class);
        assertNotNull(jsonSubscription);
        assertEquals(s.getInitialTerminationTime(), jsonSubscription.getInitialTerminationTime());
        assertEquals(s.getHeartbeatInterval(), jsonSubscription.getHeartbeatInterval());
        assertEquals(s.getName(), jsonSubscription.getName());
        assertEquals(s.getType(), jsonSubscription.getType());
        assertEquals(s.isUseSiriSubscriptionModel(), jsonSubscription.isUseSiriSubscriptionModel());
        assertThat(jsonSubscription.getCodespaces(), containsInAnyOrder(s.getCodespaces().toArray()));
        assertThat(jsonSubscription.getLineRefs(), containsInAnyOrder(s.getLineRefs().toArray()));
        assertEquals(s.getName(), jsonSubscription.getName());
    }


    @Test
    public void testXML() throws JAXBException {
        Subscription s = new Subscription();
        ZonedDateTime time = ZonedDateTime.now().plusWeeks(1).truncatedTo(ChronoUnit.MILLIS);
        s.setInitialTerminationTime(time);
        s.setHeartbeatInterval("PT30M");
        s.setName("My Other Test Subscription");
        s.setType(SubscriptionTypeEnum.ALL);
        s.setPushAddress("http://someotherserver:someotherport/push");
        s.setUseSiriSubscriptionModel(true);
        s.addCodespace("RUT");
        s.addLineRef("RUT:Line:L4");
        s.addLineRef("RUT:Line:L3");
        s.addLineRef("RUT:Line:L2");
        String xml = s.toXML();
        logger.info("XML\n{}", xml);
        assertNotNull(xml);

        //ensure it is proper xml by unmarshalling it:
        JAXBContext jaxbContext = JAXBContext.newInstance(Siri.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Siri siri = (Siri) unmarshaller.unmarshal(new StringReader(xml));
        assertNotNull(siri);
        SubscriptionRequest subscriptionRequest = siri.getSubscriptionRequest();
        assertNotNull(subscriptionRequest);
        assertEquals(s.getPushAddress(), subscriptionRequest.getAddress());
        assertNotNull(subscriptionRequest.getRequestorRef());
        assertEquals("CLIENT-SPECIFIED-REQUESTOR_REF", subscriptionRequest.getRequestorRef().getValue()); //Hardcoded value...
        assertNotNull(subscriptionRequest.getSubscriptionContext());
        assertNotNull(subscriptionRequest.getSubscriptionContext().getHeartbeatInterval());
        assertEquals(s.getHeartbeatInterval(), subscriptionRequest.getSubscriptionContext().getHeartbeatInterval().toString());

        assertNotNull(subscriptionRequest.getSituationExchangeSubscriptionRequests());
        assertEquals(1, subscriptionRequest.getSituationExchangeSubscriptionRequests().size());
        SituationExchangeSubscriptionStructure situationExchangeSubscriptionStructure = subscriptionRequest.getSituationExchangeSubscriptionRequests().get(0);
        assertEquals(time, situationExchangeSubscriptionStructure.getInitialTerminationTime());
        assertNotNull(situationExchangeSubscriptionStructure.getSubscriptionIdentifier());
        assertEquals("CLIENT-SPECIFIED-SUBSCRIPTION_ID_1", situationExchangeSubscriptionStructure.getSubscriptionIdentifier().getValue()); //Hardcoded value...
        SituationExchangeRequestStructure situationExchangeRequest = situationExchangeSubscriptionStructure.getSituationExchangeRequest();
        assertNotNull(situationExchangeRequest);
        List<LineRef> lineReves = situationExchangeRequest.getLineReves();
        assertNotNull(lineReves);
        assertThat(lineReves.stream().map(LineRef::getValue).collect(Collectors.toList()), containsInAnyOrder(s.getLineRefs().toArray()));

        assertNotNull(subscriptionRequest.getEstimatedTimetableSubscriptionRequests());
        assertEquals(1, subscriptionRequest.getEstimatedTimetableSubscriptionRequests().size());
        EstimatedTimetableSubscriptionStructure estimatedTimetableSubscriptionStructure = subscriptionRequest.getEstimatedTimetableSubscriptionRequests().get(0);
        assertEquals(time, estimatedTimetableSubscriptionStructure.getInitialTerminationTime());
        assertNotNull(estimatedTimetableSubscriptionStructure.getSubscriptionIdentifier());
        assertEquals("CLIENT-SPECIFIED-SUBSCRIPTION_ID_2", estimatedTimetableSubscriptionStructure.getSubscriptionIdentifier().getValue()); //Hardcoded value...
        EstimatedTimetableRequestStructure estimatedTimetableRequest = estimatedTimetableSubscriptionStructure.getEstimatedTimetableRequest();
        assertNotNull(estimatedTimetableRequest);
        assertNotNull(estimatedTimetableRequest.getLines());
        List<LineDirectionStructure> lineDirections = estimatedTimetableRequest.getLines().getLineDirections();
        assertNotNull(lineDirections);
        List<LineRef> lineRefs = lineDirections.stream().map(LineDirectionStructure::getLineRef).collect(Collectors.toList());
        assertThat(lineRefs.stream().map(LineRef::getValue).collect(Collectors.toList()), containsInAnyOrder(s.getLineRefs().toArray()));

    }
}
