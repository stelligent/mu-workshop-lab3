package com.stelligent.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Created by casey.lee on 5/5/17.
 */
@Component
public class BananaProvider implements FlavorProvider {

  @Autowired
  private RestTemplate restTemplate;

  @Override
  public Flavor getFlavor() {
    return Flavor.Banana;
  }

  @Override
  public void getIngredients() throws IngredientException {
    List<Map<String,Object>> bananas = getAll();
    if(bananas.size() < 2) {
      throw new IngredientException("Not enough bananas to make the shake.");
    }

    bananas.stream()
            .limit(2)
            .forEach(this::delete);
  }

  private List<Map<String,Object>> getAll() {
    ParameterizedTypeReference<List<Map<String, Object>>> typeRef =
            new ParameterizedTypeReference<List<Map<String, Object>>>() {};

    ResponseEntity<List<Map<String, Object>>> exchange =
            this.restTemplate.exchange("http://banana-service/bananas",
                    HttpMethod.GET,null, typeRef);

    return exchange.getBody();
  }

  private void delete(Map<String,Object> b) {
    String url = ((List<Map<String,String>>)b.get("links"))
            .stream()
            .filter(l -> l.get("rel").equals("self"))
            .map(l -> l.get("href"))
            .findFirst()
            .get();
    URI uri = URI.create(url);
    this.restTemplate.delete("http://banana-service/{path}",uri.getPath());
  }
}
