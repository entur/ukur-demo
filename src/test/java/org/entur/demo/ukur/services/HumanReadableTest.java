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

import org.entur.demo.ukur.entities.ReceivedMessage;
import org.entur.demo.ukur.entities.Subscription;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.siri.siri20.EstimatedVehicleJourney;
import uk.org.siri.siri20.PtSituationElement;
import uk.org.siri.siri20.Siri;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class HumanReadableTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @InjectMocks
    private MessageService messageService;

    @Test
    public void testDelayedTo() throws JAXBException {
        String subscriptionId = UUID.randomUUID().toString();
        messageService.addPushMessage(subscriptionId, getEtMessage("<?xml version=\"1.0\" ?>\n" +
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
                "</EstimatedVehicleJourney>"));
        Collection<ReceivedMessage> messages = messageService.getMessages(subscriptionId);
        assertEquals(1, messages.size());
        ReceivedMessage message = messages.iterator().next();
        logger.info(message.getHumanReadable());
        assertEquals("Line L1 towards Lillestrøm to Oslo S with aimed arrival 10:39 is delayed and expected to arrive 10:44", message.getHumanReadable());
    }

    @Test
    public void testExpectedDelayedFromAndTo() throws JAXBException {
        String subscriptionId = UUID.randomUUID().toString();
        Subscription subscription = new Subscription();
        subscription.addFromStopPoint("NSR:Quay:555");
        messageService.addPushMessage(subscriptionId, getEtMessage("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<EstimatedVehicleJourney xmlns=\"http://www.siri.org.uk/siri\" xmlns:ns2=\"http://www.ifopt.org.uk/acsb\" xmlns:ns4=\"http://datex2.eu/schema/2_0RC1/2_0\" xmlns:ns3=\"http://www.ifopt.org.uk/ifopt\">\n" +
                "    <LineRef>NSB:Line:R11</LineRef>\n" +
                "    <DirectionRef>Larvik</DirectionRef>\n" +
                "    <DatedVehicleJourneyRef>813:2018-02-07</DatedVehicleJourneyRef>\n" +
                "    <VehicleMode>rail</VehicleMode>\n" +
                "    <OriginName>Eidsvoll</OriginName>\n" +
                "    <OriginShortName>EVL</OriginShortName>\n" +
                "    <OperatorRef>NSB</OperatorRef>\n" +
                "    <ProductCategoryRef>IC</ProductCategoryRef>\n" +
                "    <ServiceFeatureRef>passengerTrain</ServiceFeatureRef>\n" +
                "    <VehicleRef>813</VehicleRef>\n" +
                "    <RecordedCalls/>\n" +
                "    <EstimatedCalls>\n" +
                "        <EstimatedCall>\n" +
                "            <StopPointRef>NSR:Quay:559</StopPointRef>\n" +
                "            <StopPointName>Oslo S</StopPointName>\n" +
                "            <RequestStop>false</RequestStop>\n" +
                "            <AimedArrivalTime>2018-02-07T10:36:00+01:00</AimedArrivalTime>\n" +
                "            <ExpectedArrivalTime>2018-02-07T11:04:34+01:00</ExpectedArrivalTime>\n" +
                "            <ArrivalStatus>delayed</ArrivalStatus>\n" +
                "            <ArrivalPlatformName>5</ArrivalPlatformName>\n" +
                "            <ArrivalBoardingActivity>alighting</ArrivalBoardingActivity>\n" +
                "            <AimedDepartureTime>2018-02-07T10:39:00+01:00</AimedDepartureTime>\n" +
                "            <ExpectedDepartureTime>2018-02-07T11:05:04+01:00</ExpectedDepartureTime>\n" +
                "            <DepartureStatus>delayed</DepartureStatus>\n" +
                "            <DeparturePlatformName>5</DeparturePlatformName>\n" +
                "            <DepartureBoardingActivity>boarding</DepartureBoardingActivity>\n" +
                "            <Extensions>\n" +
                "                <StopsAtAirport>true</StopsAtAirport>\n" +
                "            </Extensions>\n" +
                "        </EstimatedCall>\n" +
                "        <EstimatedCall>\n" +
                "            <StopPointRef>NSR:Quay:698</StopPointRef>\n" +
                "            <StopPointName>Asker</StopPointName>\n" +
                "            <RequestStop>false</RequestStop>\n" +
                "            <AimedArrivalTime>2018-02-07T10:59:00+01:00</AimedArrivalTime>\n" +
                "            <ExpectedArrivalTime>2018-02-07T11:21:03+01:00</ExpectedArrivalTime>\n" +
                "            <ArrivalStatus>delayed</ArrivalStatus>\n" +
                "            <ArrivalPlatformName>1</ArrivalPlatformName>\n" +
                "            <ArrivalBoardingActivity>alighting</ArrivalBoardingActivity>\n" +
                "            <AimedDepartureTime>2018-02-07T11:00:00+01:00</AimedDepartureTime>\n" +
                "            <ExpectedDepartureTime>2018-02-07T11:21:33+01:00</ExpectedDepartureTime>\n" +
                "            <DepartureStatus>delayed</DepartureStatus>\n" +
                "            <DeparturePlatformName>1</DeparturePlatformName>\n" +
                "            <DepartureBoardingActivity>boarding</DepartureBoardingActivity>\n" +
                "            <Extensions>\n" +
                "                <StopsAtAirport>true</StopsAtAirport>\n" +
                "            </Extensions>\n" +
                "        </EstimatedCall>\n" +
                "    </EstimatedCalls>\n" +
                "</EstimatedVehicleJourney>"));
        Collection<ReceivedMessage> messages = messageService.getMessages(subscriptionId);
        assertEquals(1, messages.size());
        ReceivedMessage message = messages.iterator().next();
        logger.info(message.getHumanReadable());
        assertEquals("Line R11 towards Larvik from Oslo S 10:39 is delayed and expected to depart 11:05 to Asker with aimed arrival 10:59 is delayed and expected to arrive 11:21", message.getHumanReadable());
    }

    @Test
    public void testRecordedFromAndEstimatedDeleayedTo() throws JAXBException {
        String subscriptionId = UUID.randomUUID().toString();
        Subscription subscription = new Subscription();
        subscription.addFromStopPoint("NSR:Quay:555");
        messageService.addPushMessage(subscriptionId, getEtMessage("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<EstimatedVehicleJourney xmlns=\"http://www.siri.org.uk/siri\" xmlns:ns2=\"http://www.ifopt.org.uk/acsb\" xmlns:ns4=\"http://datex2.eu/schema/2_0RC1/2_0\" xmlns:ns3=\"http://www.ifopt.org.uk/ifopt\">\n" +
                "    <LineRef>NSB:Line:R10</LineRef>\n" +
                "    <DirectionRef>Lillehammer</DirectionRef>\n" +
                "    <DatedVehicleJourneyRef>313:2018-02-07</DatedVehicleJourneyRef>\n" +
                "    <VehicleMode>rail</VehicleMode>\n" +
                "    <OriginName>Drammen</OriginName>\n" +
                "    <OriginShortName>DRM</OriginShortName>\n" +
                "    <OperatorRef>NSB</OperatorRef>\n" +
                "    <ProductCategoryRef>IC</ProductCategoryRef>\n" +
                "    <ServiceFeatureRef>passengerTrain</ServiceFeatureRef>\n" +
                "    <VehicleRef>313</VehicleRef>\n" +
                "    <RecordedCalls>\n" +
                "        <RecordedCall>\n" +
                "            <StopPointRef>NSR:Quay:697</StopPointRef>\n" +
                "            <StopPointName>Asker</StopPointName>\n" +
                "            <AimedArrivalTime>2018-02-07T10:08:00+01:00</AimedArrivalTime>\n" +
                "            <ExpectedArrivalTime>2018-02-07T10:08:00+01:00</ExpectedArrivalTime>\n" +
                "            <ActualArrivalTime>2018-02-07T10:08:00+01:00</ActualArrivalTime>\n" +
                "            <ArrivalPlatformName>3</ArrivalPlatformName>\n" +
                "            <AimedDepartureTime>2018-02-07T10:09:00+01:00</AimedDepartureTime>\n" +
                "            <ExpectedDepartureTime>2018-02-07T10:09:00+01:00</ExpectedDepartureTime>\n" +
                "            <DeparturePlatformName>3</DeparturePlatformName>\n" +
                "            <ActualDepartureTime>2018-02-07T10:09:00+01:00</ActualDepartureTime>\n" +
                "        </RecordedCall>\n" +
                "    </RecordedCalls>\n" +
                "    <EstimatedCalls>\n" +
                "        <EstimatedCall>\n" +
                "            <StopPointRef>NSR:Quay:571</StopPointRef>\n" +
                "            <StopPointName>Oslo S</StopPointName>\n" +
                "            <RequestStop>false</RequestStop>\n" +
                "            <AimedArrivalTime>2018-02-07T10:31:00+01:00</AimedArrivalTime>\n" +
                "            <ExpectedArrivalTime>2018-02-07T10:48:34+01:00</ExpectedArrivalTime>\n" +
                "            <ArrivalStatus>delayed</ArrivalStatus>\n" +
                "            <ArrivalPlatformName>11</ArrivalPlatformName>\n" +
                "            <ArrivalBoardingActivity>alighting</ArrivalBoardingActivity>\n" +
                "            <AimedDepartureTime>2018-02-07T10:34:00+01:00</AimedDepartureTime>\n" +
                "            <ExpectedDepartureTime>2018-02-07T10:49:04+01:00</ExpectedDepartureTime>\n" +
                "            <DepartureStatus>delayed</DepartureStatus>\n" +
                "            <DeparturePlatformName>11</DeparturePlatformName>\n" +
                "            <DepartureBoardingActivity>boarding</DepartureBoardingActivity>\n" +
                "            <Extensions>\n" +
                "                <StopsAtAirport>true</StopsAtAirport>\n" +
                "            </Extensions>\n" +
                "        </EstimatedCall>\n" +
                "    </EstimatedCalls>\n" +
                "</EstimatedVehicleJourney>"));
        Collection<ReceivedMessage> messages = messageService.getMessages(subscriptionId);
        assertEquals(1, messages.size());
        ReceivedMessage message = messages.iterator().next();
        logger.info(message.getHumanReadable());
        assertEquals("Line R10 towards Lillehammer from Asker 10:09 to Oslo S with aimed arrival 10:31 is delayed and expected to arrive 10:48", message.getHumanReadable());
    }

    @Test
    public void testPtSituationElement() throws JAXBException {
        String subscriptionId = UUID.randomUUID().toString();
        Subscription subscription = new Subscription();
        subscription.addFromStopPoint("NSR:Quay:555");
        messageService.addPushMessage(subscriptionId, getSxMessage());
        Collection<ReceivedMessage> messages = messageService.getMessages(subscriptionId);
        assertEquals(1, messages.size());
        ReceivedMessage message = messages.iterator().next();
        logger.info(message.getHumanReadable());
    }

    private PtSituationElement getSxMessage() throws JAXBException {
        String xml = "<?xml version=\"1.0\" ?>\n" +
                "<PtSituationElement xmlns=\"http://www.siri.org.uk/siri\">\n" +
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
                "</PtSituationElement>";
        JAXBContext jaxbContext = JAXBContext.newInstance(Siri.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (PtSituationElement) unmarshaller.unmarshal(new StringReader(xml));
    }

    private EstimatedVehicleJourney getEtMessage(String xml) throws JAXBException {

        JAXBContext jaxbContext = JAXBContext.newInstance(Siri.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (EstimatedVehicleJourney) unmarshaller.unmarshal(new StringReader(xml));
    }


}
