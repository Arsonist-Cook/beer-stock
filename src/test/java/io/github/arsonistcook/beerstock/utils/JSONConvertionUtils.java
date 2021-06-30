package io.github.arsonistcook.beerstock.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONConvertionUtils {
    public static String asJSONString(Object beerDTO) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            return mapper.writeValueAsString(beerDTO);
        }
        catch (Exception exception){
            throw new RuntimeException(exception);
        }
    }
}
