package net.xmeter.samplers.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MQTT5PublishReq {
    private String correlationData;
    private long messageExpiryInterval;
    private Map<String, String> userProperties;
    private String contentType;
    private String responseTopic;

    private String payloadFormatIndicator;
    private String topicAlias;
    private String subscriptionIdentifier;

    public Map<String, String> getUserProperties() {
        return userProperties;
    }

    public void setUserProperties(String userPropertiesJson) {
        if (userPropertiesJson == null || userPropertiesJson.isEmpty()) {
            this.userProperties = new HashMap<>();
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        try{
            this.userProperties = mapper.readValue(userPropertiesJson, Map.class);
        }catch (IOException e){
            this.userProperties = new HashMap<>();
            e.printStackTrace();
        }
    }

    public String getCorrelationData() {
        return correlationData;
    }

    public void setCorrelationData(String correlationData) {
        this.correlationData = correlationData;
    }

    public long getMessageExpiryInterval() {
        return messageExpiryInterval;
    }

    public void setMessageExpiryInterval(long messageExpiryInterval) {
        this.messageExpiryInterval = messageExpiryInterval;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getResponseTopic() {
        return responseTopic;
    }

    public void setResponseTopic(String responseTopic) {
        this.responseTopic = responseTopic;
    }

    public String getPayloadFormatIndicator() {
        return payloadFormatIndicator;
    }

    public void setPayloadFormatIndicator(String payloadFormatIndicator) {
        this.payloadFormatIndicator = payloadFormatIndicator;
    }

    public String getTopicAlias() {
        return topicAlias;
    }

    public void setTopicAlias(String topicAlias) {
        this.topicAlias = topicAlias;
    }

    public String getSubscriptionIdentifier() {
        return subscriptionIdentifier;
    }

    public void setSubscriptionIdentifier(String subscriptionIdentifier) {
        this.subscriptionIdentifier = subscriptionIdentifier;
    }
}
