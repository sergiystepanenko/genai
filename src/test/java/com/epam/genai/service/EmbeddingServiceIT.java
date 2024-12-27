package com.epam.genai.service;

import com.epam.genai.GenAiApplication;
import com.epam.genai.service.embedding.EmbeddingService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {GenAiApplication.class})
class EmbeddingServiceIT {

  @SpyBean
  private EmbeddingService embeddingService;

  @Test
  void search() {
    ArrayList<String> animalFacts = new ArrayList<>();

    animalFacts.add("Communicating with clicks, dolphins use echolocation to hunt.");
    animalFacts.add("Gliding silently, owls have specially designed feather edges.");
    animalFacts.add("Changing colors, chameleons blend with their surroundings.");
    animalFacts.add("Regenerating limbs, starfish can regrow lost appendages.");
    animalFacts.add("Producing silk, spiders create intricate webs to trap prey.");
    animalFacts.add("Migrating yearly, monarch butterflies travel thousands of miles.");
    animalFacts.add("Sleeping upside down, bats hang from their feet in caves.");
    animalFacts.add("Filtering water, whales consume tons of tiny krill daily.");
    animalFacts.add("Building dams, beavers alter ecosystems to create ponds.");
    animalFacts.add("Flaring feathers, peacocks attract mates with vibrant displays.");

    animalFacts.forEach(animalFact -> embeddingService.saveEmbedding(animalFact));

    List<String> results = embeddingService.searchEmbedding("Who uses water?", 3);
    Assertions.assertNotNull(results);
    Assertions.assertFalse(results.isEmpty());
  }

}