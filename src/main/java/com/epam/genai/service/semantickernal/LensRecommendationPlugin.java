package com.epam.genai.service.semantickernal;

import com.epam.genai.service.LensDataService;
import com.epam.genai.service.semantickernal.model.lens.Brand;
import com.epam.genai.service.semantickernal.model.lens.Lens;
import com.epam.genai.service.semantickernal.model.lens.LensMount;
import com.epam.genai.service.semantickernal.model.lens.PhotographyGenre;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LensRecommendationPlugin {

  private final LensDataService lensDataService;

  @DefineKernelFunction(
      name = "recommend_lens",
      description = "Recommend photographers to select the best lens for their specific use case, taking into account their camera model, lens mount, budget, and photography genre.",
      returnDescription = "Returns the list of recommended lenses.",
      returnType = "java.util.List")
  public List<Lens> recommendLenses(
      @KernelFunctionParameter(name = "brand", description = "The brand or manufacturer of camera.", type = Brand.class)
      Brand brand,
      @KernelFunctionParameter(name = "lensMount", description = "The lens mount.", type = LensMount.class, required = false)
      LensMount lensMount,
      @KernelFunctionParameter(name = "photographyGenre", description = "The major photography genre to use the lens.", type = PhotographyGenre.class)
      PhotographyGenre photographyGenre,
      @KernelFunctionParameter(name = "maxPrice", description = "The maximum price of lens", type = Double.class)
      Double maxPrice
  ) {
    log.debug("Function recommend_lens params: brand={}, lensMount={}, photographyGenre={}, maxPrice={}", brand, lensMount,
        photographyGenre,
        maxPrice);

    var lenses = lensDataService.recommendLenses(brand, lensMount, photographyGenre, maxPrice);
    log.debug("Function recommend_lens return: lenses={}", lenses);
    return lenses;
  }
}
