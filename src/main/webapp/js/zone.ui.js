var tableData, reverse = true;

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
        clearTable();
        drawTable(sortByName(tableData), 30);
    });

    $("#heatingpercent").click(function()
    {
        clearTable();
        drawTable(sortByAttribute(tableData, "heatingpercent"), 30);
    });

    $("#coolingpercent").click(function()
    {
        clearTable();
        drawTable(sortByAttribute(tableData, "coolingpercent"), 30);
    });

    $("#operationalpercent").click(function()
    {
        clearTable();
        drawTable(sortByAttribute(tableData, "operationalpercent"), 30);
    });

    $("#averageEI").click(function()
    {
        clearTable();
        drawTable(sortByAttribute(tableData, "averageEI"), 30);
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
    clearTable();

    // get active tab to determine which test to run
    var location = getActiveNodeKey();
    if (location)
        runColorReport(location, getTimeRange(), false, 500, 375, true, true); // TODO - remove reportCombo ref
}

function sortByName(data)
{
    data = data.sort(function(a, b)
    {
        var eq1, eq2;
        if (reverse)
        {
            eq1 = a["eqDisplayName"];
            eq2 = b["eqDisplayName"];
        }
        else
        {
            eq1 = b["eqDisplayName"];
            eq2 = a["eqDisplayName"];
        }

        if (eq1 < eq2)
            return -1;
        if (eq1 > eq2)
            return 1;
        return 0;
    });

    reverse = !reverse;
    return data;
}

function sortByAttribute(data, propertyName)
{
    data = data.sort(function(a, b)
    {
        var var1 = a[propertyName];
        var var2 = b[propertyName];

        if (reverse)
            return var1 - var2;
        else
            return var2 - var1;

    });

    reverse = !reverse;
    return data;
}

function clearTable()
{
    var table = document.getElementById("detailsTable");
    while (table.rows.length > 1)
        table.deleteRow(table.rows.length - 1);
    $("#zoneDetails").hide();
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
