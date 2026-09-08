package com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.res;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ResKit {
    private static final ResKit kit = new ResKit();
    private final Map<String, String> res = new HashMap<>();

    public ResKit() {
        try {
            for (Field field : ResConstant.class.getDeclaredFields()) {
                if (field.getType() == String.class) {
                    res.put(field.getName(), (String) field.get(null));
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static ResKit instance() {
        return kit;
    }

    public boolean hasResName(String resName) {
        return res.containsKey(resName);
    }

    public String getLocalString(String resName) {
        String value = res.get(resName);
        return value != null ? value : resName;
    }
}
