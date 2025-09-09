package com.keetlo.banner_management.routes;

public class ProjectRoutes {
    public static final String PROJECT_PREFIX = "/project";
    public static final String PROJECT_OPTION = PROJECT_PREFIX+"/options";
    public  static  final String PROJECT =  PROJECT_PREFIX+"/{projectId}";
    public  static  final  String PROJECT_CREATE = PROJECT_PREFIX+"/create";
    public  static  final  String PROJECT_UPDATE = PROJECT_PREFIX+"/update/{projectId}";
    public  static  final  String PROJECT_DELETE = PROJECT_PREFIX+"/delete/{projectId}";
}
