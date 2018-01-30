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

    @RequestMapping(path= "/push/{pushId}", method = RequestMethod.POST)
    public PushAcknowledge receivePushMessage(@PathVariable("pushId")String pushId, @RequestBody PushMessage pushMessage) {
        String msg;
        if (pushMessage == null) {
            msg = "null";
        } else {
            msg =   "messagename= '" + pushMessage.getMessagename()+ "\'\n" +
                    "node       = '" + pushMessage.getNode() + "\'\n" +
                    "xmlPayload = '" + pushMessage.getXmlPayload() + '\'';
        }
        logger.debug("Called with pushId='{}' and PushMessage:\n{}", pushId, msg);
        Subscription subscription = subscriptionService.getByPushId(pushId);
        if (subscription == null) {
            logger.warn("Received push message for unknown push id '{}' - responds {}", pushId, PushAcknowledge.FORGET_ME);
            return PushAcknowledge.FORGET_ME;
        } else {
            logger.info("Received new push message for pushId={} and subscriptionId={}", pushId, subscription.getId());
            messageService.addPushMessage(subscription.getId(), pushMessage);
            return PushAcknowledge.OK;
        }
    }
}
