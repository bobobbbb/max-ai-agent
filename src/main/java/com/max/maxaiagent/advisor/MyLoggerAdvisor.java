package com.max.maxaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import opennlp.tools.tokenize.Detokenizer;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.model.MessageAggregator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;


@Component
@Slf4j
public class MyLoggerAdvisor implements StreamAroundAdvisor {
    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        advisedRequest = this.before(advisedRequest);
        Flux<AdvisedResponse> advisedResponseFlux = chain.nextAroundStream(advisedRequest);

        return (new MessageAggregator()).aggregateAdvisedResponse(advisedResponseFlux,this::after);
    }

    @Override
    public String getName() {
        return "LoggerAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
    public AdvisedRequest before(AdvisedRequest advisedRequest){
        log.info("before:");
        return advisedRequest;
    }

    public void after(AdvisedResponse advisedResponse){
        // 只输出 promptTokens
        log.info("after:");
        try {
            Object response = advisedResponse.response();
            // ChatResponse 一般有 getMetadata().getUsage().getPromptTokens()
            // 反射方式兼容不同依赖
            java.lang.reflect.Method getMetadata = response.getClass().getMethod("getMetadata");
            Object metadata = getMetadata.invoke(response);
            java.lang.reflect.Method getUsage = metadata.getClass().getMethod("getUsage");
            Object usage = getUsage.invoke(metadata);
            java.lang.reflect.Method getPromptTokens = usage.getClass().getMethod("getPromptTokens");
            Object promptTokens = getPromptTokens.invoke(usage);
            log.info("promptTokens={}", promptTokens);
        } catch (Exception e) {
            log.warn("Failed to extract promptTokens", e);
        }
    }
}
