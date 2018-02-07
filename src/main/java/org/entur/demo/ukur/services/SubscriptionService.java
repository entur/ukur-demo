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
        Subscription askerTilOslo = new Subscription();
        askerTilOslo.setName("Asker til OsloS");
        askerTilOslo.addFromStopPoint("NSR:StopPlace:418");
        askerTilOslo.addFromStopPoint("NSR:Quay:695");
        askerTilOslo.addFromStopPoint("NSR:Quay:696");
        askerTilOslo.addFromStopPoint("NSR:Quay:697");
        askerTilOslo.addFromStopPoint("NSR:Quay:698");
        askerTilOslo.addFromStopPoint("NSR:Quay:699");
        askerTilOslo.addFromStopPoint("NSR:Quay:700");
        askerTilOslo.addToStopPoint("NSR:StopPlace:337");
        askerTilOslo.addToStopPoint("NSR:Quay:550");
        askerTilOslo.addToStopPoint("NSR:Quay:551");
        askerTilOslo.addToStopPoint("NSR:Quay:553");
        askerTilOslo.addToStopPoint("NSR:Quay:554");
        askerTilOslo.addToStopPoint("NSR:Quay:555");
        askerTilOslo.addToStopPoint("NSR:Quay:556");
        askerTilOslo.addToStopPoint("NSR:Quay:563");
        askerTilOslo.addToStopPoint("NSR:Quay:557");
        askerTilOslo.addToStopPoint("NSR:Quay:559");
        askerTilOslo.addToStopPoint("NSR:Quay:561");
        askerTilOslo.addToStopPoint("NSR:Quay:562");
        askerTilOslo.addToStopPoint("NSR:Quay:564");
        askerTilOslo.addToStopPoint("NSR:Quay:566");
        askerTilOslo.addToStopPoint("NSR:Quay:567");
        askerTilOslo.addToStopPoint("NSR:Quay:568");
        askerTilOslo.addToStopPoint("NSR:Quay:569");
        askerTilOslo.addToStopPoint("NSR:Quay:565");
        askerTilOslo.addToStopPoint("NSR:Quay:570");
        askerTilOslo.addToStopPoint("NSR:Quay:571");
        add(askerTilOslo);

        Subscription osloTilAsker = new Subscription();
        osloTilAsker.setName("OsloS til Asker");
        osloTilAsker.addFromStopPoint("NSR:StopPlace:337");
        osloTilAsker.addFromStopPoint("NSR:Quay:550");
        osloTilAsker.addFromStopPoint("NSR:Quay:551");
        osloTilAsker.addFromStopPoint("NSR:Quay:553");
        osloTilAsker.addFromStopPoint("NSR:Quay:554");
        osloTilAsker.addFromStopPoint("NSR:Quay:555");
        osloTilAsker.addFromStopPoint("NSR:Quay:556");
        osloTilAsker.addFromStopPoint("NSR:Quay:563");
        osloTilAsker.addFromStopPoint("NSR:Quay:557");
        osloTilAsker.addFromStopPoint("NSR:Quay:559");
        osloTilAsker.addFromStopPoint("NSR:Quay:561");
        osloTilAsker.addFromStopPoint("NSR:Quay:562");
        osloTilAsker.addFromStopPoint("NSR:Quay:564");
        osloTilAsker.addFromStopPoint("NSR:Quay:566");
        osloTilAsker.addFromStopPoint("NSR:Quay:567");
        osloTilAsker.addFromStopPoint("NSR:Quay:568");
        osloTilAsker.addFromStopPoint("NSR:Quay:569");
        osloTilAsker.addFromStopPoint("NSR:Quay:565");
        osloTilAsker.addFromStopPoint("NSR:Quay:570");
        osloTilAsker.addFromStopPoint("NSR:Quay:571");
        osloTilAsker.addToStopPoint("NSR:StopPlace:418");
        osloTilAsker.addToStopPoint("NSR:Quay:695");
        osloTilAsker.addToStopPoint("NSR:Quay:696");
        osloTilAsker.addToStopPoint("NSR:Quay:697");
        osloTilAsker.addToStopPoint("NSR:Quay:698");
        osloTilAsker.addToStopPoint("NSR:Quay:699");
        osloTilAsker.addToStopPoint("NSR:Quay:700");
        add(osloTilAsker);

    }
}
