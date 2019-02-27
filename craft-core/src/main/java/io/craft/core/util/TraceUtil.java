package io.craft.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TraceUtil {

    private static final ThreadLocal<String> traceId = new ThreadLocal<>();

    private static final ThreadLocal<Map<String, String>> header = new ThreadLocal<>();

    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static void setTraceId(String tid) {
        traceId.set(tid);
    }

    public static String getTraceId() {
        return traceId.get();
    }

    public static Map<String, String> getHeader() {
        Map<String, String> head = header.get();
        if (head == null) {
            head = new HashMap<>();
            header.set(head);
        }
        return head;
    }

    public static void setHeader(Map<String, String> head) {
        getHeader().putAll(head);
    }

    public static void addHeader(String name, String value) {
        getHeader().put(name, value);
    }

    public static void removeHeader(String name) {
        getHeader().remove(name);
    }

    public static void clear() {
        traceId.remove();
        getHeader().clear();
    }

}
