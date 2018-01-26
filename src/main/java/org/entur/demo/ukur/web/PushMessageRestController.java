package org.entur.demo.ukur.web;

import org.entur.demo.ukur.entities.PushAcknowledge;
import org.entur.demo.ukur.entities.PushMessage;
import org.entur.demo.ukur.entities.Subscription;
import org.entur.demo.ukur.services.MessageService;
import org.entur.demo.ukur.services.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class PushMessageRestController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MessageService messageService;

    @Autowired
    private SubscriptionService subscriptionService;

    @RequestMapping(path="/push/{subscriptionId}", method = RequestMethod.POST)
    public PushAcknowledge receivePushMessage(@PathVariable("subscriptionId")String subscriptionId, @RequestBody PushMessage pushMessage) {
        String msg;
        if (pushMessage == null) {
            msg = "null";
        } else {
            msg =   "messagename= '" + pushMessage.getMessagename()+ "\'\n" +
                    "node       = '" + pushMessage.getNode() + "\'\n" +
                    "xmlPayload = '" + pushMessage.getXmlPayload() + '\'';
        }
        logger.debug("Called with subscriptionId='{}' and PushMessage:\n{}", subscriptionId, msg);
        Subscription subscription = subscriptionService.get(subscriptionId);
        if (subscription == null) {
            logger.warn("Received push message for unknown subscription id '{}' - responds {}", subscriptionId, PushAcknowledge.FORGET_ME);
            return PushAcknowledge.FORGET_ME;
        } else {
            logger.info("Received new push message for subscription id '{}'", subscriptionId);
            messageService.addPushMessage(subscriptionId, pushMessage);
            return PushAcknowledge.OK;
        }
    }
}
