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

import org.entur.demo.ukur.entities.Subscription;
import org.entur.demo.ukur.entities.SubscriptionTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;

@Service
public class SubscriptionService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String ukurURL;
    private String pushURL;
    private long idCounter = 0;
    private final String basePushId;//need some uniqueness so we don't reuse push addresses at restart

    private HashMap<String, Subscription> subscriptions = new HashMap<>();

    public SubscriptionService(@Value("${ukur.subscription.url}") String ukurURL,
                               @Value("${push.baseurl}") String pushURL) {
        basePushId = Long.toString(System.currentTimeMillis(), Character.MAX_RADIX)+"-";
        logger.info("Started with ukurURL={}, pushURL={} and basePushId={}", ukurURL, pushURL, basePushId);
        this.ukurURL = ukurURL;
        this.pushURL = pushURL;
        if (!pushURL.endsWith("/")) {
            this.pushURL = pushURL + "/";
        }
        int delay = 20;
        logger.info("Delays addition of test subscriptions by {} seconds", delay);
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        logger.info("Adds test subscriptions after {} seconds", delay);
                        addTestSubscriptions();
                    }
                },
                delay*1000);
    }

    public Collection<Subscription> list() {
        return Collections.unmodifiableCollection(subscriptions.values());
    }

    public Subscription get(String id) {
        if (id != null) {
            for (Subscription subscription : subscriptions.values()) {
                if (id.equals(subscription.getId())) {
                    return subscription;
                }
            }
        }
        return null;
    }

    public Subscription getByPushId(String id) {
        return subscriptions.get(id);
    }

    public boolean add(final Subscription subscription) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            URI url = URI.create(ukurURL);
            String pushId = basePushId + idCounter++;
            subscription.setPushAddress(pushURL+pushId);
            Subscription returnedSubscription = restTemplate.postForObject(url, subscription, Subscription.class);
            if (returnedSubscription == null) {
                logger.error("Adding subscription failed...");
            } else {
                logger.info("Added subscription at Ukur, received subscription id {}", returnedSubscription.getId());
                returnedSubscription.setPushId(pushId); //not part of Ukur's subscription
                subscriptions.put(pushId, returnedSubscription); //uses returned subscription since it is normalized
            }
            return true;
        } catch (Exception e) {
            logger.error("Could not add new subscription", e);
        }
        return false;
    }

    public void remove(String id) {
        Subscription subscription = get(id);
        if (subscription != null) {
            subscriptions.remove(subscription.getPushId());
            RestTemplate restTemplate = new RestTemplate();
            try {
                URI url = URI.create(ukurURL + "/" + id);
                logger.debug("Removes subscription with delete to url {}", url);
                restTemplate.delete(url);
            } catch (Exception e) {
                logger.warn("Could not remove subscription with id {}", id, e);
            }
        }
    }


    private void addTestSubscriptions() {
        Subscription askerOslo1 = new Subscription();
        askerOslo1.setName("Asker-OsloS #1");
        askerOslo1.addFromStopPoint("NSR:StopPlace:418");
        askerOslo1.addFromStopPoint("NSR:Quay:695");
        askerOslo1.addFromStopPoint("NSR:Quay:696");
        askerOslo1.addFromStopPoint("NSR:Quay:697");
        askerOslo1.addFromStopPoint("NSR:Quay:698");
        askerOslo1.addFromStopPoint("NSR:Quay:699");
        askerOslo1.addFromStopPoint("NSR:Quay:700");
        askerOslo1.addToStopPoint("NSR:StopPlace:337");
        askerOslo1.addToStopPoint("NSR:Quay:550");
        askerOslo1.addToStopPoint("NSR:Quay:551");
        askerOslo1.addToStopPoint("NSR:Quay:553");
        askerOslo1.addToStopPoint("NSR:Quay:554");
        askerOslo1.addToStopPoint("NSR:Quay:555");
        askerOslo1.addToStopPoint("NSR:Quay:556");
        askerOslo1.addToStopPoint("NSR:Quay:563");
        askerOslo1.addToStopPoint("NSR:Quay:557");
        askerOslo1.addToStopPoint("NSR:Quay:559");
        askerOslo1.addToStopPoint("NSR:Quay:561");
        askerOslo1.addToStopPoint("NSR:Quay:562");
        askerOslo1.addToStopPoint("NSR:Quay:564");
        askerOslo1.addToStopPoint("NSR:Quay:566");
        askerOslo1.addToStopPoint("NSR:Quay:567");
        askerOslo1.addToStopPoint("NSR:Quay:568");
        askerOslo1.addToStopPoint("NSR:Quay:569");
        askerOslo1.addToStopPoint("NSR:Quay:565");
        askerOslo1.addToStopPoint("NSR:Quay:570");
        askerOslo1.addToStopPoint("NSR:Quay:571");
        add(askerOslo1);

        Subscription askerOslo2 = new Subscription();
        askerOslo2.setName("[SIRI] Asker-OsloS #2 (stopplace only)");
        askerOslo2.addFromStopPoint("NSR:StopPlace:418");
        askerOslo2.addToStopPoint("NSR:StopPlace:337");
        askerOslo2.setUseSiriSubscriptionModel(true);
        add(askerOslo2);

        Subscription osloTilAsker1 = new Subscription();
        osloTilAsker1.setName("OsloS-Asker #1");
        osloTilAsker1.addFromStopPoint("NSR:StopPlace:337");
        osloTilAsker1.addFromStopPoint("NSR:Quay:550");
        osloTilAsker1.addFromStopPoint("NSR:Quay:551");
        osloTilAsker1.addFromStopPoint("NSR:Quay:553");
        osloTilAsker1.addFromStopPoint("NSR:Quay:554");
        osloTilAsker1.addFromStopPoint("NSR:Quay:555");
        osloTilAsker1.addFromStopPoint("NSR:Quay:556");
        osloTilAsker1.addFromStopPoint("NSR:Quay:563");
        osloTilAsker1.addFromStopPoint("NSR:Quay:557");
        osloTilAsker1.addFromStopPoint("NSR:Quay:559");
        osloTilAsker1.addFromStopPoint("NSR:Quay:561");
        osloTilAsker1.addFromStopPoint("NSR:Quay:562");
        osloTilAsker1.addFromStopPoint("NSR:Quay:564");
        osloTilAsker1.addFromStopPoint("NSR:Quay:566");
        osloTilAsker1.addFromStopPoint("NSR:Quay:567");
        osloTilAsker1.addFromStopPoint("NSR:Quay:568");
        osloTilAsker1.addFromStopPoint("NSR:Quay:569");
        osloTilAsker1.addFromStopPoint("NSR:Quay:565");
        osloTilAsker1.addFromStopPoint("NSR:Quay:570");
        osloTilAsker1.addFromStopPoint("NSR:Quay:571");
        osloTilAsker1.addToStopPoint("NSR:StopPlace:418");
        osloTilAsker1.addToStopPoint("NSR:Quay:695");
        osloTilAsker1.addToStopPoint("NSR:Quay:696");
        osloTilAsker1.addToStopPoint("NSR:Quay:697");
        osloTilAsker1.addToStopPoint("NSR:Quay:698");
        osloTilAsker1.addToStopPoint("NSR:Quay:699");
        osloTilAsker1.addToStopPoint("NSR:Quay:700");
        add(osloTilAsker1);

        Subscription osloAsker2 = new Subscription();
        osloAsker2.setName("[SIRI] OsloS-Asker #2 (stopplace only)");
        osloAsker2.addFromStopPoint("NSR:StopPlace:337");
        osloAsker2.addToStopPoint("NSR:StopPlace:418");
        osloAsker2.setUseSiriSubscriptionModel(true);
        osloAsker2.setHeartbeatInterval("PT1H");
        add(osloAsker2);

        Subscription lineL14 = new Subscription();
        lineL14.setName("[SIRI] Line L14 (heartbeats every hour)");
        lineL14.addLineRef("NSB:Line:L14");
        lineL14.setUseSiriSubscriptionModel(true);
        lineL14.setHeartbeatInterval("PT1H");
        add(lineL14);

        Subscription ruterLine1 = new Subscription();
        ruterLine1.setName("Ruter Line 1");
        ruterLine1.addLineRef("RUT:Line:1");
        add(ruterLine1);

        Subscription ruterSX = new Subscription();
        ruterSX.setType(SubscriptionTypeEnum.SX);
        ruterSX.setName("All SX from RUT");
        ruterSX.addCodespace("RUT");
        add(ruterSX);

        Subscription ruterQASX = new Subscription();
        ruterQASX.setType(SubscriptionTypeEnum.SX);
        ruterQASX.setName("All SX from QA-RUT");
        ruterQASX.addCodespace("QA-RUT");
        add(ruterQASX);

        Subscription osloAskerAllData = new Subscription();
        osloAskerAllData.setName("[SIRI|AllData] OsloS-Asker #3 (stopplace only)");
        osloAskerAllData.addFromStopPoint("NSR:StopPlace:337");
        osloAskerAllData.addToStopPoint("NSR:StopPlace:418");
        osloAskerAllData.setUseSiriSubscriptionModel(true);
        osloAskerAllData.setPushAllData(true);
        add(osloAskerAllData);

    }
}
