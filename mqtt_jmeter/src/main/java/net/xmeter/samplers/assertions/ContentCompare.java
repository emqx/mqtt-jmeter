
package net.xmeter.samplers.assertions;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;
import net.minidev.json.JSONArray;
import net.xmeter.samplers.SubSampler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.XPathUtil;
import org.apache.oro.text.regex.Pattern;

import java.util.logging.Level;
import java.util.logging.Logger;


public class ContentCompare {

    private static final Logger logger = Logger.getLogger(SubSampler.class.getCanonicalName());


    public boolean textCompare (String textContent , AssertionsContent text) {
        logger.log(Level.INFO , "文本内容为" + textContent );
        logger.log(Level.INFO , "需要匹配的内容为" + text);
        if (StringUtils.isEmpty(textContent) || StringUtils.isEmpty(text.getValue())) {
            return false;
        }
        if (StringUtils.isNotEmpty(text.getOption()) && StringUtils.isNotEmpty(text.getValue())) {
            text.setExpect(text.getValue());
            return compareMethod(text, textContent);
        } else {
            return false;
        }
    }

    private boolean arrayMatched(JSONArray value , AssertionsContent jsonPath) {
        if (value.isEmpty() && "[]".equals(jsonPath.getExpect())) {
            return false;
        } else {
            Object[] var2 = value.toArray();
            int var3 = var2.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                Object subj = var2[var4];
                if (subj == null || this.compareMethod(jsonPath , subj.toString())) {
                    return false;
                }
            }
            return this.compareMethod(jsonPath , value.toString());
        }
    }

    private boolean isGt(String v1, String v2) {
        try {
            return v1.compareTo(v2) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isLt(String v1, String v2) {
        try {
            return v1.compareTo(v2) < 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean jsonPathCompare(String jsonString , AssertionsContent jsonPath) {
        if (StringUtils.isEmpty(jsonPath.getExpression()) || StringUtils.isEmpty(jsonString) || StringUtils.isEmpty(jsonPath.getExpect())) {
            return false;
        }
        Object value = JsonPath.read(jsonString, jsonPath.getExpression() , new Predicate[0]);
        logger.log(Level.INFO , "json内容为" + value );
        logger.log(Level.INFO , "需要匹配的json内容为" + jsonPath);
        if (value instanceof JSONArray) {
            return this.arrayMatched((JSONArray) value , jsonPath);
        } else {
            String str = value.toString();
            return compareMethod(jsonPath, str);
        }
    }

    private boolean compareMethod(AssertionsContent assertionsContent, String str) {
            if (StringUtils.isNotEmpty(assertionsContent.getOption())) {
                if (StringUtils.equals("REGEX", assertionsContent.getOption())) {
                    Pattern pattern = JMeterUtils.getPatternCache().getPattern(assertionsContent.getExpect());
                    return JMeterUtils.getMatcher().matches(str, pattern);
                } else {
                    boolean refFlag = false;
                    switch (assertionsContent.getOption()) {
                        case "CONTAINS":
                            refFlag = str.contains(assertionsContent.getExpect());
                            break;
                        case "NOT_CONTAINS":
                            refFlag = !str.contains(assertionsContent.getExpect());
                            break;
                        case "EQUALS":
                            refFlag = valueEquals(str, assertionsContent.getExpect());
                            break;
                        case "NOT_EQUALS":
                            refFlag = valueNotEquals(str, assertionsContent.getExpect());
                            break;
                        case "GT":
                            refFlag = isGt(str, assertionsContent.getExpect());
                            break;
                        case "LT":
                            refFlag = isLt(str, assertionsContent.getExpect());
                            break;
                        case "START_WITH":
                            refFlag = str.startsWith(assertionsContent.getExpect());
                            break;
                        case "END_WITH":
                            refFlag = str.endsWith(assertionsContent.getExpect());
                    }
                    return refFlag;
                }
            }
            return false;
    }

    public boolean xpathCompare (String xpathString , AssertionsContent xpath) {
        if (StringUtils.isEmpty(xpathString) || StringUtils.isEmpty(xpath.getExpression()) || xpath.getEnable() == false) {
            return false;
        }
        AssertionResult result = new AssertionResult("XPath2 Assertion");
        try {
            result.setFailure(false);
            result.setFailureMessage("");
            XPathUtil.computeAssertionResultUsingSaxon(result, xpathString, xpath.getExpression(),
                    "",false);
        } catch (Exception e) {
            logger.log(Level.INFO , "xpath断言报错信息为" + e);
            return false;
        }
        logger.log(Level.INFO , "xpath断言后的结果为" + result.isFailure());
        if (result.isFailure() == true) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean valueEquals(String v1, String v2) {
        try {
            Number number1 = NumberUtils.createNumber(v1);
            Number number2 = NumberUtils.createNumber(v2);
            return number1.equals(number2);
        } catch (Exception e) {
            return StringUtils.equals(v1, v2);
        }
    }

    private static boolean valueNotEquals(String v1, String v2) {
        try {
            Number number1 = NumberUtils.createNumber(v1);
            Number number2 = NumberUtils.createNumber(v2);
            return !number1.equals(number2);
        } catch (Exception e) {
            return !StringUtils.equals(v1, v2);
        }
    }

}
