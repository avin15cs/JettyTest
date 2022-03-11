package com;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.xml.XmlConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class JettyTest {


        private static List<Resource> xmls;
        private static Map<String, Object> idMap;
        private static Server myServer;
        private static String myJettyDir = "C://jetty";
        private static Path myJettyBase;
        private static boolean enableHttps= false;
        public static void main(String[] args) throws Exception {

            System.setProperty("org.eclipse.jetty.LEVEL", "DEBUG");
            System.setProperty("jetty.home",myJettyDir);
            loadJettyXmlConfigurations();
        try {
            idMap = configure(xmls);
            myServer = (Server) idMap.get("Server");
//            myServer.setDumpAfterStart(true);
            myServer.start();
        }
        catch(InvocationTargetException ex){
            System.out.println("Error");
        }

        }

    private static void loadJettyXmlConfigurations() {
        xmls = new ArrayList<>();
        myJettyBase = Paths.get(myJettyDir+"/base");
        xmls.add(new PathResource(myJettyBase.resolve("jetty-bytebufferpool.xml")));
        xmls.add(new PathResource(myJettyBase.resolve("jetty-threadpool.xml")));
        xmls.add(new PathResource(myJettyBase.resolve("jetty.xml")));
        xmls.add(new PathResource(myJettyBase.resolve("jetty-http.xml")));

        if(enableHttps) {
            xmls.add(new PathResource(myJettyBase.resolve("jetty-ssl.xml")));
            xmls.add(new PathResource(myJettyBase.resolve("jetty-ssl-context.xml")));
            xmls.add(new PathResource(myJettyBase.resolve("jetty-https.xml")));
        }
        xmls.add(new PathResource(myJettyBase.resolve("jetty-customrequestlog.xml")));
        xmls.add(new PathResource(myJettyBase.resolve("sample.xml")));
    }

    /**
         * Configure for the list of XML Resources and Properties.
         *
         * @param xmls the xml resources (in order of execution)
         * @return the ID Map of configured objects (key is the id name in the XML, and the value is configured object)
         */
    private static Map<String, Object> configure(List<Resource> xmls)
    {
        Map<String, String> properties = loadProperties(myJettyBase.resolve("jetty.properties"));
        properties.put("jetty.base", myJettyDir);
        Map<String, Object> idMap = new HashMap<>();

        try {
            for (Resource xmlResource : xmls) {
                if(!xmlResource.exists()){
                    System.out.println( "Unable to start web server -- "+ xmlResource + " is missing!" );
                    throw new RuntimeException( "Unable to start web server -- " + xmlResource + " is missing!" );
                }
                XmlConfiguration configuration = new XmlConfiguration(xmlResource);
                configuration.getIdMap().putAll(idMap);
                configuration.getProperties().putAll(properties);
                configuration.configure();
                idMap.putAll(configuration.getIdMap());
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }

        return idMap;
    }

    private static Map<String, String> loadProperties(Path path)
    {
        Properties properties = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8))
        {
            properties.load(reader);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return properties.entrySet().stream().collect(
                Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        e -> String.valueOf(e.getValue()),
                        (prev, next) -> next, HashMap::new
                ));
    }
}

