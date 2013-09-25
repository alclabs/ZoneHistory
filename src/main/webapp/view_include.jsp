<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.controlj.green.addonsupport.web.WebContextFactory" %>
<%@ page import="com.controlj.green.addonsupport.web.WebContext" %>
<%@ page import="com.controlj.green.addonsupport.access.*" %>
<%@ page import="com.controlj.addon.zonehistory.util.Logging" %>
<%@ page import="com.controlj.green.addonsupport.AddOnInfo" %>

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

        String showOccupied = request.getParameter("showoccupied");
        if (showOccupied == null) showOccupied = "false";

        String showEI = request.getParameter("showei");
        if (showEI == null) showEI = "false";

        String canvasHeight = request.getParameter("h_ctx");
        String canvasWidth = request.getParameter("w_ctx");

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
        catch (Exception e)
        {
            Logging.LOGGER.print(e);
        }
    %>
</head>
<body>
<div id="graph" class="graph"></div>
<table id="detailsTable" cellpadding="0" cellspacing="0">
    <thead></thead>
    <tbody> <tr></tr> </tbody>
</table>

<script type="text/javascript">
    var showLegendTemp = <%=showLegend.contains("true")%>;
    var showCooling = <%=showCooling.contains("true")%>;
    var showHeating = <%=showHeating.contains("true")%>;
    var showOccupied = <%=showOccupied.contains("true")%>;
    var showEI = <%=showEI.contains("true")%>;

    <%-- we need to calcuate the radius of the pie chart as well as its location based on how big the canvas height/width are and
whether the table shows the rows it requires--%>
    var cHeight = <%=canvasHeight%>;
    var cWidth = <%=canvasWidth%>;

    cHeight -= showCooling === true ? 20 : 0;
    cHeight -= showEI === true ? 20 : 0;
    cHeight -= showHeating === true ? 20 : 0;
    cHeight -= showOccupied === true ? 20 : 0;
    cHeight -= showLegendTemp === true ? 80 : 0;
    cWidth -= showLegendTemp === true ? 200 : 0;

    // we need to pass on the values so that we can determine the radius, alert the user, but not use an alert while using raphael...yay
    var radius = (cWidth >= cHeight ? cHeight : cWidth) / 2;
    if (cHeight < 20 || cWidth < 20)
    {
        mainChartPaperLocation = new Raphael("graph", 200, 200).text(0, 0, "Dimensions too small for zonehistory.\nMake this area larger.")
    }
    else
    {
        // initialize raphael paper here to give to zonehistorypiechart

        if (!mainChartPaperLocation)
            mainChartPaperLocation = new Raphael("graph", 0.96*<%=canvasWidth%>, 2 * radius);

        // draw pie chart...pass in its object here in order to initialize and such
        var horizontalCenter = <%=canvasWidth%> / 2;
        var pieChart = new ZoneHistoryPieChart(mainChartPaperLocation, horizontalCenter, radius, radius);
        var isFromGrafxPage = true; // just for verbosity
        var table = new ZoneHistoryTable("detailsTable", isFromGrafxPage, showCooling, showHeating, showOccupied, showEI, 30);

        var report = new DataRetriever(pieChart, table, true);
        report.runReportForData('<%=loc%>', '<%=range%>', showLegendTemp, 'zonehistory');
        mainChartPaperLocation.renderfix();    // fix for IE subpixel rendering
    }
</script>
</body>
</html>