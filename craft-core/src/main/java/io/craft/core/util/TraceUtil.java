package io.craft.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TraceUtil {

    private static final ThreadLocal<String> traceId = new ThreadLocal<>();

    private static final ThreadLocal<Map<String, String>> cookie = new ThreadLocal<>();

    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static void setTraceId(String tid) {
        traceId.set(tid);
    }

    public static String getTraceId() {
        return traceId.get();
    }

    public static Map<String, String> getCookie() {
        Map<String, String> cookies = cookie.get();
        if (cookies == null) {
            cookies = new HashMap<>(0);
            cookie.set(cookies);
        }
        return cookies;
    }

    public static void setCookie(Map<String, String> head) {
        if (head == null) {
            return;
        }
        getCookie().putAll(head);
    }

    public static void addCookie(String name, String value) {
        getCookie().put(name, value);
    }

    public static String getCookie(String name) {
        return getCookie().get(name);
    }

    public static void removeCookie(String name) {
        getCookie().remove(name);
    }

    public static void clear() {
        traceId.remove();
        cookie.remove();
    }

}
