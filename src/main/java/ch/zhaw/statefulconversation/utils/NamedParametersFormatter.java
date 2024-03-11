package ch.zhaw.statefulconversation.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class NamedParametersFormatter {

    /*
     * https://www.baeldung.com/java-string-formatting-named-placeholders
     */
    public static String format(String template, Map<String, JsonElement> parameters) {
        StringBuilder newTemplate = new StringBuilder(template);
        List<Object> valueList = new ArrayList<>();

        Matcher matcher = Pattern.compile("[$][{](\\w+)}").matcher(template);

        String currentValue;
        while (matcher.find()) {
            String key = matcher.group(1);

            String paramName = "${" + key + "}";
            int index = newTemplate.indexOf(paramName);
            if (index != -1) {
                newTemplate.replace(index, index + paramName.length(), "%s");
                currentValue = parameters.get(key).toString();
                if (parameters.get(key) instanceof JsonPrimitive) {
                    currentValue = currentValue.replaceAll("^\"|\"$", "");
                }
                valueList.add(currentValue); // added this .toString() to turn lists into [.., .., ..]
            }
        }

        return newTemplate.toString().formatted(valueList.toArray());
    }
}