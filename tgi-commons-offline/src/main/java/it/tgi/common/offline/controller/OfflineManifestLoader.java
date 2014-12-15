package it.tgi.common.offline.controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

public class OfflineManifestLoader extends HttpServlet {

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            response.setContentType("text/cache-manifest");

            String dir = "WEB-INF";
            String realPath = request.getSession().getServletContext().getRealPath(dir);
            List<String> cacheList = getCacheList(realPath, "app");

            List<String> manifestContext = readFile(realPath + "/" + "offline.manifest.template");
            PrintWriter out = response.getWriter();
            boolean excludeListMode = false;
            List<String> excludeList = new LinkedList<>();
            for (String manifestLine : manifestContext) {
                if ("# EXCLUDE LIST".equals(manifestLine))
                    excludeListMode = true;

                if (!excludeListMode)
                    out.append(manifestLine).append("\n");
                else {
                    if ("".equals(manifestLine.trim()))
                        excludeListMode = false;
                    else
                        excludeList.add(manifestLine.split("\\#")[1].trim());
                }

                if ("CACHE:".equals(manifestLine)) {
                    for (String file : cacheList) {
                        if (!excludeList.contains(file))
                            out.append(file).append("\n");
                    }
                }
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private List<String> readFile(String pathFile) throws Exception {
        List<String> fileContent = new LinkedList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(pathFile));
            String line;
            while ((line = br.readLine()) != null)
                fileContent.add(line);
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (Exception e) {
            }
        }
        return fileContent;
    }

    private List<String> getCacheList(String realPath, String dir) throws IOException {
        List<String> cacheList = new LinkedList<>();

        File contextRoot = new File(realPath + "/" + dir);
        List<String> fileList = getFileList(contextRoot);
        for (String file : fileList) {
            String fileName = file.substring(realPath.length() + 1, file.length());
            fileName = URLEncoder.encode(fileName, "UTF-8");
            fileName = fileName.replaceAll("%2F", "/");
            fileName = fileName.replaceAll("\\+", "%20");
            cacheList.add(fileName);
        }
        return cacheList;
    }

    private List<String> getFileList(File file) throws IOException {
        List<String> fileList = new LinkedList<>();
        if (file != null && file.isDirectory()) {
            for (File subFile : file.listFiles())
                fileList.addAll(getFileList(subFile));
        } else
            fileList.add(file.getCanonicalPath());
        return fileList;
    }

}