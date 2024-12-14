package com.epam.genai.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.genai.GenAiApplication;
import com.epam.genai.service.semantickernal.LensRecommendationPlugin;
import com.epam.genai.service.semantickernal.model.lens.Brand;
import com.epam.genai.service.semantickernal.model.lens.LensMount;
import com.epam.genai.service.semantickernal.model.lens.PhotographyGenre;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {GenAiApplication.class})
class LensRecommendationLlmServiceIT {

  @SpyBean
  private LensRecommendationLlmService lensRecommendationLlmService;

  @SpyBean
  private LensRecommendationPlugin lensRecommendationPlugin;

  @Test
  void recommendLenses() {
    String result2 = lensRecommendationLlmService.recommendLenses(
        "I have DSLR Canon camera, I love to shoot portraits. Recommend lens for my camera. my budget is 1000$.", 0.5);

    verify(lensRecommendationPlugin, times(1)).recommendLenses(Brand.CANON, LensMount.CANON_EF, PhotographyGenre.PORTRAIT, 1000.0);

    Assertions.assertNotNull(result2);
  }

  @Test
  void recommendLensesUnknownLensMount() {
    String result1 = lensRecommendationLlmService.recommendLenses(
        "I have Canon R5 camera, I have EF adapter for DSLR lenses, I love to shoot portraits. Recommend lens for my camera. my budget is 1000$.",
        0.5);

    verify(lensRecommendationPlugin, times(1)).recommendLenses(Brand.CANON, LensMount.CANON_RF, PhotographyGenre.PORTRAIT, 1000.0);

    Assertions.assertNotNull(result1);
  }
}