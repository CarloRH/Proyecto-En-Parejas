package com.example.fithub360.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChatCompletionRequest {
    @SerializedName("model")
    private String model;

    @SerializedName("messages")
    private List<Message> messages;

    @SerializedName("temperature")
    private Double temperature;

    @SerializedName("top_p")
    private Double topP;

    @SerializedName("max_tokens")
    private Integer maxTokens;

    public ChatCompletionRequest(String model, List<Message> messages, Double temperature, Double topP, Integer maxTokens) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
        this.topP = topP;
        this.maxTokens = maxTokens;
    }

    public String getModel() { return model; }
    public List<Message> getMessages() { return messages; }
    public Double getTemperature() { return temperature; }
    public Double getTopP() { return topP; }
    public Integer getMaxTokens() { return maxTokens; }
}

