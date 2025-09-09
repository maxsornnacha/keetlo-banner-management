package com.keetlo.banner_management.controllers;

import com.keetlo.banner_management.middlewares.JWTAutherization;
import com.keetlo.banner_management.model.BannerModel;
import com.keetlo.banner_management.model.CampaignModel;
import com.keetlo.banner_management.model.ProjectModel;
import com.keetlo.banner_management.model.ResponseModel;
import com.keetlo.banner_management.routes.CampaignRoutes;
import com.keetlo.banner_management.routes.ProjectRoutes;
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
public class CampaignController {
    private final JdbcTemplate jdbcTemplate;
    private final JWTAutherization jWTAutherization;
    private final ImageHandler imageHandler;
    private final ActionLogService actionLogService;

    public CampaignController(JdbcTemplate jdbcTemplate, JWTAutherization jWTAutherization, ImageHandler imageHandler, ActionLogService actionLogService) {
        this.jdbcTemplate = jdbcTemplate;
        this.jWTAutherization = jWTAutherization;
        this.imageHandler = imageHandler;
        this.actionLogService = actionLogService;
    }

    @GetMapping(CampaignRoutes.CAMPAIGN_PREFIX)
    public ResponseEntity<ResponseModel> Campaign(HttpServletRequest request, String search, int page, int limit) {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        String sql = "SELECT campaigns.campaign_id, COUNT(banners.banner_id) AS total_banners , " +
                "campaigns.campaign_image_url, campaigns.campaign_name, projects.project_name, campaigns.description, " +
                "campaigns.start_date, campaigns.end_date, campaigns.status, campaigns.updated_at " +
                "FROM campaigns " +
                "LEFT JOIN projects ON campaigns.project_id = projects.project_id " +
                "LEFT JOIN banners ON campaigns.campaign_id = banners.campaign_id " +
                "WHERE projects.user_code = ? " +
                (search != null && !search.isEmpty() ?
                        " AND (campaigns.campaign_name LIKE ? OR campaigns.description LIKE ?) " : "") +
                "GROUP BY campaigns.campaign_id, campaigns.campaign_image_url, campaigns.campaign_name, " +  // Group by all project fields
                "campaigns.description, campaigns.start_date, campaigns.end_date, campaigns.status, campaigns.updated_at " +
                "ORDER BY campaigns.updated_at DESC " +
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

        String countSql = "SELECT COUNT(*) FROM campaigns " +
                "JOIN projects ON campaigns.project_id = projects.project_id " +
                "WHERE projects.user_code = ? " +
                (search != null && !search.isEmpty() ?
                        " AND (campaigns.campaign_name LIKE ? OR campaigns.description LIKE ?) " : "");
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
            List<CampaignModel> campaigns = jdbcTemplate.query(sql, params, (resultRow, rowNum) -> {
                CampaignModel campaign = new CampaignModel();
                campaign.setCampaignId(resultRow.getString("campaign_id"));
                campaign.setCampaignImageUrl(resultRow.getString("campaign_image_url"));
                campaign.setProjectName(resultRow.getString("project_name"));
                campaign.setCampaignName(resultRow.getString("campaign_name"));
                campaign.setDescription(resultRow.getString("description"));
                campaign.setTotalBanners(resultRow.getInt("total_banners"));
                campaign.setStartDate(resultRow.getString("start_date"));
                campaign.setEndDate(resultRow.getString("end_date"));
                campaign.setStatus(resultRow.getString("status"));
                campaign.setUpdatedAt(resultRow.getString("updated_at"));
                return  campaign;
            });

            int totalCampaigns = jdbcTemplate.queryForObject(countSql, countParams, Integer.class);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("campaigns", campaigns);
            responseData.put("totalCampaigns", totalCampaigns);

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

    @GetMapping(CampaignRoutes.CAMPAIGN)
    public ResponseEntity<ResponseModel> SingleCampaign(HttpServletRequest request, @PathVariable("campaignId") String campaignId, String search, int page, int limit) {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        String sql = "SELECT campaigns.campaign_id, campaigns.project_id, COUNT(banners.banner_id) AS total_banners , " +
                "campaigns.campaign_image_url, campaigns.campaign_name, campaigns.description, " +
                "campaigns.status, campaigns.start_date, campaigns.end_date, campaigns.updated_at " +
                "FROM campaigns " +
                "LEFT JOIN banners ON campaigns.campaign_id = banners.campaign_id " +
                "LEFT JOIN projects ON campaigns.project_id = projects.project_id " +
                "WHERE user_code = ? AND campaigns.campaign_id = ?" +
                "GROUP BY campaigns.campaign_id, campaigns.campaign_image_url, campaigns.campaign_name, " +  // Group by all project fields
                "campaigns.description, campaigns.status, campaigns.updated_at " +
                "ORDER BY campaigns.updated_at DESC";
        Object[] params = new Object[]{
                userCode,
                campaignId
        };

        String bannerSql = "SELECT banners.banner_id, banners.banner_name, banners.description, " +
                "banners.image_url, banners.image_width, banners.image_height, " +
                "banners.clicks, banners.views, banners.link, banners.updated_at " +
                "FROM banners " +
                "WHERE banners.campaign_id = ? " +
                (search != null && !search.isEmpty() ?
                        "AND (banners.banner_name LIKE ? OR banners.description LIKE ?) " : "") +
                "ORDER BY banners.updated_at DESC " +
                "LIMIT ? OFFSET ?";
        Object[] bannerParams;
        if(search != null && !search.isEmpty()) {
            bannerParams  = new Object[]{
                    campaignId,
                    "%" + search + "%",
                    "%" + search + "%",
                    limit,
                    (page - 1) * limit
            };
        } else {
            bannerParams = new Object[]{
                    campaignId,
                    limit,
                    (page - 1) * limit
            };
        }

        String bannerCountSql = "SELECT COUNT(*) FROM banners " +
                "WHERE campaign_id = ? " +
                (search != null && !search.isEmpty() ?
                        "AND (banner_name LIKE ? OR description LIKE ?) " : "");
        Object[] bannerCountParams;
        if(search != null && !search.isEmpty()) {
            bannerCountParams  = new Object[]{
                    campaignId,
                    "%" + search + "%",
                    "%" + search + "%",
            };
        } else {
            bannerCountParams = new Object[]{
                    campaignId,
            };
        }

        try {
            List<CampaignModel> campaigns = jdbcTemplate.query(sql, params, (resultRow, rowNum) -> {
                CampaignModel campaign = new CampaignModel();
                campaign.setCampaignId(resultRow.getString("campaign_id"));
                campaign.setProjectId(resultRow.getString("project_id"));
                campaign.setCampaignImageUrl(resultRow.getString("campaign_image_url"));
                campaign.setCampaignName(resultRow.getString("campaign_name"));
                campaign.setDescription(resultRow.getString("description"));
                campaign.setTotalBanners(resultRow.getInt("total_banners"));
                campaign.setStatus(resultRow.getString("status"));
                campaign.setStartDate(resultRow.getString("start_date"));
                campaign.setEndDate(resultRow.getString("end_date"));
                campaign.setUpdatedAt(resultRow.getString("updated_at"));
                return  campaign;
            });

            if (campaigns.isEmpty()) {
                response.setMessage("Campaign is not found.");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            CampaignModel campaign = campaigns.get(0);

            List<BannerModel> banners = jdbcTemplate.query(bannerSql, bannerParams, (resultRow, rowNum) -> {
                BannerModel banner = new BannerModel();
                banner.setBannerId(resultRow.getString("banner_id"));
                banner.setBannerName(resultRow.getString("banner_name"));
                banner.setDescription(resultRow.getString("description"));
                banner.setImageUrl(resultRow.getString("image_url"));
                banner.setImageWidth(resultRow.getInt("image_width"));
                banner.setImageHeight(resultRow.getInt("image_height"));
                banner.setClicks(resultRow.getInt("clicks"));
                banner.setViews(resultRow.getInt("views"));
                banner.setLink(resultRow.getString("link"));
                banner.setUpdatedAt(resultRow.getString("updated_at"));
                return  banner;
            });


            int totalBanners = jdbcTemplate.queryForObject(bannerCountSql, bannerCountParams, Integer.class);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("campaign", campaign);
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

    @PostMapping(CampaignRoutes.CAMPAIGN_CREATE)
    public ResponseEntity<ResponseModel> CreateCampeign(HttpServletRequest request, @RequestBody CampaignModel campaignRequest) throws IOException {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        // Check if project belongs to the user
        List<ProjectModel> existingProjects = jdbcTemplate.query("SELECT project_id FROM projects WHERE project_id = ? AND user_code = ?", new Object[]{
           campaignRequest.getProjectId(), userCode
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

        if(campaignRequest.getCampaignName() == null || campaignRequest.getStatus() == null
        || campaignRequest.getStartDate() == null
        ) {
            response.setMessage("All fields are required.");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String base64Image = campaignRequest.getCampaignImageUrl();
        if (base64Image != null && !base64Image.isEmpty() && imageHandler.isBase64(base64Image)) {
            String uploadDir = "/home/sornnacha_bur/main/code_projects/keetlo/banner_management/uploads/images/campaigns/";  // Ensure this directory exists
            String imageName = "campaign_image_" + System.currentTimeMillis() + ".webp";  // Unique file name
            String filePath = uploadDir + imageName;
            try {
                imageHandler.saveBase64Image(base64Image, filePath);  // Assuming ImageHandler handles Base64 decoding and file saving
            } catch (IOException e) {
                response.setMessage("Failed to save image.");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            campaignRequest.setCampaignImageUrl("/images/campaigns/" + imageName);
        }

        String sql = "INSERT INTO campaigns (campaign_id, project_id, campaign_image_url, " +
                "campaign_name, description, status, start_date, end_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String createdCampaignId = campaignRequest.createCampaignId();
        try{
            int rowsAffected = jdbcTemplate.update(sql,
                    createdCampaignId,
                    campaignRequest.getProjectId(),
                    campaignRequest.getCampaignImageUrl(),
                    campaignRequest.getCampaignName(),
                    campaignRequest.getDescription(),
                    campaignRequest.getStatus(),
                    campaignRequest.getStartDate(),
                    campaignRequest.getEndDate()
            );
            if(rowsAffected==0){
                response.setMessage("Something is incorrect");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            actionLogService.log(userCode, "campaign", "Campaign Created", "A new campaign was created in the system.");
            response.setMessage("Campaign " + createdCampaignId + " created successfully!");
            response.setStatus("success");
            return  new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Internal Server Error: "+ e.getMessage());
            response.setMessage("Internal Server Error: ");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(CampaignRoutes.CAMPAIGN_UPDATE)
    public ResponseEntity<ResponseModel> UpdateCampaign(HttpServletRequest request, @PathVariable("campaignId") String campaignId) {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        // Check if project belongs to the user
        List<ProjectModel> existingProjects = jdbcTemplate.query("SELECT projects.project_id " +
                "FROM campaigns " +
                 "JOIN projects ON campaigns.project_id = projects.project_id " +
                " WHERE campaigns.campaign_id = ? AND user_code = ?",
                new Object[]{
                campaignId, userCode
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

        String sql = "SELECT campaign_id, " +
                "campaign_image_url, campaign_name, description, start_date, end_date, status " +
                "FROM campaigns " +
                "WHERE campaign_id = ?";
        Object[] params = new Object[]{
                campaignId
        };

        try {
            List<CampaignModel> campaigns = jdbcTemplate.query(sql, params, (resultRow, rowNum) -> {
                CampaignModel c = new CampaignModel();
                c.setCampaignId(resultRow.getString("campaign_id"));
                c.setCampaignImageUrl(resultRow.getString("campaign_image_url"));
                c.setCampaignName(resultRow.getString("campaign_name"));
                c.setDescription(resultRow.getString("description"));
                c.setStartDate(resultRow.getString("start_date"));
                c.setEndDate(resultRow.getString("end_date"));
                c.setStatus(resultRow.getString("status"));
                return c;
            });

            if (campaigns.isEmpty()) {
                response.setMessage("Campaign is not found.");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            CampaignModel campaign = campaigns.get(0);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("campaign", campaign);

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

    @PutMapping(CampaignRoutes.CAMPAIGN_UPDATE)
    public ResponseEntity<ResponseModel> PostUpdateCampaign(HttpServletRequest request, @RequestBody CampaignModel campaignRequest, @PathVariable("campaignId") String campaignId) throws IOException {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        // Check if project belongs to the user
        List<ProjectModel> existingProjects = jdbcTemplate.query("SELECT projects.project_id " +
                        "FROM campaigns " +
                        "JOIN projects ON campaigns.project_id = projects.project_id " +
                        " WHERE campaigns.campaign_id = ? AND user_code = ?",
                new Object[]{
                        campaignId, userCode
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

        if(campaignRequest.getCampaignName() == null || campaignRequest.getStatus() == null
        || campaignRequest.getStartDate() == null
        ) {
            response.setMessage("All fields are required.");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String existingCampaignSql = "SELECT campaign_id, " +
                "campaign_image_url, campaign_name, description, start_date, end_date, status " +
                "FROM campaigns " +
                "WHERE campaign_id = ?";
        Object[] existingCampaignSqlParams = new Object[]{
                campaignId,
        };

        String sql = "UPDATE campaigns SET campaign_image_url = ?, campaign_name = ?, description = ?, start_date = ?, end_date = ?, status = ?, updated_at = NOW() WHERE campaign_id = ?";
        try{
            List<CampaignModel> existingCampaigns = jdbcTemplate.query(existingCampaignSql, existingCampaignSqlParams, (resultRow, rowNum) -> {
                CampaignModel c = new CampaignModel();
                c.setCampaignId(resultRow.getString("campaign_id"));
                c.setCampaignImageUrl(resultRow.getString("campaign_image_url"));
                c.setCampaignName(resultRow.getString("campaign_name"));
                c.setDescription(resultRow.getString("description"));
                c.setStartDate(resultRow.getString("start_date"));
                c.setEndDate(resultRow.getString("end_date"));
                c.setStatus(resultRow.getString("status"));
                return c;
            });
            if (existingCampaigns.isEmpty()) {
                response.setMessage("Campaign is not found.");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            CampaignModel existingCampaign = existingCampaigns.get(0);

            String base64Image = campaignRequest.getCampaignImageUrl();
            if (base64Image != null && !base64Image.isEmpty() && imageHandler.isBase64(base64Image)) {
                String uploadDir = "/home/sornnacha_bur/main/code_projects/keetlo/banner_management/uploads/images/campaigns/";
                String imageName = "campaign_image_" + System.currentTimeMillis() + ".webp";
                String filePath = uploadDir + imageName;
                try {
                    imageHandler.saveBase64Image(base64Image, filePath);
                    if(existingCampaign.getCampaignImageUrl() != null && !existingCampaign.getCampaignImageUrl().trim().isEmpty()) {
                        String oldFilePath = "src/main/resources/static" + existingCampaign.getCampaignImageUrl();
                        imageHandler.deleteImage(oldFilePath);
                    }
                } catch (IOException e) {
                    response.setMessage("Failed to save image.");
                    response.setStatus("error");
                    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                campaignRequest.setCampaignImageUrl("/images/campaigns/" + imageName);
            }

            int rowsAffected = jdbcTemplate.update(sql,
                    campaignRequest.getCampaignImageUrl(),
                    campaignRequest.getCampaignName(),
                    campaignRequest.getDescription(),
                    campaignRequest.getStartDate(),
                    campaignRequest.getEndDate(),
                    campaignRequest.getStatus(),
                    existingCampaign.getCampaignId()
            );
            if(rowsAffected==0){
                response.setMessage("Something is incorrect");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            actionLogService.log(userCode, "campaign", "Campaign Updated", "Campaign has been updated.");
            response.setMessage("Campaign " + existingCampaign.getCampaignId() + " updated successfully!");
            response.setStatus("success");
            return  new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Internal Server Error: "+ e.getMessage());
            response.setMessage("Internal Server Error: ");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(CampaignRoutes.CAMPAIGN_DELETE)
    public ResponseEntity<ResponseModel> DeleteCampaign(HttpServletRequest request, @RequestBody CampaignModel campaignRequest, @PathVariable("campaignId") String campaignId) throws IOException {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        String existingCampaignSql = "SELECT campaigns.campaign_id, " +
                "campaigns.campaign_image_url, campaigns.campaign_name, campaigns.description, campaigns.status " +
                "FROM campaigns " +
                "LEFT JOIN projects ON campaigns.project_id = projects.project_id " +
                "WHERE projects.user_code = ? AND campaigns.campaign_id = ?";
        Object[] existingCampaignSqlParams = new Object[]{
                userCode,
                campaignId,
        };

        String sql = "DELETE campaigns FROM campaigns " +
                "LEFT JOIN projects ON campaigns.project_id = projects.project_id " +
                "WHERE projects.user_code = ? AND campaigns.campaign_id = ?";
        Object[] params = new Object[]{
                userCode,
                campaignId,
        };

        String getBannersSql = "SELECT banner_id, image_url FROM banners WHERE campaign_id = ?";
        String deleteBannersSql = "DELETE FROM banners WHERE banner_id = ?";

        try{
            List<CampaignModel> existingCampaigns = jdbcTemplate.query(existingCampaignSql, existingCampaignSqlParams, (resultRow, rowNum) -> {
                CampaignModel c = new CampaignModel();
                c.setCampaignId(resultRow.getString("campaign_id"));
                c.setCampaignImageUrl(resultRow.getString("campaign_image_url"));
                c.setCampaignName(resultRow.getString("campaign_name"));
                c.setDescription(resultRow.getString("description"));
                c.setStatus(resultRow.getString("status"));
                return c;
            });
            if (existingCampaigns.isEmpty()) {
                response.setMessage("Project is not found.");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            CampaignModel existingCampaign = existingCampaigns.get(0);

            if(existingCampaign.getCampaignImageUrl() != null && !existingCampaign.getCampaignImageUrl().trim().isEmpty()) {
                String oldFilePath = "src/main/resources/static" + existingCampaign.getCampaignImageUrl();
                imageHandler.deleteImage(oldFilePath);
            }

            //Check the banners in the project
            for (CampaignModel campaign : existingCampaigns) {
                //Delete the banners in the project
                List<BannerModel> existingBanners = jdbcTemplate.query(getBannersSql, new Object[]{
                        campaign.getCampaignId(),
                }, (resultRow, rowNum) -> {
                    BannerModel b = new BannerModel();
                    b.setBannerId(resultRow.getString("banner_id"));
                    b.setImageUrl(resultRow.getString("image_url"));
                    return b;
                });
                // Delete the banners and campaignImageUrl in the campaign
                for (BannerModel banner : existingBanners) {
                    if(banner.getImageUrl() != null && !banner.getImageUrl().trim().isEmpty()) {
                        String oldFilePath = "src/main/resources/static" + banner.getImageUrl();
                        imageHandler.deleteImage(oldFilePath);
                    }
                    int bannerRowsAffected = jdbcTemplate.update(deleteBannersSql, banner.getBannerId());
                    if(bannerRowsAffected==0){
                        response.setMessage("Something is incorrect");
                        response.setStatus("error");
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }
                }

            }

            int rowsAffected = jdbcTemplate.update(sql, params);
            if(rowsAffected==0){
                response.setMessage("Something is incorrect");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            actionLogService.log(userCode, "campaign", "Campaign Deleted", "Campaign was deleted from the system.");
            response.setMessage("Campaign " + campaignId + " deleted successfully!");
            response.setStatus("success");
            return  new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Internal Server Error: "+ e.getMessage());
            response.setMessage("Internal Server Error: ");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(CampaignRoutes.CAMPAIGN_OPTION)
    public ResponseEntity<ResponseModel> CampaignOption(HttpServletRequest request) {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        String sql = "SELECT campaigns.campaign_id, " +
                "campaigns.campaign_name, " +
                "projects.project_name, " +
                "campaigns.status " +
                "FROM campaigns " +
                "JOIN projects ON campaigns.project_id = projects.project_id " +
                "WHERE user_code = ? " +
                "ORDER BY campaigns.updated_at DESC";
        Object[] params =  new Object[]{userCode};

        try {
            List<CampaignModel> options = jdbcTemplate.query(sql, params, (resultRow, rowNum) -> {
                CampaignModel campaign = new CampaignModel();
                campaign.setCampaignId(resultRow.getString("campaign_id"));
                campaign.setProjectName(resultRow.getString("project_name"));
                campaign.setCampaignName(resultRow.getString("campaign_name"));
                campaign.setStatus(resultRow.getString("status"));
                return  campaign;
            });

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("options", options);

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
