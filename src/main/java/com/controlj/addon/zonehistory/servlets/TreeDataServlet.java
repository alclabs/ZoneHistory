/*
 * Copyright (c) 2011 Automated Logic Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.controlj.addon.zonehistory.servlets;

import com.controlj.addon.zonehistory.SetpointWithEnabledColorTrendAcceptor;
import com.controlj.addon.zonehistory.util.Logging;
import com.controlj.addon.zonehistory.util.TreeIcon;
import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.access.aspect.SetPoint;
import com.controlj.green.addonsupport.access.util.LocationSort;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TreeDataServlet extends HttpServlet
{
   @Override protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
         throws ServletException, IOException
   {
      resp.setContentType("application/json");

      try
      {
         SystemConnection connection = DirectAccess.getDirectAccess().getUserSystemConnection(req);
         connection.runReadAction(new ReadAction()
         {
            @Override
            public void execute(@NotNull SystemAccess access) throws Exception
            {
               Tree geoTree = access.getTree(SystemTree.Geographic);
               Collection<Location> treeChildren = getEntries(geoTree, req.getParameter("key"));
               JSONArray arrayData = toJSON(treeChildren);
               arrayData.write(resp.getWriter());
            }
         });
      }
      catch (Exception e)
      {
         e.printStackTrace(Logging.LOGGER);
      }
   }

   private Collection<Location> getEntries(Tree tree, String lookupString)
   {
      if (lookupString == null)
         return getRoot(tree);

      try
      {
         return getChildren(tree.resolve(lookupString));
      }
      catch (UnresolvableException e)
      {
         e.printStackTrace(Logging.LOGGER);
         return Collections.emptyList();
      }
   }

   private Collection<Location> getRoot(Tree tree)
   {
      return Collections.singleton(tree.getRoot());
   }

   private Collection<Location> getChildren(Location location)
   {
      AspectAcceptor<SetPoint> acceptor = new SetpointWithEnabledColorTrendAcceptor();

      List<Location> treeChildren = new ArrayList<Location>();
      for (Location child : location.getChildren(LocationSort.PRESENTATION))
      {
         if (child.has(SetPoint.class, acceptor))
            treeChildren.add(child);
      }
      return treeChildren;
   }

   private String getIconForType(LocationType type)
   {
      return TreeIcon.findIcon(type).getImageUrl();
   }

   private JSONArray toJSON(Collection<Location> treeEntries) throws JSONException
   {
      JSONArray arrayData = new JSONArray();
      for (Location location : treeEntries)
      {
         JSONObject next = new JSONObject();

         next.put("title", location.getDisplayName());
         next.put("key", location.getTransientLookupString());
         next.put("path", location.getDisplayPath());

         if (location.getType() != LocationType.Equipment && location.getChildren().size() > 0)
         {
            next.put("hideCheckbox", true);
            next.put("isLazy", true);
         }
         else
            next.put("hideCheckbox", false);

         next.put("icon", getIconForType(location.getType()));
         arrayData.put(next);
      }
      return arrayData;
   }
}