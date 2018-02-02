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
import org.entur.demo.ukur.entities.ReceivedMessage;
import org.entur.demo.ukur.entities.Subscription;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HumanReadableTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Mock
    private SubscriptionService subscriptionServiceMock;

    @InjectMocks
    private MessageService messageService;


    @Test
    public void testDelayedTo() {
        PushMessage pushMessage = getEtMessage();
        String subscriptionId = UUID.randomUUID().toString();
        when(subscriptionServiceMock.get(subscriptionId)).thenReturn(new Subscription());
        messageService.addPushMessage(subscriptionId, pushMessage);
        Collection<ReceivedMessage> messages = messageService.getMessages(subscriptionId);
        assertEquals(1, messages.size());
        ReceivedMessage message = messages.iterator().next();
        logger.info(message.getHumanReadable());
        assertEquals("Line L1 towards Lillestrøm to Oslo S with aimed arrival 10:39 is delayed and expected to arrive 10:44", message.getHumanReadable());
    }

    @Test
    public void testDelayedFrom() {
        PushMessage pushMessage = getEtMessage();
        String subscriptionId = UUID.randomUUID().toString();
        Subscription subscription = new Subscription();
        subscription.addFromStopPoint("NSR:Quay:555");
        when(subscriptionServiceMock.get(subscriptionId)).thenReturn(subscription);
        messageService.addPushMessage(subscriptionId, pushMessage);
        Collection<ReceivedMessage> messages = messageService.getMessages(subscriptionId);
        assertEquals(1, messages.size());
        ReceivedMessage message = messages.iterator().next();
        logger.info(message.getHumanReadable());
        assertEquals("Line L1 towards Lillestrøm from Oslo S with aimed departure 10:41 is delayed and expected to depart 10:45", message.getHumanReadable());
    }

    @Test
    public void testPtSituationElement() {
        PushMessage pushMessage = new PushMessage();
        pushMessage.setXmlPayload("<?xml version=\"1.0\" ?>\n" +
                "<PtSituationElement>\n" +
                "  <CreationTime>2018-02-02T12:19:31+01:00</CreationTime>\n" +
                "  <ParticipantRef>NSB</ParticipantRef>\n" +
                "  <SituationNumber>status-168267394</SituationNumber>\n" +
                "  <Version>1</Version>\n" +
                "  <Source>\n" +
                "    <SourceType>web</SourceType>\n" +
                "  </Source>\n" +
                "  <Progress>published</Progress>\n" +
                "  <ValidityPeriod>\n" +
                "    <StartTime>2018-02-02T00:00:00+01:00</StartTime>\n" +
                "    <EndTime>2018-02-04T00:00:00+01:00</EndTime>\n" +
                "  </ValidityPeriod>\n" +
                "  <UndefinedReason></UndefinedReason>\n" +
                "  <ReportType>incident</ReportType>\n" +
                "  <Keywords></Keywords>\n" +
                "  <Description xml:lang=\"NO\">Vennligst ta neste eller andre tog.</Description>\n" +
                "  <Description xml:lang=\"EN\">Passengers are requested to take the next train.</Description>\n" +
                "</PtSituationElement>");
        String subscriptionId = UUID.randomUUID().toString();
        Subscription subscription = new Subscription();
        subscription.addFromStopPoint("NSR:Quay:555");
        when(subscriptionServiceMock.get(subscriptionId)).thenReturn(subscription);
        messageService.addPushMessage(subscriptionId, pushMessage);
        Collection<ReceivedMessage> messages = messageService.getMessages(subscriptionId);
        assertEquals(1, messages.size());
        ReceivedMessage message = messages.iterator().next();
        logger.info(message.getHumanReadable());
    }

    private PushMessage getEtMessage() {
        PushMessage pushMessage = new PushMessage();
        pushMessage.setXmlPayload("<?xml version=\"1.0\" ?>\n" +
                "<EstimatedVehicleJourney xmlns=\"http://www.siri.org.uk/siri\">\n" +
                "  <LineRef>NSB:Line:L1</LineRef>\n" +
                "  <DirectionRef>Lillestrøm</DirectionRef>\n" +
                "  <DatedVehicleJourneyRef>2224:2018-02-01</DatedVehicleJourneyRef>\n" +
                "  <VehicleMode>rail</VehicleMode>\n" +
                "  <OriginName>Spikkestad</OriginName>\n" +
                "  <OriginShortName>SPI</OriginShortName>\n" +
                "  <OperatorRef>NSB</OperatorRef>\n" +
                "  <ProductCategoryRef>Lt</ProductCategoryRef>\n" +
                "  <ServiceFeatureRef>passengerTrain</ServiceFeatureRef>\n" +
                "  <VehicleRef>2224</VehicleRef>\n" +
                "  <EstimatedCalls>\n" +
                "    <EstimatedCall>\n" +
                "      <StopPointRef>NSR:Quay:555</StopPointRef>\n" +
                "      <StopPointName>Oslo S</StopPointName>\n" +
                "      <RequestStop>false</RequestStop>\n" +
                "      <AimedArrivalTime>2018-02-01T10:39:00+01:00</AimedArrivalTime>\n" +
                "      <ExpectedArrivalTime>2018-02-01T10:44:54+01:00</ExpectedArrivalTime>\n" +
                "      <ArrivalStatus>delayed</ArrivalStatus>\n" +
                "      <ArrivalPlatformName>9</ArrivalPlatformName>\n" +
                "      <ArrivalBoardingActivity>alighting</ArrivalBoardingActivity>\n" +
                "      <AimedDepartureTime>2018-02-01T10:41:00+01:00</AimedDepartureTime>\n" +
                "      <ExpectedDepartureTime>2018-02-01T10:45:24+01:00</ExpectedDepartureTime>\n" +
                "      <DepartureStatus>delayed</DepartureStatus>\n" +
                "      <DeparturePlatformName>9</DeparturePlatformName>\n" +
                "      <DepartureBoardingActivity>boarding</DepartureBoardingActivity>\n" +
                "    </EstimatedCall>\n" +
                "  </EstimatedCalls>\n" +
                "</EstimatedVehicleJourney>");
        return pushMessage;
    }


}
