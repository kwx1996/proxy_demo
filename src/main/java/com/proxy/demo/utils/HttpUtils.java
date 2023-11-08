package com.proxy.demo.utils;

import java.util.HashMap;

public class HttpUtils {

    public static HashMap<String,String> getHeader(String Header){
        HashMap<String, String> map = new HashMap<>();
        String host = null;
        String[] split = Header.split("\r\n");

        for (int i = 0;i < split.length;i++){
            if (i == 0){
                String[] temp = split[i].split(" ");
                if (temp.length == 3){
                    map.put("method",temp[0]);
                    map.put("path",temp[1]);
                    map.put("edition",temp[2]);
                }
            }
            String[] temp = split[i].split(": ");
            if (temp.length == 2){
                if (temp[0].toLowerCase().equals("host")){
                    host = temp[1];
                }
                map.put(temp[0], temp[1]);
            }
        }
        if (map.get("method").toUpperCase().equals("CONNECT")){
            host = map.get("path");
        }

        if (host == null){
            throw new RuntimeException("该字符串不是标准的http报文");
        }

        String path = map.get("path");

        path = path.replaceAll("http://" + host,"");

        map.put("path",path);

        map.put("host_temp",host);

        return map;
    }
}
