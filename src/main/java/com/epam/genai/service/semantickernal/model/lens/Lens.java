package com.epam.genai.service.semantickernal.model.lens;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Lens {
  private Brand brand;
  private String model;
  private PhotographyGenre photographyGenre;
  private double price;
  private LensMount lensMount;
}

