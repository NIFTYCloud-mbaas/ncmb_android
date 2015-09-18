package com.nifty.cloud.mb.core;

import com.google.gson.Gson;
import com.squareup.okhttp.mockwebserver.Dispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.apache.maven.artifact.ant.shaded.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.skyscreamer.jsonassert.JSONCompare.compareJSON;

/**
 * NCMBDispatcher class
 */
public class NCMBDispatcher {

    private static final String NUMBER_PATTERN = "[0-9]+";
    private static final String BOOL_PATTERN = "[true|false]";

    //URLごとにレスポンスを定義するdispatcherを作成
    public static Dispatcher dispatcher = new Dispatcher() {

        @Override
        public MockResponse dispatch(RecordedRequest request) throws InterruptedException {

            InputStream input = null;
            try {
                input = new FileInputStream(new File("src/test/assets/yaml/mbaas.yml"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Yaml yaml = new Yaml();
            Map<String, Object> map = null;
            Map<String, Object>requestMap = null;
            Map<String, Object>responseMap = null;
            String requestBody = null;
            for (Object data : yaml.loadAll(input)) {
                map = (Map<String, Object>)data;
                requestMap = (Map)map.get("request");
                responseMap = (Map)map.get("response");
                String[] pathAndQuery = request.getPath().split("\\?", 0);
                String path = pathAndQuery[0];
                String query = null;
                if (pathAndQuery.length > 1) {
                    query = pathAndQuery[1];
                }
                if (!requestMap.get("url").equals(path)) {
                    continue;
                    //return defaultErrorResponse();
                }
                if (!requestMap.get("method").equals(request.getMethod())){
                    continue;
                    //return defaultErrorResponse();
                }
                if (query != null) {
                    if (requestMap.containsKey("query")) {
                        Object mockQuery = requestMap.get("query");
                        String mockQueryStr = new Gson().toJson(mockQuery);
                        if (!checkRequestQuery(mockQueryStr, query)) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }

                if (requestMap.containsKey("body")) {
                    try {
                        if (requestBody == null) {
                            requestBody = request.getBody().readString(request.getBodySize(), Charset.forName("UTF-8"));
                        }
                        Object mockBody = requestMap.get("body");
                        String mockBodyStr = new Gson().toJson(mockBody);
                        if (checkRequestBody(mockBodyStr, requestBody)) {
                            //Responseをreturn
                            return new MockResponse().setResponseCode((int)responseMap.get("status"))
                                    .setHeader("Content-Type", "application/json")
                                    .setBody(readJsonResponse(responseMap.get("file").toString()));
                        } else {
                            continue;
                        }
                    } catch (IOException e) {
                        return defaultErrorResponse();
                    }
                }
                return new MockResponse().setResponseCode((int)responseMap.get("status"))
                        .setHeader("Content-Type", "application/json")
                        .setBody(readJsonResponse(responseMap.get("file").toString()));

            }
            return defaultErrorResponse();
        }
    };

    private static MockResponse defaultErrorResponse(){
        return new MockResponse().setResponseCode(404)
        .setHeader("Content-Type", "application/json")
        .setBody(readJsonResponse("valid_error_response.json"));
    }

    /**
     * Utilities **
     */

    private static Boolean checkRequestQuery(String mockRequestQueryStr, String realRequestQueryStr) {
        System.out.println("checkRequestQuery");
        try {
            JSONObject mockQuery = new JSONObject(mockRequestQueryStr);

            String decodedQueryStr = URLDecoder.decode(realRequestQueryStr, "UTF-8");

            HashMap<String, Object> realQueryMap = new HashMap<String, Object>();
            String[] queryArray = decodedQueryStr.split("&", 0);
            for (String query: queryArray) {
                String[] queryData = query.split("=", 0);
                String key = queryData[0];
                String value = queryData[1];
                if (value.matches(NUMBER_PATTERN)){
                    realQueryMap.put(key, Integer.parseInt(value));
                } else if (value.matches(BOOL_PATTERN)){
                    realQueryMap.put(key, Boolean.parseBoolean(value));
                } else {
                    realQueryMap.put(key, value);
                }

            }
            JSONObject realQuery = new JSONObject(realQueryMap);

            System.out.println("mockQuery:" + mockQuery.toString());
            System.out.println("realQuery:" + realQuery.toString());
            if (!compareJSON(mockQuery, realQuery, JSONCompareMode.LENIENT).passed()){
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static Boolean checkRequestBody(String mockRequestBodyStr, String realRequestBodyStr) {

        try {
            JSONObject mockBody = new JSONObject(mockRequestBodyStr);

            JSONObject realBody = new JSONObject(URLDecoder.decode(realRequestBodyStr, "UTF-8"));

            if (mockBody.length() != realBody.length()) {
                return false;
            }
            if (!compareJSON(mockBody, realBody, JSONCompareMode.LENIENT).passed()){
                return false;
            }
            /*
            Iterator<String> iter = mockBody.keys();
            while (iter.hasNext()){
                String key = iter.next();
                System.out.println("key: " + key);
                if (!mockBody.get(key).equals(realBody.get(key))){
                    System.out.println(mockBody.get(key) + " is not equals " + realBody.get(key));
                    result = false;
                }
            }
            */
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static String readJsonResponse(String file_name) {
        File file = new File("src/test/assets/json/" + file_name);
        String json = null;
        try {
            json = FileUtils.fileRead(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }
}