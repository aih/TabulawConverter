package com.tabulaw.config;

import java.io.InputStream;

public class ResourceLoader {
    

    public static String getResourcefolderPath(){
        return ResourceLoader.class.getPackage().getName().replace('.', '/');
    }

    public static InputStream getResourceAsStream(String filename) {
    	final ClassLoader classLoader = ResourceLoader.class.getClassLoader();
        String resourceFolderPath = getResourcefolderPath();
        return classLoader.getResourceAsStream(String.format("%s/%s",resourceFolderPath,filename));
    }

}
