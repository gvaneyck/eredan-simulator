package eredan.simulator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eredan.dto.Hero;

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
            for (int i = 0; i < heroes.size(); i++) {
                heroes.get(i).id = i;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
