package org.harvan.example.springbootwebflux.repository;

import reactor.core.publisher.Mono;

public interface SampleRepository {

  Mono<String> findSlowData(long sleepSecond);
}