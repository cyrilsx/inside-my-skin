export default {
  localeName: '中文',
  nav: {
    today: '今日',
    trends: '趋势',
    activities: '活动',
    mental: '心理健康',
    sources: '数据源'
  },
  app: {
    tagline: '您的健康数据，汇聚于同一个规范 API。',
    footer: '私密仪表盘 — 数据仅保留在您自己的服务器上。屏幕使用时长为估计值。',
    language: '语言'
  },
  errors: {
    loadFailed: '无法加载数据，请重试。',
    exportFailed: '导出失败。',
    deletionFailed: '删除请求失败。',
    saveFailed: '保存失败。',
    deleteFailed: '删除失败。',
    renderError: '渲染此页面时出错。'
  },
  common: {
    loading: '加载中…',
    skipToMain: '跳到主要内容',
    none: '—',
    minutes: '{count} 分钟',
    hours: '{count} 小时',
    kilometers: '{count} 公里',
    kilograms: '{count} 公斤'
  },
  today: {
    title: '今日',
    steps: '步数',
    sleep: '睡眠',
    restingHr: '静息心率 (bpm)',
    weight: '体重',
    screenTime: '屏幕使用时长（估计）',
    sources: '来源',
    noData: '{date} 暂无数据。'
  },
  trends: {
    title: '趋势',
    metric: '指标'
  },
  activities: {
    title: '活动',
    caption: '最近 30 天的活动',
    started: '开始时间',
    type: '类型',
    activityTitle: '标题',
    duration: '时长',
    distance: '距离',
    source: '来源',
    empty: '最近 30 天没有活动。'
  },
  mental: {
    title: '心理健康',
    screenTime: '屏幕使用时长',
    screenTimeChart: '每日屏幕使用时长（分钟，估计）',
    sessions: '专注时段',
    sessionsEmpty: '最近 30 天没有专注时段。',
    mood: '心情打卡',
    moodChart: '心情效价',
    valence: '效价'
  },
  sources: {
    title: '数据源',
    caption: '已连接的数据源',
    provider: '提供商',
    label: '标签',
    connected: '已连接',
    lastSync: '上次同步',
    status: '状态',
    actions: '操作',
    empty: '暂无连接。',
    syncNow: '立即同步',
    connectWithings: '连接 Withings',
    connectGarmin: '连接 Garmin',
    exportJson: '导出 JSON',
    exportCsv: '导出 CSV',
    deleteAll: '删除所有数据',
    lastSyncResult: '上次同步：已导入 {ingested} 条，重复 {duplicates} 条。',
    deletionAccepted: '删除任务 {id} 已接受（状态：{status}）；完成通知将通过事件流送达。'
  },
  credentials: {
    title: '提供商凭据',
    hint: '为提供商填写您自己的凭据。它们在服务器上加密存储，不会再次显示。',
    provider: '提供商',
    configured: '已配置（{keys}）',
    notConfigured: '尚未存储凭据。',
    save: '保存凭据',
    delete: '删除凭据',
    saved: '凭据已保存。',
    deleted: '凭据已删除。'
  },
  dialog: {
    deleteTitle: '删除所有数据',
    deleteMessage: '删除所有已存储的数据和提供商令牌？此操作为异步执行，且不可撤销。',
    confirm: '确认',
    cancel: '取消',
    deleteConfirm: '删除所有数据'
  },
  notFound: {
    title: '页面未找到',
    body: '您要查找的页面不存在。',
    home: '前往今日'
  }
}
