package kika;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;

public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String writeJson(Object args) throws JsonProcessingException {
        return mapper.writeValueAsString(args);
    }

    public static String numericList(String... numbers) throws JsonProcessingException {
        long[] longNumbers = Arrays.stream(numbers).mapToLong(Long::parseLong).toArray();
        return mapper.writeValueAsString(longNumbers);
    }
}
