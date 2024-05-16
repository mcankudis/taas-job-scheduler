const loadData = async () => {
    const url = new URL(window.location.href);
    const id = url.searchParams.get('id');
    if (!id) return undefined;
    return await d3.csv(`/plot/data/usage-stats-${id}.csv`);
};

const MAX_LOAD = 100;
const getBarColor = (d) => {
    let r = Math.floor((d.usedNodes / MAX_LOAD) * 255);
    let g = 255 - r;

    // over max, turn color to black grdually
    if (d.usedNodes > MAX_LOAD) {
        const diff = d.usedNodes - MAX_LOAD;
        r -= Math.floor(diff * 4);
        g -= Math.floor(diff * 4);
    }

    return `rgb(${r}, ${g}, 0)`;
};

const plotData = (data) => {
    /* data is {
        timestamp: Date;
        tick: number;
        freeNodes: number;
        usedNodes: number;
    }[] */

    // x-axis: tick
    // y-axis: usedNodes

    const svg = d3.select('#load').append('svg');

    // set the width and height of the svg element
    const width = data.length * 20;
    const height =
        5 +
        3 *
            Math.max(
                data.reduce((acc, d) => Math.max(acc, d.usedNodes), 0),
                MAX_LOAD
            );
    svg.attr('width', width).attr('height', height);

    // scale the x-axis
    const x = d3
        .scaleTime()
        .domain([data[0].tick, data[data.length - 1].tick])
        .range([0, width]);

    // scale the y-axis
    const y = d3
        .scaleLinear()
        .domain([0, height / 2])
        .range([height, 0]);

    // x-axis
    const xAxis = d3.axisBottom(x);
    svg.append('g').attr('transform', `translate(0, ${height})`).call(xAxis);

    // y-axis
    const yAxis = d3.axisLeft(y);
    svg.append('g').call(yAxis);

    // x axis label
    svg.append('text')
        .attr('x', 4)
        .attr('y', height - 15)
        .style('font-size', '12px')
        .text('Tick');

    // line for the max load
    svg.append('line')
        .attr('x1', 0)
        .attr('y1', y(MAX_LOAD + 15))
        .attr('x2', width)
        .attr('y2', y(MAX_LOAD + 15))
        .attr('stroke', 'red')
        .attr('stroke-dasharray', '5,5');

    // label for the max load
    svg.append('text')
        .attr('x', 8)
        .attr('y', y(MAX_LOAD + 18))
        .text('Max Load')
        .style('font-size', '12px')
        .style('letter-spacing', '-1px')
        .style('fill', 'red');

    // dotted vertical lines every 10 ticks
    for (let i = 0; i < width; i += 10) {
        svg.append('line')
            .attr('x1', i * 20)
            .attr('y1', 0)
            .attr('x2', i * 20)
            .attr('y2', height)
            .attr('stroke', 'gray')
            .attr('stroke-dasharray', '5,5');
    }

    // bars
    svg.selectAll('rect')
        .data(data)
        .enter()
        .append('rect')
        .attr('x', (d) => x(d.tick))
        .attr('y', (d) => y(d.usedNodes) - 30)
        .attr('width', 10)
        .attr('height', (d) => height - y(d.usedNodes))
        .attr('fill', (d) => getBarColor(d));

    // line
    const line = d3
        .line()
        .x((d) => x(d.tick) + 5)
        .y((d) => y(d.usedNodes) - 30);

    svg.append('path')
        .datum(data)
        .attr('fill', 'none')
        .attr('stroke', 'steelblue')
        .attr('stroke-width', 1.5)
        .attr('d', line);

    // load labels
    svg.selectAll('text.usage')
        .data(data)
        .enter()
        .append('text')
        .attr('x', (d) => x(d.tick) - 3)
        .attr('y', (d) => y(d.usedNodes) - 35)
        .text((d) => d.usedNodes)
        .style('font-size', '10px')
        .style('letter-spacing', '-1px')
        .attr('text-anchor', 'center')
        .attr('class', 'usage');

    svg.selectAll('text.tick')
        .data(data)
        .enter()
        .append('text')
        .text((d) => d.tick)
        .attr('x', (d) => x(d.tick))
        .attr('y', height - 2)
        .attr('text-anchor', 'start')
        .style('font-size', '10px')
        .style('letter-spacing', '-1px')
        .attr('class', 'tick');

    // add a legend
    const legend = svg.append('g');
    legend
        .append('rect')
        .attr('x', 10)
        .attr('y', 10)
        .attr('width', 10)
        .attr('height', 10)
        .style('fill', 'steelblue');

    legend.append('text').attr('x', 25).attr('y', 20).text('Used Nodes');

    // add a tooltip
    const tooltip = d3.select('body').append('div');
    svg.selectAll('rect')
        .on('mouseover', (event, d) => {
            tooltip.style('visibility', 'visible').text(d.usedNodes);
        })
        .on('mousemove', (event) => {
            tooltip.style('top', event.pageY + 10 + 'px').style('left', event.pageX + 10 + 'px');
        })
        .on('mouseout', () => {
            tooltip.style('visibility', 'hidden');
        });

    svg.append('g');
};

const run = async () => {
    const data = await loadData();

    if (!data) return;

    const h1 = document.getElementById('title');
    h1.innerHTML = 'Load and running jobs over time';

    console.log(data);
    plotData(data);
};

run();
