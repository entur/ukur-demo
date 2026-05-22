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
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;

public abstract class AbstractSubscriptionController {

    protected final SubscriptionService subscriptionService;
    protected final MessageService messageService;

    @Autowired(required = false)
    private BuildProperties buildProperties;

    protected AbstractSubscriptionController(SubscriptionService subscriptionService, MessageService messageService) {
        this.subscriptionService = subscriptionService;
        this.messageService = messageService;
    }

    @ModelAttribute("allSubscriptions")
    public Collection<Subscription> populateSubscriptions() {
        Collection<Subscription> all = subscriptionService.list();
        for (Subscription subscription : all) {
            subscription.setNumberOfMessages(messageService.getMessageCount(subscription.getId()));
        }
        return all;
    }

    @ModelAttribute("buildVersion")
    public String buildVersion() {
        if (buildProperties != null && buildProperties.getTime() != null) {
            String buildTime = LocalDateTime.ofInstant(buildProperties.getTime(), ZoneId.systemDefault()).toString();
            return "build-name: " + buildProperties.getName() + ", build-version: " + buildProperties.getVersion() + ", build-time: " + buildTime;
        }
        return "build-name: unknown, build-version: unknown, build-time: " + LocalDateTime.now();
    }

    protected void validate(Subscription subscription, BindingResult bindingResult) {
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
                        && (subscription.getFromStopPoints().isEmpty() || subscription.getToStopPoints().isEmpty())) {
                    bindingResult.addError(new ObjectError("subscription", "No criterias given, must have at least one lineRef or one codespace or a fromStop and a toStop"));
                }
            }
        }
    }
}