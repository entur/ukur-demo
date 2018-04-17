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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

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
        addTestSubscriptions();
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

    public void add(final Subscription subscription) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            URI url = URI.create(ukurURL);
            String pushId = basePushId + idCounter++;
            subscription.setPushAddress(pushURL+pushId);
            Subscription returnedSubscription = restTemplate.postForObject(url, subscription, Subscription.class);
            logger.info("Added subscription at Ukur, received subscription id {}", returnedSubscription.getId());
            returnedSubscription.setPushId(pushId); //not part of Ukur's subscription
            subscriptions.put(pushId, returnedSubscription); //uses returned subscription since it is normalized
        } catch (Exception e) {
            logger.error("Could not add new subscription", e);
        }
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
        askerOslo1.setName("Asker til OsloS #1");
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
        askerOslo2.setName("Asker-OsloS #2 (kun stopplace)");
        askerOslo2.addFromStopPoint("NSR:StopPlace:418");
        askerOslo2.addToStopPoint("NSR:StopPlace:337");
        add(askerOslo2);


        Subscription osloTilAsker1 = new Subscription();
        osloTilAsker1.setName("OsloS til Asker #1");
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
        osloAsker2.setName("OsloS-Asker #2 (kun stopplace)");
        osloAsker2.addFromStopPoint("NSR:StopPlace:337");
        osloAsker2.addToStopPoint("NSR:StopPlace:418");
        add(osloAsker2);

        Subscription lineL14 = new Subscription();
        lineL14.setName("Line L14");
        lineL14.addLineRef("NSB:Line:L14");
        add(lineL14);

    }
}
