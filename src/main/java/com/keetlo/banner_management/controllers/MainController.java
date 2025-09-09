package com.keetlo.banner_management.controllers;

import com.keetlo.banner_management.middlewares.JWTAutherization;
import com.keetlo.banner_management.model.BannerModel;
import com.keetlo.banner_management.model.CampaignModel;
import com.keetlo.banner_management.model.ProjectModel;
import com.keetlo.banner_management.model.ResponseModel;
import com.keetlo.banner_management.routes.MainRoutes;
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
public class MainController {
    private final JdbcTemplate jdbcTemplate;
    private final JWTAutherization jWTAutherization;

    public MainController(JdbcTemplate jdbcTemplate, JWTAutherization jWTAutherization) {
        this.jdbcTemplate = jdbcTemplate;
        this.jWTAutherization = jWTAutherization;
    }

    @GetMapping(MainRoutes.MAIN_ROUTE)
    public String Display(){
        return "Welcome to Keetlo - Banner Management System";
    }

    @GetMapping(MainRoutes.MAIN_DASHBOARD)
    public ResponseEntity<ResponseModel> DisplayDashboard(HttpServletRequest request){
        ResponseModel response = new ResponseModel();
        String userCode = jWTAutherization.JWTHeaderVerification(request);
        if(userCode == null) {
            response.setMessage("Authentication Failed");
            response.setStatus("error");
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        //Kpis
        //Projects
        String projectSql = "SELECT projects.project_id, " +
                "projects.project_image_url, " +
                "projects.project_name, " +
                "projects.description, " +
                "projects.status " +
                "FROM projects " +
                "WHERE user_code = ?";
        //Campaigns
        String campaignSql = "SELECT campaigns.campaign_id, " +
                "campaigns.project_id, " +
                "campaigns.campaign_name, " +
                "campaigns.description, " +
                "campaigns.campaign_image_url, " +
                "campaigns.start_date, " +
                "campaigns.end_date, " +
                "campaigns.status " +
                "FROM campaigns " +
                "JOIN projects ON campaigns.project_id = projects.project_id " +
                "WHERE projects.user_code = ?";
        //Banners
        String bannerSql = "SELECT banners.banner_id, " +
                "banners.campaign_id, " +
                "banners.banner_name," +
                "banners.description," +
                "banners.image_url, " +
                "banners.image_width, " +
                "banners.image_height, " +
                "banners.views, " +
                "banners.clicks, " +
                "banners.link " +
                "FROM banners " +
                "JOIN campaigns ON banners.campaign_id = campaigns.campaign_id " +
                "JOIN projects ON campaigns.project_id = projects.project_id " +
                "WHERE projects.user_code = ?";
        Object[] params = new Object[]{userCode};
        try {
            List<ProjectModel> projects = jdbcTemplate.query(projectSql, params, (resultRow, rowNum) -> {
                ProjectModel project = new ProjectModel();
                project.setProjectId(resultRow.getString("project_id"));
                project.setProjectImageUrl(resultRow.getString("project_image_url"));
                project.setProjectName(resultRow.getString("project_name"));
                project.setDescription(resultRow.getString("description"));
                project.setStatus(resultRow.getString("status"));
                return  project;
            });

            List<CampaignModel> campaigns = jdbcTemplate.query(campaignSql, params, (resultRow, rowNum) -> {
                CampaignModel campaign = new CampaignModel();
                campaign.setCampaignId(resultRow.getString("campaign_id"));
                campaign.setProjectId(resultRow.getString("project_id"));
                campaign.setCampaignName(resultRow.getString("campaign_name"));
                campaign.setDescription(resultRow.getString("description"));
                campaign.setCampaignImageUrl(resultRow.getString("campaign_image_url"));
                campaign.setStartDate(resultRow.getString("start_date"));
                campaign.setEndDate(resultRow.getString("end_date"));
                campaign.setStatus(resultRow.getString("status"));
                return  campaign;
            });

            List<BannerModel> banners = jdbcTemplate.query(bannerSql, params, (resultRow, rowNum) -> {
                BannerModel banner = new BannerModel();
                banner.setBannerId(resultRow.getString("banner_id"));
                banner.setCampaignId(resultRow.getString("campaign_id"));
                banner.setBannerName(resultRow.getString("banner_name"));
                banner.setDescription(resultRow.getString("description"));
                banner.setImageUrl(resultRow.getString("image_url"));
                banner.setImageWidth(resultRow.getInt("image_width"));
                banner.setImageHeight(resultRow.getInt("image_height"));
                banner.setViews(resultRow.getInt("views"));
                banner.setClicks(resultRow.getInt("clicks"));
                banner.setLink(resultRow.getString("link"));
                return  banner;
            });

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("projects", projects);
            responseData.put("campaigns", campaigns);
            responseData.put("banners", banners);

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
