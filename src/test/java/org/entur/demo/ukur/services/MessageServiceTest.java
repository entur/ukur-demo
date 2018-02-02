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

import org.entur.demo.ukur.entities.MessageTypeEnum;
import org.entur.demo.ukur.entities.PushMessage;
import org.entur.demo.ukur.entities.ReceivedMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;

import static org.entur.demo.ukur.services.MessageService.MAX_SIZE_PER_SUBSCRIPTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class MessageServiceTest {

    @InjectMocks
    private MessageService messageService;

    @Test
    public void testUnmarshallingWithAndWithoutNamespace() {

        PushMessage pushMessage = new PushMessage();
        pushMessage.setNode("test-node");
        pushMessage.setMessagename("SX test message name");
        final String subscriptionId = "1";
        assertEquals(0, messageService.getMessageCount(subscriptionId));
        pushMessage.setXmlPayload("<?xml version=\"1.0\" ?>\n<PtSituationElement/>");
        messageService.addPushMessage(subscriptionId, pushMessage);
        assertEquals(1, messageService.getMessageCount(subscriptionId));
        assertEquals(1, count(messageService.getMessages(subscriptionId), MessageTypeEnum.SX));
        assertEquals(0, count(messageService.getMessages(subscriptionId), MessageTypeEnum.ET));
        pushMessage.setXmlPayload("<?xml version=\"1.0\" ?>\n<PtSituationElement xmlns=\"http://www.siri.org.uk/siri\"/>");
        messageService.addPushMessage(subscriptionId, pushMessage);
        assertEquals(2, messageService.getMessageCount(subscriptionId));
        assertEquals(2, count(messageService.getMessages(subscriptionId), MessageTypeEnum.SX));
        assertEquals(0, count(messageService.getMessages(subscriptionId), MessageTypeEnum.ET));
        pushMessage.setXmlPayload("<?xml version=\"1.0\" ?>\n<EstimatedVehicleJourney/>");
        messageService.addPushMessage(subscriptionId, pushMessage);
        assertEquals(3, messageService.getMessageCount(subscriptionId));
        assertEquals(2, count(messageService.getMessages(subscriptionId), MessageTypeEnum.SX));
        assertEquals(1, count(messageService.getMessages(subscriptionId), MessageTypeEnum.ET));
        pushMessage.setXmlPayload("<?xml version=\"1.0\" ?>\n<EstimatedVehicleJourney xmlns=\"http://www.siri.org.uk/siri\"/>");
        messageService.addPushMessage(subscriptionId, pushMessage);
        assertEquals(4, messageService.getMessageCount(subscriptionId));
        assertEquals(2, count(messageService.getMessages(subscriptionId), MessageTypeEnum.SX));
        assertEquals(2, count(messageService.getMessages(subscriptionId), MessageTypeEnum.ET));
    }

    private int count(Collection<ReceivedMessage> messages, MessageTypeEnum type) {
        int counter = 0;
        for (ReceivedMessage message : messages) {
            if (type == message.getType()) {
                counter++;
            }
        }
        return counter;
    }

    @Test
    public void verifyEviction() {
        String subscriptionId = "eviction";
        PushMessage pushMessage = new PushMessage();
        pushMessage.setXmlPayload("<data/>");
        assertEquals(0, messageService.getMessageCount(subscriptionId));
        for (int i = 0; i < MAX_SIZE_PER_SUBSCRIPTION+2; i++) {
             messageService.addPushMessage(subscriptionId, pushMessage);
        }
        assertEquals(MAX_SIZE_PER_SUBSCRIPTION, messageService.getMessageCount(subscriptionId));
    }

    @Test
    public void ptSituationElement() {
        PushMessage pushMessage = new PushMessage();
        pushMessage.setNode("test-node");
        pushMessage.setMessagename("SX test message name");
        pushMessage.setXmlPayload("<?xml version=\"1.0\" ?>\n" +
                "<PtSituationElement>\n" +
                "  <CreationTime>2018-02-01T10:55:08+01:00</CreationTime>\n" +
                "  <ParticipantRef>NSB</ParticipantRef>\n" +
                "  <SituationNumber>status-168235041</SituationNumber>\n" +
                "  <Version>1</Version>\n" +
                "  <Source>\n" +
                "    <SourceType>web</SourceType>\n" +
                "  </Source>\n" +
                "  <Progress>published</Progress>\n" +
                "  <ValidityPeriod>\n" +
                "    <StartTime>2018-02-01T00:00:00+01:00</StartTime>\n" +
                "    <EndTime>2018-02-03T00:00:00+01:00</EndTime>\n" +
                "  </ValidityPeriod>\n" +
                "  <UndefinedReason></UndefinedReason>\n" +
                "  <ReportType>incident</ReportType>\n" +
                "  <Keywords></Keywords>\n" +
                "  <Description xml:lang=\"NO\">Vennligst ta neste eller andre tog.</Description>\n" +
                "  <Description xml:lang=\"EN\">Passengers are requested to take the next train.</Description>\n" +
                "</PtSituationElement>");
        String subscriptionId = "SX-1";
        assertEquals(0, messageService.getMessageCount(subscriptionId));
        assertEquals(0, messageService.getMessages(subscriptionId).size());
        messageService.addPushMessage(subscriptionId, pushMessage);
        assertEquals(1, messageService.getMessageCount(subscriptionId));
        ReceivedMessage message = messageService.getMessages(subscriptionId).iterator().next();
        assertEquals(MessageTypeEnum.SX, message.getType());
        assertNotNull(message.getPtSituationElement());
    }

    @Test
    public void estimatedVehicleJourney() {
        PushMessage pushMessage = new PushMessage();
        pushMessage.setNode("test-node");
        pushMessage.setMessagename("SX test message name");
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
        String subscriptionId = "ET-1";
        assertEquals(0, messageService.getMessageCount(subscriptionId));
        messageService.addPushMessage(subscriptionId, pushMessage);
        assertEquals(1, messageService.getMessageCount(subscriptionId));
        ReceivedMessage message = messageService.getMessages(subscriptionId).iterator().next();
        assertEquals(MessageTypeEnum.ET, message.getType());
        assertNotNull(message.getEstimatedVehicleJourney());
    }
}
