package com.keetlo.banner_management.controllers;

import com.keetlo.banner_management.middlewares.JWTAutherization;
import com.keetlo.banner_management.model.BannerModel;
import com.keetlo.banner_management.model.CampaignModel;
import com.keetlo.banner_management.model.ProjectModel;
import com.keetlo.banner_management.model.ResponseModel;
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
public class ProjectController {
    private final JdbcTemplate jdbcTemplate;
    private final JWTAutherization jWTAutherization;
    private final ImageHandler imageHandler;
    private final ActionLogService actionLogService;

    public ProjectController(JdbcTemplate jdbcTemplate, JWTAutherization jWTAutherization, ImageHandler imageHandler, ActionLogService actionLogService) {
        this.jdbcTemplate = jdbcTemplate;
        this.jWTAutherization = jWTAutherization;
        this.imageHandler = imageHandler;
        this.actionLogService = actionLogService;
    }

    @GetMapping(ProjectRoutes.PROJECT_PREFIX)
    public ResponseEntity<ResponseModel> Project(HttpServletRequest request, String search, int page, int limit) {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        String sql = "SELECT projects.project_id, COUNT(campaigns.campaign_id) AS total_campaigns , " +
                "projects.project_image_url, projects.project_name, projects.description, " +
                "projects.status, projects.updated_at " +
                "FROM projects " +
                "LEFT JOIN campaigns ON projects.project_id = campaigns.project_id " +
                "WHERE user_code = ? " +
                (search != null && !search.isEmpty() ?
                " AND (projects.project_name LIKE ? OR projects.description LIKE ?) " : "") +
                "GROUP BY projects.project_id, projects.project_image_url, projects.project_name, " +  // Group by all project fields
                "projects.description, projects.status, projects.updated_at " +
                "ORDER BY updated_at DESC " +
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

        String countSql = "SELECT COUNT(*) FROM projects " +
                "WHERE projects.user_code = ? " +
                (search != null && !search.isEmpty() ?
                " AND (projects.project_name LIKE ? OR projects.description LIKE ?) " : "");
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
             List<ProjectModel> projects = jdbcTemplate.query(sql, params, (resultRow, rowNum) -> {
                 ProjectModel project = new ProjectModel();
                 project.setProjectId(resultRow.getString("project_id"));
                 project.setProjectImageUrl(resultRow.getString("project_image_url"));
                 project.setProjectName(resultRow.getString("project_name"));
                 project.setDescription(resultRow.getString("description"));
                 project.setTotalCampaigns(resultRow.getInt("total_campaigns"));
                 project.setStatus(resultRow.getString("status"));
                 project.setUpdatedAt(resultRow.getString("updated_at"));
                 return  project;
             });

            int totalProjects = jdbcTemplate.queryForObject(countSql, countParams, Integer.class);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("projects", projects);
            responseData.put("totalProjects", totalProjects);

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

    @GetMapping(ProjectRoutes.PROJECT)
    public ResponseEntity<ResponseModel> SingleProject(HttpServletRequest request, @PathVariable("projectId") String projectId, String search, int page, int limit) {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        String sql = "SELECT projects.project_id, COUNT(campaigns.campaign_id) AS total_campaigns , " +
                "projects.project_image_url, projects.project_name, projects.description, " +
                "projects.status, projects.updated_at " +
                "FROM projects " +
                "LEFT JOIN campaigns ON projects.project_id = campaigns.project_id " +
                "WHERE user_code = ? AND projects.project_id = ?" +
                "GROUP BY projects.project_id, projects.project_image_url, projects.project_name, " +  // Group by all project fields
                "projects.description, projects.status, projects.updated_at " +
                "ORDER BY updated_at DESC";
        Object[] params = new Object[]{
                userCode,
                projectId
        };

        String campaignSql = "SELECT campaigns.campaign_id, COUNT(banners.banner_id) AS total_banners, campaigns.campaign_name, campaigns.description, campaigns.campaign_image_url, campaigns.start_date, campaigns.end_date, " +
                "campaigns.status, campaigns.updated_at " +
                "FROM campaigns " +
                "LEFT JOIN banners ON campaigns.campaign_id = banners.campaign_id " +
                "WHERE project_id = ? " +
                (search != null && !search.isEmpty() ?
                "AND (campaigns.campaign_name LIKE ? OR campaigns.description LIKE ?) " : "") +
                "GROUP BY campaigns.campaign_id, campaigns.campaign_name " +
                "ORDER BY campaigns.updated_at DESC " +
                "LIMIT ? OFFSET ?";
        Object[] campaignParams;
        if(search != null && !search.isEmpty()) {
            campaignParams  = new Object[]{
                    projectId,
                    "%" + search + "%",
                    "%" + search + "%",
                    limit,
                    (page - 1) * limit
            };
        } else {
            campaignParams = new Object[]{
                    projectId,
                    limit,
                    (page - 1) * limit
            };
        }

        String campaignCountSql = "SELECT COUNT(*) FROM campaigns " +
                "WHERE project_id = ? " +
                (search != null && !search.isEmpty() ?
                "AND (campaign_name LIKE ? OR description LIKE ?) " : "");
        Object[] campaignCountParams;
        if(search != null && !search.isEmpty()) {
            campaignCountParams  = new Object[]{
                    projectId,
                    "%" + search + "%",
                    "%" + search + "%",
            };
        } else {
            campaignCountParams = new Object[]{
                    projectId,
            };
        }

        try {
            List<ProjectModel> projects = jdbcTemplate.query(sql, params, (resultRow, rowNum) -> {
                ProjectModel project = new ProjectModel();
                project.setProjectId(resultRow.getString("project_id"));
                project.setProjectImageUrl(resultRow.getString("project_image_url"));
                project.setProjectName(resultRow.getString("project_name"));
                project.setDescription(resultRow.getString("description"));
                project.setTotalCampaigns(resultRow.getInt("total_campaigns"));
                project.setStatus(resultRow.getString("status"));
                project.setUpdatedAt(resultRow.getString("updated_at"));
                return  project;
            });

            if (projects.isEmpty()) {
                response.setMessage("Project is not found.");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            ProjectModel project = projects.get(0);

            List<CampaignModel> campaigns = jdbcTemplate.query(campaignSql, campaignParams, (resultRow, rowNum) -> {
                CampaignModel campaign = new CampaignModel();
                campaign.setCampaignId(resultRow.getString("campaign_id"));
                campaign.setTotalBanners(resultRow.getInt("total_banners"));
                campaign.setCampaignName(resultRow.getString("campaign_name"));
                campaign.setDescription(resultRow.getString("description"));
                campaign.setCampaignImageUrl(resultRow.getString("campaign_image_url"));
                campaign.setStartDate(resultRow.getString("start_date"));
                campaign.setEndDate(resultRow.getString("end_date"));
                campaign.setStatus(resultRow.getString("status"));
                campaign.setUpdatedAt(resultRow.getString("updated_at"));
                return  campaign;
            });


            int totalCampaigns = jdbcTemplate.queryForObject(campaignCountSql, campaignCountParams, Integer.class);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("project", project);
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

    @PostMapping(ProjectRoutes.PROJECT_CREATE)
    public ResponseEntity<ResponseModel> CreateProject(HttpServletRequest request, @RequestBody ProjectModel projectRequest) throws IOException {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(projectRequest.getProjectName() == null || projectRequest.getStatus() == null) {
            response.setMessage("All fields are required.");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String base64Image = projectRequest.getProjectImageUrl();
        if (base64Image != null && !base64Image.isEmpty() && imageHandler.isBase64(base64Image)) {
            String uploadDir = "/home/sornnacha_bur/main/code_projects/keetlo/banner_management/uploads/images/projects/";  // Ensure this directory exists
            String imageName = "project_image_" + System.currentTimeMillis() + ".webp";  // Unique file name
            String filePath = uploadDir + imageName;
            try {
                imageHandler.saveBase64Image(base64Image, filePath);  // Assuming ImageHandler handles Base64 decoding and file saving
            } catch (IOException e) {
                response.setMessage("Failed to save image.");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            projectRequest.setProjectImageUrl("/images/projects/" + imageName);
        }

        String sql = "INSERT INTO projects (project_id, user_code, project_image_url, project_name, description, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        String createdProjectId = projectRequest.createProjectId();
        try{
            int rowsAffected = jdbcTemplate.update(sql,
                    createdProjectId,
                    userCode,
                    projectRequest.getProjectImageUrl(),
                    projectRequest.getProjectName(),
                    projectRequest.getDescription(),
                    projectRequest.getStatus()
            );
            if(rowsAffected==0){
                response.setMessage("Something is incorrect");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            actionLogService.log(userCode, "project", "Project Created", "A new project was created in the system.");
            response.setMessage("Project " + createdProjectId + " created successfully!");
            response.setStatus("success");
            return  new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Internal Server Error: "+ e.getMessage());
            response.setMessage("Internal Server Error: ");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(ProjectRoutes.PROJECT_UPDATE)
    public ResponseEntity<ResponseModel> UpdateProject(HttpServletRequest request, @PathVariable("projectId") String projectId) {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        String sql = "SELECT project_id, " +
                "project_image_url, project_name, description, status " +
                "FROM projects " +
                "WHERE user_code = ? AND project_id = ?";
        Object[] params = new Object[]{
                userCode,
                projectId
        };

        try {
            List<ProjectModel> projects = jdbcTemplate.query(sql, params, (resultRow, rowNum) -> {
                ProjectModel p = new ProjectModel();
                p.setProjectId(resultRow.getString("project_id"));
                p.setProjectImageUrl(resultRow.getString("project_image_url"));
                p.setProjectName(resultRow.getString("project_name"));
                p.setDescription(resultRow.getString("description"));
                p.setStatus(resultRow.getString("status"));
                return p;
            });

            if (projects.isEmpty()) {
                response.setMessage("Project is not found.");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            ProjectModel project = projects.get(0);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("project", project);

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

    @PutMapping(ProjectRoutes.PROJECT_UPDATE)
    public ResponseEntity<ResponseModel> PostUpdateProject(HttpServletRequest request, @RequestBody ProjectModel projectRequest, @PathVariable("projectId") String projectId) throws IOException {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(projectRequest.getProjectName() == null || projectRequest.getStatus() == null) {
            response.setMessage("All fields are required.");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String existingProjectSql = "SELECT project_id, " +
                "project_image_url, project_name, description, status " +
                "FROM projects " +
                "WHERE user_code = ? AND project_id = ?";
        Object[] existingProjectSqlParams = new Object[]{
                userCode,
                projectId,
        };

        String sql = "UPDATE projects SET project_image_url = ?, project_name = ?, description = ?, status = ?, updated_at = NOW() WHERE project_id = ? AND user_code = ?";
        try{
            List<ProjectModel> existingProjects = jdbcTemplate.query(existingProjectSql, existingProjectSqlParams, (resultRow, rowNum) -> {
                ProjectModel p = new ProjectModel();
                p.setProjectId(resultRow.getString("project_id"));
                p.setProjectImageUrl(resultRow.getString("project_image_url"));
                p.setProjectName(resultRow.getString("project_name"));
                p.setDescription(resultRow.getString("description"));
                p.setStatus(resultRow.getString("status"));
                return p;
            });
            if (existingProjects.isEmpty()) {
                response.setMessage("Project is not found.");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            ProjectModel existingProject = existingProjects.get(0);

            String base64Image = projectRequest.getProjectImageUrl();
            if (base64Image != null && !base64Image.isEmpty() && imageHandler.isBase64(base64Image)) {
                String uploadDir = "/home/sornnacha_bur/main/code_projects/keetlo/banner_management/uploads/images/projects/";
                String imageName = "project_image_" + System.currentTimeMillis() + ".webp";
                String filePath = uploadDir + imageName;
                try {
                    imageHandler.saveBase64Image(base64Image, filePath);
                    if(existingProject.getProjectImageUrl() != null && !existingProject.getProjectImageUrl().trim().isEmpty()) {
                        String oldFilePath = "src/main/resources/static" + existingProject.getProjectImageUrl();
                        imageHandler.deleteImage(oldFilePath);
                    }
                } catch (IOException e) {
                    response.setMessage("Failed to save image.");
                    response.setStatus("error");
                    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                projectRequest.setProjectImageUrl("/images/projects/" + imageName);
            }

            int rowsAffected = jdbcTemplate.update(sql,
                    projectRequest.getProjectImageUrl(),
                    projectRequest.getProjectName(),
                    projectRequest.getDescription(),
                    projectRequest.getStatus(),
                    existingProject.getProjectId(),
                    userCode
            );
            if(rowsAffected==0){
                response.setMessage("Something is incorrect");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            actionLogService.log(userCode, "project", "Project Updated", "Project has been updated.");
            response.setMessage("Project " + existingProject.getProjectId() + " updated successfully!");
            response.setStatus("success");
            return  new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Internal Server Error: "+ e.getMessage());
            response.setMessage("Internal Server Error: ");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(ProjectRoutes.PROJECT_DELETE)
    public ResponseEntity<ResponseModel> CreateProject(HttpServletRequest request, @RequestBody ProjectModel projectRequest, @PathVariable("projectId") String projectId) throws IOException {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        String existingProjectSql = "SELECT project_id, " +
                "project_image_url, project_name, description, status " +
                "FROM projects " +
                "WHERE user_code = ? AND project_id = ?";
        Object[] existingProjectSqlParams = new Object[]{
                userCode,
                projectId,
        };

        String sql = "DELETE FROM projects WHERE user_code = ? AND project_id = ?";
        Object[] params = new Object[]{
                userCode,
                projectId,
        };

        String getCampaignsSql = "SELECT campaign_id, campaign_image_url FROM campaigns WHERE project_id = ?";
        String deleteCampaignsSql = "DELETE FROM campaigns WHERE campaign_id = ?";
        Object[] campaignParams = new Object[]{
                projectId,
        };

        String getBannersSql = "SELECT banner_id, image_url FROM banners WHERE campaign_id = ?";
        String deleteBannersSql = "DELETE FROM banners WHERE banner_id = ?";
        try{
            List<ProjectModel> existingProjects = jdbcTemplate.query(existingProjectSql, existingProjectSqlParams, (resultRow, rowNum) -> {
                ProjectModel p = new ProjectModel();
                p.setProjectId(resultRow.getString("project_id"));
                p.setProjectImageUrl(resultRow.getString("project_image_url"));
                p.setProjectName(resultRow.getString("project_name"));
                p.setDescription(resultRow.getString("description"));
                p.setStatus(resultRow.getString("status"));
                return p;
            });
            if (existingProjects.isEmpty()) {
                response.setMessage("Project is not found.");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            ProjectModel existingProject = existingProjects.get(0);

            if(existingProject.getProjectImageUrl() != null && !existingProject.getProjectImageUrl().trim().isEmpty()) {
                String oldFilePath = "src/main/resources/static" + existingProject.getProjectImageUrl();
                imageHandler.deleteImage(oldFilePath);
            }

            //Check the campaigns in the project
            for (ProjectModel project : existingProjects) {
                //Delete the campaigns in the project
                List<CampaignModel> existingCampaigns = jdbcTemplate.query(getCampaignsSql, new Object[]{
                        project.getProjectId(),
                }, (resultRow, rowNum) -> {
                    CampaignModel c = new CampaignModel();
                    c.setCampaignId(resultRow.getString("campaign_id"));
                    c.setCampaignImageUrl(resultRow.getString("campaign_image_url"));
                    return c;
                });
                // Delete the banners and campaignImageUrl in the campaign
                for (CampaignModel campaign : existingCampaigns) {
                    if(campaign.getCampaignImageUrl() != null && !campaign.getCampaignImageUrl().trim().isEmpty()) {
                        String oldFilePath = "src/main/resources/static" + campaign.getCampaignImageUrl();
                        imageHandler.deleteImage(oldFilePath);
                    }
                    int campaignRowsAffected = jdbcTemplate.update(deleteCampaignsSql, campaign.getCampaignId());
                    if(campaignRowsAffected==0){
                        response.setMessage("Something is incorrect");
                        response.setStatus("error");
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }

                    //Delete the banners in the project
                    List<BannerModel> existingBanners = jdbcTemplate.query(getBannersSql, new Object[]{
                            campaign.getCampaignId(),
                    }, (resultRow, rowNum) -> {
                        BannerModel b = new BannerModel();
                        b.setBannerId(resultRow.getString("banner_id"));
                        b.setImageUrl(resultRow.getString("image_url"));
                        return b;
                    });

                    for (BannerModel banner : existingBanners) {
                        if (banner.getImageUrl() != null && !banner.getImageUrl().trim().isEmpty()) {
                            String oldFilePath = "src/main/resources/static" + banner.getImageUrl();
                            imageHandler.deleteImage(oldFilePath);
                        }
                        int bannerRowsAffected = jdbcTemplate.update(deleteBannersSql, banner.getBannerId());
                        if (bannerRowsAffected == 0) {
                            response.setMessage("Something is incorrect");
                            response.setStatus("error");
                            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                        }
                    }

                }

            }

            int rowsAffected = jdbcTemplate.update(sql, params);
            if(rowsAffected==0){
                response.setMessage("Something is incorrect");
                response.setStatus("error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            actionLogService.log(userCode, "project", "Project Deleted", "Project was deleted from the system.");
            response.setMessage("Project " + projectId + " deleted successfully!");
            response.setStatus("success");
            return  new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Internal Server Error: "+ e.getMessage());
            response.setMessage("Internal Server Error: ");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(ProjectRoutes.PROJECT_OPTION)
    public ResponseEntity<ResponseModel> ProjectOption(HttpServletRequest request) {
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        String sql = "SELECT projects.project_id, " +
                "projects.project_name, " +
                "projects.status " +
                "FROM projects " +
                "WHERE user_code = ? " +
                "ORDER BY updated_at DESC";
        Object[] params =  new Object[]{userCode};

        try {
            List<ProjectModel> options = jdbcTemplate.query(sql, params, (resultRow, rowNum) -> {
                ProjectModel project = new ProjectModel();
                project.setProjectId(resultRow.getString("project_id"));
                project.setProjectName(resultRow.getString("project_name"));
                project.setStatus(resultRow.getString("status"));
                return  project;
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
