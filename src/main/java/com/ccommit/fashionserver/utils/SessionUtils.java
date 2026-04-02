package com.ccommit.fashionserver.utils;

import javax.servlet.http.HttpSession;

public class SessionUtils {
    private static final String USER = "USER";
    private static final String SELLER = "SELLER";
    private static final String ADMIN = "ADMIN";

    private SessionUtils() {
    }

    public static void setUserLoginSession(HttpSession session, int id) {
        session.setAttribute(USER, id);
    }

    public static void setAdminLoginSession(HttpSession session, int id) {
        session.setAttribute(ADMIN, id);
    }

    public static void setSellerLoginSession(HttpSession session, int id) {
        session.setAttribute(SELLER, id);
    }

    public static Integer getUserLoginSession(HttpSession session) {
        return (Integer) session.getAttribute(USER);
    }

    public static Integer getAdminLoginSession(HttpSession session) {
        return (Integer) session.getAttribute(ADMIN);
    }

    public static Integer getSellerLoginSession(HttpSession session) {
        return (Integer) session.getAttribute(SELLER);
    }

    public static void clearSession(HttpSession session) {
        session.invalidate();
    }

}
