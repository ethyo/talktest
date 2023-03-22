package com.jhs.serviceimpl;


import com.jhs.entity.UserString;
import com.jhs.repository.UserStringRepository;
import com.jhs.service.BlogSearchService;
import com.jhs.util.JavaOpenKoreanTextProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.util.ArrayUtil;
import org.openkoreantext.processor.phrase_extractor.KoreanPhraseExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

import com.google.gson.Gson;
import scala.annotation.meta.param;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlogSearchServiceImpl implements BlogSearchService {
    private final String KAKAO_BLOG_SEARCH_URI = "https://dapi.kakao.com/v2/search/blog";
    private final UserStringRepository userStringRepository;

    @Value("${bearer.headerstring}")
    private String headstring;

    @Value("${bearer.issuancekey}")
    private String issuancekey;

    public Mono<ServerResponse> getList(ServerRequest request) {
        Mono<MultiValueMap<String, String>> formData = request.formData();
        Gson gson = new Gson();

        return checkParams(formData)
                .onErrorReturn(new HashMap())
                .doOnNext(param -> {
                    log.debug("request param size: " + param.size());
                })
                .map(data -> {
                    if(data.size() != 0) {
                        UserString us = UserString.builder().userstring(data.get("query").toString()).createddate(LocalDateTime.now()).build();
                        userStringRepository.save(us);
                        return data;
                    }else {
                        return new HashMap();
                    }
                })
                .flatMap(data -> {
                    if(data.size() != 0) {
                        // 형태소가 분석된 토큰 추출
                        String userInputString = data.get("query").toString();
                        List<KoreanPhraseExtractor.KoreanPhrase> text = JavaOpenKoreanTextProcessor.getProcessedInput(userInputString);
                        StringBuilder sb = new StringBuilder();
                        for (KoreanPhraseExtractor.KoreanPhrase koreanPhrase : text) {
                            log.debug("token: " + koreanPhrase.text() + "\n");
                            sb.append(koreanPhrase.text() + " ");
                        }

                        data.put("query", sb.toString()); // 형태소 분석된 키워드로 query 스트링 생성
                        log.debug("query string: " + sb);

                        String response = this.getRequest(KAKAO_BLOG_SEARCH_URI, data);
                        Map successMap = gson.fromJson(response, HashMap.class);
                        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(successMap);
                    }else {
                        Map<Object, Object> failMap = new HashMap<>();
                        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(failMap);
                    }
                });
    }

    private Mono<Map> checkParams(Mono<MultiValueMap<String, String>> params) {
        String[] sort_strings = {"accuracy", "recency"};
        List<String> valid_sort_strings = Arrays.asList(sort_strings);

        // 필수 값 확인
        return params.flatMap(item -> {
            Map<String, String> pItem = item.toSingleValueMap();

            String page = pItem.get("page");
            if(page != null) {
                if(!page.chars().allMatch(Character::isDigit)) {
                    return Mono.error(new Exception("page param is not valid, input number type"));
                }
            }

            if(!"".equals(pItem.get("query")) && pItem.get("query") != null) {
                String sort = pItem.get("sort");
                if(valid_sort_strings.contains(sort)) {
                    pItem.put("query", pItem.get("query"));
                    return Mono.just(pItem);
                }else {
                    return Mono.error(new Exception("sort param is not valid"));
                }
            }else {
                return Mono.error(new Exception("query is empty"));
            }
        });
    }

    public String getRequest(String pURL, Map<String, String> pList) {
        String myResult = "";

        try {
            //   URL 설정
            URL url = new URL(pURL);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            //전송 모드 설정 - 기본적인 설정
            http.setDefaultUseCaches(false);
            http.setDoInput(true);
            http.setDoOutput(true);
            http.setRequestMethod("GET");

            //헤더 세팅
            http.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            http.setRequestProperty("Authorization", headstring + " " + issuancekey);

            //서버로 값 전송
            StringBuffer buffer = new StringBuffer();

            //HashMap으로 전달받은 파라미터가 null이 아닌경우 버퍼에 넣어준다
            if (pList != null) {
                Set<String> key = pList.keySet();
                for (Iterator<String> iterator = key.iterator(); iterator.hasNext();) {
                    String keyName = iterator.next();
                    String valueName = pList.get(keyName);
                    if(iterator.hasNext()) {
                        buffer.append(keyName).append("=").append(valueName).append("&");
                    }else {
                        buffer.append(keyName).append("=").append(valueName);
                    }
                }
            }

            OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "UTF-8");
            PrintWriter writer = new PrintWriter(outStream);
            writer.write(buffer.toString());
            writer.flush();

            // 응답 처리
            InputStreamReader tmp = new InputStreamReader(http.getInputStream(), "UTF-8");
            BufferedReader reader = new BufferedReader(tmp);
            StringBuilder builder = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {
                builder.append(str + "\n");
            }
            myResult = builder.toString();
            return myResult;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myResult;
    }
}