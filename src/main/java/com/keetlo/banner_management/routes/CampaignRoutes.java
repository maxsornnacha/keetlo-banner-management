package com.keetlo.banner_management.routes;

public class CampaignRoutes {
    public static final String CAMPAIGN_PREFIX = "/campaign";
    public static final String CAMPAIGN_OPTION = CAMPAIGN_PREFIX+"/options";
    public  static  final String CAMPAIGN =  CAMPAIGN_PREFIX+"/{campaignId}";
    public  static  final  String CAMPAIGN_CREATE = CAMPAIGN_PREFIX+"/create";
    public  static  final  String CAMPAIGN_UPDATE = CAMPAIGN_PREFIX+"/update/{campaignId}";
    public  static  final  String CAMPAIGN_DELETE = CAMPAIGN_PREFIX+"/delete/{campaignId}";
}
