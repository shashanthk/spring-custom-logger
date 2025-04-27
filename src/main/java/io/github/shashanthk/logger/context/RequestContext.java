package io.github.shashanthk.logger.context;

public class RequestContext {

    private static final ThreadLocal<String> requestId = new ThreadLocal<>();
    private static final ThreadLocal<String> userId = new ThreadLocal<>();
    private static final ThreadLocal<String> ipAddress = new ThreadLocal<>();

    public static String getRequestId() {
        return requestId.get();
    }

    public static void setRequestId(String id) {
        requestId.set(id);
    }

    public static String getUserid() {
        return userId.get();
    }

    public static void setUserId(String id) {
        userId.set(id);
    }

    public static String getIpAddress() {
        return ipAddress.get();
    }

    public static void setIpAddress(String ip) {
        ipAddress.set(ip);
    }

    public static void clear() {
        requestId.remove();
        userId.remove();
        ipAddress.remove();
    }
}


