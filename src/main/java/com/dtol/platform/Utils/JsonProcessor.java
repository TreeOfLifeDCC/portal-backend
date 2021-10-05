package com.dtol.platform.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.util.List;

public class JsonProcessor {
    public static <T> List<T> unmarshallToList(String json, Class<T> classType)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        CollectionType javaType = mapper.getTypeFactory() // Check here whether you can take different function to directly de-serialize to the object than to a List.
                .constructCollectionType(List.class, classType);

        return mapper.readValue(json, javaType);
    }
    public static Object unmarshallToObject(String json, Class classType)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, classType);
    }
}
