package com.stelligent.service;

import org.springframework.stereotype.Component;


/**
 * Created by casey.lee on 5/5/17.
 */
@Component
public class VanillaProvider implements FlavorProvider {
  @Override
  public Flavor getFlavor() {
    return Flavor.Vanilla;
  }

  @Override
  public void getIngredients() throws IngredientException {
    // always available
  }
}
