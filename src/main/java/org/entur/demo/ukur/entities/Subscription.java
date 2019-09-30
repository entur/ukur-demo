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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.siri.siri20.EstimatedTimetableRequestStructure;
import uk.org.siri.siri20.EstimatedTimetableSubscriptionStructure;
import uk.org.siri.siri20.LineDirectionStructure;
import uk.org.siri.siri20.LineRef;
import uk.org.siri.siri20.RequestorRef;
import uk.org.siri.siri20.Siri;
import uk.org.siri.siri20.SituationExchangeRequestStructure;
import uk.org.siri.siri20.SituationExchangeSubscriptionStructure;
import uk.org.siri.siri20.SubscriptionContextStructure;
import uk.org.siri.siri20.SubscriptionQualifierStructure;
import uk.org.siri.siri20.SubscriptionRequest;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import java.io.Serializable;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Objects;

public class Subscription implements Serializable, Comparable {

    @JsonIgnore
    private static final Logger logger = LoggerFactory.getLogger(Subscription.class);

    private String id;
    private String name;
    private String pushAddress;
    private ArrayList<String> fromStopPoints = new ArrayList<>();
    private ArrayList<String> toStopPoints = new ArrayList<>();
    private ArrayList<String> lineRefs = new ArrayList<>();
    private ArrayList<String> codespaces = new ArrayList<>();
    private SubscriptionTypeEnum type = SubscriptionTypeEnum.ALL;
    private DeviationType deviationType = DeviationType.ALL;
    private boolean useSiriSubscriptionModel = false;
    private boolean pushAllData = false;
    @JsonIgnore
    private int numberOfMessages = 0;
    @JsonIgnore
    private String pushId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private String initialTerminationTime;
    private String heartbeatInterval = null;
    private String minimumDelay = null;
    @JsonIgnore
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    @JsonIgnore
    private String xmlCache; //Used to cache the xml representation (works since we don't support updates)
    @JsonIgnore
    private String jsonCache; //Used to cache the jsonCache representation (works since we don't support updates)

    private static final JAXBContext jaxbContext;
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        try {
            jaxbContext = JAXBContext.newInstance(Siri.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

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

    public DeviationType getDeviationType() {
        return deviationType;
    }

    public void setDeviationType(DeviationType deviationType) {
        this.deviationType = deviationType;
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

    public ZonedDateTime convertInitialTerminationTime() {
        if (initialTerminationTime != null) {
            TemporalAccessor parse = dateTimeFormatter.parse(initialTerminationTime);
            return ZonedDateTime.from(parse);
        }
        return null;
    }

    public void setInitialTerminationTime(ZonedDateTime time) {
        initialTerminationTime = dateTimeFormatter.format(time);
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

    public String getMinimumDelay() {
        return minimumDelay;
    }

    public void setMinimumDelay(String minimumDelay) {
        this.minimumDelay = minimumDelay;
    }

    public boolean isPushAllData() {
        return pushAllData;
    }

    public void setPushAllData(boolean pushAllData) {
        this.pushAllData = pushAllData;
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
        pushAllData = false;
    }

    public String toJSON() {
        if (jsonCache == null) {
            logger.debug("calculates json ({})", name);
            try {
                jsonCache = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
            } catch (Exception e) {
                logger.error("Could not generate JSON", e);
                jsonCache = "Error";
            }
        }
        return jsonCache;
    }

    public String toXML() {
        if (xmlCache == null) {
            logger.debug("calculates xml ({})", name);
            try {

                //Generic part
                RequestorRef requestorRef = new RequestorRef();
                requestorRef.setValue("CLIENT-SPECIFIED-REQUESTOR_REF"); //TODO: store somewhere on subscription
                SubscriptionRequest request = new SubscriptionRequest();
                request.setRequestorRef(requestorRef);
                ZonedDateTime now = ZonedDateTime.now();
                request.setRequestTimestamp(now);
                request.setAddress(pushAddress);

                if (heartbeatInterval != null) {
                    DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
                    SubscriptionContextStructure ctx = new SubscriptionContextStructure();
                    ctx.setHeartbeatInterval(datatypeFactory.newDuration(heartbeatInterval));
                    request.setSubscriptionContext(ctx);
                }

                //The XSD allows only one of SituationExchangeRequestStructure/EstimatedTimetableRequestStructure...!

                if (type == SubscriptionTypeEnum.ALL || type == SubscriptionTypeEnum.SX) {
                    //SX subscription part
                    SituationExchangeRequestStructure sxRequest = new SituationExchangeRequestStructure();
                    sxRequest.setRequestTimestamp(now);
                    if (lineRefs != null && !lineRefs.isEmpty()) {
                        for (String ref : lineRefs) {
                            LineRef lineRef = new LineRef();
                            lineRef.setValue(ref);
                            sxRequest.getLineReves().add(lineRef);
                        }
                    }
                    SituationExchangeSubscriptionStructure sxSubscriptionReq = new SituationExchangeSubscriptionStructure();
                    sxSubscriptionReq.setSituationExchangeRequest(sxRequest);
                    SubscriptionQualifierStructure sxSubscriptionIdentifier = new SubscriptionQualifierStructure();
                    sxSubscriptionIdentifier.setValue("CLIENT-SPECIFIED-SUBSCRIPTION_ID_1");//TODO: store somewhere on subscription
                    sxSubscriptionReq.setSubscriptionIdentifier(sxSubscriptionIdentifier);
                    sxSubscriptionReq.setInitialTerminationTime(convertInitialTerminationTime());
                    request.getSituationExchangeSubscriptionRequests().add(sxSubscriptionReq);
                }

                if (type == SubscriptionTypeEnum.ALL || type == SubscriptionTypeEnum.ET) {
                    //ET subscription part
                    EstimatedTimetableRequestStructure etRequest = new EstimatedTimetableRequestStructure();
                    etRequest.setRequestTimestamp(now);
                    if (lineRefs != null && !lineRefs.isEmpty()) {
                        EstimatedTimetableRequestStructure.Lines lines = new EstimatedTimetableRequestStructure.Lines();
                        for (String ref : lineRefs) {
                            LineRef lineRef = new LineRef();
                            lineRef.setValue(ref);
                            LineDirectionStructure line = new LineDirectionStructure();
                            line.setLineRef(lineRef);
                            lines.getLineDirections().add(line);
                        }
                        etRequest.setLines(lines);
                    }
                    EstimatedTimetableSubscriptionStructure etSubscriptionReq = new EstimatedTimetableSubscriptionStructure();
                    etSubscriptionReq.setEstimatedTimetableRequest(etRequest);
                    SubscriptionQualifierStructure etSubscriptionIdentifier = new SubscriptionQualifierStructure();
                    etSubscriptionIdentifier.setValue("CLIENT-SPECIFIED-SUBSCRIPTION_ID_2"); //TODO: store somewhere on subscription
                    etSubscriptionReq.setSubscriptionIdentifier(etSubscriptionIdentifier);
                    etSubscriptionReq.setInitialTerminationTime(convertInitialTerminationTime());
                    request.getEstimatedTimetableSubscriptionRequests().add(etSubscriptionReq);
                }

                Siri siri = new Siri();
                siri.setVersion("2.0");
                siri.setSubscriptionRequest(request);


                Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
                StringWriter writer = new StringWriter();
                marshaller.marshal(siri, writer);
                StringBuilder sb = new StringBuilder();
                if (type == SubscriptionTypeEnum.ALL) {
                    sb.append("<!-- The SIRI XSD only allows one of SituationExchangeSubscriptionRequest or EstimatedTimetableSubscriptionRequest, not both like this sample. -->\n");
                    sb.append("<!-- Also make sure the CLIENT-SPECIFIED-SUBSCRIPTION_ID's is not already used, else the existing subscription is overwritten.                 -->\n");
                    sb.append("<!-- Subscriptions are identified with the combination of RequestorRef and the SubscriptionIdentifier.                                         -->\n");
                }
                if (!fromStopPoints.isEmpty() || !toStopPoints.isEmpty()) {
                    sb.append("<!-- This subscription has from-/toStops. That is not supported by the SIRI XSD and not visible below.                                         -->\n");
                }
                if (!codespaces.isEmpty()) {
                    sb.append("<!-- The subscription has codespace, that is not directly supported by the SIRI XSD. Add the codespace to the request path to support one.     -->\n");
                    if (codespaces.size() > 1) {
                        sb.append("<!-- Only one codespace is supported by each SIRI subscription due to the path trick (you can have many subscriptions!).                       -->\n");
                    }
                }
                String xml = writer.getBuffer().toString();
                xml = xml.substring(xml.indexOf("\n"), xml.length());
                sb.append("<!-- The requestor/client must provide values prefixed with 'CLIENT-SPECIFIED-'.                                                               -->\n");
                sb.append("<Siri version=\"2.0\" xmlns=\"http://www.siri.org.uk/siri\">"); //removes unused namespaces somewhat brutal...
                sb.append(xml);
                xmlCache = sb.toString();
            } catch (Exception e) {
                logger.error("Could not generate XML", e);
                xmlCache = "Error";
            }
        }
        return xmlCache;
    }

    public boolean validateHeartbeat() {
        if (StringUtils.isNotBlank(heartbeatInterval)) {
            try {
                DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
                datatypeFactory.newDuration(heartbeatInterval);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }
}
