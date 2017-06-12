package com.stelligent.service;

/**
 * Created by casey.lee on 5/5/17.
 */
public interface FlavorProvider {
  Flavor getFlavor();
  void getIngredients() throws IngredientException;
}
