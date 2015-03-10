function ZoneHistoryTable(renderTargetElement, _isFromGfxPage, showCool, showHeat, showOperational, showOccupied, show_EI, inlinePieDiameter)
{
    // private stuff
    var renderTarget = renderTargetElement;
    var reverse = false;
    var isFromGfxPage = _isFromGfxPage;
    var showCooling = showCool;
    var showHeating = showHeat;
    var showOperational = showOperational;
    var showOccupied = showOccupied;
    var showEI = show_EI;
    var inlinePieSize = inlinePieDiameter;
    var localTableData;

    function drawTable(tableData)
    {
        document.getElementById(renderTarget).style.display = 'block';

        for (var index in tableData)
        {
            var item = tableData[index];
            var eqLink = item.eqDisplayName;
            var rowId = 'lil_chart_' + index;
            var style = index % 2 == 1 ? "odd" : "even";
            var transientLookup = item.eqTransLookup;
            var path = item.eqTransLookupPath;

            var totalTime = item.totalTime;

            var heatingvalue = Math.round(100 * item.heatingvalue / totalTime);
            var coolingvalue = Math.round(100 * item.coolingvalue / totalTime);
            var operationalvalue = Math.round(100 * item.operationalvalue / totalTime);
            var occupiedvalue = Math.round(100 * item.occupiedvalue / totalTime);
            var eivalue = Math.round(item.eivalue);

            var tableRow = "<tr class=" + style + " onclick=\"jumpToTreeLocation(\'" + path + "\')\">" + "<td>" + eqLink + '</td>';

            if (showHeating === true)
                tableRow += '<td style="text-align: center;">' + (heatingvalue + "%") + '</td>';
            if (showCooling === true)
                tableRow += '<td style="text-align: center;">' + (coolingvalue + "%") + '</td>';
            if (showOperational === true)
                tableRow += '<td style="text-align: center;">' + (operationalvalue + "%") + '</td>';
            if (showOccupied === true)
                tableRow += '<td style="text-align: center;">' + (occupiedvalue + "%") + '</td>';
            if (showEI === true)
                tableRow += '<td style="text-align: center;">' + (eivalue + "%") + '</td>';

            tableRow += '<td style="text-align: center;"><span id="' + rowId + "\" class=\"sparkline\"></span>" + '</td></tr>';

            // generalize?
            $('#' + renderTarget + " tbody").append(tableRow);

            // draw each sparkline pie chart (separate all data first)
            var chartData = [];
            var chartColors = [];

            var rowChartColors = item.rowChart.colors.sort(function(a, b)
            {
                return b.percent - a.percent;
            });

            for (var innerindex in rowChartColors)
            {
                var temp = item.rowChart.colors[innerindex];
                chartData[innerindex] = temp.percent;
                chartColors[innerindex] = "rgb(" + temp["rgb-red"] + ',' + temp["rgb-green"] + ',' + temp["rgb-blue"] + ')';
            }
            // sparklines has a bug where it won't draw the pie chart if there is only 1 data point, so in this case
            // add a second data point of value 0 (so that it doesn't show).
            if (chartData.length == 1)
                chartData[1] = 0;

            $("#" + rowId).sparkline(chartData, { type:'pie', height: inlinePieSize + 'px',  width: inlinePieSize + 'px', sliceColors: chartColors, offset: 180 });
        }
    }

    function drawGfxPageTable(tableData, showCooling, showHeating, showOperational, showOccupied, showEI)
    {
        var heatingvalue     = Math.round(100 * tableData.heatingvalue / tableData.totalTime);
        var coolingvalue     = Math.round(100 * tableData.coolingvalue / tableData.totalTime);
        var operatingvalue   = Math.round(100 * tableData.operationalvalue / tableData.totalTime);
        var occupiedvalue    = Math.round(100 * tableData.occupiedvalue / tableData.totalTime);
        var eivalue          = tableData.eivalue === 0 ? 0 : Math.round(tableData.eivalue); // check for 0 EI

        document.getElementById(renderTarget).style.display = 'table';

        var tableRow = "<tr class='label'>";

        if (showHeating === true)
            tableRow += '<tr><td class="label">Heating:</td><td style="text-align: center;"><span class="value">' + heatingvalue + '</span> %</td></tr>';
        if (showCooling === true)
            tableRow += '<tr><td class="label">Cooling:</td><td style="text-align: center;"><span class="value">' + coolingvalue + '</span> %</td></tr>';
        if (showOperational === true)
            tableRow += '<tr><td class="label">Operating:</td><td style="text-align: center;"><span class="value">' + operatingvalue + '</span> %</td></tr>';
        if (showOccupied === true)
            tableRow += '<tr><td class="label">Occupied:</td><td style="text-align: center;"><span class="value">' + occupiedvalue + '</span> %</td></tr>';
        if (showEI === true)
            tableRow += '<tr><td class="label">Average EI:</td><td style="text-align: center;"><span class="value">' + eivalue + '</span> %</td></tr>';

        tableRow += '</tr>';
        $('#' + renderTarget + " tbody").append(tableRow);
    }

    // public stuffs
    this.clearTable = function()
    {
        if (renderTarget === undefined) return;
        var table = document.getElementById(renderTarget);

        while (table.rows.length > 1)
            table.deleteRow(table.rows.length - 1);
        document.getElementById(renderTarget).style.display = 'none';
    };

    this.sortByName = function(increasing)
    {
        reverse = !reverse;
        localTableData = localTableData.sort(function(a, b)
        {
            var eq1, eq2;
            if (increasing)
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

        drawTable(localTableData);
    };

    this.sortByAttribute = function(propertyName, increasing)
    {
        if (propertyName == "eqDisplayName") {
            return this.sortByName(increasing);
        }

        localTableData = localTableData.sort(function(a, b)
        {
            var var1 = a[propertyName];
            var var2 = b[propertyName];

            if (increasing)
                return var1 - var2;
            else
                return var2 - var1;
        });

        drawTable(localTableData);
    };

    this.renderTable = function(tableData)
    {
        this.clearTable();
        localTableData = tableData;
        if (isFromGfxPage)
            drawGfxPageTable(localTableData, showCooling, showHeating, showOperational,showOccupied, showEI);
        else
        {
            if (tableData === undefined)
                return;

            this.sortByAttribute("operationalvalue", false);
            $('#detailsTable th[propname="operationalvalue"]').addClass("down");
            /*
            // sort by operational percentage - low to high
            if (tableData.length !== undefined)
            {
                tableData = tableData.sort(function(a, b)
                {
                    return a.operationalvalue - b.operationalvalue;
                });
            }
            drawTable(localTableData);
            */
        }
    };
}

