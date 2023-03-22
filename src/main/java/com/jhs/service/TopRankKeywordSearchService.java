package com.jhs.service;


import com.jhs.entity.UserStringInterface;
import org.springframework.web.reactive.function.server.ServerRequest;
import java.util.List;

public interface TopRankKeywordSearchService {
    List<UserStringInterface> getList(ServerRequest request);
}