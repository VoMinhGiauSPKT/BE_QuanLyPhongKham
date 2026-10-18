package com.mycompany.quanlyphongkham.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

public abstract class BaseServlet extends HttpServlet {

    protected static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .serializeNulls()
            .create();

    protected <T> T readJsonBody(HttpServletRequest request, Class<T> clazz) throws IOException {
        request.setCharacterEncoding("UTF-8");
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = new BufferedReader(
                new java.io.InputStreamReader(request.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String body = sb.toString();
        if (body.trim().isEmpty()) {
            return null;
        }
        return gson.fromJson(body, clazz);
    }

    protected void sendJsonResponse(HttpServletResponse response, int status, ApiResponse<?> apiResponse) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        String json = gson.toJson(apiResponse);
        response.getWriter().write(json);
    }

    protected void handleSqlException(HttpServletResponse response, SQLException ex, String defaultMsg) throws IOException {
        // SQL Server error 229 is "The %ls permission was denied on the object '%ls', database '%ls', schema '%ls'."
        // SQL Server error 230 is "The %ls permission was denied on the column '%ls' of the object '%ls'..."
        if (ex.getErrorCode() == 229 || ex.getErrorCode() == 230 || 
            (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("permission was denied"))) {
            sendJsonResponse(response, HttpServletResponse.SC_FORBIDDEN,
                    ApiResponse.forbiddenDb("Lỗi phân quyền DBMS: " + ex.getMessage()));
        } else {
            String msg = (ex.getMessage() != null && !ex.getMessage().isEmpty()) ? ex.getMessage() : defaultMsg;
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error(msg, "DB_ERROR"));
        }
    }

    protected String getCurrentUserRole(HttpServletRequest request) {
        Object role = request.getAttribute("USER_ROLE");
        return role != null ? role.toString() : null;
    }

    protected String getCurrentUserId(HttpServletRequest request) {
        Object uid = request.getAttribute("USER_ID");
        return uid != null ? uid.toString() : null;
    }

    protected String getCurrentUsername(HttpServletRequest request) {
        Object username = request.getAttribute("USER_NAME");
        return username != null ? username.toString() : null;
    }

    protected int getIntParameter(HttpServletRequest request, String paramName, int defaultValue) {
        String val = request.getParameter(paramName);
        if (val == null || val.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(val.trim());
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
