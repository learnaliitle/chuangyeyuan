package com.ruoyi.system.domain.DTO;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("处理聊天请求")
public class ChatResponse {

    private String id;
    private String model;
    private List<Choice> choices;
    private Usage usage;

    // Getters and Setters
    @Data
    public static class Choice {
        private Integer index;
        private Message message;
        private String finishReason;

        // Getters and Setters
        @Data
        public static class Message {
            private String role;
            private String content;

            // Getters and Setters
        }
    }
    @Data
    public static class Usage {
        private Integer completionTokens;
        private Integer promptTokens;
        private Integer totalTokens;

        // Getters and Setters
    }
}
