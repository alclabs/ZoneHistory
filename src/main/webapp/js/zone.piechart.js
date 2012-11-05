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
    showTotal = showTotal && checkCanvasDimensionsForTotal(canvasWidth, canvasHeight);

    var radius = determineChartRadius(canvasWidth, canvasHeight, showLegend, showTotal);
    var textColor = isWebContext ? "#FFFFFF" : "#000000";
    var locationToDraw = "graph";

    if (!mainChartLocation)
        mainChartLocation = initChartLocation(isWebContext, canvasWidth, canvasHeight, locationToDraw);

    mainChartLocation.clear();
    mainChartLocation.text(canvasWidth / 2, canvasHeight / 3, "Loading...").attr({"fill": textColor});

    var obj = { "location":node, "prevdays":prevDays , "action": testToRun};
    $.getJSON("servlets/results", obj,
            function(data)
            {
                mainChartLocation.clear();

                var mainChartData = data.mainChart;
                drawChart(mainChartData.colors, showLegend, isWebContext, mainChartLocation, radius);

                if (showTotal && testToRun !== "environmental index")
                {
                    var satisfactionNumber = Math.round(mainChartData.percentlabel);
                    var satisfactionText = satisfactionNumber == -1 ? "N/A" : satisfactionNumber + "%";
                    var mainSatisfaction = "Satisfaction: " + satisfactionText;
                    var textX = getCoords(radius, animationScale);
                    var textY = getCoords(2 * radius, animationScale);

                    mainChartLocation.text(textX, textY, mainSatisfaction).attr({ "fill": textColor, "font-weight": "normal", font: "12px sans-serif"});
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

function drawChart(data, drawLegend, useWhiteTextForLegend, chartLocation, radius)
{
    data = data.sort(function(a, b)
    {
        return b.percent - a.percent;
    });

    var piePercentages = [];
    var pieLabels = [];
    var pieColors = [];

    // Graphael combines 2 or more slices less than 1.0% into an ambiguous 'Others' category
    // Instead, we take control and combine them into a single "Values remaining" slice which will be rendered correctly
    combineDataBelowCutoff(data, 1.0, piePercentages, pieLabels, pieColors);

    var params = {};
    params.colors = pieColors;
    if (drawLegend)
    {
        params.legend = pieLabels;
        params.legendpos = "east";
        params.legendcolor = useWhiteTextForLegend ? '#fff' : '#000';
    }
    var pie = chartLocation.piechart(getCoords(radius, animationScale), getCoords(radius, animationScale), radius, piePercentages, params);
    var popup;

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
        popup = chartLocation.popup(this.sector.middle.x,this.sector.middle.y, this.label[1].attr("text"), 'up');

    }, function ()
    {
        this.sector.animate({ transform: 's1 1 ' + this.cx + ' ' + this.cy }, 500, "bounce");

        if (this.label)
        {
            this.label[0].animate({ r: 5 }, 500, "bounce");
            this.label[1].attr({ "font-weight": 400 });
        }

        popup.hide();
    });
}

function combineDataBelowCutoff(data, cutoff, piePercentages, pieLabels, pieColors)
{
    var tinySlices = [];
    var bigSlices = [];

    // split values that are less than the cutoff from those that are greater than the cutoff
    for (var index in data)
    {
        var temp = data[index];
        var percent = temp.percent;

        if (percent < cutoff)
            tinySlices.push(temp);
        else
            bigSlices.push(temp);
    }

//    we need to sum all the items in tinySlices to ensure that every slice in bigSlices is larger than this number
//    if this is not done, raphael will sort the percentages in the legend without changing the order of the colors and labels
    var sumOfTiny = sumOfArray(tinySlices);

    if (tinySlices.length == 1)
        bigSlices.push(tinySlices[0]);
    else if (sumOfTiny > 0 && tinySlices.length > 1)
        bigSlices.push({"rgb-red":0, "rgb-green": 0, "rgb-blue": 0, "percent": sumOfTiny, "color": "Others (<1%)"});

//    sort the bigSlices so that the values are all sorted with the correct colors and labels associated with the percentages large to small
    bigSlices.sort(function(a, b)
    {
        return b.percent - a.percent;
    });

//    separate the bigSlices into the 3 arrays needed for rendering the pie correctly
    for (var index2 in bigSlices)
    {
        var onlyItem = bigSlices[index2];
        piePercentages.push(onlyItem.percent);
        pieColors.push("rgb(" + onlyItem["rgb-red"] + ", " + onlyItem["rgb-green"] + ", " + onlyItem["rgb-blue"] + ")");
        pieLabels.push("%%.%%: " + readablizeString(onlyItem.color.toString().replace("_", " ").toLocaleLowerCase()) + "");
    }
}

function sumOfArray(data)
{
    var sum = 0;
    for (var index in data)
        sum += data[index].percent;

    return sum;
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

        var heatingpercent = Math.round(item.heatingpercent);
        var coolingpercent = Math.round(item.coolingpercent);
        var operationalpercent = Math.round(item.operationalpercent);
        var averageEI = Math.round(item.averageEI);


        var tableRow =
                "<tr class=" + style + " onclick=\"jumpToTreeLocation(\'" + path + "\')\">" +
                        "<td>" + eqLink + '</td>' +
                        '<td style="text-align: center;">' + (satisfactionNumber == -1 ? "N/A" : (satisfactionNumber + "%")) + '</td>' +
                        '<td style="text-align: center;">' + (heatingpercent + "%") + '</td>' +
                        '<td style="text-align: center;">' + (coolingpercent + "%") + '</td>' +
                        '<td style="text-align: center;">' + (operationalpercent + "%") + '</td>' +
                        '<td style="text-align: center;">' + (averageEI == -1 ? "N/A" : (averageEI + "%")) + '</td>' +
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
//    return canvasWidth >= 300 && canvasHeight >= 180;
    return true;
}

function checkCanvasDimensionsForTotal(canvasWidth, canvasHeight)
{
//    return canvasWidth >= 80 && canvasHeight >= 110;
    return true;
}

function determineChartRadius(canvasWidth, canvasHeight, drawLegend, drawTotal)
{
    if (drawLegend)
        canvasWidth -= 100;
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
