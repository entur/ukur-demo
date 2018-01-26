package org.entur.demo.ukur.services;

import org.entur.demo.ukur.entities.Subscription;
import org.entur.demo.ukur.repositories.SubscriptionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class SubscriptionService {

    private SubscriptionsRepository subscriptionsRepository;

    @Autowired
    public SubscriptionService(SubscriptionsRepository subscriptionsRepository) {
        this.subscriptionsRepository = subscriptionsRepository;
        addTestSubscriptions();
    }

    public Collection<Subscription> list() {
        return this.subscriptionsRepository.findAll();
    }

    public Subscription get(String id) {
        return this.subscriptionsRepository.findById(id);
    }

    public void add(final Subscription subscription) {
        //TODO: Call ukur and subscribe!
        this.subscriptionsRepository.add(subscription);
    }

    public void remove(final Subscription subscription) {
        //TODO: Call ukur and remove subscription (will also be removed automatically if we dont ACK when ukur pushes on this subscription)
        this.subscriptionsRepository.remove(subscription);
    }

    private void addTestSubscriptions() {
        //TODO: Vi trenger noen test susbcriptions så vi slipper å taste dette hver gang etter oppstart... Hardkodes her i første omgang!
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
