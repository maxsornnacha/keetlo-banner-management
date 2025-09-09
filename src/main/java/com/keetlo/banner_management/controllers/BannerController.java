package com.keetlo.banner_management.controllers;

import com.keetlo.banner_management.middlewares.JWTAutherization;
import com.keetlo.banner_management.model.BannerModel;
import com.keetlo.banner_management.model.CampaignModel;
import com.keetlo.banner_management.model.ProjectModel;
import com.keetlo.banner_management.model.ResponseModel;
import com.keetlo.banner_management.routes.BannerRoutes;
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
public class BannerController {
    private final JdbcTemplate jdbcTemplate;
    private final JWTAutherization jWTAutherization;
    private final ImageHandler imageHandler;
    private final ActionLogService actionLogService;

    public BannerController(JdbcTemplate jdbcTemplate, JWTAutherization jWTAutherization, ImageHandler imageHandler, ActionLogService actionLogService) {
        this.jdbcTemplate = jdbcTemplate;
        this.jWTAutherization = jWTAutherization;
        this.imageHandler = imageHandler;
        this.actionLogService = actionLogService;
    }

    @GetMapping(BannerRoutes.BANNER_PREFIX)
    public ResponseEntity<ResponseModel> Banner(HttpServletRequest request, String search, int page, int limit) {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        String sql = "SELECT banners.banner_id, " +
                "banners.image_url, banners.banner_name, projects.project_name, campaigns.campaign_name, " +
                "banners.description, " +
                "banners.image_width, banners.image_height, " +
                "banners.views, banners.clicks, banners.link, " +
                "banners.updated_at " +
                "FROM banners " +
                "LEFT JOIN campaigns ON banners.campaign_id = campaigns.campaign_id " +
                "LEFT JOIN projects ON campaigns.project_id = projects.project_id " +
                "WHERE projects.user_code = ? " +
                (search != null && !search.isEmpty() ?
                        " AND (banners.banner_name LIKE ? OR banners.description LIKE ?) " : "") +// Group by all project fields
                "ORDER BY banners.updated_at DESC " +
                "LIMIT ? OFFSET ?";
        Object[] params;
        if(search != null && !search.isEmpty()) {
            params  = new Object[]{
                    userCode,
                    "%" + search + "%",
                    "%" + search + "%",
                    limit,
                    (page - 1) * limit
            };
        } else {
            params = new Object[]{
                    userCode,
                    limit,
                    (page - 1) * limit
            };
        }

        String countSql = "SELECT COUNT(*) FROM banners " +
                "JOIN campaigns ON banners.campaign_id = campaigns.campaign_id " +
                "JOIN projects ON campaigns.project_id = projects.project_id " +
                "WHERE projects.user_code = ? " +
                (search != null && !search.isEmpty() ?
                        " AND (banners.banner_name LIKE ? OR banners.description LIKE ?) " : "");
        Object[] countParams;
        if(search != null && !search.isEmpty()) {
            countParams  = new Object[]{
                    userCode,
                    "%" + search + "%",
                    "%" + search + "%",
            };
        } else {
            countParams = new Object[]{
                    userCode,
            };
        }

        try {
            List<BannerModel> banners = jdbcTemplate.query(sql, params, (resultRow, rowNum) -> {
                BannerModel banner = new BannerModel();
                banner.setBannerId(resultRow.getString("banner_id"));
                banner.setImageUrl(resultRow.getString("image_url"));
                banner.setBannerName(resultRow.getString("banner_name"));
                banner.setProjectName(resultRow.getString("project_name"));
                banner.setCampaignName(resultRow.getString("campaign_name"));
                banner.setDescription(resultRow.getString("description"));
                banner.setImageWidth(resultRow.getInt("image_width"));
                banner.setImageHeight(resultRow.getInt("image_height"));
                banner.setClicks(resultRow.getInt("clicks"));
                banner.setViews(resultRow.getInt("views"));
                banner.setLink(resultRow.getString("link"));
                banner.setUpdatedAt(resultRow.getString("updated_at"));
                return  banner;
            });

            int totalBanners = jdbcTemplate.queryForObject(countSql, countParams, Integer.class);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("banners", banners);
            responseData.put("totalBanners", totalBanners);

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

    @GetMapping(BannerRoutes.BANNER)
    public ResponseEntity<ResponseModel> SingleBanner(HttpServletRequest request, @PathVariable("bannerId") String bannerId) {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        String sql = "SELECT banners.banner_id, campaigns.campaign_id, campaigns.campaign_name, " +
                "projects.project_id, projects.project_name, " +
                "projects.description AS project_description, campaigns.description AS campaign_description," +
                "projects.project_image_url, campaigns.campaign_image_url," +
                "banners.image_url, banners.banner_name, banners.description, " +
                "banners.image_width, banners.image_height, " +
                "banners.views, banners.clicks, banners.link, " +
                "banners.updated_at " +
                "FROM banners " +
                "LEFT JOIN campaigns ON banners.campaign_id = campaigns.campaign_id " +
                "LEFT JOIN projects ON campaigns.project_id = projects.project_id " +
                "WHERE projects.user_code = ? AND banners.banner_id = ?" +
                "ORDER BY banners.updated_at DESC";
        Object[] params = new Object[]{
                userCode,
                bannerId
        };

        try {
            List<BannerModel> banners = jdbcTemplate.query(sql, params, (resultRow, rowNum) -> {
                BannerModel banner = new BannerModel();
                banner.setBannerId(resultRow.getString("banner_id"));
                banner.setCampaignId(resultRow.getString("campaign_id"));
                banner.setProjectId(resultRow.getString("project_id"));
                banner.setProjectName(resultRow.getString("project_name"));
                banner.setCampaignName(resultRow.getString("campaign_name"));
                banner.setCampaignDescription(resultRow.getString("campaign_description"));
                banner.setProjectDescription(resultRow.getString("project_description"));
                banner.setProjectImageUrl(resultRow.getString("project_image_url"));
                banner.setCampaignImageUrl(resultRow.getString("campaign_image_url"));
                banner.setImageUrl(resultRow.getString("image_url"));
                banner.setBannerName(resultRow.getString("banner_name"));
                banner.setDescription(resultRow.getString("description"));
                banner.setImageWidth(resultRow.getInt("image_width"));
                banner.setImageHeight(resultRow.getInt("image_height"));
                banner.setViews(resultRow.getInt("views"));
                banner.setClicks(resultRow.getInt("clicks"));
                banner.setLink(resultRow.getString("link"));
                banner.setUpdatedAt(resultRow.getString("updated_at"));
                return  banner;
            });

            if (banners.isEmpty()) {
                response.setMessage("Banner is not found.");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            BannerModel banner = banners.get(0);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("banner", banner);

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

    @PostMapping(BannerRoutes.BANNER_CREATE)
    public ResponseEntity<ResponseModel> CreateBanner(HttpServletRequest request, @RequestBody BannerModel bannerRequest) throws IOException {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        // Check if campaign belongs to the user
        List<CampaignModel> existingCampaigns = jdbcTemplate.query("SELECT campaigns.campaign_id FROM campaigns " +
                "JOIN projects ON campaigns.project_id = projects.project_id " +
                "WHERE campaigns.campaign_id = ? AND projects.user_code = ?", new Object[]{
                bannerRequest.getCampaignId(), userCode
        }, (resultRow, rowNum) -> {
            CampaignModel c = new CampaignModel();
            c.setCampaignId(resultRow.getString("campaign_id"));
            return c;
        });
        if (existingCampaigns.isEmpty()) {
            response.setMessage("Permission Denied For The Campaign");
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(bannerRequest.getBannerName() == null || bannerRequest.getImageUrl() == null
                || bannerRequest.getImageWidth() == null  || bannerRequest.getImageHeight() == null
        ) {
            response.setMessage("All fields are required.");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String base64Image = bannerRequest.getImageUrl();
        if (base64Image != null && !base64Image.isEmpty() && imageHandler.isBase64(base64Image)) {
            String uploadDir = "/home/sornnacha_bur/main/code_projects/keetlo/banner_management/uploads/images/banners/";  // Ensure this directory exists
            String imageName = "banner_image_" + System.currentTimeMillis() + ".webp";  // Unique file name
            String filePath = uploadDir + imageName;
            try {
                imageHandler.saveBase64Image(base64Image, filePath);  // Assuming ImageHandler handles Base64 decoding and file saving
            } catch (IOException e) {
                response.setMessage("Failed to save image.");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            bannerRequest.setImageUrl("/images/banners/" + imageName);
        }

        String sql = "INSERT INTO banners (banner_id, campaign_id, image_url, " +
                "banner_name, description, image_width, image_height, link) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String createdBannerId = bannerRequest.createBannerId();
        try{
            int rowsAffected = jdbcTemplate.update(sql,
                    createdBannerId,
                    bannerRequest.getCampaignId(),
                    bannerRequest.getImageUrl(),
                    bannerRequest.getBannerName(),
                    bannerRequest.getDescription(),
                    bannerRequest.getImageWidth(),
                    bannerRequest.getImageHeight(),
                    bannerRequest.getLink()
            );
            if(rowsAffected==0){
                response.setMessage("Something is incorrect");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            actionLogService.log(userCode, "banner", "Banner Created", "A new banner was created in the system.");
            response.setMessage("Banner " + createdBannerId + " created successfully!");
            response.setStatus("success");
            return  new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Internal Server Error: "+ e.getMessage());
            response.setMessage("Internal Server Error: ");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(BannerRoutes.BANNER_UPDATE)
    public ResponseEntity<ResponseModel> PostUpdateBanner(HttpServletRequest request, @RequestBody BannerModel bannerRequest, @PathVariable("bannerId") String bannerId) throws IOException {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        // Check if project belongs to the user
        List<ProjectModel> existingProjects = jdbcTemplate.query("SELECT projects.project_id " +
                        "FROM banners " +
                        "JOIN campaigns ON banners.campaign_id = campaigns.campaign_id " +
                        "JOIN projects ON campaigns.project_id = projects.project_id " +
                        " WHERE banners.banner_id = ? AND user_code = ?",
                new Object[]{
                        bannerId, userCode
                }, (resultRow, rowNum) -> {
                    ProjectModel p = new ProjectModel();
                    p.setProjectId(resultRow.getString("project_id"));
                    return p;
                });
        if (existingProjects.isEmpty()) {
            response.setMessage("Permission Denied For The Project");
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(bannerRequest.getBannerName() == null
        ) {
            response.setMessage("All fields are required.");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String existingBannerSql = "SELECT banner_id, " +
                "image_url, banner_name, description, " +
                "image_width, image_height, link " +
                "FROM banners " +
                "WHERE banner_id = ?";
        Object[] existingBannerSqlParams = new Object[]{
                bannerId,
        };

        String sql = "UPDATE banners SET image_url = ?, banner_name = ?, description = ?, " +
                "image_width = ?, image_height = ?, link = ?, updated_at = NOW() WHERE banner_id = ?";
        try{
            List<BannerModel> existingBanners = jdbcTemplate.query(existingBannerSql, existingBannerSqlParams, (resultRow, rowNum) -> {
                BannerModel b = new BannerModel();
                b.setBannerId(resultRow.getString("banner_id"));
                b.setImageUrl(resultRow.getString("image_url"));
                b.setBannerName(resultRow.getString("banner_name"));
                b.setDescription(resultRow.getString("description"));
                b.setImageWidth(resultRow.getInt("image_width"));
                b.setImageHeight(resultRow.getInt("image_height"));
                b.setLink(resultRow.getString("link"));
                return b;
            });
            if (existingBanners.isEmpty()) {
                response.setMessage("Banner is not found.");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            BannerModel existingBanner = existingBanners.get(0);

            String base64Image = bannerRequest.getImageUrl();
            if (base64Image != null && !base64Image.isEmpty() && imageHandler.isBase64(base64Image)) {
                String uploadDir = "/home/sornnacha_bur/main/code_projects/keetlo/banner_management/uploads/images/banners/";
                String imageName = "banner_image_" + System.currentTimeMillis() + ".webp";
                String filePath = uploadDir + imageName;
                try {
                    imageHandler.saveBase64Image(base64Image, filePath);
                    if(existingBanner.getImageUrl() != null && !existingBanner.getImageUrl().trim().isEmpty()) {
                        String oldFilePath = "src/main/resources/static" + existingBanner.getImageUrl();
                        imageHandler.deleteImage(oldFilePath);
                    }
                } catch (IOException e) {
                    response.setMessage("Failed to save image.");
                    response.setStatus("error");
                    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                bannerRequest.setImageUrl("/images/banners/" + imageName);
            }

            int rowsAffected = jdbcTemplate.update(sql,
                    bannerRequest.getImageUrl(),
                    bannerRequest.getBannerName(),
                    bannerRequest.getDescription(),
                    bannerRequest.getImageWidth(),
                    bannerRequest.getImageHeight(),
                    bannerRequest.getLink(),
                    existingBanner.getBannerId()
            );
            if(rowsAffected==0){
                response.setMessage("Something is incorrect");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            actionLogService.log(userCode, "banner", "Banner Updated", "Banner have been updated.");
            response.setMessage("Banner " + existingBanner.getBannerId() + " updated successfully!");
            response.setStatus("success");
            return  new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Internal Server Error: "+ e.getMessage());
            response.setMessage("Internal Server Error: ");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(BannerRoutes.BANNER_DELETE)
    public ResponseEntity<ResponseModel> DeleteBanner(HttpServletRequest request, @RequestBody BannerModel bannerRequest, @PathVariable("bannerId") String bannerId) throws IOException {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        String existingBannerSql = "SELECT banners.banner_id, " +
                "banners.image_url, banners.banner_name, banners.description " +
                "FROM banners " +
                "LEFT JOIN campaigns ON banners.campaign_id = campaigns.campaign_id " +
                "LEFT JOIN projects ON campaigns.project_id = projects.project_id " +
                "WHERE projects.user_code = ? AND banners.banner_id = ?";
        Object[] existingBannerSqlParams = new Object[]{
                userCode,
                bannerId,
        };

        String sql = "DELETE banners FROM banners " +
                "LEFT JOIN campaigns ON banners.campaign_id = campaigns.campaign_id " +
                "LEFT JOIN projects ON campaigns.project_id = projects.project_id " +
                "WHERE projects.user_code = ? AND banners.banner_id = ?";
        Object[] params = new Object[]{
                userCode,
                bannerId,
        };

        try{
            List<BannerModel> existingBanners = jdbcTemplate.query(existingBannerSql, existingBannerSqlParams, (resultRow, rowNum) -> {
                BannerModel b = new BannerModel();
                b.setBannerId(resultRow.getString("banner_id"));
                b.setImageUrl(resultRow.getString("image_url"));
                b.setBannerName(resultRow.getString("banner_name"));
                b.setDescription(resultRow.getString("description"));
                return b;
            });
            if (existingBanners.isEmpty()) {
                response.setMessage("Banner is not found.");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            BannerModel existingBanner = existingBanners.get(0);

            if(existingBanner.getImageUrl() != null && !existingBanner.getImageUrl().trim().isEmpty()) {
                String oldFilePath = "src/main/resources/static" + existingBanner.getImageUrl();
                imageHandler.deleteImage(oldFilePath);
            }

            int rowsAffected = jdbcTemplate.update(sql, params);
            if(rowsAffected==0){
                response.setMessage("Something is incorrect");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            actionLogService.log(userCode, "banner", "Banner Deleted", "Banner was deleted from the system.");
            response.setMessage("Banner " + bannerId + " deleted successfully!");
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
