const loadData = async () => {
    const url = new URL(window.location.href);
    const id = url.searchParams.get('id');
    if (!id) return undefined;
    return await d3.csv(`/plot/data/execution-log-${id}.csv`);
};

const plotData = (dataRaw) => {
    /*
    data is {
        jobId: string
        executionId: string
        jobName: string
        startedTick: number
        finishedTick: number
        ticks: number
        startedAt: number
        finishedAt: number
        lateBySeconds: number
    }[]
    */
    const data = dataRaw.map((d) => ({
        ...d,
        startedTick: parseInt(d.startedTick),
        finishedTick: parseInt(d.finishedTick),
        ticks: parseInt(d.ticks),
        startedAt: parseInt(d.startedAt),
        finishedAt: parseInt(d.finishedAt)
    }));

    const svg = d3.select('#jobs').append('svg');

    const maxTick = data.reduce((acc, d) => Math.max(acc, d.startedTick + d.ticks), 0);
    const uniqueJobs = new Set(data.map((d) => d.jobName));

    // set the width and height of the svg element
    const width = maxTick * 20;
    const height = uniqueJobs.size * 20 + 5;
    svg.attr('width', width).attr('height', height);

    console.log(width, height);

    const x = d3.scaleLinear().domain([0, maxTick]).range([0, width]);

    const y = d3
        .scaleBand()
        .domain(data.map((d) => d.jobName).sort())
        .range([0, height])
        .padding(0.1);

    const xAxis = d3.axisBottom(x);
    const yAxis = d3.axisLeft(y);

    svg.append('g')
        .attr('transform', 'translate(0,' + height + ')')
        .call(xAxis);

    svg.append('g').call(yAxis);

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

    svg.selectAll('.bar')
        .data(data)
        .enter()
        .append('rect')
        .attr('class', 'bar')
        .attr('fill', '#69b3a2')
        .attr('y', (d) => y(d.jobName))
        .attr('height', y.bandwidth())
        .attr('x', (d) => x(d.startedTick))
        .attr('width', (d) => x(d.ticks));

    svg.selectAll('.text')
        .data(data)
        .enter()
        .append('text')
        .attr('class', 'label')
        .attr('y', (d) => y(d.jobName) + y.bandwidth() / 2)
        .attr('x', (d) => x(d.startedTick))
        .text(
            (d) =>
                `${d.jobName} {ticks=[${d.startedTick}-${d.startedTick + d.ticks}], nodes=${d.resourceUsage ?? 'X'}, lateBy=${d.lateBySeconds ?? 0}}`
        )
        .style('font-size', '10px')
        .attr('dy', '.35em')
        .attr('dx', '1em');
};

const run = async () => {
    const data = await loadData();

    if (!data) return;

    console.log(data);

    plotData(data);
};

run();
