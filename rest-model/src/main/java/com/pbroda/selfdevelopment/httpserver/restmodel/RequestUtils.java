package com.pbroda.selfdevelopment.httpserver.restmodel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import lombok.NoArgsConstructor;

import java.lang.reflect.Type;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class RequestUtils {

    static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss.S";

    private static Gson gson;

    static {
        Type accountType = new TypeToken<Account>() {}.getType();

        JsonSerializer<Account> accountSerializer = (Account account, Type type, JsonSerializationContext context) -> {
            return new JsonPrimitive(account.getAccount());
        };

        JsonDeserializer<Account> accountDeserializer = (JsonElement json, Type typeOfT, JsonDeserializationContext context) -> {
            return new Account(json.getAsString());
        };

        gson = new GsonBuilder().setDateFormat(DATE_FORMAT_STRING)
                                .registerTypeAdapter(accountType, accountSerializer)
                                .registerTypeAdapter(Account.class, accountDeserializer)
                                .create();
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }

}
