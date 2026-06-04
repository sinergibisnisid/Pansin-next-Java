package com.bjb.pansin.common.constants;

public final class AppConstants {

    private AppConstants() {}

    public static final String API_PREFIX = "/api/v1";

    public static final String HEADER_AUTH = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    public static final String CACHE_USER = "user";
    public static final String CACHE_PERMISSION = "permission";
    public static final String CACHE_ROLE = "role";

    public static final String REDIS_KEY_REFRESH = "auth:refresh:";
    public static final String REDIS_KEY_OTP = "auth:otp:";
    public static final String REDIS_KEY_LOGIN_FAIL = "auth:fail:";
    public static final String REDIS_KEY_LOGIN_LOCK = "auth:lock:";
    public static final String REDIS_KEY_BLACKLIST = "auth:bl:";
    public static final String REDIS_KEY_RATE = "rate:";

    public static final long DEFAULT_PAGE_SIZE = 20;
    public static final long MAX_PAGE_SIZE = 200;
}
