package simulator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.Hero;

import java.io.File;
import java.util.List;

public class Heroes {
    public static List<Hero> heroes;

    static {
        loadHeroes();
    }

    private static void loadHeroes() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            heroes = objectMapper.readValue(new File("heroes.json"), new TypeReference<List<Hero>>() { });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
