package com.shashanth.logger.context;

public class RequestContext {

    private static final ThreadLocal<String> requestId = new ThreadLocal<>();

    public static String getRequestId() {
        return requestId.get();
    }

    public static void setRequestId(String id) {
        requestId.set(id);
    }

    public static void clear() {
        requestId.remove();
    }
}


