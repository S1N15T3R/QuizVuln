package com.team404bnf.quizvuln.network;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface OpenRouterService {
    @POST("chat/completions")
    Call<Map<String, Object>> createChatCompletion(@Body Map<String, Object> body);
}
