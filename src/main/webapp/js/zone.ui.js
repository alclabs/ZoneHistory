var tableData, reverse = true;

$(function()
{
    // init the tabs
//    $('#tabs').tabs();


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
    $("#dateCombo").simpleCombo().change(
            function()
            {
                runReport();
            }
    );

    $("#reportCombo").simpleCombo().change(
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
    });

    $("#satisfaction_content").bind( "tabsshow", function(event, ui) {
        runReport();
    });

    $("#environmental_index").bind( "tabsshow", function(event, ui) {
        runReport();
    });

});

// Will be used later to allow links from charts on a graphic to the whole application
function treeInitialized() {
    if (location.search) {
        var splits = location.split("=", 2);
        if (splits.length === 2) {
            if (splits[0] === "loc") {
                jumpToTreeLocation(splits[1]);
            }
        }
    }
}

function jumpToTreeLocation(keyPath)
{
    var dynatree = $('#tree').dynatree('getTree');
    dynatree.loadKeyPath(keyPath, function(node,status) {
        if (status == 'ok') {
            node.activate();
        }
    });
}

function runReport()
{
   $("#welcome").css('display','none');
   clearTable();

    // get active tab to determine which test to run
   var testToRun = $( "#reportCombo" ).val();
//   var testToRun = /*"satisfaction";*/ "environmental index";
   var location = getActiveNodeKey();
   if (location)
      runColorReport(location, getTimeRange(), false, 640, 430, true, true, testToRun);
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
            return b["rowChart"]["percentlabel"] - a["rowChart"]["percentlabel"];
        });
    }
    else
    {
        data = data.sort(function(a, b)
        {
            return a["rowChart"]["percentlabel"] - b["rowChart"]["percentlabel"];
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
