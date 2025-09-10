package com.keetlo.banner_management.controllers;

import com.keetlo.banner_management.middlewares.JWTAutherization;
import com.keetlo.banner_management.model.ResponseModel;
import com.keetlo.banner_management.model.SettingModel;
import com.keetlo.banner_management.model.UserModel;
import com.keetlo.banner_management.routes.UserRoutes;
import com.keetlo.banner_management.utils.ActionLogService;
import com.keetlo.banner_management.utils.Authentication;
import com.keetlo.banner_management.utils.ImageHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
public class UserController {
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private  final Authentication authentication;
    private  final JWTAutherization jwtAutherization;
    private  final ImageHandler imageHandler;
    private final ActionLogService actionLogService;

    public UserController(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder, Authentication authentication, JWTAutherization jwtAutherization, ImageHandler imageHandler, ActionLogService actionLogService) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.authentication = authentication;
        this.jwtAutherization = jwtAutherization;
        this.imageHandler = imageHandler;
        this.actionLogService = actionLogService;
    }

    @GetMapping(UserRoutes.USER_PREFIX)
    public ResponseEntity<ResponseModel> User(HttpServletRequest request) {
        ResponseModel response= new ResponseModel();
        String userCode = jwtAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        String sql = "SELECT users.username, users.email, users.firstname, users.lastname, users.avatar_url, " +
                "system_general_settings.timezone, system_general_settings.date_format, system_general_settings.product_name, system_brand_settings.brand_image_url " +
                "FROM users " +
                "JOIN system_settings ON users.user_code = system_settings.user_code " +
                "JOIN system_general_settings ON system_settings.system_setting_id = system_general_settings.system_setting_id " +
                "JOIN system_brand_settings ON system_settings.system_setting_id = system_brand_settings.system_setting_id " +
                "WHERE users.user_code = ?";
        try {
            List<UserModel> users = jdbcTemplate.query(sql, new Object[]{userCode},
                    (resultRow, rowNum) -> {
                        UserModel u = new UserModel();
                        u.setUsername(resultRow.getString("username"));
                        u.setEmail(resultRow.getString("email"));
                        u.setFirstname(resultRow.getString("firstname"));
                        u.setLastname(resultRow.getString("lastname"));
                        u.setAvatarUrl(resultRow.getString("avatar_url"));
                        u.setTimezone(resultRow.getString("timezone"));
                        u.setDateFormat(resultRow.getString("date_format"));
                        u.setProductName(resultRow.getString("product_name"));
                        u.setBrandImageUrl(resultRow.getString("brand_image_url"));
                        return u;
                    }
            );
            if(users.isEmpty()){
                response.setMessage("User is not found.");
                response.setStatus("error");
                return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            UserModel user = users.get(0);

            response.setMessage("Getting user data successfully.");
            response.setStatus("success");
            response.setData(user);
            return  new ResponseEntity<>(response, HttpStatus.OK);

        } catch (EmptyResultDataAccessException e) {
            response.setMessage("User is not found.");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            response.setMessage("An error occurred during getting user data.");
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(UserRoutes.USER_ME)
    public ResponseEntity<ResponseModel> UserMe(HttpServletRequest request) {
        ResponseModel response= new ResponseModel();
        String userCode = jwtAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        String sql = "SELECT user_code, username, email, firstname, lastname, avatar_url, " +
                "created_at, updated_at " +
                "FROM users WHERE user_code = ?";
        try {
            List<UserModel> users = jdbcTemplate.query(sql, new Object[]{userCode},
                    (resultRow, rowNum) -> {
                        UserModel u = new UserModel();
                        u.setUserCode(resultRow.getString("user_code"));
                        u.setUsername(resultRow.getString("username"));
                        u.setEmail(resultRow.getString("email"));
                        u.setFirstname(resultRow.getString("firstname"));
                        u.setLastname(resultRow.getString("lastname"));
                        u.setAvatarUrl(resultRow.getString("avatar_url"));
                        u.setCreatedAt(resultRow.getString("created_at"));
                        u.setUpdatedAt(resultRow.getString("updated_at"));
                        return u;
                    }
            );
            if(users.isEmpty()){
                response.setMessage("User is not found.");
                response.setStatus("error");
                return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            UserModel user = users.get(0);

            response.setMessage("Getting user data successfully.");
            response.setStatus("success");
            response.setData(user);
            return  new ResponseEntity<>(response, HttpStatus.OK);

        } catch (EmptyResultDataAccessException e) {
            response.setMessage("User is not found.");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            response.setMessage("An error occurred during getting user data.");
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(UserRoutes.USER_ME)
    public ResponseEntity<ResponseModel> UpdateUserMe(HttpServletRequest request, @RequestBody UserModel userRequest) {
        ResponseModel response= new ResponseModel();
        String userCode = jwtAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        String existingUserSql = "SELECT user_code, username, email, firstname, lastname, avatar_url, " +
                "created_at, updated_at " +
                "FROM users WHERE user_code = ?";
        String updateUserSql = "UPDATE users SET avatar_url = ?, firstname = ?, lastname = ?, updated_at = NOW() WHERE user_code = ?";
        try {
            List<UserModel> existingUsers = jdbcTemplate.query(existingUserSql, new Object[]{userCode},
                    (resultRow, rowNum) -> {
                        UserModel u = new UserModel();
                        u.setUserCode(resultRow.getString("user_code"));
                        u.setUsername(resultRow.getString("username"));
                        u.setEmail(resultRow.getString("email"));
                        u.setFirstname(resultRow.getString("firstname"));
                        u.setLastname(resultRow.getString("lastname"));
                        u.setAvatarUrl(resultRow.getString("avatar_url"));
                        u.setCreatedAt(resultRow.getString("created_at"));
                        u.setUpdatedAt(resultRow.getString("updated_at"));
                        return u;
                    }
            );
            if(existingUsers.isEmpty()){
                response.setMessage("User is not found.");
                response.setStatus("error");
                return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            UserModel existinguser = existingUsers.get(0);

            String base64Image = userRequest.getAvatarUrl();
            if (base64Image != null && !base64Image.isEmpty() && imageHandler.isBase64(base64Image)) {
                String uploadDir = "/images/users/";
                String imageName = "user_image_" + System.currentTimeMillis() + ".webp";
                String filePath = uploadDir + imageName;
                try {
                    imageHandler.saveBase64Image(base64Image, filePath);
                    if(existinguser.getAvatarUrl() != null && !existinguser.getAvatarUrl().trim().isEmpty()) {
                        String oldFilePath = existinguser.getAvatarUrl();
                        imageHandler.deleteImage(oldFilePath);
                    }
                } catch (IOException e) {
                    response.setMessage("Failed to save image.");
                    response.setStatus("error");
                    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                userRequest.setAvatarUrl("/images/users/" + imageName);
            }

            int rowsAffected = jdbcTemplate.update(updateUserSql,
                    userRequest.getAvatarUrl(),
                    userRequest.getFirstname(),
                    userRequest.getLastname(),
                    userCode
            );
            if(rowsAffected==0){
                response.setMessage("Something is incorrect");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            actionLogService.log(existinguser.getUserCode(), "user", "Profile Updated", "User profile updated successfully.");

            response.setMessage("User " + existinguser.getUserCode() + " updated successfully!");
            response.setStatus("success");
            return  new ResponseEntity<>(response, HttpStatus.OK);

        } catch (EmptyResultDataAccessException e) {
            response.setMessage("User is not found.");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            response.setMessage("An error occurred during getting user data.");
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(UserRoutes.USER_CHANGE_PASSWORD)
    public ResponseEntity<ResponseModel> ChangeUserPassword(HttpServletRequest request, @RequestBody UserModel userRequest) {
        ResponseModel response= new ResponseModel();
        String userCode = jwtAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        String existingUserSql = "SELECT password " +
                "FROM users WHERE user_code = ?";
        String updateUserPasswordSql = "UPDATE users SET password = ?, updated_at = NOW() WHERE user_code = ?";
        try {
            if(userRequest.getPassword() == null || userRequest.getPassword().trim().isEmpty() ||
            userRequest.getNewPassword()  == null || userRequest.getNewPassword().trim().isEmpty() ||
            userRequest.getConfirmPassword() == null || userRequest.getConfirmPassword().trim().isEmpty()
            ) {
                response.setMessage("All fields are required.");
                response.setStatus("error");
                return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            List<UserModel> existingUsers = jdbcTemplate.query(existingUserSql, new Object[]{userCode},
                    (resultRow, rowNum) -> {
                        UserModel u = new UserModel();
                        u.setPassword(resultRow.getString("password"));
                        return u;
                    }
            );
            if(existingUsers.isEmpty()){
                response.setMessage("User is not found.");
                response.setStatus("error");
                return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            UserModel existinguser = existingUsers.get(0);

            if(!passwordEncoder.matches(userRequest.getPassword(), existinguser.getPassword())){
                response.setMessage("Current Password is incorrect");
                response.setStatus("error");
                return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            if(passwordEncoder.matches(userRequest.getNewPassword(), existinguser.getPassword())){
                response.setMessage("New Password is the same as the old password");
                response.setStatus("error");
                return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            String hashedPassword = passwordEncoder.encode(userRequest.getNewPassword());
            userRequest.setNewPassword(hashedPassword);

            int rowsAffected = jdbcTemplate.update(updateUserPasswordSql,
                    userRequest.getNewPassword(),
                    userCode
            );
            if(rowsAffected==0){
                response.setMessage("Something is incorrect");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            actionLogService.log(userCode, "auth", "Password Changed", "User changed their password.");

            response.setMessage("User " + existinguser.getUserCode() + " updated successfully!");
            response.setStatus("success");
            return  new ResponseEntity<>(response, HttpStatus.OK);

        } catch (EmptyResultDataAccessException e) {
            response.setMessage("User is not found.");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            response.setMessage("An error occurred during getting user data.");
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(UserRoutes.USER_REGISTER)
    public ResponseEntity<ResponseModel> UserRegister(@RequestBody UserModel userRegistration){
        ResponseModel response = new ResponseModel();

        if(userRegistration.getEmail() == null
          || userRegistration.getUsername() == null
          || userRegistration.getPassword() == null
          || userRegistration.getConfirmPassword() == null
          || userRegistration.getFirstname() == null
          || userRegistration.getLastname() == null
        ){
            response.setMessage("All fields are required.");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String checkUsernameSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        int usernameCount = jdbcTemplate.queryForObject(checkUsernameSql, Integer.class, userRegistration.getUsername());

        if (usernameCount > 0) {
            response.setMessage("Username already taken. Please choose another one.");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(!Objects.equals(userRegistration.getPassword(), userRegistration.getConfirmPassword())){
            response.setMessage("Passwords do not match.");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String hashedPassword = passwordEncoder.encode(userRegistration.getPassword());
        userRegistration.setPassword(hashedPassword);
        String newUserCode = userRegistration.createUserCode();

        String sql = "INSERT INTO users (user_code,username, email, password, firstname, lastname) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            int rowsAffected = jdbcTemplate.update(sql,
                    newUserCode,
                    userRegistration.getUsername(),
                    userRegistration.getEmail(),
                    userRegistration.getPassword(),
                    userRegistration.getFirstname(),
                    userRegistration.getLastname()
            );
            if (rowsAffected == 0) {
                response.setMessage("Username or password is incorrect");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            SettingModel setting = new SettingModel();
            String newSettingId = setting.createSettingCode();
            String settingSql = "INSERT INTO system_settings (system_setting_id, user_code) VALUES (?, ?)";
            String generalSettingSql = "INSERT INTO system_general_settings (system_general_setting_id, system_setting_id, product_name, timezone, date_format) VALUES (?, ?, ?, ?, ?)";
            String brandSettingSql = "INSERT INTO system_brand_settings (system_brand_setting_id, system_setting_id, brand_image_url) VALUES (?, ?, ?)";
            String trackingAndAnalyticSettingSql = "INSERT INTO system_tracking_and_analytic_settings (system_tracking_and_analytic_setting_id, system_setting_id, track_views, track_clicks, track_action_logs) VALUES (?, ?, ?, ?, ?)";
            String apiSettingSql = "INSERT INTO system_api_settings (system_api_setting_id, system_setting_id, public_key, secret_key) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(settingSql,
                    newSettingId,
                    newUserCode
            );
            jdbcTemplate.update(generalSettingSql,
                    setting.createSettingCode(),
                    newSettingId,
                    "Banner Manangement",
                    "Asia/Bangkok",
                    "DD/MM/YYYY"
            );
            jdbcTemplate.update(brandSettingSql,
                    setting.createSettingCode(),
                    newSettingId,
                    null
            );
            jdbcTemplate.update(trackingAndAnalyticSettingSql,
                    setting.createSettingCode(),
                    newSettingId,
                    1,
                    1,
                    1
            );
            jdbcTemplate.update(apiSettingSql,
                    setting.createSettingCode(),
                    newSettingId,
                    setting.createPublicKey(),
                    null
            );

            response.setMessage("User " + userRegistration.getUsername() + " registered successfully!");
            response.setStatus("success");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Internal Server Error: "+ e.getMessage());
            response.setMessage("Internal Server Error: ");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(UserRoutes.USER_LOGIN)
    public  ResponseEntity<ResponseModel> UserLogin(@RequestBody UserModel userLogin){
        ResponseModel response = new ResponseModel();

        String sql = "SELECT user_code, username, password, deleted_at FROM users WHERE username = ?";
        try {
            UserModel user = jdbcTemplate.queryForObject(sql, new Object[]{userLogin.getUsername()},
                    (resultRow, rowNum) -> {
                        UserModel u = new UserModel();
                        u.setUserCode(resultRow.getString("user_code"));
                        u.setUsername(resultRow.getString("username"));
                        u.setPassword(resultRow.getString("password"));
                        u.setDeletedAt(resultRow.getString("deleted_at"));
                        return u;
                    }
            );
            if(user == null){
                response.setMessage("Username or password is incorrect");
                response.setStatus("error");
                return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            if(user.getDeletedAt() != null){
                response.setMessage("User " + userLogin.getUsername() + " has been deleted!");
                response.setStatus("warning");
                return  new ResponseEntity<>(response, HttpStatus.OK);
            }

            if(!passwordEncoder.matches(userLogin.getPassword(), user.getPassword())){
                response.setMessage("Username or password is incorrect");
                response.setStatus("error");
                return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            String newToken = authentication.createToken(user.getUserCode());
            String sqlUpdateJWTToken = "UPDATE users SET jwt_token = ? WHERE user_code = ?";
            jdbcTemplate.update(sqlUpdateJWTToken, newToken, user.getUserCode());

            actionLogService.log(user.getUserCode(), "auth", "Login", "User logged in into the system.");

            response.setMessage("Login successfully!");
            response.setToken(newToken);
            response.setStatus("success");
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (EmptyResultDataAccessException e) {
            response.setMessage("Username or password is incorrect");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            response.setMessage("An error occurred during login.");
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
