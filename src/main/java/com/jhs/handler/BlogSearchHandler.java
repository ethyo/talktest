package com.jhs.handler;

import com.jhs.service.BlogSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/blog_search")
public class BlogSearchHandler {
    private final BlogSearchService blogSearchService;

    public Mono<ServerResponse> getList(ServerRequest request) {
        return blogSearchService.getList(request);
    }
}