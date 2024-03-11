package ch.zhaw.statefulconversation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import ch.zhaw.statefulconversation.utils.NamedParametersFormatter;

@SpringBootTest
public class NamedParametersFormatterTest {

    private static String expected;

    @Test
    void test() {
        NamedParametersFormatterTest.expected = "This is a text stating that Daniel is 21 old.";

        String template = "This is a text stating that ${name} is ${age} old.";

        Map<String, JsonElement> params = new HashMap<>();
        params.put("name", new JsonPrimitive("Daniel"));
        params.put("age", new JsonPrimitive(21));

        String actual = NamedParametersFormatter.format(template, params);
        assertEquals(expected, actual);
    }

}