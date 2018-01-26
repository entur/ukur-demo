package org.entur.demo.ukur.repositories;

import org.entur.demo.ukur.entities.Subscription;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * Simple in-memory storage of subscriptions.
 */
@Repository
public class SubscriptionsRepository {

    private long idCounter = 0;
    private HashMap<String, Subscription> subscriptions = new HashMap<>();

    public Collection<Subscription> findAll() {
        return Collections.unmodifiableCollection(subscriptions.values());
    }

    public void add(Subscription subscription) {
        String id = Long.toString(idCounter++);
        subscription.setId(id);
        subscriptions.put(id, subscription);
    }

    public void remove(Subscription subscription) {
        subscriptions.remove(subscription.getId());
    }

    public Subscription findById(String id) {
        return subscriptions.get(id);
    }
}
