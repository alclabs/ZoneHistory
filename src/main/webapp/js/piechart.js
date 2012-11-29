function ZoneHistoryPieChart(paper, coordinateX, coordinateY, pieRadius)
{
    // private stuff
    var animationScale = 1.2;
    var raphaelPaper = paper;
    var x = (coordinateX == -1 ? pieRadius : coordinateX);
    var y = (coordinateY == -1 ? pieRadius : coordinateY);
    var radius = pieRadius / animationScale;

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
            bigSlices.push({"rgb-red":0, "rgb-green": 0, "rgb-blue": 0, "percent": sumOfTiny, "color": "Others (<1%)  "});

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
            pieLabels.push("%%.%%: " + readablizeString(onlyItem.color.toString().replace("_", " ").toLocaleLowerCase()) + "  ");
        }
    }

    // Used to make the color names appear in a more readable form (example: from "MODERATE_COOLING" to "Moderate Cooling")
    function readablizeString(str)
    {
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


    /*
     * Make the text for the popups that show up on the pie chart when hovering over a section
     * */
    function makePopupText(number, existingLabel)
    {
        if (existingLabel.charAt(0) != '%')
            return existingLabel;
        return existingLabel.substring(7) + ": " + Math.round(number) + "%";
    }

    function sumOfArray(data)
    {
        var sum = 0;
        for (var index in data)
            sum += data[index].percent;

        return sum;
    }

    // public stuff
    this.renderChart = function(data, drawLegend)
    {
        // sort data coming in by percentage of slice largest to smallest
        data = data.sort(function(a, b)
        {
            return b.percent - a.percent;
        });

        // define the three vars for g.raphael's piechart

        var piePercentages = [];
        var pieLabels = [];
        var pieColors = [];

        // Graphael combines 2 or more slices less than 1.0% into an ambiguous 'Others' category
        // Instead, we take control and combine them into a single "Values remaining" slice which will be rendered correctly
        combineDataBelowCutoff(data, 1.0, piePercentages, pieLabels, pieColors);

        // set up for custom pie colors and whether or not to draw the legend
        var params = {};
        params.colors = pieColors;
        if (drawLegend === true)
        {
            params.legend = pieLabels;
            params.legendpos = "east";
            params.legendcolor = '#fff';
//            params.legendcolor = useWhiteTextForLegend ? '#fff' : '#000'; // left over code to change text color
        }

        var popup;
        raphaelPaper.piechart(x, y, radius, piePercentages, params)
                .hover(function ()
                {
                    this.sector.stop();
                    this.sector.animate({ transform: 's1.1 1.1 ' + this.cx + ' ' + this.cy }, 500, "bounce");

                    if (this.label)
                    {
                        this.label[0].stop();
                        this.label[0].attr({ r: 7.5 });
                        this.label[1].attr({ "font-weight": 800 });
                    }

                    var popupText = makePopupText(this.value.valueOf(), pieLabels[this.value.order]);
                    popup = raphaelPaper.popup(this.sector.middle.x, this.sector.middle.y, popupText, 'up');
                }, function ()
                {
                    this.sector.animate({ transform: 's1 1 ' + this.cx + ' ' + this.cy }, 500, "bounce");

                    if (this.label)
                    {
                        this.label[0].animate({ r: 5 }, 500, "bounce");
                        this.label[1].attr({ "font-weight": 400});
                    }

                    popup.hide();
                });
    };

    this.clear = function()
    {
        raphaelPaper.clear();
    }
}


// example of public facing method
//ZoneHistoryPieChart.prototype.setRaphaelPaper = function(paper)
//{
//    this.raphaelPaper = paper;
//};