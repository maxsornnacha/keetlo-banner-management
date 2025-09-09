package com.keetlo.banner_management.routes;

public class BannerRoutes {
    public static final String BANNER_PREFIX = "/banner";
    public  static  final String BANNER =  BANNER_PREFIX+"/{bannerId}";
    public  static  final  String BANNER_CREATE = BANNER_PREFIX+"/create";
    public  static  final  String BANNER_UPDATE = BANNER_PREFIX+"/update/{bannerId}";
    public  static  final  String BANNER_DELETE = BANNER_PREFIX+"/delete/{bannerId}";
}
