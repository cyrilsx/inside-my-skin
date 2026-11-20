import * as echarts from 'echarts/core'
import { BarChart, LineChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { EChartsOption } from 'echarts'

echarts.use([LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

export { echarts }

export function lineSeriesOption(title: string, series: { name: string; data: [string, number][] }[]): EChartsOption {
  return {
    title: { text: title, textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    legend: { data: series.map(s => s.name), bottom: 0 },
    grid: { left: 48, right: 16, top: 40, bottom: 48 },
    xAxis: { type: 'time' },
    yAxis: { type: 'value', scale: true },
    series: series.map(s => ({ name: s.name, type: 'line', data: s.data, showSymbol: true, symbolSize: 6 }))
  }
}

export function barSeriesOption(title: string, categories: string[], data: number[]): EChartsOption {
  return {
    title: { text: title, textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 16, top: 40, bottom: 64 },
    xAxis: { type: 'category', data: categories, axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', scale: true },
    series: [{ type: 'bar', data }]
  }
}
