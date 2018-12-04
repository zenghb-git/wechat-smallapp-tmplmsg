package com.wuwenze.wechatsmallapptmplmsg.util;

import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.extra.servlet.multipart.MultipartFormData;
import cn.hutool.extra.servlet.multipart.UploadSetting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

@Slf4j
public class WebUtil extends ServletUtil {

    public static HttpServletRequest getRequest() {
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        if (null == request) {
            throw new IllegalArgumentException("#WebUtil#getRequest() is null.");
        }
        return request;
    }

    public static HttpServletResponse getResponse() {
        HttpServletResponse response = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
        if (null == response) {
            throw new IllegalArgumentException("#WebUtil#getResponse() is null.");
        }
        return response;
    }

    public static Map<String, String[]> getParams() {
        return getParams(getRequest());
    }

    public static Map<String, String> getParamMap() {
        return getParamMap(getRequest());
    }

    public static String getBody() {
        return getBody(getRequest());
    }

    public static byte[] getBodyBytes() {
        return getBodyBytes(getRequest());
    }

    public static <T> T fillBean(T bean, CopyOptions copyOptions) {
        return fillBean(getRequest(), bean, copyOptions);
    }

    public static <T> T fillBean(T bean, boolean isIgnoreError) {
        return fillBean(getRequest(), bean, CopyOptions.create().setIgnoreError(isIgnoreError));
    }

    public static <T> T toBean(Class<T> beanClass, boolean isIgnoreError) {
        return fillBean(getRequest(), ReflectUtil.newInstance(beanClass, new Object[0]), isIgnoreError);
    }

    public static String getClientIP(String... otherHeaderNames) {
        return getClientIP(getRequest());
    }

    public static MultipartFormData getMultipart() throws IORuntimeException {
        return getMultipart(getRequest());
    }

    public static MultipartFormData getMultipart(UploadSetting uploadSetting) throws IORuntimeException {
        return getMultipart(getRequest(), uploadSetting);
    }

    public static final String getHeaderIgnoreCase(String nameIgnoreCase) {
        return getHeaderIgnoreCase(getRequest(), nameIgnoreCase);
    }

    public static final String getHeader(String name, String charset) {
        return getHeader(getRequest(), name, charset);
    }

    public static boolean isIE() {
        return isIE(getRequest());
    }

    public static boolean isGetMethod() {
        return isGetMethod(getRequest());
    }

    public static boolean isPostMethod() {
        return isPostMethod(getRequest());
    }

    public static boolean isMultipart() {
        return isMultipart(getRequest());
    }

    public static final Cookie getCookie(String name) {
        return getCookie(getRequest(), name);
    }

    public static final Map<String, Cookie> readCookieMap() {
        return readCookieMap(getRequest());
    }

    public static final void addCookie(Cookie cookie) {
        addCookie(getResponse(), cookie);
    }

    public static final void addCookie(String name, String value) {
        addCookie(getResponse(), name, value);
    }

    public static final void addCookie(String name, String value, int maxAgeInSeconds, String path, String domain) {
        addCookie(getResponse(), name, value, maxAgeInSeconds, path, domain);
    }

    public static final void addCookie(String name, String value, int maxAgeInSeconds) {
        addCookie(getResponse(), name, value, maxAgeInSeconds, "/", (String)null);
    }

    public static PrintWriter getWriter() throws IORuntimeException {
        return getWriter(getResponse());
    }

    public static void write(String text, String contentType) {
        write(getResponse(), text, contentType);
    }

    public static void write(String text) {
        HttpServletResponse response = getResponse();
        try (Writer writer = response.getWriter()) {
            writer.write(text);
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void write(InputStream in, String contentType) {
        write(getResponse(), in, contentType);
    }

    public static void write(InputStream in) {
        write(getResponse(), in);
    }

    public static void write(InputStream in, int bufferSize) {
        write(getResponse(), in, bufferSize);
    }

    public static void setHeader(String name, Object value) {
        setHeader(name, value);
    }

    public static String getRequestURI() {
        return getRequestURI(getRequest());
    }

    public static String getRequestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
