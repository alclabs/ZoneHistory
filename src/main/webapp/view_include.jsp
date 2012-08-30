<%@ page import="com.controlj.green.addonsupport.web.WebContextFactory" %>
<%@ page import="com.controlj.green.addonsupport.web.WebContext" %>
<%@ page import="com.controlj.green.addonsupport.access.*" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <script type="text/javascript" src="js/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui-1.8.18.custom.min.js"></script>
    <script type="text/javascript" src="js/raphael-min.js"></script>
    <script type="text/javascript" src="js/g.raphael-min.js"></script>
    <script type="text/javascript" src="js/g.pie-min.js"></script>
    <script type="text/javascript" src="js/zone.piechart.js"></script>
    <%
        String loc = "";
        String range = request.getParameter("prevdays");
        if (range == null)
            range = "0";
        String showLegend = request.getParameter("showlegend");
        if (showLegend == null)
            showLegend = "false";
        String showTotal = request.getParameter("showtotal");
        if (showTotal == null)
            showTotal = "false";
        String canvasHeight = request.getParameter("h_ctx");
        String canvasWidth = request.getParameter("w_ctx");

        try
        {
            if (WebContextFactory.hasLinkedWebContext(request))
            {
                // then we might was to initialize so data based off that location
                final WebContext webContext = WebContextFactory.getLinkedWebContext(request);
                SystemConnection systemConnection = DirectAccess.getDirectAccess().getUserSystemConnection(request);
                loc = systemConnection.runReadAction(new ReadActionResult<String>()
                {
                    public String execute(SystemAccess systemAccess) throws Exception
                    {
                        Location location = webContext.getLinkedFromLocation(systemAccess.getTree(SystemTree.Geographic));
                        return location.getTransientLookupString();
                    }
                });
            }
        }
        catch (Exception e) {}
    %>
</head>
<body>
    <div id="graph" class="graph"></div>
    <script type="text/javascript" >
        runColorReport('<%=loc%>', '<%=range%>', true, <%=canvasWidth%>, <%=canvasHeight%>,
                       <%=showLegend.contains("true")%>, <%=showTotal.contains("true")%>);
    </script>
</body>
</html>