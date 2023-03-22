package com.jhs.service;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface BlogSearchService {
    Mono<ServerResponse> getList(ServerRequest request);
}