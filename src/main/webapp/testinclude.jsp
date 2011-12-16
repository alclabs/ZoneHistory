<%@ page import="org.jetbrains.annotations.NotNull" %>
<%@ page import="com.controlj.green.addonsupport.access.*" %>
<%@ page import="com.controlj.green.addonsupport.access.aspect.SetPoint" %>
<%@ page import="com.controlj.green.addonsupport.access.util.Acceptors" %>
<%@ page import="com.controlj.green.addonsupport.access.aspect.EquipmentColorTrendSource" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    private void writeAll(final List<SetPoint> setPoints, final PrintWriter writer, SystemConnection connection) throws SystemException, ActionExecutionException
    {
        writer.println("<html>\n" +
                "<div>List of locations</div>\n" +
                "<table>\n" +
                "    <thead>\n" +
                "        <tr>\n" +
                "            <th>Location</th>\n" +
                "            <th>ColorTrend</th>\n" +
                "            <th>Enabled</th>\n" +
                "        </tr>\n" +
                "    </thead>\n" +
                "    <tbody>");
        writeRows(setPoints, writer, connection);
        writer.println("</tbody>\n" +
                "</table>\n" +
                "</html>");
    }

    private void writeRows(final List<SetPoint> setPoints, final PrintWriter writer, SystemConnection connection) throws SystemException, ActionExecutionException {
        connection.runReadAction(new ReadAction() {
            @Override
            public void execute(@NotNull SystemAccess systemAccess) throws Exception {
                for (SetPoint setPoint : setPoints) {
                    writer.println("<tr>");
                    writer.print("<td>");
                    writer.print(setPoint.getLocation().getDisplayPath());
                    writer.print("</td>");

                    writer.print("<td>");
                    writer.print(setPoint.getLocation().hasAspect(EquipmentColorTrendSource.class));
                    writer.print("</td>");

                    writer.print("<td>");
                    writer.print(setPoint.getLocation().hasAspect(EquipmentColorTrendSource.class) ? setPoint.getLocation().getAspect(EquipmentColorTrendSource.class).isEnabled() : "N/A" );
                    writer.print("</td>");

                    writer.println("</tr>");
                }
            }
        });
    }
%>
<%
    SystemConnection connection = DirectAccess.getDirectAccess().getUserSystemConnection(request);
    final ArrayList<SetPoint> setPoints = new ArrayList<SetPoint>();

    connection.runReadAction(new ReadAction() {
        @Override
        public void execute(@NotNull SystemAccess access) throws Exception {
            Tree geoTree = access.getTree(SystemTree.Geographic);
            Location root = geoTree.getRoot();

            setPoints.addAll(root.find(SetPoint.class, Acceptors.<SetPoint>acceptAll()));
            Collections.sort(setPoints, new Comparator<SetPoint>() {
                @Override
                public int compare(SetPoint setPoint, SetPoint setPoint1) {
                    return setPoint.getLocation().getDisplayPath().compareTo(setPoint1.getLocation().getDisplayPath());
                }
            });
        }
    });

    PrintWriter writer = response.getWriter();
    writeAll(setPoints, writer, connection);
    writer.flush();
%>
