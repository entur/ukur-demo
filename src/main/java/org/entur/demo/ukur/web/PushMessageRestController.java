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

package org.entur.demo.ukur.web;

import org.entur.demo.ukur.entities.Subscription;
import org.entur.demo.ukur.services.MessageService;
import org.entur.demo.ukur.services.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.org.siri.siri20.EstimatedVehicleJourney;
import uk.org.siri.siri20.PtSituationElement;

@RestController
public class PushMessageRestController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MessageService messageService;

    private final SubscriptionService subscriptionService;

    @Autowired
    public PushMessageRestController(MessageService messageService, SubscriptionService subscriptionService) {
        this.messageService = messageService;
        this.subscriptionService = subscriptionService;
    }

    @RequestMapping(path = "/push/{pushId}/et", method = RequestMethod.POST,
            consumes = "application/xml")
    public ResponseEntity estimatedVehicleJourney(@PathVariable("pushId") String pushId, @RequestBody EstimatedVehicleJourney estimatedVehicleJourney) {
        logger.debug("Called with pushId='{}'", pushId);
        return handlePush(pushId, estimatedVehicleJourney, null);
    }

    @RequestMapping(path = "/push/{pushId}/sx", method = RequestMethod.POST,
            consumes = "application/xml")
    public ResponseEntity ptSituationElement(@PathVariable("pushId") String pushId, @RequestBody PtSituationElement ptSituationElement) {
        logger.debug("Called with pushId='{}'", pushId);
        return handlePush(pushId, null, ptSituationElement);
    }

    private ResponseEntity handlePush(String pushId, EstimatedVehicleJourney estimatedVehicleJourney, PtSituationElement ptSituationElement) {
        Subscription subscription = subscriptionService.getByPushId(pushId);
        if (subscription == null) {
            logger.warn("Received push message for unknown push id '{}' - responds {}", pushId, HttpStatus.RESET_CONTENT);
            return new ResponseEntity(HttpStatus.RESET_CONTENT);
        } else {
            if (estimatedVehicleJourney != null) {
                logger.info("Received new EstimatedVehicleJourney push message for pushId={} and subscriptionId={}", pushId, subscription.getId());
                messageService.addPushMessage(subscription.getId(), estimatedVehicleJourney);
            }
            if (ptSituationElement != null) {
                logger.info("Received new PtSituationElement push message for pushId={} and subscriptionId={}", pushId, subscription.getId());
                messageService.addPushMessage(subscription.getId(), ptSituationElement);
            }
            return new ResponseEntity(HttpStatus.OK);
        }
    }

    @RequestMapping(path = "/health/ready")
    public String ready() {
        return "OK";
    }

    @RequestMapping(path = "/health/live")
    public String live() {
        return "OK";
    }
}
