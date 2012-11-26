//var tableData, reverse = true;
var table;
$(function()
{
    // Attach the dynatree widget to an existing <div id="tree"> element
    // and pass the tree options as an argument to the dynatree() function:
    $("#tree").dynatree(
            {
                title: "System",
                selectMode:1,
                autoCollapse:true,
                autoFocus:false,

                initAjax:
                {
                    url: "servlets/treedata"
                },

                onLazyRead: function(dtnode)
                {
                    dtnode.appendAjax({
                                url:"servlets/treedata",
                                data: {
                                    key:dtnode.data.key
                                }
                            })
                },

                onActivate: function(dtnode)
                {
                    if (dtnode.hasChildren)
                        dtnode.expand();
                    runReport();
                },

                //onPostInit : treeInitialized, // see comment in treeInitialized below

                cache: false
            }
    );

    $("#dateCombo").simpleCombo().change(function()
    {
        runReport();
    });

    $("#equipmentLocation").click(function()
    {
        table.clearTable();
        table.sortByName();
    });

    $("#heatingpercent").click(function()
    {
        table.clearTable();
        table.sortByAttribute("heatingpercent");
    });

    $("#coolingpercent").click(function()
    {
        table.clearTable();
        table.sortByAttribute("coolingpercent");
    });

    $("#operationalpercent").click(function()
    {
        table.clearTable();
        table.sortByAttribute("operationalpercent");
    });

    $("#averageEI").click(function()
    {
        table.clearTable();
        table.sortByAttribute("eivalue");
    });

});

// Will be used later to allow links from charts on a graphic to the whole application
function treeInitialized()
{
    if (location.search)
    {
        var splits = location.split("=", 2);
        if (splits.length === 2)
        {
            if (splits[0] === "loc")
            {
                jumpToTreeLocation(splits[1]);
            }
        }
    }
}

function jumpToTreeLocation(keyPath)
{
    var dynatree = $('#tree').dynatree('getTree');
    dynatree.loadKeyPath(keyPath, function(node, status)
    {
        if (status == 'ok')
        {
            node.activate();
        }
    });
}

function runReport()
{
    $("#welcome").css('display', 'none');

    // get active tab to determine which test to run
    var location = getActiveNodeKey();
    if (location)
    {
        if (!mainChartPaperLocation)
            mainChartPaperLocation = new Raphael("graph", 500, 500);

        var pieChart = new ZoneHistoryPieChart(mainChartPaperLocation, 140, 150, 150);
        table = new ZoneHistoryTable("detailsTable", true, true, true, true, true, 30);

        var report = new DataRetriever(pieChart, table, false);
        report.runReportForData(location, getTimeRange(), true);
    }
}

function getActiveNodeKey()
{
    var node = $('#tree').dynatree('getActiveNode');
    if (node)
        return node.data.key;
    else
        return null;
}

function getTimeRange()
{
    return $('#dateCombo').val();
}
