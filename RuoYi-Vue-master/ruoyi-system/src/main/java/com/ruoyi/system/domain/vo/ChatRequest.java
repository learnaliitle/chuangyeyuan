package com.ruoyi.system.domain.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("处理聊天请求")
public class ChatRequest {

    private String model;
    private List<Message> messages;
    private Boolean stream;
    private Float temperature;
    private Float topP;
    private Integer maxTokens;

    // Getters and Setters

    @Data
    public static class Message {
        private String role;
        private String content;

        // Getters and Setters
    }
}
