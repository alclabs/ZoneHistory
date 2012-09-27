var mainChartLocation;
var animationScale = 1.2;

function runColorReport(node, prevDays, isWebContext, canvasWidth, canvasHeight, showLegend, showTotal, testToRun)
{
    if (!node)
    {
        alert("Please select a node.");
        return;
    }

    // check if canvas size is capable of displaying features
    showLegend = showLegend && checkCanvasDimensionsForLegend(canvasWidth, canvasHeight);
    showTotal  = showTotal && checkCanvasDimensionsForTotal(canvasWidth, canvasHeight);

    var radius = determineChartRadius(canvasWidth, canvasHeight, showLegend, showTotal);
    var textColor = isWebContext ? "#FFFFFF" : "#000000";
    var locationToDraw = "graph";

    if (!mainChartLocation)
        mainChartLocation = initChartLocation(isWebContext, canvasWidth, canvasHeight, locationToDraw);

    mainChartLocation.clear();
    mainChartLocation.text(canvasWidth / 2, canvasHeight / 3, "Loading...").attr("fill", textColor);

    var obj = { "location":node, "prevdays":prevDays , "action": testToRun};
    $.getJSON("servlets/results", obj,
            function(data)
            {
                var mainChartData = data.mainChart;
                drawChart(mainChartData.colors, showLegend, isWebContext, mainChartLocation, radius);

                if (showTotal && testToRun !== "environmental index")
                {
                    var satisfactionNumber = Math.round(mainChartData.percentlabel);
                    var satisfactionText =   satisfactionNumber == -1 ? "N/A" : satisfactionNumber + "%";
                    var mainSatisfaction =   "Satisfaction: " + satisfactionText;
                    var textX =              getCoords(radius, animationScale);
                    var textY =              getCoords(2 * radius + 10, animationScale);
                    var text =               mainChartLocation.text(textX, textY, mainSatisfaction);

                    text.attr({ "fill": textColor, "font-weight": "normal" });
                }

                if (!isWebContext)
                {
                    var tableChartData = data.table;
                    if (tableChartData)
                    {
                        // sort by percentage - low to high
                        tableData = tableChartData.sort(function(a, b)
                        {
                            return a.rowChart.percentlabel - b.rowChart.percentlabel;
                        });
                        clearTable();
                        drawTable(tableData, 30, testToRun === "satisfaction");
                    }
                }
            }).error(function (a, textStatus, error)
            {
                alert(error);
            });
}

function clearPie()
{

//    mainChartLocation.clear();
}

function drawChart(data, drawLegend, useWhiteTextForLegend, chartLocation, radius)
{
    var piePercentages = [];
    var pieLabels = [];
    var pieColors = [];
    var sumOfTiny = 0.0;

    data = data.sort(function(a, b)
    {
        return b.percent - a.percent;
    });

//     bug fixed in updated G.Raphael library - disabled but still here in case we need it
    // Graphael limits the entries for the legend to 7 entries total;
    // everything else is combined into an ambiguous "others" as the 7th entry
    // that does not allow to choose the color we want. So we combine them
    for (var index in data)
    {
        var temp = data[index];
//        if (index < 6)
//        {
            pieLabels.push("%%.%%: " + readablizeString(temp.color.toString().replace("_", " ").toLocaleLowerCase()) + "");
            piePercentages.push(temp.percent);
            pieColors.push("rgb(" + temp["rgb-red"] + ", " + temp["rgb-green"] + ", " + temp["rgb-blue"] + ")");
//        }
//        else
//        {
//            sumOfTiny += temp.percent;
//        }
    }

    if (sumOfTiny != 0.0)
    {
        pieLabels.push("%%.%%: Others");
        piePercentages.push(sumOfTiny);
        pieColors.push("rgb(0, 0, 0)");
    }

    var params = {};
    params.colors = pieColors;
    if (drawLegend)
    {
        params.legend = pieLabels;
        params.legendpos = "east";
        params.legendColor = useWhiteTextForLegend ? '#fff' : '#000';
    }
    var pie = chartLocation.piechart(getCoords(radius, animationScale), getCoords(radius, animationScale), radius, piePercentages, params);

    pie.hover(function ()
    {
        this.sector.stop();
        this.sector.animate({ transform: 's1.1 1.1 ' + this.cx + ' ' + this.cy }, 500, "bounce");

        if (this.label)
        {
            this.label[0].stop();
            this.label[0].attr({ r: 7.5 });
            this.label[1].attr({ "font-weight": 800 });
        }
    }, function ()
    {
        this.sector.animate({ transform: 's1 1 ' + this.cx + ' ' + this.cy }, 500, "bounce");

        if (this.label)
        {
            this.label[0].animate({ r: 5 }, 500, "bounce");
            this.label[1].attr({ "font-weight": 400 });
        }
    });
}

function drawTable(tableData, sparklineDiameter, isSatisfaction)
{
    $("#zoneDetails").show();

    for (var index in tableData)
    {
        var item = tableData[index];
        var eqLink = item.eqDisplayName;
        var rowId = 'lil_chart_' + index;
        var style = index % 2 == 1 ? "odd" : "even";
        var transientLookup = item.eqTransLookup;
        var path = item.eqTransLookupPath;
        var satisfactionNumber = Math.round(item.rowChart.percentlabel);
        var tableRow =
                "<tr class=" + style + " onclick=\"jumpToTreeLocation(\'" + path + "\')\">"+
                        "<td>" + eqLink + '</td>"+' +
                        '"<td style="text-align: center;">' + (satisfactionNumber == -1 ? "N/A" : (satisfactionNumber + "%")) + '</td>' +
                        '<td style="text-align: center;"><span id="' + rowId + "\" class=\"sparkline\"></span>" + '</td></tr>';


        $("#detailsTable tbody").append(tableRow);

        // draw each sparkline pie chart (separate all data first)
        var chartData = [];
        var chartColors = [];
        for (var innerindex in item.rowChart.colors)
        {
            var temp = item.rowChart.colors[innerindex];
            chartData[innerindex] = temp.percent;
            chartColors[innerindex] = "rgb(" + temp["rgb-red"] + ',' + temp["rgb-green"] + ',' + temp["rgb-blue"] + ')';
        }
        // sparklines has a bug where it won't draw the pie chart if there is only 1 data point, so in this case
        // add a second data point of value 0 (so that it doesn't show).
        if (chartData.length == 1)
            chartData[1] = 0;

        $("#" + rowId).sparkline(chartData,
                { type:'pie', height: sparklineDiameter + 'px',  width: sparklineDiameter + 'px', sliceColors: chartColors });
    }
}

function initChartLocation(isWebContext, canvasWidth, canvasHeight, locationToDraw)
{
    if (isWebContext)
        return Raphael(0, 0, canvasWidth, canvasHeight);
    else if (locationToDraw)
        return Raphael(document.getElementById(locationToDraw), canvasWidth, canvasHeight);

    return Raphael("graph", canvasWidth, canvasHeight);
}

function checkCanvasDimensionsForLegend(canvasWidth, canvasHeight)
{
    return canvasWidth > 160 && canvasHeight > 40;
}

function checkCanvasDimensionsForTotal(canvasWidth, canvasHeight)
{
    return canvasWidth > 150 && canvasHeight > 70;
}

function determineChartRadius(canvasWidth, canvasHeight, drawLegend, drawTotal)
{
    if (drawLegend)
        canvasWidth -= 160;
    if (drawTotal)
        canvasHeight -= 30;

    var smallerCanvasDimension = Math.min(canvasHeight, canvasWidth);
    var radius = 0.50 * smallerCanvasDimension;

    return radius / animationScale;
}

function getCoords(radius, scale)
{
    return scale * radius;
}

function readablizeString(str)
{
    // used to make the color names appear in a more readable form (from MODERATE_COOLING to Moderate Cooling)
    var result = str.charAt(0).toLocaleUpperCase();
    var oldloc = 1;

    while (oldloc > -1 && oldloc < str.length - 1)
    {
        var newloc = str.indexOf(" ", oldloc);
        if (newloc == -1 || newloc >= str.length - 1)
            return result + str.substring(oldloc);

        result += str.substring(oldloc, newloc) + " " + str.charAt(newloc + 1).toUpperCase();
        oldloc = newloc + 2;
    }

    return result;
}
