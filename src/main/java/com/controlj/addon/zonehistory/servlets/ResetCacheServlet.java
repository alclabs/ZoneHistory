package com.controlj.addon.zonehistory.servlets;

import com.controlj.addon.zonehistory.cache.ZoneHistoryCache;
import com.controlj.addon.zonehistory.util.Logging;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 */
public class ResetCacheServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Logging.LOGGER.println("Resetting the cache");
        ZoneHistoryCache.CACHE.reset();
        PrintWriter writer = resp.getWriter();
        writer.println("<html><body><h1>Cached data has been cleared.</h1></body></html>");

        // TODO: make in to javascipt alert
    }
}
