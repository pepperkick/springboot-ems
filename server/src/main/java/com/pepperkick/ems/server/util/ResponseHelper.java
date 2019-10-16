package com.pepperkick.ems.server.util;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ResponseHelper {
    private ResponseHelper() {}

    public static JSONObject createErrorResponseJson(String message, HttpStatus status) {
        JSONObject res = new JSONObject();
        res.put("message", message);
        res.put("status", status);
        return res;
    }

    public static ResponseEntity createErrorResponseEntity(String message, HttpStatus status) {
        JSONObject res = createErrorResponseJson(message, status);
        return new ResponseEntity<>(res.toMap(), status);
    }
}
