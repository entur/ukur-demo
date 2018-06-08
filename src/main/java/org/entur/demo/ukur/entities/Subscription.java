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

package org.entur.demo.ukur.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Subscription implements Serializable, Comparable {

    private String id;
    private String name;
    private String pushAddress;
    private ArrayList<String> fromStopPoints = new ArrayList<>();
    private ArrayList<String> toStopPoints = new ArrayList<>();
    private ArrayList<String> lineRefs = new ArrayList<>();
    private ArrayList<String> codespaces = new ArrayList<>();
    private SubscriptionTypeEnum type = SubscriptionTypeEnum.ALL;
    private boolean useSiriSubscriptionModel = false;
    @JsonIgnore
    private int numberOfMessages = 0;
    @JsonIgnore
    private String pushId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private String initialTerminationTime;
    private String heartbeatInterval;


    public ArrayList<String> getFromStopPoints() {
        return fromStopPoints;
    }

    public void addFromStopPoint(String stopPointRef) {
        fromStopPoints.add(stopPointRef);
    }

    public ArrayList<String> getToStopPoints() {
        return toStopPoints;
    }

    public void addToStopPoint(String stopPointRef) {
        toStopPoints.add(stopPointRef);
    }

    public ArrayList<String> getLineRefs() {
        return lineRefs;
    }

    public void addLineRef(String lineRef) {
        lineRefs.add(lineRef);
    }

    public ArrayList<String> getCodespaces() {
        return codespaces;
    }

    public void addCodespace(String codespace) {
        codespaces.add(codespace);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumberOfMessages() {
        return numberOfMessages;
    }

    public void setNumberOfMessages(int numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
    }

    public String getPushAddress() {
        return pushAddress;
    }

    public void setPushAddress(String pushAddress) {
        this.pushAddress = pushAddress;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public SubscriptionTypeEnum getType() {
        return type;
    }

    public void setType(SubscriptionTypeEnum type) {
        this.type = type;
    }

    public boolean isUseSiriSubscriptionModel() {
        return useSiriSubscriptionModel;
    }

    public void setUseSiriSubscriptionModel(boolean useSiriSubscriptionModel) {
        this.useSiriSubscriptionModel = useSiriSubscriptionModel;
    }

    public String getInitialTerminationTime() {
        return initialTerminationTime;
    }

    public void setInitialTerminationTime(ZonedDateTime time) {
        initialTerminationTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(time);
    }

    public void setInitialTerminationTime(String initialTerminationTime) {
        this.initialTerminationTime = initialTerminationTime;
    }

    public String getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(String heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Object o) {
        Subscription that = (Subscription) o;
        return this.pushId.compareTo(that.pushId);
    }

    public void clear() {
        id = null;
        name = null;
        pushAddress = null;
        fromStopPoints = new ArrayList<>();
        toStopPoints = new ArrayList<>();
        lineRefs = new ArrayList<>();
        codespaces = new ArrayList<>();
        type = SubscriptionTypeEnum.ALL;
        useSiriSubscriptionModel = false;
    }
}
