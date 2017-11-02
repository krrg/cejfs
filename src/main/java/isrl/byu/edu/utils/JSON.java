package isrl.byu.edu.utils;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSON {

    public static String SetOfStringsToJson(HashSet<String> setOfStrings) throws JsonProcessingException
    {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(setOfStrings);
        return jsonString;
    }

    public static HashSet<String>  JsonToSetOfStrings(String jsonString) throws IOException {
        //todo: test this. I am unsure this will work
        HashSet<String> setOfStrings = new HashSet<>();
        ObjectMapper mapper = new ObjectMapper();
        setOfStrings = mapper.readValue(jsonString,setOfStrings.getClass());
        return setOfStrings;
    }

}
