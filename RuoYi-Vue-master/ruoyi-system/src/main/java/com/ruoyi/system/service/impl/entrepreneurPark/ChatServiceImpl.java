package com.ruoyi.system.service.impl.entrepreneurPark;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.system.domain.DTO.ChatResponse;
import com.ruoyi.system.domain.vo.ChatRequest;
import com.ruoyi.system.service.entrepreneurPark.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class ChatServiceImpl implements ChatService {

    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String GRANT_TYPE = "client_credentials";
    private static final String API_URL = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-4.0-turbo-128k";

    @Override
    public String getChatResponse(String args) {
        try {
            String accessToken = getAccessToken();
            System.out.println("Access Token: " + accessToken);

            String response = callChatAPI(accessToken, args);
            System.out.println("API Response: " + response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String callChatAPI(String accessToken, String message) throws Exception {
        URL url = new URL(API_URL + "?access_token=" + accessToken);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonInputString = String.format("{\"messages\": [{\"role\":\"user\", \"content\": \"%s\"}], \"stream\": true}", message);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                // 去除前缀 "data: "（如果有的话）
                if (responseLine.startsWith("data: ")) {
                    responseLine = responseLine.substring(5).trim();
                }

                // 如果是空行或者心跳包，跳过
                if (responseLine.isEmpty() || responseLine.equals("[DONE]")) {
                    continue;
                }

                // 解析每一行的JSON对象
                JSONObject jsonLine = JSON.parseObject(responseLine);
                boolean isEnd = jsonLine.getBooleanValue("is_end");

                // 累积结果
                if (jsonLine.containsKey("result")) {
                    response.append(jsonLine.getString("result"));
                }

                // 如果是最后一行，跳出循环
                if (isEnd) {
                    break;
                }
            }
        }

        return response.toString();
    }

    public String getAccessToken() {
        try {
            String clientId = "MTgZ3tb7eTR1CBWEcKBtEPoO";
            String clientSecret = "AyAGHvURuVqoLpzoZUyAgmHuD5Z78mjq";
            // 构建请求的URL
            URL url = new URL(TOKEN_URL);

            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // 构建请求体内容
            String requestBody = "grant_type=" + GRANT_TYPE + "&client_id=" + clientId + "&client_secret=" + clientSecret;

            // 发送请求
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 获取响应内容
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // 解析响应JSON
            JSONObject jsonResponse = JSON.parseObject(response.toString());

            // 提取access_token
            if (jsonResponse.containsKey("access_token")) {
                return jsonResponse.getString("access_token");
            } else {
                System.out.println("Error: " + jsonResponse.getString("error_description"));
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
