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

                cache: false
            }
    );
    $("#dateCombo").simpleCombo().change(
            function()
            {
                runReport();
            }
    );

    $("#equipmentLocation").click(function()
    {
        clearTable();
        drawTable(sortByName(tableData), 30);
    });

    $("#satisfaction").click(function()
    {
        clearTable();
        drawTable(sortBySatisfaction(tableData), 30);
    })

});

function runReport()
{
   $("#welcome").css('display','none');
   clearTable();

   var location = getActiveNodeKey();
   if (location)
      runColorReport(location, getTimeRange(), false, 640, 430, true, true);
   else
      clearPie();
}

function sortByName(data)
{
    if (reverse)
    {
        data = data.sort(function(a, b)
        {
            var eq1 = a["eqDisplayName"];
            var eq2 = b["eqDisplayName"];
            if (eq1 < eq2)
                return -1;
            if (eq1 > eq2)
                return 1;
            return 0;
        });
    }
    else
    {
         data = data.sort(function(a, b)
        {
            var eq1 = a["eqDisplayName"];
            var eq2 = b["eqDisplayName"];
            if (eq1 > eq2)
                return -1;
            if (eq1 < eq2)
                return 1;
            return 0;
        });
    }

    reverse = !reverse;
    return data;
}

function sortBySatisfaction(data)
{
    if (reverse)
    {
        data = data.sort(function(a, b)
        {
            return b["rowChart"]["satisfaction"] - a["rowChart"]["satisfaction"];
        });
    }
    else
    {
        data = data.sort(function(a, b)
        {
            return a["rowChart"]["satisfaction"] - b["rowChart"]["satisfaction"];
        });
    }

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
    var node = $('#tree').dynatree('getActiveNode')
    if (node)
        return node.data.key;
    else
        return null;
}

function getTimeRange()
{
    return $('#dateCombo').val();
}
