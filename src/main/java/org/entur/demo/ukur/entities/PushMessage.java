package org.entur.demo.ukur.entities;

//TODO: Get this class from an ukur-api artifact!
public class PushMessage {
    private String messagename;
    private String node;
    private String xmlPayload;

    public String getMessagename() {
        return messagename;
    }

    public void setMessagename(String messagename) {
        this.messagename = messagename;
    }

    public String getXmlPayload() {
        return xmlPayload;
    }

    public void setXmlPayload(String xmlPayload) {
        this.xmlPayload = xmlPayload;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

}
