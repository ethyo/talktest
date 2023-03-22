package com.jhs.serviceimpl;


import com.jhs.entity.UserString;
import com.jhs.repository.UserStringRepository;
import com.jhs.service.BlogSearchService;
import com.jhs.util.JavaOpenKoreanTextProcessor;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        return formData
                .map(data -> {
                    Map<String, String> dataMap = data.toSingleValueMap();
                    UserString us = UserString.builder().userstring(dataMap.get("query")).createddate(LocalDateTime.now()).build();
                    userStringRepository.save(us);
                    return dataMap;
                })
                .flatMap(data -> {
                    // 형태소가 분석된 토큰 추출
                    String userInputString = data.get("query");
                    List<KoreanPhraseExtractor.KoreanPhrase> text = JavaOpenKoreanTextProcessor.getProcessedInput(userInputString);
                    StringBuilder sb = new StringBuilder();
                    for (KoreanPhraseExtractor.KoreanPhrase koreanPhrase : text) {
                        log.debug("token: " + koreanPhrase.text() + "\n");
                        sb.append(koreanPhrase.text() + " ");
                    }

                    data.put("query", sb.toString()); // 형태소 분석된 키워드로 query 스트링 생성
                    log.debug("query string: " + sb);

                    String response = this.postRequest(KAKAO_BLOG_SEARCH_URI, data);
                    Map res = gson.fromJson(response, HashMap.class);
                    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res);
                })
                .switchIfEmpty(Mono.error(new NotFoundException("no param")));

    }

    public String postRequest(String pURL, Map<String, String> pList) {

        String myResult = "";

        try {
            //   URL 설정하고 접속하기
            URL url = new URL(pURL); // URL 설정

            HttpURLConnection http = (HttpURLConnection) url.openConnection(); // 접속
            //--------------------------
            //   전송 모드 설정 - 기본적인 설정
            //--------------------------
            http.setDefaultUseCaches(false);
            http.setDoInput(true); // 서버에서 읽기 모드 지정
            http.setDoOutput(true); // 서버로 쓰기 모드 지정
            http.setRequestMethod("POST"); // 전송 방식은 POST

            //--------------------------
            // 헤더 세팅
            //--------------------------
            // 서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다
            http.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            //http.setRequestProperty("Authorization", "KakaoAK 0606458087ba27b68f726181e3a79be3");
            http.setRequestProperty("Authorization", headstring + " " + issuancekey);


            //--------------------------
            //   서버로 값 전송
            //--------------------------
            StringBuffer buffer = new StringBuffer();

            //HashMap으로 전달받은 파라미터가 null이 아닌경우 버퍼에 넣어준다
            if (pList != null) {

                Set key = pList.keySet();

                for (Iterator iterator = key.iterator(); iterator.hasNext();) {
                    String keyName = (String) iterator.next();
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

            //--------------------------
            //   서버에서 전송받기
            //--------------------------
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