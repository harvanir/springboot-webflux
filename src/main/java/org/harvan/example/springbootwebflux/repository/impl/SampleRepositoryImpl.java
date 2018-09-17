package org.harvan.example.springbootwebflux.repository.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.harvan.example.springbootwebflux.repository.SampleRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class SampleRepositoryImpl implements SampleRepository {

  private static final Logger LOGGER = LogManager.getLogger(SampleRepositoryImpl.class);

  private void sleep(long sleepSecond) {
    try {
      Thread.sleep(sleepSecond * 1000);
    } catch (InterruptedException e) {
      LOGGER.error("Error sleep.");
      Thread.interrupted();
    }
  }

  @Override
  public Mono<String> findSlowData(long sleepSecond) {
    return Mono.from(Mono.just("Slow data.").doOnNext(s -> {
      sleep(sleepSecond);
      LOGGER.debug("Emit data.");
    }));
  }
}