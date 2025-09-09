package com.keetlo.banner_management.utils;

import com.keetlo.banner_management.model.PublicModel;
import com.keetlo.banner_management.model.SettingModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ActionLogService {
    private final JdbcTemplate jdbcTemplate;

    public ActionLogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(String userCode, String type, String topic, String message) {
        try {
            String getTrackingAndAnalyticsSettingSql = """
              SELECT system_tracking_and_analytic_settings.track_action_logs FROM system_tracking_and_analytic_settings
              JOIN system_settings ON
               system_tracking_and_analytic_settings.system_setting_id = system_settings.system_setting_id
              WHERE system_settings.user_code = ?
            """;
            Object[] getTrackingAndAnalyticsSettingParams = new Object[]{userCode};
            List<SettingModel> settings = jdbcTemplate.query(getTrackingAndAnalyticsSettingSql, getTrackingAndAnalyticsSettingParams, (rowResult, rowNum) -> {
                SettingModel setting = new SettingModel();
                setting.setTrackActionLogs(rowResult.getInt("track_action_logs"));
                return setting;
            });

            if (settings.isEmpty()) {
                return;
            }
            SettingModel setting = settings.get(0);
            if (setting.getTrackActionLogs() == 1) {

                String sql = "INSERT INTO action_logs " +
                        "(action_log_id, user_code, type, topic, message) " +
                        "VALUES (?, ?, ?, ?, ?)";
                jdbcTemplate.update(sql,
                        UUID.randomUUID().toString(),
                        userCode,
                        type,
                        topic,
                        message
                );
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
