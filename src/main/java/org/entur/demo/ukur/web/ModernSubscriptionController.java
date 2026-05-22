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
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/modern")
public class ModernSubscriptionController extends AbstractSubscriptionController {

    @Autowired
    public ModernSubscriptionController(SubscriptionService subscriptionService, MessageService messageService) {
        super(subscriptionService, messageService);
    }

    @RequestMapping({"/", "/subscriptions"})
    public String showSubscription(Subscription subscription) {
        return "modern/subscriptions";
    }

    @RequestMapping(value = "/subscriptions", params = {"save"})
    public String saveSubscription(Subscription subscription, BindingResult bindingResult, Model model) {
        validate(subscription, bindingResult);
        if (!bindingResult.hasErrors()) {
            if (this.subscriptionService.add(subscription)) {
                return "redirect:subscriptions";
            } else {
                bindingResult.addError(new ObjectError("subscription", "Could not add subscription to Ukur"));
                model.addAttribute("showAddModal", true);
            }
        } else {
            model.addAttribute("showAddModal", true);
        }
        return "modern/subscriptions";
    }

    @RequestMapping(value = "/subscriptions", params = {"deleteSubscriptionId"})
    public String removeSubscription(Subscription s, HttpServletRequest req) {
        subscriptionService.remove(req.getParameter("deleteSubscriptionId"));
        return "redirect:subscriptions";
    }
}