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

import org.apache.commons.lang3.StringUtils;
import org.entur.demo.ukur.entities.Subscription;
import org.entur.demo.ukur.services.MessageService;
import org.entur.demo.ukur.services.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;

/**
 * Controller for the modern UI version
 */
@Controller
@RequestMapping("/modern")
@SuppressWarnings("unused")
public class ModernSubscriptionController {

    private final SubscriptionService subscriptionService;
    private final MessageService messageService;

    @Autowired
    BuildProperties buildProperties;

    @Autowired
    public ModernSubscriptionController(SubscriptionService subscriptionService, MessageService messageService) {
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

    @ModelAttribute("buildVersion")
    public String buildVersion() {
        final String buildTime = LocalDateTime.ofInstant(buildProperties.getTime(), ZoneId.systemDefault()).toString();
        final String name = buildProperties.getName();
        final String version = buildProperties.getVersion();
        return "build-name: " + name + ", build-version: " + version + ", build-time: " + buildTime;
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
            // Reopen modal if there are validation errors
            model.addAttribute("showAddModal", true);
        }
        return "modern/subscriptions";
    }

    private void validate(Subscription subscription, BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            if (subscription == null) {
                bindingResult.addError(new ObjectError("subscription", "Subscription is null..."));
            } else {
                if (StringUtils.isBlank(subscription.getName())) {
                    bindingResult.addError(new ObjectError("name", "A name is required"));
                }
                if (!subscription.validateHeartbeat()) {
                    bindingResult.addError(new ObjectError("heartbeatInterval", "Illegal heartbeatInterval"));
                }
                if (subscription.getCodespaces().isEmpty() && subscription.getLineRefs().isEmpty()
                        && ( subscription.getFromStopPoints().isEmpty() || subscription.getToStopPoints().isEmpty() ) ) {
                    bindingResult.addError(new ObjectError("subscription", "No criterias given, must have at least one lineRef or one codespace or a fromStop and a toStop"));
                }
            }
        }
    }

    @RequestMapping(value = "/subscriptions", params = {"deleteSubscriptionId"})
    public String removeSubscription(Subscription s, Model model, HttpServletRequest req) {
        String id = req.getParameter("deleteSubscriptionId");
        subscriptionService.remove(id);
        return "redirect:subscriptions";
    }
}
