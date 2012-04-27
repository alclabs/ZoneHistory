var mainChartLocation;
var animationScale = 1.1;

function runColorReport(node, prevDays, isWebContext, canvasWidth, canvasHeight, showLegend, showTotal)
{
    if (!node)
    {
        alert("Please select a node.");
        return;
    }

    // check if canvas size is capable of displaying features
    showLegend = showLegend && checkCanvasDimensionsForLegend(canvasWidth, canvasHeight);
    showTotal = showTotal && checkCanvasDimensionsForTotal(canvasWidth, canvasHeight);

    var radius = determineChartRadius(canvasWidth, canvasHeight, showLegend, showTotal);
    var textColor = isWebContext ? "#FFFFFF" : "#000000";

    if (!mainChartLocation)
        mainChartLocation = initChartLocation(isWebContext, canvasWidth, canvasHeight);

    mainChartLocation.clear();
    mainChartLocation.text(canvasWidth / 2, canvasHeight / 3, "Loading...").attr("fill", textColor);

    var obj = { "location":node, "prevdays":prevDays };
    $.getJSON("servlets/results", obj,
            function(data) {
                var mainChartData = data.mainChart;
                drawChart(mainChartData.colors, showLegend, isWebContext, mainChartLocation, radius);

                if (showTotal)
                {
                    var satisfactionNumber = Math.round(mainChartData.satisfaction);
                    var satisfactionText = satisfactionNumber==-1?"N/A":satisfactionNumber+"%";
                    var mainSatisfaction = "Satisfaction: " + satisfactionText;
                    var textX = getCoords(radius, animationScale);
                    var textY = getCoords(2*radius+10, animationScale);
                    var text = mainChartLocation.text(textX, textY, mainSatisfaction);
                    text.attr({ "fill": textColor, "font-weight": "normal" });
                }

                if (!isWebContext)
                {
                    var tableChartData = data.table
                    if (tableChartData)
                    {
                        // sort by satisfaction - low to high
                        tableData = tableChartData.sort(function(a, b) {
                            return a.rowChart.satisfaction - b.rowChart.satisfaction;
                        });
                        clearTable();
                        drawTable(tableData, 30);
                    }
                }
            });
}

function clearPie()
{
    mainChartLocation.clear();
}

function drawChart(data, drawLegend, useWhiteTextForLegend, chartLocation, radius)
{
    var piePercentages = [];
    var pieLabels = [];
    var pieColors = [];

    data = data.sort(function(a,b) {return b.percent - a.percent;});

    for (var index in data)
    {
        var temp = data[index];
        if (temp.percent > 0.1)
        {
            pieLabels.push("%%.%: " + readablizeString(temp.color.toString().replace("_", " ").toLocaleLowerCase()) + "");
            piePercentages.push(temp.percent);
            pieColors.push("rgb(" + temp["rgb-red"] + ", " + temp["rgb-green"] + ", " + temp["rgb-blue"] + ")");
        }
    }

    chartLocation.clear();

    // Raphael has a bug where if a single color is specified in the piechart params then it is ignored.  However,
    // setting it this way seems to fix the issue.
    chartLocation.g.colors = pieColors;

    var params = {}
    if (drawLegend)
    {
      params.legend = pieLabels;
      params.legendpos = "east";
      params.legendColor = useWhiteTextForLegend ? '#fff' : '#000';
    }
    var pie = chartLocation.g.piechart(getCoords(radius, animationScale), getCoords(radius, animationScale), radius, piePercentages, params);

    pie.hover(function ()
    {
        this.sector.stop();
        this.sector.scale(animationScale, animationScale, this.cx, this.cy);
        if (this.label)
        {
            this.label[0].stop();
            this.label[0].scale(1.5);
            this.label[1].attr({"font-weight": 800});
        }
    }, function ()
    {
        this.sector.animate({scale: [1, 1, this.cx, this.cy]}, 500, "bounce");
        if (this.label)
        {
            this.label[0].animate({scale: 1}, 500, "bounce");
            this.label[1].attr({"font-weight": 400});
        }
    });
}

function drawTable(tableData, sparklineDiameter)
{
    $("#zoneDetails").show();

    for (var index in tableData)
    {
        var item = tableData[index];
        var eqLink = item.eqDisplayName;
        var rowId = 'lil_chart_' + index;
        var style = index % 2 == 1 ? "odd" : "even"
        var transientLookup = item.eqTransLookup;
        var path = item.eqTransLookupPath;
        var satisfactionNumber = Math.round(item.rowChart.satisfaction);
        var tableRow =
                "<tr class="+style+" onclick=\"jumpToTreeLocation(\'"+path+"\')\"><td>" +
                eqLink + '</td><td style="text-align: center;">' + (satisfactionNumber==-1?"N/A":(satisfactionNumber + "%")) +
                '</td><td style="text-align: center;"><span id="' + rowId + "\" class=\"sparkline\"></span>" + '</td></tr>';


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

function initChartLocation(isWebContext, canvasWidth, canvasHeight)
{
    if (isWebContext)
        return Raphael(0, 0, canvasWidth, canvasHeight);
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
