package com.stelligent.controller;

import com.stelligent.domain.Milkshake;
import com.stelligent.service.Flavor;
import com.stelligent.service.FlavorProvider;
import com.stelligent.service.IngredientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


/**
 * REST service to handle the CRUD operations for a Milkshake
 */
@RequestMapping("/milkshakes")
@RestController
public class MilkshakeController {

  private final AtomicLong counter = new AtomicLong();
  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private List<FlavorProvider> flavorProviders;

  /**
   * Handle a POST method by creating a new Milkshake.
   *  Queries appropriate fruit service to check for inventory and consume the fruit into the milkshake
   *
   * @param flavor to create
   * @return a newly created Milkshake
   */
  @RequestMapping(method = RequestMethod.POST)
  public @ResponseBody Milkshake create(@RequestParam Flavor flavor) {

    try {
      FlavorProvider provider = getFlavorProvider(flavor);
      provider.getIngredients();
    } catch (IngredientException e) {
      throw new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
    }

    Milkshake milkshake = new Milkshake();
    milkshake.setId(counter.incrementAndGet());
    milkshake.setFlavor(flavor);
    return milkshake;
  }

  private FlavorProvider getFlavorProvider(Flavor flavor) throws IngredientException {
    return flavorProviders
            .stream()
            .filter(fp -> fp.getFlavor() == flavor)
            .findFirst()
            .orElseThrow(() -> new IngredientException("No flavor provider for "+flavor));
  }


}