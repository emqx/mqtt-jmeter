package net.xmeter.samplers;

import com.alibaba.fastjson.JSONObject;
import net.xmeter.SubBean;
import net.xmeter.samplers.assertions.Assertions;
import net.xmeter.samplers.assertions.AssertionsContent;
import net.xmeter.samplers.assertions.ContentCompare;
import net.xmeter.samplers.mqtt.MQTTConnection;
import net.xmeter.samplers.mqtt.MQTTQoS;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class SubSampler extends AbstractMQTTSampler {
    private static final long serialVersionUID = 2979978053740194951L;
    private static final Logger logger = Logger.getLogger(SubSampler.class.getCanonicalName());

    private transient MQTTConnection connection = null;
    private transient String clientId;
    private boolean subFailed = false;

    private boolean sampleByTime = true; // initial values
    private int sampleElapsedTime = 1000;
    private int sampleCount = 1;
    private int sampleCountTime;
    private boolean sampleByContent = false;

    private transient ConcurrentLinkedQueue<SubBean> batches = new ConcurrentLinkedQueue<>();
    private boolean printFlag = false;
    private int timeOut;

    private transient Object dataLock = new Object();



    public String getQOS() {
        return getPropertyAsString(QOS_LEVEL, String.valueOf(QOS_0));
    }

    public void setQOS(String qos) {
        setProperty(QOS_LEVEL, qos);
    }

    public String getTopics() {
        return getPropertyAsString(TOPIC_NAME, DEFAULT_TOPIC_NAME);
    }

    public void setTopics(String topicsName) {
        setProperty(TOPIC_NAME, topicsName);
    }

    public String getSampleCondition() {
        return getPropertyAsString(SAMPLE_CONDITION, SAMPLE_ON_CONDITION_OPTION1);
    }


    public void setSampleCondition(String option) {
        setProperty(SAMPLE_CONDITION, option);
    }

    public String getSampleCount() {
        return getPropertyAsString(SAMPLE_CONDITION_VALUE, DEFAULT_SAMPLE_VALUE_COUNT);
    }

    public void setSampleCount(String count) {
        setProperty(SAMPLE_CONDITION_VALUE, count);
    }

    public String getSampleCountTime() {
        return getPropertyAsString(SAMPLE_CONDITION_TIME, String.valueOf(0));
    }

    public void setSampleCountTime(String count) {
        setProperty(SAMPLE_CONDITION_VALUE, count);
    }

    public String getSampleElapsedTime() {
        return getPropertyAsString(SAMPLE_CONDITION_TIME, DEFAULT_SAMPLE_VALUE_ELAPSED_TIME_MILLI_SEC);
    }

    public void setSampleElapsedTime(String elapsedTime) {
        setProperty(SAMPLE_CONDITION_TIME, elapsedTime);
    }

    public boolean isAddTimestamp() {
        return getPropertyAsBoolean(ADD_TIMESTAMP);
    }

    public void setAddTimestamp(boolean addTimestamp) {
        setProperty(ADD_TIMESTAMP, addTimestamp);
    }

    public boolean isDebugResponse() {
        return getPropertyAsBoolean(DEBUG_RESPONSE, false);
    }

    public void setDebugResponse(boolean debugResponse) {
        setProperty(DEBUG_RESPONSE, debugResponse);
    }

    public String getAssertions() {
        return getPropertyAsString(SAMPLE_CONDITION_CONTENT, null);
    }

    @Override
    public SampleResult sample(Entry arg0) {
        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());

        JMeterVariables vars = JMeterContextService.getContext().getVariables();
        connection = (MQTTConnection) vars.getObject(getConnName());
        clientId = (String) vars.getObject(getConnName() + "_clientId");
        if (connection == null) {
            return fillFailedResult(result, "500", "Subscribe failed because connection is not established.");
        }
        logger.log(Level.INFO, connection + "服务连接成功，并开始接收消息");
        Assertions assertions = null;
        try {
            sampleByTime = SAMPLE_ON_CONDITION_OPTION1.equals(getSampleCondition());
            sampleByContent = SAMPLE_ON_CONDITION_OPTION3.equals(getSampleCondition());
            if (sampleByTime) {
                sampleElapsedTime = Integer.parseInt(getSampleElapsedTime());
                logger.log(Level.INFO, "sub名称为：" + getName() + " 结束接收方式为按持续时间");
            } else if (sampleByContent) {
                logger.log(Level.INFO, "sub名称为：" + getName() + " 结束接收方式为内容");
                logger.log(Level.INFO, "sub名称为：" + getName() +" 接收到的匹配内容：" + getAssertions());
                if (StringUtils.isNotEmpty(getAssertions())) {
                    assertions = JSONObject.parseObject(getAssertions(), Assertions.class);
                    timeOut = StringUtils.isNotEmpty(assertions.getTimeOut()) ? Integer.parseInt(assertions.getTimeOut()) : 0;
                }
            } else {
                sampleCount = Integer.parseInt(getSampleCount());
                sampleCountTime = Integer.parseInt(getSampleCountTime());
                logger.log(Level.INFO, "sub名称为：" + getName() +" 结束接收方式为按消息数量");
            }
        } catch (NumberFormatException e) {
            logger.info(e.getMessage());
            return fillFailedResult(result, "510", "Unrecognized value for sample elapsed time or message count.");
        }

        if (sampleByTime && sampleElapsedTime <= 0) {
            return fillFailedResult(result, "511", "Sample on elapsed time: must be greater than 0 ms.");
        } else if (sampleCount < 1) {
            return fillFailedResult(result, "512", "Sample on message count: must be greater than 1.");
        } else if ((sampleByContent && assertions == null) || (sampleByContent && assertions.getList().stream().filter(assertionList -> assertionList.getEnable() == true).collect(Collectors.toList()).size() == 0)) {
            return fillFailedResult(result, "513", "Sample on message content: match content cannot be empty");
        }

        final String topicsName = getTopics();
        setListener(sampleByTime, sampleCount, sampleByContent, assertions);
        Set<String> topics = topicSubscribed.get(clientId);
        if (topics == null) {
            logger.severe("subscribed topics haven't been initiated. [clientId: " + (clientId == null ? "null" : clientId) + "]");
            topics = new HashSet<>();
            topics.add(topicsName);
            topicSubscribed.put(clientId, topics);
            listenToTopics(topicsName);  // TODO: run once or multiple times ?
        } else {
            if (!topics.contains(topicsName)) {
                topics.add(topicsName);
                topicSubscribed.put(clientId, topics);
                logger.fine("Listen to topics: " + topicsName);
                listenToTopics(topicsName);  // TODO: run once or multiple times ?
            }
        }

        if (subFailed) {
            return fillFailedResult(result, "501", "Failed to subscribe to topic(s):" + topicsName);
        }
        if (sampleByTime) {
            try {
                TimeUnit.MILLISECONDS.sleep(sampleElapsedTime);
            } catch (InterruptedException e) {
                logger.log(Level.INFO, "Received exception when waiting for notification signal", e);
            }
            synchronized (dataLock) {
                result.sampleStart();
                return produceResult(result, topicsName);
            }
        } else if (sampleByContent) {
            synchronized (dataLock) {
                try {
                    if (timeOut > 0) {
                        dataLock.wait(timeOut);
                    } else {
                        dataLock.wait();
                    }
                } catch (InterruptedException e) {
                    logger.log(Level.INFO, "Received exception when waiting for notification signal", e);
                }
                result.sampleStart();
                return produceResult(result, topicsName);
            }
        } else {
            synchronized (dataLock) {
                int receivedCount1 = (batches.isEmpty() ? 0 : batches.element().getReceivedCount());
                boolean needWait = false;
                if (receivedCount1 < sampleCount) {
                    needWait = true;
                }

                if (needWait) {
                    try {
                        dataLock.wait(sampleCountTime);
                    } catch (InterruptedException e) {
                        logger.log(Level.INFO, "Received exception when waiting for notification signal", e);
                    }
                }
                result.sampleStart();
                return produceResult(result, topicsName);
            }
        }

    }

    private SampleResult produceResult(SampleResult result, String topicName) {
        SubBean bean = batches.poll();
        if (bean == null) { // In "elapsed time" mode, return "dummy" when time is reached
            bean = new SubBean();
        }
        int receivedCount = bean.getReceivedCount();
        List<String> contents = bean.getContents();
        String message = MessageFormat.format("Received {0} of message.", receivedCount);
        StringBuffer content = new StringBuffer("");
        if (isDebugResponse()) {
            for (int i = 0; i < contents.size(); i++) {
                content.append(contents.get(i) + "\n");
            }
        }
        result = fillOKResult(result, bean.getReceivedMessageSize(), message, content.toString());
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("sub [topic]: " + topicName + ", [payload]: " + content.toString());
        }

        if (receivedCount == 0) {
            result.setEndTime(result.getStartTime()); // dummy result, rectify sample time
        } else {
            if (isAddTimestamp()) {
                result.setEndTime(result.getStartTime() + (long) bean.getAvgElapsedTime()); // rectify sample time
                result.setLatency((long) bean.getAvgElapsedTime());
            } else {
                result.setEndTime(result.getStartTime()); // received messages w/o timestamp, then we cannot reliably calculate elapsed time
            }
        }
        result.setSampleCount(receivedCount);

        return result;
    }

    private void listenToTopics(final String topicsName) {
        int qos;
        try {
            qos = Integer.parseInt(getQOS());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex, () -> MessageFormat.format("Specified invalid QoS value {0}, set to default QoS value {1}!", ex.getMessage(), QOS_0));
            qos = QOS_0;
        }

        final String[] paraTopics = topicsName.split(",");
        if (qos < 0 || qos > 2) {
            logger.severe("Specified invalid QoS value, set to default QoS value " + qos);
            qos = QOS_0;
        }

        connection.subscribe(paraTopics, MQTTQoS.fromValue(qos), () -> {
            logger.fine(() -> "sub successful, topic length is " + paraTopics.length);
        }, error -> {
            logger.log(Level.INFO, "subscribe failed", error);
            subFailed = true;
        });
    }

    private void setListener(final boolean sampleByTime, final int sampleCount, final boolean sampleByContent, final Assertions assertion) {
        connection.setSubListener(((topic, message, ack) -> {
            ack.run();
            if (sampleByTime) {
                synchronized (dataLock) {
                    handleSubBean(sampleByTime, message, sampleCount , sampleByContent);
                }
            } else if (sampleByContent) {
                synchronized (dataLock) {

                    if (assertion == null) {
                        return;
                    }
                    SubBean bean = handleSubBean(sampleByTime, message, sampleCount , sampleByContent);
                    List<String> contentsMessage = bean.getContents();
                    logger.log(Level.INFO, "sub名称为：" + getName() + " sub接收到的消息为" + contentsMessage);
                    if (CollectionUtils.isNotEmpty(contentsMessage)) {
                        for (String contents : contentsMessage) {
                            logger.log(Level.INFO, "需要匹配的内容为 " + contents);
                            ContentCompare contentCompare = new ContentCompare();
                            List<Boolean> flagList = new ArrayList<>();
                            if (CollectionUtils.isNotEmpty(assertion.getList())) {
                                assertion.getList().forEach(item -> {
                                    if (StringUtils.isNotEmpty(item.getType())) {
                                        switch (item.getType()) {
                                            case "Text":
                                                if (item.getEnable()) {
                                                    flagList.add(contentCompare.textCompare(contents, item));
                                                }
                                                break;
                                            case "JSON":
                                                try {
                                                    JSONObject jsonObject = JSONObject.parseObject(contents);
                                                    if (item.getEnable()) {
                                                        flagList.add(contentCompare.jsonPathCompare(contents, item));
                                                    }
                                                    break;
                                                } catch (Exception e) {
                                                    logger.log(Level.INFO, "收到的消息不是正常的json格式,无法获得匹配内容");
                                                    break;
                                                }
                                            case "XPath2":
                                                if (item.getEnable()) {
                                                    flagList.add(contentCompare.xpathCompare(contents, item));
                                                }
                                                break;
                                        }
                                    }
                                });
                            }
                            logger.log(Level.INFO, "sub名称为：" + getName() + "匹配内容结果为" + flagList);
                            if (CollectionUtils.isNotEmpty(flagList)) {
                                List<Boolean> compareCollect = flagList.stream().filter(flagStatus -> flagStatus == true).collect(Collectors.toList());
                                List<AssertionsContent> collect = assertion.getList().stream().filter(assertionList -> assertionList.getEnable() == true).collect(Collectors.toList());
                                logger.log(Level.INFO, "过滤结果为" + compareCollect);
                                if (StringUtils.equals(assertion.getFilterType(), "And")) {
                                    if (compareCollect.size() == collect.size() && collect.size() > 0) {
                                        logger.log(Level.INFO, "匹配条件为and  退出等待");
                                        dataLock.notify();
                                        break;
                                    }
                                } else {
                                    if (compareCollect.size() > 0) {
                                        logger.log(Level.INFO, "匹配条件为or  退出等待");
                                        dataLock.notify();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                synchronized (dataLock) {
                    SubBean bean = handleSubBean(sampleByTime, message, sampleCount , sampleByContent);
                    logger.log(Level.INFO,  "sub名称为：" + getName() + "sub接收到的消息为" + bean.getContents());
                    if (bean.getReceivedCount() == sampleCount) {
                        dataLock.notify();
                    }
                }
            }
        }));
    }

    private SubBean handleSubBean(boolean sampleByTime, String msg, int sampleCount , boolean sampleByContent) {
        SubBean bean = null;
        if (batches.isEmpty()) {
            bean = new SubBean();
            batches.add(bean);
        } else {
            SubBean[] beans = new SubBean[batches.size()];
            batches.toArray(beans);
            bean = beans[beans.length - 1];
        }

        if ((!sampleByTime) && (bean.getReceivedCount() == sampleCount && (!sampleByContent))) { //Create a new batch when latest bean is full.
            logger.info("The tail bean is full, will create a new bean for it.");
            bean = new SubBean();
            batches.add(bean);
        }
        if (isAddTimestamp()) {
            long now = System.currentTimeMillis();
            int index = msg.indexOf(TIME_STAMP_SEP_FLAG);
            if (index == -1 && (!printFlag)) {
                logger.info(() -> "Payload does not include timestamp: " + msg);
                printFlag = true;
            } else if (index != -1) {
                long start = Long.parseLong(msg.substring(0, index));
                long elapsed = now - start;

                double avgElapsedTime = bean.getAvgElapsedTime();
                int receivedCount = bean.getReceivedCount();
                avgElapsedTime = (avgElapsedTime * receivedCount + elapsed) / (receivedCount + 1);
                bean.setAvgElapsedTime(avgElapsedTime);
            }
        }
        if (isDebugResponse()) {
            bean.getContents().add(msg);
        }
        bean.setReceivedMessageSize(bean.getReceivedMessageSize() + msg.length());
        bean.setReceivedCount(bean.getReceivedCount() + 1);
        return bean;
    }

    private SampleResult fillFailedResult(SampleResult result, String code, String message) {
        result.sampleStart();
        result.setResponseCode(code); // 5xx means various failures
        result.setSuccessful(false);
        result.setResponseMessage(message);
        if (clientId != null) {
            result.setResponseData(MessageFormat.format("Client [{0}]: {1}", clientId, message).getBytes());
        } else {
            result.setResponseData(message.getBytes());
        }
        result.sampleEnd();

        // avoid massive repeated "early stage" failures in a short period of time
        // which probably overloads JMeter CPU and distorts test metrics such as TPS, avg response time
        try {
            TimeUnit.MILLISECONDS.sleep(SUB_FAIL_PENALTY);
        } catch (InterruptedException e) {
            logger.log(Level.INFO, "Received exception when waiting for notification signal", e);
        }
        return result;
    }

    private SampleResult fillOKResult(SampleResult result, int size, String message, String contents) {
        result.setResponseCode("200");
        result.setSuccessful(true);
        result.setResponseMessage(message);
        result.setBodySize(size);
        result.setBytes(size);
        result.setResponseData(contents.getBytes());
        result.sampleEnd();
        logger.log(Level.INFO, connection + "服务器接收消息成功");
        return result;
    }

}
