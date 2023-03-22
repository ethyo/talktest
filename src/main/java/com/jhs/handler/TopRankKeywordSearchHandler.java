package com.jhs.handler;



import com.jhs.entity.UserStringInterface;
import com.jhs.service.TopRankKeywordSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/top_rank_keyword_search")
public class TopRankKeywordSearchHandler {
    private final TopRankKeywordSearchService topRankKeywordSearchService;

    public Mono<ServerResponse> getList(ServerRequest request) {
        List<UserStringInterface> list = topRankKeywordSearchService.getList(request);
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(list);
    }
}