package com.jhs.config;

import com.jhs.handler.TopRankKeywordSearchHandler;
import com.jhs.handler.BlogSearchHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class WebRoutingConfig {
    // 블로그 검색
    @Bean
    public RouterFunction<ServerResponse> blogSearchRouter(BlogSearchHandler blogSearchHandler) {
        return RouterFunctions.route()
                .GET("/api/blog_search", blogSearchHandler::getList)
                .build();
    }

    // 인기 검색어 목록
    @Bean
    public RouterFunction<ServerResponse> topRankKeywordSearchRouter(TopRankKeywordSearchHandler topRankKeywordSearchHandler) {
        return RouterFunctions.route()
                .GET("/api/top_rank_keyword_search", topRankKeywordSearchHandler::getList)
                .build();
    }
}
