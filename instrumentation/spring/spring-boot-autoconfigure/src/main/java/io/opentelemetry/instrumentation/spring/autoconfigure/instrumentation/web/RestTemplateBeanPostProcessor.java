/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.spring.autoconfigure.instrumentation.web;

import io.opentelemetry.instrumentation.spring.autoconfigure.internal.OpenTelemetrySupplier;
import io.opentelemetry.instrumentation.spring.web.v3_1.SpringWebTelemetry;
import java.util.List;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

final class RestTemplateBeanPostProcessor implements BeanPostProcessor {

  private final OpenTelemetrySupplier openTelemetrySupplier;

  RestTemplateBeanPostProcessor(OpenTelemetrySupplier openTelemetrySupplier) {
    this.openTelemetrySupplier = openTelemetrySupplier;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) {
    if (!(bean instanceof RestTemplate)) {
      return bean;
    }

    RestTemplate restTemplate = (RestTemplate) bean;
    ClientHttpRequestInterceptor interceptor =
        SpringWebTelemetry.create(openTelemetrySupplier.get()).newInterceptor();
    addRestTemplateInterceptorIfNotPresent(restTemplate, interceptor);
    return restTemplate;
  }

  private static void addRestTemplateInterceptorIfNotPresent(
      RestTemplate restTemplate, ClientHttpRequestInterceptor instrumentationInterceptor) {
    List<ClientHttpRequestInterceptor> restTemplateInterceptors = restTemplate.getInterceptors();
    if (restTemplateInterceptors.stream()
        .noneMatch(
            interceptor -> interceptor.getClass() == instrumentationInterceptor.getClass())) {
      restTemplateInterceptors.add(0, instrumentationInterceptor);
    }
  }
}
