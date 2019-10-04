package com.pepperkick.ems.util;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseHelper {
    public static JSONObject createErrorResponseJson(String message, HttpStatus status) {
        JSONObject res = new JSONObject();
        res.put("message", message);
        res.put("status", status);
        return res;
    }

    public static ResponseEntity CreateErrorResponseEntity(String message, HttpStatus status) {
        JSONObject res = createErrorResponseJson(message, status);
        return new ResponseEntity<>(res.toMap(), status);
    }
}
