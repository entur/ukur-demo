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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

@Controller
@SuppressWarnings("unused")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    private final MessageService messageService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService, MessageService messageService) {
        this.subscriptionService = subscriptionService;
        this.messageService = messageService;
    }

    @ModelAttribute("allSubscriptions")
    public Collection<Subscription> populateSubscriptions() {
        Collection<Subscription> all = this.subscriptionService.list();
        for (Subscription subscription : all) {
            subscription.setNumberOfMessages(messageService.getMessageCount(subscription.getId()));
        }
        return all;
    }

    @RequestMapping({"/", "subscriptions"})
    public String showSubscription(Subscription subscription) {
        return "subscriptions";
    }

    @RequestMapping(value = "subscriptions", params = {"save"})
    public String saveSubscription(Subscription subscription, BindingResult bindingResult, Model model) {
        if (!bindingResult.hasErrors()) {
            this.subscriptionService.add(subscription);
        }
        return "subscriptions";
    }

    @RequestMapping(value = "subscriptions", params = {"addFrom", "from_value"})
    public String addFrom(Subscription subscription, HttpServletRequest req) {
        String stop = req.getParameter("from_value");
        subscription.addFromStopPoint(stop);
        return "subscriptions";
    }

    @RequestMapping(value = "subscriptions", params = {"removeFrom"})
    public String removeFrom(Subscription subscription, HttpServletRequest req) {
        Integer rowId = Integer.valueOf(req.getParameter("removeFrom"));
        subscription.getFromStopPoints().remove(rowId.intValue());
        return "subscriptions";
    }

    @RequestMapping(value = "subscriptions", params = {"addTo", "to_value"})
    public String addTo(Subscription subscription, HttpServletRequest req) {
        String stop = req.getParameter("to_value");
        subscription.addToStopPoint(stop);
        return "subscriptions";
    }

    @RequestMapping(value = "subscriptions", params = {"removeTo"})
    public String removeTo(Subscription subscription, HttpServletRequest req) {
        Integer rowId = Integer.valueOf(req.getParameter("removeTo"));
        subscription.getToStopPoints().remove(rowId.intValue());
        return "subscriptions";
    }

    @RequestMapping(value = "subscriptions", params = {"deleteSubscriptionId"})
    public String removeSubscription( Subscription s, Model model, HttpServletRequest req) {
        String id = req.getParameter("deleteSubscriptionId");
        subscriptionService.remove(id);
        return "subscriptions";
    }

    @RequestMapping(value = "subscriptions", params = {"addLineRef", "lineref_value"})
    public String addLineRef(Subscription subscription, HttpServletRequest req) {
        String lineref = req.getParameter("lineref_value");
        subscription.addLineRef(lineref);
        return "subscriptions";
    }

    @RequestMapping(value = "subscriptions", params = {"removeLineRef"})
    public String removeLineRef(Subscription subscription, HttpServletRequest req) {
        Integer rowId = Integer.valueOf(req.getParameter("removeLineRef"));
        subscription.getLineRefs().remove(rowId.intValue());
        return "subscriptions";
    }

    @RequestMapping(value = "subscriptions", params = {"addVehicleRef", "vehicleref_value"})
    public String addVehicleRef(Subscription subscription, HttpServletRequest req) {
        String vehicleRef = req.getParameter("vehicleref_value");
        subscription.addVehicleRef(vehicleRef);
        return "subscriptions";
    }

    @RequestMapping(value = "subscriptions", params = {"removeVehicleRef"})
    public String removeVehicleRef(Subscription subscription, HttpServletRequest req) {
        Integer rowId = Integer.valueOf(req.getParameter("removeVehicleRef"));
        subscription.getVehicleRefs().remove(rowId.intValue());
        return "subscriptions";
    }

}
