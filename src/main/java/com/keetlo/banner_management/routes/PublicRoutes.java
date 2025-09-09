package com.keetlo.banner_management.routes;

public class PublicRoutes {
    public static final String PUBLIC_PREFIX = "/public";
    public static final String PUBLIC_BANNER_INSIDE = PUBLIC_PREFIX+"/inside/campaigns/{campaignId}/banners";
    public static final String PUBLIC_BANNER = PUBLIC_PREFIX+"/campaigns/{campaignId}/banners";
    public static final String PUBLIC_TRACK_VIEW = PUBLIC_PREFIX+"/track/view";
    public static final String PUBLIC_TRACK_CLICK = PUBLIC_PREFIX+"/track/click";
}
