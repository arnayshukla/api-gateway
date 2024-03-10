package com.api.gateway.util;

public interface Constants {

  public static final String CONFIG_RESOURCE_LOCATION = "classpath:/{deployment_type}/**/*.json";
  public static final String PROFILE_PLACEHOLDER = "{deployment_type}";
  public static final String SEGMENT = "segment";
  public static final String DEPLOYMENT_TYPE = "DEPLOYMENT_TYPE";

  public interface Filter {
    public static final String REWRITE_PATH = "RewritePath";
    public static final String PRE_LOGGING = "PreLogging";
    public static final String POST_LOGGING = "PostLogging";
    public static final String REQUEST_BODY_MODIFICATION = "RequestBodyModification";
    public static final String RESPONSE_BODY_MODIFICATION = "ResponseBodyModification";
    public static final String SET_REQUEST_HEADER = "SetRequestHeader";
    public static final String SET_RESPONSE_HEADER = "SetResponseHeader";
    public static final String AUTHENTICATION = "Authentication";
    public static final String AUTHORIZATION = "Authorization";
    public static final String REQUEST_HEADER_MODIFICATION = "RequestHeaderModification";
    public static final String URL_MODIFICATION = "UrlModification";
    public static final String REQUEST_TYPE_MODIFICATION = "RequestTypeModification";
  }

  public interface Template {
    public static final String TEMPLATE_DIR = "templateDirectory";
    public static final String TEMPLATE_NAME = "templateName";
    public static final String MODIFICATION_TYPE = "modificationType";
  }

  public interface Authentication {
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String URL = "url";
    public static final String METHOD = "method";
    public static final String REQUEST_BODY = "request";
    public static final String REQUEST_HEADERS = "headers";
    public static final String TOKEN = "token";
    public static final String BASIC = "Basic ";
    public static final String BEARER = "Bearer ";
    public static final String AUTH_TOKEN = "AuthToken";
  }

  public interface Partner {
    public static final String X_PARTNER_CODE = "X-Partner-Code";
  }

  public interface Symbols {
    public static final String COMMA = ",";
    public static final String COLON = ":";
    public static final String UNDERSCORE = "_";
    public static final String QUESTION_MARK = "?";
    public static final String FORWARD_SLASH = "/";
  }

  public interface Authorization {
    String HEADER_NOT_PRESENT = "Authorization Header not Present";
    String INVALID_HEADER = "Invalid Authorization Header";
    String INVALID_MERCHANT_KEY = "Invalid Merchant Key";
    String MERCHANT_ID = "X-Merchant-Id";
  }
}
