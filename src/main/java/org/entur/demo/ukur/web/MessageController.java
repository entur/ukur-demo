package org.entur.demo.ukur.web;

import org.entur.demo.ukur.entities.Subscription;
import org.entur.demo.ukur.services.MessageService;
import org.entur.demo.ukur.services.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class MessageController {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private MessageService messageService;

    @RequestMapping(value = "/messages", params = {"id"})
    public String listMessagesForSubscription(Model model, HttpServletRequest req) {
        String id = req.getParameter("id");
        Subscription subscription = subscriptionService.get(id);
        model.addAttribute("subscription",subscription);
        model.addAttribute("messages", messageService.getMessages(id));
        model.addAttribute("last", messageService.getLastMessageReceived(id));
        return "messages";
    }

    @RequestMapping(value = "/messages", params = {"delete", "id"})
    public String removeMessagesForSubscription(Model model, HttpServletRequest req) {
        String id = req.getParameter("id");
        messageService.removeMessages(id);
        return listMessagesForSubscription(model, req);
    }

}
