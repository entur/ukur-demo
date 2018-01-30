package org.entur.demo.ukur.web;

import org.entur.demo.ukur.entities.Subscription;
import org.entur.demo.ukur.services.MessageService;
import org.entur.demo.ukur.services.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

@Controller
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private MessageService messageService;

    @ModelAttribute("allSubscriptions")
    public Collection<Subscription> populateSubscriptions() {
        Collection<Subscription> all = this.subscriptionService.list();
        for (Subscription subscription : all) {
            subscription.setNumberOfMessages(messageService.getMessageCount(subscription.getId()));
        }
        return all;
    }

    @SuppressWarnings("unused")
    @RequestMapping({"/", "/subscriptions"})
    public String showSubscription(Subscription subscription) {
        return "subscriptions";
    }

    @RequestMapping(value = "/subscriptions", params = {"save"})
    public String saveSubscription(Subscription subscription, BindingResult bindingResult, ModelMap model) {
        if (bindingResult.hasErrors()) {
            return "subscriptions";
        }
        this.subscriptionService.add(subscription);
        model.clear();
        return "redirect:/subscriptions";
    }

    @RequestMapping(value = "/subscriptions", params = {"addFrom", "from_value"})
    public String addFrom(Subscription subscription, HttpServletRequest req) {
        String stop = req.getParameter("from_value");
        subscription.addFromStopPoint(stop);
        return "subscriptions";
    }

    @RequestMapping(value = "/subscriptions", params = {"removeFrom"})
    public String removeFrom(Subscription subscription, HttpServletRequest req) {
        Integer rowId = Integer.valueOf(req.getParameter("removeFrom"));
        subscription.getFromStopPoints().remove(rowId.intValue());
        return "subscriptions";
    }

    @RequestMapping(value = "/subscriptions", params = {"addTo", "to_value"})
    public String addTo(Subscription subscription, HttpServletRequest req) {
        String stop = req.getParameter("to_value");
        subscription.addToStopPoint(stop);
        return "subscriptions";
    }

    @RequestMapping(value = "/subscriptions", params = {"removeTo"})
    public String removeTo(Subscription subscription, HttpServletRequest req) {
        Integer rowId = Integer.valueOf(req.getParameter("removeTo"));
        subscription.getToStopPoints().remove(rowId.intValue());
        return "subscriptions";
    }

    @RequestMapping(value = "/subscriptions", params = {"deleteSubscriptionId"})
    public String removeSubscription( ModelMap model, HttpServletRequest req) {
        String id = req.getParameter("deleteSubscriptionId");
        subscriptionService.remove(id);
        model.clear();
        return "redirect:/subscriptions";
    }

}
