package com.keetlo.banner_management.controllers;

import com.keetlo.banner_management.model.BannerModel;
import com.keetlo.banner_management.model.PublicModel;
import com.keetlo.banner_management.model.ResponseModel;
import com.keetlo.banner_management.routes.PublicRoutes;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class PublicController {
    private final JdbcTemplate jdbcTemplate;

    public PublicController(JdbcTemplate jdbcTemplate) {  this.jdbcTemplate = jdbcTemplate; }

    @GetMapping(PublicRoutes.PUBLIC_BANNER_INSIDE)
    public ResponseEntity<ResponseModel> insideBanners(@PathVariable String campaignId, @RequestParam String key) {
        ResponseModel response = new ResponseModel();
        String getPublicKeySql = """
      SELECT system_api_settings.public_key,
             projects.status AS project_status, campaigns.status AS campaign_status,
             campaigns.start_date, campaigns.end_date 
      FROM campaigns
      JOIN projects ON
       campaigns.project_id = projects.project_id
      JOIN system_settings ON
        projects.user_code = system_settings.user_code 
      JOIN system_api_settings ON
        system_settings.system_setting_id = system_api_settings.system_setting_id
      WHERE campaigns.campaign_id = ?
      ORDER BY campaigns.updated_at DESC
    """;
        Object[] params = new Object[]{campaignId};
        List<PublicModel> publicList = jdbcTemplate.query(getPublicKeySql, params, (rowResult, rowNum) -> {
            PublicModel publicModel = new PublicModel();
            publicModel.setPublicKey(rowResult.getString("public_key"));
            publicModel.setCampaignStatus(rowResult.getString("campaign_status"));
            publicModel.setProjectStatus(rowResult.getString("project_status"));
            publicModel.setStartDate(rowResult.getString("start_date"));
            publicModel.setEndDate(rowResult.getString("end_date"));
            return publicModel;
        });

        if (publicList.isEmpty()) {
            response.setMessage("Public key is invalid");
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
        PublicModel publicData = publicList.get(0);
        if(!publicData.getPublicKey().equals(key)){
            response.setMessage("Public key is invalid");
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
        if(publicData.getProjectStatus().equals("inactive")){
            response.setMessage("The Project is inactive");
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if(publicData.getCampaignStatus().equals("inactive")){
            response.setMessage("The Campaign is inactive");
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if(!publicData.isDateInRange(publicData.getStartDate(), publicData.getEndDate())){
            response.setMessage("The current date is outside the allowed range. Please check the start and end dates of the campaign.");
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String sql = """
      SELECT banners.banner_id, banners.banner_name, banners.image_url, banners.image_width, banners.image_height, banners.link 
      FROM banners
      WHERE banners.campaign_id = ?
      ORDER BY banners.updated_at DESC
    """;
        List<BannerModel> banners = jdbcTemplate.query(sql, params,  (rs, i) -> {
            BannerModel banner = new BannerModel();
            banner.setBannerId(rs.getString("banner_id"));
            banner.setBannerName(rs.getString("banner_name"));
            banner.setImageUrl(rs.getString("image_url"));
            banner.setImageWidth(rs.getInt("image_width"));
            banner.setImageHeight(rs.getInt("image_height"));
            banner.setLink(rs.getString("link"));
            return  banner;
        });
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("banners", banners);
        response.setStatus("success");
        response.setData(responseData);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(PublicRoutes.PUBLIC_BANNER)
    public ResponseEntity<ResponseModel> banners(HttpServletRequest request, @PathVariable String campaignId, @RequestParam String key) {
        ResponseModel response = new ResponseModel();
        String getPublicKeySql = """
      SELECT system_api_settings.public_key,
             projects.status AS project_status, campaigns.status AS campaign_status,
             campaigns.start_date, campaigns.end_date 
      FROM campaigns
      JOIN projects ON
       campaigns.project_id = projects.project_id
      JOIN system_settings ON
        projects.user_code = system_settings.user_code 
      JOIN system_api_settings ON
        system_settings.system_setting_id = system_api_settings.system_setting_id
      WHERE campaigns.campaign_id = ?
      ORDER BY campaigns.updated_at DESC
    """;
        Object[] params = new Object[]{campaignId};
        List<PublicModel> publicList = jdbcTemplate.query(getPublicKeySql, params, (rowResult, rowNum) -> {
            PublicModel publicModel = new PublicModel();
            publicModel.setPublicKey(rowResult.getString("public_key"));
            publicModel.setCampaignStatus(rowResult.getString("campaign_status"));
            publicModel.setProjectStatus(rowResult.getString("project_status"));
            publicModel.setStartDate(rowResult.getString("start_date"));
            publicModel.setEndDate(rowResult.getString("end_date"));
            return publicModel;
        });

        if (publicList.isEmpty()) {
            response.setMessage("Public key is invalid");
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
        PublicModel publicData = publicList.get(0);
        if(!publicData.getPublicKey().equals(key)){
            response.setMessage("Public key is invalid");
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
        if(publicData.getProjectStatus().equals("inactive")){
            response.setMessage("The Project is inactive");
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if(publicData.getCampaignStatus().equals("inactive")){
            response.setMessage("The Campaign is inactive");
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if(!publicData.isDateInRange(publicData.getStartDate(), publicData.getEndDate())){
            response.setMessage("The current date is outside the allowed range. Please check the start and end dates of the campaign.");
            response.setStatus("error");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String sql = """
      SELECT banners.banner_id, banners.banner_name, banners.image_url, banners.image_width, banners.image_height, banners.link 
      FROM banners
      WHERE banners.campaign_id = ?
      ORDER BY banners.updated_at DESC
    """;
        List<BannerModel> banners = jdbcTemplate.query(sql, params,  (rs, i) -> {
            String origin = request.getScheme() + "://" + request.getServerName()
                    + (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort());

            BannerModel banner = new BannerModel();
            banner.setBannerId(rs.getString("banner_id"));
            banner.setBannerName(rs.getString("banner_name"));
            banner.setImageUrl(rs.getString("image_url"));
            banner.setImageWidth(rs.getInt("image_width"));
            banner.setImageHeight(rs.getInt("image_height"));
            banner.setLink(rs.getString("link"));
            if(banner.getLink() != null){
               String link =  origin+"/public/track/click?bannerId="+banner.getBannerId()+"&key="+key;
               banner.setLink(link);
            }
            if(banner.getImageUrl() != null){
                String image = origin+banner.getImageUrl();
                banner.setImageUrl(image);
            }
            return  banner;
        });
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("banners", banners);
        response.setStatus("success");
        response.setData(responseData);
        jdbcTemplate.update("UPDATE banners SET views = COALESCE(views,0) + 1 WHERE campaign_id = ?", campaignId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

        @PostMapping(PublicRoutes.PUBLIC_TRACK_VIEW)
        public ResponseEntity<Void> view(@RequestParam String bannerId, @RequestParam String key) {
        try {
            // validate
            String getValidateSql = """
              SELECT system_tracking_and_analytic_settings.track_views ,system_api_settings.public_key FROM banners
              JOIN campaigns ON
               banners.campaign_id = campaigns.campaign_id
              JOIN projects ON
               campaigns.project_id = projects.project_id
              JOIN system_settings ON
                projects.user_code = system_settings.user_code 
              JOIN system_api_settings ON
                system_settings.system_setting_id = system_api_settings.system_setting_id
              JOIN system_tracking_and_analytic_settings ON 
                system_settings.system_setting_id = system_tracking_and_analytic_settings.system_setting_id                                      
              WHERE banners.banner_id = ?
              ORDER BY banners.updated_at DESC
            """;
            Object[] params = new Object[]{bannerId};
            List<PublicModel> publicList = jdbcTemplate.query(getValidateSql, params, (rowResult, rowNum) -> {
                PublicModel publicModel = new PublicModel();
                publicModel.setPublicKey(rowResult.getString("public_key"));
                publicModel.setTrackViews(rowResult.getInt("track_views"));
                return publicModel;
            });

            if (publicList.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            PublicModel publicData = publicList.get(0);
            if (!publicData.getPublicKey().equals(key)) {
                return ResponseEntity.noContent().build();
            }
            if (publicData.getTrackViews() == 0) {
                return ResponseEntity.noContent().build();
            }
            jdbcTemplate.update("UPDATE banners SET views = COALESCE(views,0) + 1 WHERE banner_id = ?", bannerId);
            return ResponseEntity.noContent().build();
        }catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.noContent().build();
        }
        }

        // Click via redirect (so target site gets opened)
        @GetMapping(PublicRoutes.PUBLIC_TRACK_CLICK)
        public ResponseEntity<Void> click(@RequestParam String bannerId, @RequestParam String key) {
            try {
            // validate
            String getValidateSql = """
              SELECT system_tracking_and_analytic_settings.track_clicks ,system_api_settings.public_key FROM banners
              JOIN campaigns ON
               banners.campaign_id = campaigns.campaign_id
              JOIN projects ON
               campaigns.project_id = projects.project_id
              JOIN system_settings ON
                projects.user_code = system_settings.user_code 
              JOIN system_api_settings ON
                system_settings.system_setting_id = system_api_settings.system_setting_id
              JOIN system_tracking_and_analytic_settings ON 
                system_settings.system_setting_id = system_tracking_and_analytic_settings.system_setting_id                                      
              WHERE banners.banner_id = ?
              ORDER BY banners.updated_at DESC
            """;
            Object[] params = new Object[]{bannerId};
            List<PublicModel> publicList = jdbcTemplate.query(getValidateSql, params, (rowResult, rowNum) -> {
                PublicModel publicModel = new PublicModel();
                publicModel.setPublicKey(rowResult.getString("public_key"));
                publicModel.setTrackClicks(rowResult.getInt("track_clicks"));
                return publicModel;
            });

            if (publicList.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            System.out.println(2);
            PublicModel publicData = publicList.get(0);
            if (!publicData.getPublicKey().equals(key)) {
                return ResponseEntity.noContent().build();
            }
            if (publicData.getTrackClicks() == 0) {
                return ResponseEntity.noContent().build();
            }

            var link = jdbcTemplate.queryForObject("SELECT link FROM banners WHERE banner_id = ?", String.class, bannerId);
            jdbcTemplate.update("UPDATE banners SET clicks = COALESCE(clicks,0) + 1 WHERE banner_id = ?", bannerId);
            return ResponseEntity.status(302).header("Location", link != null ? link : "about:blank").build();
            }catch (Exception e) {
                System.err.println(e.getMessage());
                return ResponseEntity.noContent().build();
            }
        }

}
