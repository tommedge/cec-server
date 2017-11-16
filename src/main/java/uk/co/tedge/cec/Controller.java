package uk.co.tedge.cec;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class Controller {

    private final Map<String, Integer> inputMappings;

    public Controller() throws IOException {
        inputMappings = getInputMappings();
    }

    @PutMapping("/input/{value}")
    public void input(@PathVariable("value") String value) throws IOException {
        System.out.println(value);
        Socket clientSocket = new Socket("192.168.0.11", 9526);
        BufferedOutputStream outToServer = new BufferedOutputStream(clientSocket.getOutputStream());

        Optional<Integer> number = lookupNumber(value);

        if (number.isPresent()) {
            String message = "4f:82:" + number.get() + "0:00\n";

            outToServer.write(message.getBytes());
            outToServer.close();
            clientSocket.close();
        }
    }

    private Optional<Integer> lookupNumber(String value) {
        try {
            return Optional.of(Integer.valueOf(value));
        } catch (NumberFormatException e) {

        }

        return Optional.ofNullable(inputMappings.get(value.toLowerCase()));
    }

    private Map<String, Integer> getInputMappings() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream stream = Controller.class.getResourceAsStream("/inputs.json");
        String collect = new BufferedReader(new InputStreamReader(stream))
                .lines().collect(Collectors.joining("\n"));

        Map<String, Integer> mappings = new HashMap<>();

        Map<String, Integer> map = mapper.readValue(collect, Map.class);
        for (String key : map.keySet()) {
            mappings.put(key, map.get(key));
        }
        return mappings;
    }
}