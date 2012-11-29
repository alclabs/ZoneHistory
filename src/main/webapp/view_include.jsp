<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.controlj.green.addonsupport.web.WebContextFactory" %>
<%@ page import="com.controlj.green.addonsupport.web.WebContext" %>
<%@ page import="com.controlj.green.addonsupport.access.*" %>
<%@ page import="com.controlj.addon.zonehistory.util.Logging" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <link rel="stylesheet" type="text/css" href="css/smoothness/jquery-ui-1.8.9.custom.css"/>
    <link rel='stylesheet' type='text/css' href='skin/ui.dynatree.css'/>
    <link rel='stylesheet' type='text/css' href='skin/jquery.svg.css'/>
    <link rel='stylesheet' type='text/css' href="css/ui.grafxpage.css"/>

    <script type="text/javascript" src="js/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui-1.8.18.custom.min.js"></script>
    <script type="text/javascript" src="js/jquery.sparkline.min.js"></script>
    <script type="text/javascript" src="js/raphael-min.js"></script>
    <script type="text/javascript" src="js/g.raphael-min.js"></script>
    <script type="text/javascript" src="js/g.pie-min.js"></script>
    <script type="text/javascript" src="js/piechart.js"></script>
    <script type="text/javascript" src="js/zh_table.js"></script>
    <script type="text/javascript" src="js/dataretriever.js"></script>

    <%
        String loc = "";

        String range = request.getParameter("prevdays");
        if (range == null) range = "0";

        String showLegend = request.getParameter("showlegend");
        if (showLegend == null) showLegend = "false";

        String showCooling = request.getParameter("showcooling");
        if (showCooling == null) showCooling = "false";

        String showHeating = request.getParameter("showheating");
        if (showHeating == null) showHeating = "false";

        String showOperational = request.getParameter("showoperational");
        if (showOperational == null) showOperational = "false";

        String showEI = request.getParameter("showei");
        if (showEI == null) showEI = "false";

        String canvasHeight = request.getParameter("h_ctx");
        String canvasWidth = request.getParameter("w_ctx");

        String posXStr = request.getParameter("pos-x");
        if (posXStr == null) posXStr = "-1";
        Integer positionX = Integer.parseInt(posXStr);

        String posYStr = request.getParameter("pos-y");
        if (posYStr == null) posYStr = "-1";
        Integer positionY = Integer.parseInt(posYStr);

        String radius = request.getParameter("radius");
        if (radius == null) radius = "75";
        Integer pieRadius = Integer.parseInt(radius);

        try
        {
            if (WebContextFactory.hasLinkedWebContext(request))
            {
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
        catch (Exception e) {
            Logging.LOGGER.print(e);
        }
    %>
</head>
<body>
    <div id="graph" class="graph"></div>
        <table id="detailsTable" cellpadding="0" cellspacing="0">
            <thead>
                <%--<c:if test="<%=showHeating.contains("true")%>"> <th id="heatingpercent"></th>  </c:if>--%>
                <%--<c:if test="<%=showCooling.contains("true")%>"> <th id="coolingpercent"></th> </c:if>--%>
                <%--<c:if test="<%=showOperational.contains("true")%>"> <th id="operationalpercent"></th> </c:if>--%>
                <%--<c:if test="<%=showEI.contains("true")%>"> <th id="averageEI"></th> </c:if>--%>
            </thead>
            <tbody>
            <tr>

            </tr>
            </tbody>
        </table>

    <script type="text/javascript" >
        var showLegendTemp = <%=showLegend.contains("true")%>;
        var xcoord = <%=positionX%>;
        var ycoord = <%=positionY%>;
        var radius = <%=pieRadius%>;
        var diameter = 2 * radius;
        var showCooling = <%=showCooling.contains("true")%>;
        var showHeating = <%=showHeating.contains("true")%>;
        var showOperational = <%=showOperational.contains("true")%>;
        var showEI = <%=showEI.contains("true")%>;

        // initialize raphael paper here to give to zonehistorypiechart
        if (!mainChartPaperLocation)
            mainChartPaperLocation = new Raphael("graph", diameter + (showLegendTemp === true ? 150 : 0), diameter);

        // draw pie chart...pass in its object here in order to initialize and such
        var pieChart = new ZoneHistoryPieChart(mainChartPaperLocation, xcoord, ycoord, radius);
        var table = new ZoneHistoryTable("detailsTable", true, showCooling, showHeating, showOperational, showEI, 30);

        var report = new DataRetriever(pieChart, table, true);
        report.runReportForData('<%=loc%>', '<%=range%>', showLegendTemp);
    </script>
</body>
</html>