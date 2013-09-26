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

                onPostInit : treeInitialized, // see comment in treeInitialized below

                cache: false
            }
    );

    $("th.sortable").click(function() {
        table.clearTable();

        var th = $(this);
        var increasing = false;
        if (th.hasClass('down'))
        {
            increasing = false;
        } else if (th.hasClass('up')) {
            increasing = true;
        } else {
            increasing = (th.attr('defsort') != 'inc')
        }
        $("th.sortable").removeClass('down up');

        increasing = !increasing;
        th.addClass(increasing ? 'up' : 'down');
        table.sortByAttribute($(this).attr('propname'), increasing);
    });

    var params = $.deparam.querystring();

    function treeInitialized()
    {
        if (params.locationPath)
        {
            jumpToTreeLocation(params.locationPath);
        }
    }

    var prevdays = params.prevdays;
    if ($.inArray(prevdays, ['0','1','7','31']) != -1 ) {
        $('#dateCombo').val(prevdays);
    }

    $("#dateCombo").simpleCombo().change(function()
    {
        runReport();
    });
});


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
            mainChartPaperLocation = new Raphael("graph", 500, 350);

        $("th.sortable").removeClass('down up');

        var pieChart = new ZoneHistoryPieChart(mainChartPaperLocation, 150, 150, 150);
        table = new ZoneHistoryTable("detailsTable", false, true, true, true, true, true, 30);

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
