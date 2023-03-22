package com.jhs.serviceimpl;



import com.jhs.entity.UserString;
import com.jhs.entity.UserStringInterface;
import com.jhs.repository.TopRankKeywordSearchRepository;
import com.jhs.service.TopRankKeywordSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import java.util.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class TopRankKeywordSearchServiceImpl implements TopRankKeywordSearchService {

    @Autowired
    private final TopRankKeywordSearchRepository topRankKeywordSearchRepository;

    @Override
    public List<UserStringInterface> getList(ServerRequest request) {
        return topRankKeywordSearchRepository.findDistinctBy();
    }
}