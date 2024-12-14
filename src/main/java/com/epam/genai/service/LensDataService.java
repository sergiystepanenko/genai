package com.epam.genai.service;

import com.epam.genai.service.semantickernal.model.lens.Brand;
import com.epam.genai.service.semantickernal.model.lens.Lens;
import com.epam.genai.service.semantickernal.model.lens.LensMount;
import com.epam.genai.service.semantickernal.model.lens.PhotographyGenre;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LensDataService {
  private final List<Lens> lensDb = new ArrayList<>();
  private final Map<Brand, Set<LensMount>> brandLensMountMap = Map.of(
      Brand.CANON, Set.of(LensMount.CANON_EF, LensMount.CANON_RF),
      Brand.NIKON, Set.of(LensMount.NIKON_F, LensMount.NIKON_Z),
      Brand.SONY, Set.of(LensMount.SONY_E),
      Brand.FUJIFILM, Set.of(LensMount.FUJIFILM_X),
      Brand.OLYMPUS, Set.of(LensMount.MICRO_FOUR_THIRDS),
      Brand.LEICA, Set.of(LensMount.LEICA_L)
  );

  public LensDataService() {
    initializeDatabase();
  }

  private void initializeDatabase() {
    lensDb.add(new Lens(Brand.CANON, "EF 50mm f/1.8 STM", PhotographyGenre.PORTRAIT, 125.00, LensMount.CANON_EF));
    lensDb.add(new Lens(Brand.CANON, "RF 85mm f/1.2L", PhotographyGenre.PORTRAIT, 3000, LensMount.CANON_RF));
    lensDb.add(new Lens(Brand.CANON, "RF 35mm f/1.4L VCM", PhotographyGenre.PORTRAIT, 999, LensMount.CANON_RF));
    lensDb.add(new Lens(Brand.CANON, "RF 15-35mm f/2.8L IS USM", PhotographyGenre.LANDSCAPE, 2399.00, LensMount.CANON_RF));
    lensDb.add(new Lens(Brand.CANON, "EF 100-400mm f/4.5-5.6L IS II USM", PhotographyGenre.WILDLIFE, 2399.00, LensMount.CANON_EF));

    lensDb.add(new Lens(Brand.NIKON, "Z 24-70mm f/2.8 S", PhotographyGenre.LANDSCAPE, 2299.95, LensMount.NIKON_Z));
    lensDb.add(new Lens(Brand.NIKON, "AF-S 85mm f/1.4G", PhotographyGenre.PORTRAIT, 1596.95, LensMount.NIKON_F));
    lensDb.add(new Lens(Brand.NIKON, "Z 14-30mm f/4 S", PhotographyGenre.LANDSCAPE, 1299.95, LensMount.NIKON_Z));

    lensDb.add(new Lens(Brand.SONY, "FE 70-200mm f/4 G OSS", PhotographyGenre.WILDLIFE, 1498.00, LensMount.SONY_E));
    lensDb.add(new Lens(Brand.SONY, "FE 24-105mm f/4 G OSS", PhotographyGenre.GENERAL, 1298.00, LensMount.SONY_E));
    lensDb.add(new Lens(Brand.SONY, "FE 85mm f/1.8", PhotographyGenre.PORTRAIT, 598.00, LensMount.SONY_E));


    lensDb.add(new Lens(Brand.TAMRON, "SP 90mm f/2.8 Di Macro", PhotographyGenre.MACRO, 649.00, LensMount.CANON_EF));
    lensDb.add(new Lens(Brand.TAMRON, "28-75mm f/2.8 Di III RXD", PhotographyGenre.GENERAL, 879.00, LensMount.SONY_E));
    lensDb.add(new Lens(Brand.TAMRON, "17-28mm f/2.8 Di III RXD", PhotographyGenre.LANDSCAPE, 899.00, LensMount.SONY_E));

    lensDb.add(new Lens(Brand.SIGMA, "18-35mm f/1.8 DC HSM", PhotographyGenre.LANDSCAPE, 799.00, LensMount.NIKON_F));
    lensDb.add(new Lens(Brand.SIGMA, "105mm f/2.8 DG DN Macro", PhotographyGenre.MACRO, 799.00, LensMount.SONY_E));
    lensDb.add(new Lens(Brand.SIGMA, "24-70mm f/2.8 DG DN Art", PhotographyGenre.GENERAL, 1099.00, LensMount.SONY_E));
    lensDb.add(new Lens(Brand.SIGMA, "35mm f/1.4 Art", PhotographyGenre.PORTRAIT, 600, LensMount.CANON_RF));

    lensDb.add(new Lens(Brand.FUJIFILM, "XF 35mm f/2 R WR", PhotographyGenre.PORTRAIT, 399.00, LensMount.FUJIFILM_X));
    lensDb.add(new Lens(Brand.FUJIFILM, "XF 16-55mm f/2.8 R LM WR", PhotographyGenre.GENERAL, 1199.00, LensMount.FUJIFILM_X));

    lensDb.add(new Lens(Brand.PANASONIC, "Lumix S Pro 50mm f/1.4", PhotographyGenre.PORTRAIT, 2299.99, LensMount.LEICA_L));

    lensDb.add(
        new Lens(Brand.OLYMPUS, "M.Zuiko Digital ED 12-40mm f/2.8 Pro", PhotographyGenre.LANDSCAPE, 999.99, LensMount.MICRO_FOUR_THIRDS));
    lensDb.add(
        new Lens(Brand.OLYMPUS, "M.Zuiko Digital ED 40-150mm f/2.8 Pro", PhotographyGenre.WILDLIFE, 1499.99, LensMount.MICRO_FOUR_THIRDS));
  }

  public List<Lens> recommendLenses(Brand brand, LensMount lensMount, PhotographyGenre photographyGenre, double budget) {
    return lensDb.stream()
        .filter(lens -> brandLensMountMap.get(brand).contains(lens.getLensMount()) || lens.getLensMount() == lensMount)
        .filter(lens -> lens.getPhotographyGenre() == photographyGenre)
        .filter(lens -> lens.getPrice() <= budget)
        .collect(Collectors.toList());
  }
}
