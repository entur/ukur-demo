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
import org.entur.demo.ukur.entities.ReceivedMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.org.siri.siri20.EstimatedVehicleJourney;
import uk.org.siri.siri20.PtSituationElement;

import static org.entur.demo.ukur.services.MessageService.MAX_SIZE_PER_SUBSCRIPTION;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageServiceTest {

    @Mock @SuppressWarnings("unused")
    private SubscriptionService subscriptionServiceMock;

    @InjectMocks
    private MessageService messageService;

    @Test
    public void verifyEviction() {
        String subscriptionId = "eviction";
        PtSituationElement pushMessage = new PtSituationElement();
        assertEquals(0, messageService.getMessageCount(subscriptionId));
        for (int i = 0; i < MAX_SIZE_PER_SUBSCRIPTION+2; i++) {
            messageService.addPushMessage(subscriptionId, pushMessage);
        }
        assertEquals(MAX_SIZE_PER_SUBSCRIPTION, messageService.getMessageCount(subscriptionId));
    }

    @Test
    public void ptSituationElement() {
        String subscriptionId = "SX-1";
        assertEquals(0, messageService.getMessageCount(subscriptionId));
        assertEquals(0, messageService.getMessages(subscriptionId).size());
        PtSituationElement pushMessage = new PtSituationElement();
        messageService.addPushMessage(subscriptionId, pushMessage);
        assertEquals(1, messageService.getMessageCount(subscriptionId));
        ReceivedMessage message = messageService.getMessages(subscriptionId).iterator().next();
        assertEquals(MessageTypeEnum.SX, message.getType());
        assertNotNull(message.getPtSituationElement());
        assertNotNull(message.getXmlString());
        assertNull(message.getEstimatedVehicleJourney());
    }

    @Test
    public void estimatedVehicleJourney() {
        String subscriptionId = "ET-1";
        assertEquals(0, messageService.getMessageCount(subscriptionId));
        EstimatedVehicleJourney pushMessage = new EstimatedVehicleJourney();
        messageService.addPushMessage(subscriptionId, pushMessage);
        assertEquals(1, messageService.getMessageCount(subscriptionId));
        ReceivedMessage message = messageService.getMessages(subscriptionId).iterator().next();
        assertEquals(MessageTypeEnum.ET, message.getType());
        assertNotNull(message.getEstimatedVehicleJourney());
        assertNotNull(message.getXmlString());
        assertNull(message.getPtSituationElement());
    }
}
