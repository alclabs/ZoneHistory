var mainChartPaperLocation;

function DataRetriever(zhPie, zhTable, isFromGraphicsPage)
{
    var zoneHistoryPiechart = zhPie;
    var zoneHistoryTable = zhTable;
    var isFromGrafxPage = isFromGraphicsPage;

    this.runReportForData = function(location, daysSinceToday, drawLegend)
    {
        // defensive
        if (!location)
        {
            alert("Please select a node.");
            return;
        }
        mainChartPaperLocation.text(10, 50, "Loading...");

        // run report and draw chart/table when done or display an error
        $.getJSON("servlets/results", { "location":location, "prevdays":daysSinceToday, "isFromGfxPge": isFromGrafxPage},
                function(data)
                {
                    zoneHistoryPiechart.renderChart(data.mainChart.colors, drawLegend);
                    zoneHistoryTable.renderTable(data.table);

                }).error(function (a, textStatus, error)
                {
                    alert(error);
                });
    }

}
