package com.keetlo.banner_management.controllers;

import com.keetlo.banner_management.middlewares.JWTAutherization;
import com.keetlo.banner_management.model.ActionLogModel;
import com.keetlo.banner_management.model.ResponseModel;
import com.keetlo.banner_management.routes.ActionLogRoutes;
import com.keetlo.banner_management.utils.ImageHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ActionLogController {
    private final JdbcTemplate jdbcTemplate;
    private final JWTAutherization jWTAutherization;

    public ActionLogController(JdbcTemplate jdbcTemplate, JWTAutherization jWTAutherization, ImageHandler imageHandler) {
        this.jdbcTemplate = jdbcTemplate;
        this.jWTAutherization = jWTAutherization;
    }

    @GetMapping(ActionLogRoutes.ACTION_LOG_PREFIX)
    public ResponseEntity<ResponseModel> ActionLog(HttpServletRequest request, String search, String type, int page, int limit) {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        String sql = "SELECT action_log_id, type, topic, message, created_at " +
                "FROM action_logs " +
                "WHERE user_code = ? " +
                (search != null && !search.isEmpty() ?
                        " AND (topic LIKE ? OR message LIKE ?) " : "") +
                (type != null && !type.isEmpty() ?
                        " AND (type = ?) " : "") +
                "ORDER BY created_at DESC " +
                "LIMIT ? OFFSET ?";
        Object[] params;
        if(search != null && !search.isEmpty()) {
            System.out.println(1);
            params  = new Object[]{
                    userCode,
                    "%" + search + "%",
                    "%" + search + "%",
                    limit,
                    (page - 1) * limit
            };
        } else if(type != null && !type.isEmpty()) {
            System.out.println(2);
            params  = new Object[]{
                    userCode,
                    type,
                    limit,
                    (page - 1) * limit
            };
        } else if ((search != null && !search.isEmpty()) && (type != null && !type.isEmpty())) {
            System.out.println(3);
            params  = new Object[]{
                    userCode,
                    "%" + search + "%",
                    "%" + search + "%",
                    type,
                    limit,
                    (page - 1) * limit
            };
        }
        else {
            System.out.println(4);
            params = new Object[]{
                    userCode,
                    limit,
                    (page - 1) * limit
            };
        }

        String countSql = "SELECT COUNT(*) FROM action_logs " +
                "WHERE user_code = ? " +
                (search != null && !search.isEmpty() ?
                        " AND (topic LIKE ? OR message LIKE ?) " : "") +
                (type != null && !type.isEmpty() ?
                        " AND (type = ?) " : "");
        Object[] countParams;
        if(search != null && !search.isEmpty()) {
            countParams  = new Object[]{
                    userCode,
                    "%" + search + "%",
                    "%" + search + "%",
            };
        } else if(type != null && !type.isEmpty()) {
            countParams  = new Object[]{
                    userCode,
                    type,
            };
        } else if ((search != null && !search.isEmpty()) && (type != null && !type.isEmpty())) {
            countParams  = new Object[]{
                    userCode,
                    "%" + search + "%",
                    "%" + search + "%",
                    type,
            };
        } else {
            countParams = new Object[]{
                    userCode,
            };
        }

        try {
            List<ActionLogModel> actionLogs = jdbcTemplate.query(sql, params, (resultRow, rowNum) -> {
                ActionLogModel actionLog = new ActionLogModel();
                actionLog.setActionLogId(resultRow.getString("action_log_id"));
                actionLog.setType(resultRow.getString("type"));
                actionLog.setTopic(resultRow.getString("topic"));
                actionLog.setMessage(resultRow.getString("message"));
                actionLog.setCreatedAt(resultRow.getString("created_at"));
                return  actionLog;
            });

            int totalActionLogs = jdbcTemplate.queryForObject(countSql, countParams, Integer.class);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("actionLogs", actionLogs);
            responseData.put("totalActionLogs", totalActionLogs);

            response.setStatus("success");
            response.setData(responseData);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            response.setMessage("Internal Server Error: ");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
