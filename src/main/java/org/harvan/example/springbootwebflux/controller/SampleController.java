package org.harvan.example.springbootwebflux.controller;


import static reactor.core.scheduler.Schedulers.elastic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class SampleController {

  private static final Logger LOGGER = LogManager.getLogger(SampleController.class);
  private static final String OK = "OK.";

  private void sleep(long second) {
    try {
      LOGGER.debug(() -> String.format("Sleeping %s seconds.", second));
      Thread.sleep(second * 1000);
    } catch (InterruptedException e) {
      LOGGER.error(() -> "Error sleep.", e);
      Thread.currentThread().interrupt();
    }
  }

  private String ok() {
    return OK;
  }

  private Mono<String> getPublisher(long sleepInSecond) {
    return Mono.defer(() -> {
      sleep(sleepInSecond);
      LOGGER.debug(() -> "Create publisher");

      return Mono.just(ok());
    });
  }

  @GetMapping("/defaultThread/{sleepInSecond}")
  public Mono<String> defaultThread(@PathVariable long sleepInSecond) {
    return getPublisher(sleepInSecond
    ).map(s -> {
      sleep(sleepInSecond);
      LOGGER.debug(() -> "Map defaultThread");
      return s;
    }).doOnSubscribe(subscription -> LOGGER.debug(() -> "Invoke defaultThread"));
  }

  @GetMapping("/elasticSubscribeOnElastic/{sleepInSecond}")
  public Mono<String> elasticSubscribeOnElastic(@PathVariable long sleepInSecond) {
    return getPublisher(sleepInSecond
    ).subscribeOn(elastic()
    ).map(s -> {
      sleep(sleepInSecond);
      LOGGER.debug(() -> "Map elasticSubscribeOnElastic");
      return s;
    }).doOnSubscribe(subscription -> LOGGER.debug(() -> "Invoke elasticSubscribeOnElastic")
    );
  }

  @GetMapping("/elasticPublishOnElastic/{sleepInSecond}")
  public Mono<String> elasticPublishOnElastic(@PathVariable long sleepInSecond) {
    return getPublisher(sleepInSecond
    ).publishOn(elastic()).map(s -> {
      sleep(sleepInSecond);
      LOGGER.debug(() -> "Map elasticPublishOnElastic");
      return s;
    }).doOnSubscribe(subscription -> LOGGER.debug(() -> "Invoke elasticPublishOnElastic")
    );
  }

  private Mono<String> innerMonoDirect() {
    LOGGER.debug(() -> "Inner mono direct.");
    return Mono.just(" Inner Mono direct.").map(s -> {
      LOGGER.debug("Map inner mono direct...");
      return s;
    });
  }

  private Mono<String> innerMonoDefer() {
    return Mono.defer(() -> {
      LOGGER.debug(() -> "Create inner mono.");

      return Mono.just(" Inner Mono defer.");
    }).map(s -> {
      LOGGER.debug("Map inner mono defer...");
      return s + " After map";
    });
  }

  @GetMapping("/elasticSubscribeOnPublishOnElastic/{sleepInSecond}")
  public Mono<String> elasticSubscribeOnPublishOnElastic(@PathVariable long sleepInSecond) {
    return getPublisher(sleepInSecond
//    ).publishOn(elastic()
    ).subscribeOn(elastic()
    ).flatMap(s -> innerMonoDirect().map(s1 -> {
      LOGGER.debug(() -> "Map external innerMonoDirect...");
      return s + s1;
    })).flatMap(s -> {
      sleep(sleepInSecond);
      LOGGER.debug(() -> "Map elasticSubscribeOnPublishOnElastic");
      return innerMonoDefer().map(s1 -> {
        LOGGER.debug(() -> "Map external innerMonoDefer...");
        return s + s1;
      });
    }).doOnSubscribe(subscription -> LOGGER.debug(() -> "Invoke elasticSubscribeOnPublishOnElastic")
    );
  }
}