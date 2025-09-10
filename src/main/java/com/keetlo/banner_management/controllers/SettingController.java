package com.keetlo.banner_management.controllers;

import com.keetlo.banner_management.middlewares.JWTAutherization;
import com.keetlo.banner_management.model.BannerModel;
import com.keetlo.banner_management.model.ResponseModel;
import com.keetlo.banner_management.model.SettingModel;
import com.keetlo.banner_management.routes.SettingRoutes;
import com.keetlo.banner_management.utils.ActionLogService;
import com.keetlo.banner_management.utils.ImageHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SettingController {
    private final JdbcTemplate jdbcTemplate;
    private final JWTAutherization jWTAutherization;
    private final ImageHandler imageHandler;
    private final ActionLogService actionLogService;

    public SettingController(JdbcTemplate jdbcTemplate, JWTAutherization jWTAutherization, ImageHandler imageHandler, ActionLogService actionLogService) {
        this.jdbcTemplate = jdbcTemplate;
        this.jWTAutherization = jWTAutherization;
        this.imageHandler = imageHandler;
        this.actionLogService = actionLogService;
    }

    @GetMapping(SettingRoutes.SETTING_PREFIX)
    public ResponseEntity<ResponseModel> setting(HttpServletRequest request) {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);

        // Authentication check
        if (userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String sql = "SELECT system_settings.system_setting_id, system_general_settings.product_name, system_general_settings.timezone, system_general_settings.date_format, " +
                "system_brand_settings.brand_image_url, " +
                "system_tracking_and_analytic_settings.track_views, system_tracking_and_analytic_settings.track_clicks, system_tracking_and_analytic_settings.track_action_logs, " +
                "system_api_settings.public_key, system_api_settings.secret_key " +
                "FROM system_settings " +
                "LEFT JOIN system_general_settings ON system_settings.system_setting_id = system_general_settings.system_setting_id " +
                "LEFT JOIN system_brand_settings ON system_settings.system_setting_id = system_brand_settings.system_setting_id " +
                "LEFT JOIN system_tracking_and_analytic_settings ON system_settings.system_setting_id = system_tracking_and_analytic_settings.system_setting_id " +
                "LEFT JOIN system_api_settings ON system_settings.system_setting_id = system_api_settings.system_setting_id " +
                "WHERE system_settings.user_code = ?";

        Object[] params = new Object[]{userCode};

        try {
            // Query the database to check for existing settings
            List<SettingModel> settingsCheck = jdbcTemplate.query(sql, params, (resultRow, rowNum) -> {
                SettingModel setting = new SettingModel();
                setting.setSystemSettingId(resultRow.getString("system_setting_id"));
                setting.setProductName(resultRow.getString("product_name"));
                setting.setTimezone(resultRow.getString("timezone"));
                setting.setDateFormat(resultRow.getString("date_format"));
                setting.setBrandImageUrl(resultRow.getString("brand_image_url"));
                setting.setTrackViews(resultRow.getInt("track_views"));
                setting.setTrackClicks(resultRow.getInt("track_clicks"));
                setting.setTrackActionLogs(resultRow.getInt("track_action_logs"));
                setting.setPublicKey(resultRow.getString("public_key"));
                setting.setSecretKey(resultRow.getString("secret_key"));
                return setting;
            });

            // If no settings exist, insert new settings for the user
            if (settingsCheck.isEmpty()) {
                SettingModel setting = new SettingModel();
                String newSettingId = setting.createSettingCode();

                // Begin transaction
                jdbcTemplate.update("INSERT INTO system_settings (system_setting_id, user_code) VALUES (?, ?)", newSettingId, userCode);
                jdbcTemplate.update("INSERT INTO system_general_settings (system_general_setting_id, system_setting_id, product_name, timezone, date_format) VALUES (?, ?, ?, ?, ?)", setting.createSettingCode(), newSettingId, "Banner Management", "Asia/Bangkok", "DD/MM/YYYY");
                jdbcTemplate.update("INSERT INTO system_brand_settings (system_brand_setting_id, system_setting_id, brand_image_url) VALUES (?, ?, ?)", setting.createSettingCode(), newSettingId, null);
                jdbcTemplate.update("INSERT INTO system_tracking_and_analytic_settings (system_tracking_and_analytic_setting_id, system_setting_id, track_views, track_clicks, track_action_logs) VALUES (?, ?, ?, ?, ?)", setting.createSettingCode(), newSettingId, 1, 1, 1);
                jdbcTemplate.update("INSERT INTO system_api_settings (system_api_setting_id, system_setting_id, public_key, secret_key) VALUES (?, ?, ?, ?)", setting.createSettingCode(), newSettingId, setting.createPublicKey(), null);
            }

            // Query the settings again to return them in the response
            List<SettingModel> settings = jdbcTemplate.query(sql, params, (resultRow, rowNum) -> {
                SettingModel setting = new SettingModel();
                setting.setSystemSettingId(resultRow.getString("system_setting_id"));
                setting.setProductName(resultRow.getString("product_name"));
                setting.setTimezone(resultRow.getString("timezone"));
                setting.setDateFormat(resultRow.getString("date_format"));
                setting.setBrandImageUrl(resultRow.getString("brand_image_url"));
                setting.setTrackViews(resultRow.getInt("track_views"));
                setting.setTrackClicks(resultRow.getInt("track_clicks"));
                setting.setTrackActionLogs(resultRow.getInt("track_action_logs"));
                setting.setPublicKey(resultRow.getString("public_key"));
                setting.setSecretKey(resultRow.getString("secret_key"));
                return setting;
            });

            // Return response if settings found
            if (settings.isEmpty()) {
                response.setMessage("Settings not found.");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            SettingModel setting = settings.get(0);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("settings", setting);

            response.setStatus("success");
            response.setData(responseData);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            response.setMessage("Internal Server Error: " + e.getMessage());
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(SettingRoutes.SETTING_UPDATE)
    public ResponseEntity<ResponseModel> PutUpdateSetting(HttpServletRequest request, @RequestBody SettingModel settingRequest) throws IOException {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        // Check if setting belongs to the user
        List<SettingModel> existingSettings = jdbcTemplate.query("SELECT system_settings.system_setting_id, " +
                        "system_brand_settings.brand_image_url " +
                        "FROM system_settings " +
                        "JOIN system_brand_settings ON system_settings.system_setting_id = system_brand_settings.system_setting_id " +
                        "WHERE system_settings.user_code = ?",
                new Object[]{
                        userCode
                }, (resultRow, rowNum) -> {
                    SettingModel s = new SettingModel();
                    s.setSystemSettingId(resultRow.getString("system_setting_id"));
                    s.setBrandImageUrl(resultRow.getString("brand_image_url"));
                    return s;
                });
        if (existingSettings.isEmpty()) {
            response.setMessage("Permission Denied For The Setting");
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(settingRequest.getSystemSettingId() == null ||
          settingRequest.getProductName() == null ||
          settingRequest.getTimezone() == null ||
           settingRequest.getDateFormat() == null ||
           settingRequest.getTrackViews() == null ||
           settingRequest.getTrackClicks() == null ||
           settingRequest.getTrackActionLogs() == null
        ) {
            response.setMessage("All fields are required.");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String updateSystemGeneralSettingSql = "UPDATE system_general_settings SET product_name = ?, timezone = ?, date_format = ? " +
                "WHERE system_setting_id = ?";
        String updateSystemBrandSettingSql = "UPDATE system_brand_settings SET brand_image_url = ? " +
                "WHERE system_setting_id = ?";
        String updateSystemTrackingAndAnalyticSettingSql = "UPDATE system_tracking_and_analytic_settings SET track_views = ?, track_clicks = ?, track_action_logs = ? " +
                "WHERE system_setting_id = ?";
        String updateSystemSettingSql = "UPDATE system_settings SET updated_at = NOW() " +
                "WHERE system_setting_id = ?";
        try{
            SettingModel existingSetting = existingSettings.get(0);
            String base64Image = settingRequest.getBrandImageUrl();

            if (base64Image != null && !base64Image.isEmpty() && imageHandler.isBase64(base64Image)) {
                String uploadDir = "/images/brands/";
                String imageName = "brand_image_" + System.currentTimeMillis() + ".webp";
                String filePath = uploadDir + imageName;
                try {
                    imageHandler.saveBase64Image(base64Image, filePath);
                    if(existingSetting.getBrandImageUrl() != null && !existingSetting.getBrandImageUrl().trim().isEmpty()) {
                        String oldFilePath = existingSetting.getBrandImageUrl();
                        imageHandler.deleteImage(oldFilePath);
                    }
                } catch (IOException e) {
                    response.setMessage("Failed to save image.");
                    response.setStatus("error");
                    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                settingRequest.setBrandImageUrl("/images/brands/" + imageName);
            } else if (base64Image == null && existingSetting.getBrandImageUrl() != null && !existingSetting.getBrandImageUrl().trim().isEmpty()) {
                if(existingSetting.getBrandImageUrl() != null && !existingSetting.getBrandImageUrl().trim().isEmpty()) {
                    String oldFilePath = existingSetting.getBrandImageUrl();
                    imageHandler.deleteImage(oldFilePath);
                }
            }

            int rowsAffectedGeneralSetting = jdbcTemplate.update(updateSystemGeneralSettingSql,
                    settingRequest.getProductName(),
                    settingRequest.getTimezone(),
                    settingRequest.getDateFormat(),
                    settingRequest.getSystemSettingId()
            );
            if(rowsAffectedGeneralSetting==0){
                response.setMessage("Something is incorrect on general setting");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            int rowsAffectedBrandSetting = jdbcTemplate.update(updateSystemBrandSettingSql,
                    settingRequest.getBrandImageUrl(),
                    settingRequest.getSystemSettingId()
            );
            if(rowsAffectedBrandSetting==0){
                response.setMessage("Something is incorrect on brand setting");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            int rowsAffectedTrackingAndAnalyticSetting = jdbcTemplate.update(updateSystemTrackingAndAnalyticSettingSql,
                    settingRequest.getTrackViews(),
                    settingRequest.getTrackClicks(),
                    settingRequest.getTrackActionLogs(),
                    settingRequest.getSystemSettingId()
            );
            if(rowsAffectedTrackingAndAnalyticSetting==0){
                response.setMessage("Something is incorrect on brand setting");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            int rowsAffectedSystemSetting = jdbcTemplate.update(updateSystemSettingSql,
                    settingRequest.getSystemSettingId()
            );
            if(rowsAffectedSystemSetting==0){
                response.setMessage("Something is incorrect on brand setting");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            actionLogService.log(userCode, "setting", "Settings Updated", "System settings have been updated.");
            response.setMessage("User " + userCode + " Settings " + " updated successfully!");
            response.setStatus("success");
            return  new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Internal Server Error: "+ e.getMessage());
            response.setMessage("Internal Server Error: ");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(SettingRoutes.SETTING_GENERATE_SECRET_KEY)
    public ResponseEntity<ResponseModel> GenerateSecretKey(HttpServletRequest request) {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        // Check if setting belongs to the user
        List<SettingModel> existingSettings = jdbcTemplate.query("SELECT system_settings.system_setting_id " +
                        "FROM system_settings " +
                        "WHERE system_settings.user_code = ?",
                new Object[]{
                        userCode
                }, (resultRow, rowNum) -> {
                    SettingModel s = new SettingModel();
                    s.setSystemSettingId(resultRow.getString("system_setting_id"));
                    return s;
                });
        if (existingSettings.isEmpty()) {
            response.setMessage("Permission Denied For The Setting");
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        SettingModel setting = new SettingModel();
        String generatedSecretKeySetting = setting.createSecretKey();
        String updateSystemApiSettingSql = "UPDATE system_api_settings " +
                "JOIN system_settings " +
                "ON system_api_settings.system_setting_id = system_settings.system_setting_id " +
                "SET system_api_settings.secret_key = ? " +
                "WHERE system_settings.user_code = ?; ";
        try{
            int rowsAffectedApiSetting = jdbcTemplate.update(updateSystemApiSettingSql,
                    generatedSecretKeySetting,
                    userCode
            );
            if(rowsAffectedApiSetting==0){
                response.setMessage("Something is incorrect on api setting");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            actionLogService.log(userCode, "setting", "Settings Secret Key Generated", "System Secret Key have been generated.");
            response.setMessage("Secret key got created successfully!");
            response.setStatus("success");
            return  new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Internal Server Error: "+ e.getMessage());
            response.setMessage("Internal Server Error: ");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(SettingRoutes.SETTING_DEFAULT)
    public ResponseEntity<ResponseModel> PutUpdateSettingDefault(HttpServletRequest request, @RequestBody SettingModel settingRequest) throws IOException {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        // Check if setting belongs to the user
        List<SettingModel> existingSettings = jdbcTemplate.query("SELECT system_settings.system_setting_id, " +
                        "system_brand_settings.brand_image_url " +
                        "FROM system_settings " +
                        "JOIN system_brand_settings ON system_settings.system_setting_id = system_brand_settings.system_setting_id " +
                        "WHERE system_settings.user_code = ?",
                new Object[]{
                        userCode
                }, (resultRow, rowNum) -> {
                    SettingModel s = new SettingModel();
                    s.setSystemSettingId(resultRow.getString("system_setting_id"));
                    s.setBrandImageUrl(resultRow.getString("brand_image_url"));
                    return s;
                });
        if (existingSettings.isEmpty()) {
            response.setMessage("Permission Denied For The Setting");
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(!settingRequest.getConfirmText().equals("DEFAULT")
        ) {
            response.setMessage("The confirm text is invalid.");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String updateSystemGeneralSettingSql = "UPDATE system_general_settings SET product_name = ?, timezone = ?, date_format = ? " +
                "WHERE system_setting_id = ?";
        String updateSystemBrandSettingSql = "UPDATE system_brand_settings SET brand_image_url = ? " +
                "WHERE system_setting_id = ?";
        String updateSystemTrackingAndAnalyticSettingSql = "UPDATE system_tracking_and_analytic_settings SET track_views = ?, track_clicks = ?, track_action_logs = ? " +
                "WHERE system_setting_id = ?";
        String updateSystemSettingSql = "UPDATE system_settings SET updated_at = NOW() " +
                "WHERE system_setting_id = ?";
        try{
            SettingModel existingSetting = existingSettings.get(0);
            System.out.println(existingSetting.getBrandImageUrl());
            if (existingSetting.getBrandImageUrl() != null && !existingSetting.getBrandImageUrl().trim().isEmpty()) {
                    String oldFilePath = existingSetting.getBrandImageUrl();
                    imageHandler.deleteImage(oldFilePath);
            }

            int rowsAffectedGeneralSetting = jdbcTemplate.update(updateSystemGeneralSettingSql,
                    "Banner Manangement",
                    "Asia/Bangkok",
                    "DD/MM/YYYY",
                    existingSetting.getSystemSettingId()
            );
            if(rowsAffectedGeneralSetting==0){
                response.setMessage("Something is incorrect on general setting");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            int rowsAffectedBrandSetting = jdbcTemplate.update(updateSystemBrandSettingSql,
                    null,
                    existingSetting.getSystemSettingId()
            );
            if(rowsAffectedBrandSetting==0){
                response.setMessage("Something is incorrect on brand setting");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            int rowsAffectedTrackingAndAnalyticSetting = jdbcTemplate.update(updateSystemTrackingAndAnalyticSettingSql,
                    1,
                    1,
                    1,
                    existingSetting.getSystemSettingId()
            );
            if(rowsAffectedTrackingAndAnalyticSetting==0){
                response.setMessage("Something is incorrect on brand setting");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            int rowsAffectedSystemSetting = jdbcTemplate.update(updateSystemSettingSql,
                existingSetting.getSystemSettingId()
            );
            if(rowsAffectedSystemSetting==0){
                response.setMessage("Something is incorrect on brand setting");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            actionLogService.log(userCode, "setting", "Settings Default Updated", "System settings have been updated to default.");
            response.setMessage("User " + userCode + " Settings " + " updated to default successfully!");
            response.setStatus("success");
            return  new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Internal Server Error: "+ e.getMessage());
            response.setMessage("Internal Server Error: ");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
