package lol.moruto.ashley.util;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {
    private static final OkHttpClient CLIENT = new OkHttpClient();

    public static HttpResponse send(String path, String method, String jsonBody) throws IOException {

        Request.Builder builder = new Request.Builder().url(path);

        if ("GET".equals(method)) {
            builder.get();
        } else {
            RequestBody body = RequestBody.create(jsonBody == null ? "" : jsonBody, MediaType.parse("application/json"));
            builder.method(method, body);
        }

        try (Response response = CLIENT.newCall(builder.build()).execute()) {
            return new HttpResponse(response.code(), response.message(), response.body() != null ? response.body().string() : "");
        }
    }

    public static String prettyJson(String text) {
        try {
            if (text.trim().startsWith("{")) return new org.json.JSONObject(text).toString(4);
            if (text.trim().startsWith("[")) return new org.json.JSONArray(text).toString(4);
        } catch (Exception ignored) {}

        return text;
    }

    public static String getResponseMeaning(int code) {
        switch (code) {
            case 100: return "Continue";
            case 101: return "Switching Protocols";
            case 102: return "Processing";
            case 200: return "OK";
            case 201: return "Created";
            case 202: return "Accepted";
            case 203: return "Non-Authoritative Information";
            case 204: return "No Content";
            case 205: return "Reset Content";
            case 206: return "Partial Content";
            case 300: return "Multiple Choices";
            case 301: return "Moved Permanently";
            case 302: return "Found";
            case 303: return "See Other";
            case 304: return "Not Modified";
            case 307: return "Temporary Redirect";
            case 308: return "Permanent Redirect";
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 405: return "Method Not Allowed";
            case 406: return "Not Acceptable";
            case 408: return "Request Timeout";
            case 409: return "Conflict";
            case 410: return "Gone";
            case 413: return "Payload Too Large";
            case 414: return "URI Too Long";
            case 415: return "Unsupported Media Type";
            case 418: return "I'm a teapot";
            case 429: return "Too Many Requests";
            case 500: return "Internal Server Error";
            case 501: return "Not Implemented";
            case 502: return "Bad Gateway";
            case 503: return "Service Unavailable";
            case 504: return "Gateway Timeout";
            case 505: return "HTTP Version Not Supported";
            default:
                return "Unknown Status";
        }
    }

    public static class HttpResponse {

        private final int code;
        private final String message;
        private final String body;

        public HttpResponse(int code, String message, String body) {
            this.code = code;
            this.message = message;
            this.body = body;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public String getBody() {
            return body;
        }
    }
}