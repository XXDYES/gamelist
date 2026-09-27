import * as echarts from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([PieChart, LegendComponent, CanvasRenderer])

export default echarts
