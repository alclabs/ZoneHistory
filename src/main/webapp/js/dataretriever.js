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

        mainChartPaperLocation.clear();
        zoneHistoryTable.clearTable();
        mainChartPaperLocation.text((mainChartPaperLocation.height / 3), (mainChartPaperLocation.width / 4), "Loading...").attr({fill: "#fff"});

        // run report and draw chart/table when done or display an error
        $.ajax({
                    url: "servlets/results",
                    dataType: 'json',
                    timeout: 300000,
                    data: { "location":location, "prevdays":daysSinceToday, "isFromGfxPge": isFromGrafxPage},
                    success: function(data)
                    {
                        zoneHistoryPiechart.renderChart(data.mainChart.colors, drawLegend);
                        zoneHistoryTable.renderTable(data.table);
                    },
                    error:  function(a, textStatus, error)
                    {
                        alert(error);
                    }
                });


//        $.getJSON("servlets/results", { "location":location, "prevdays":daysSinceToday, "isFromGfxPge": isFromGrafxPage},
//                function(data)
//                {
//                    zoneHistoryPiechart.renderChart(data.mainChart.colors, drawLegend);
//                    zoneHistoryTable.renderTable(data.table);
//
//                }).error(function (a, textStatus, error)
//                {
//                    alert(error);
//                });
    }
}
