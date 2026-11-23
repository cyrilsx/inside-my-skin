export default {
  localeName: 'English',
  nav: {
    today: 'Today',
    trends: 'Trends',
    activities: 'Activities',
    mental: 'Mental',
    sources: 'Sources'
  },
  app: {
    tagline: 'Your health and wellbeing data, through one canonical API.',
    footer: 'Private dashboard — data stays on your own server. Screen time values are estimates.',
    language: 'Language'
  },
  errors: {
    loadFailed: 'Could not load the data. Please try again.',
    exportFailed: 'Export failed.',
    deletionFailed: 'Deletion request failed.',
    renderError: 'Something went wrong while rendering this page.'
  },
  common: {
    loading: 'Loading…',
    skipToMain: 'Skip to main content',
    none: '—',
    minutes: '{count} min',
    hours: '{count} h',
    kilometers: '{count} km',
    kilograms: '{count} kg'
  },
  today: {
    title: 'Today',
    steps: 'steps',
    sleep: 'sleep',
    restingHr: 'resting HR (bpm)',
    weight: 'weight',
    screenTime: 'screen time (estimate)',
    sources: 'sources',
    noData: 'No data yet for {date}.'
  },
  trends: {
    title: 'Trends',
    metric: 'Metric'
  },
  activities: {
    title: 'Activities',
    caption: 'Activities in the last 30 days',
    started: 'Started',
    type: 'Type',
    activityTitle: 'Title',
    duration: 'Duration',
    distance: 'Distance',
    source: 'Source',
    empty: 'No activities in the last 30 days.'
  },
  mental: {
    title: 'Mental',
    screenTime: 'Screen time',
    screenTimeChart: 'Screen time per day (minutes, estimate)',
    sessions: 'Sessions',
    sessionsEmpty: 'No sessions in the last 30 days.',
    mood: 'Mood check-ins',
    moodChart: 'Mood valence',
    valence: 'valence'
  },
  sources: {
    title: 'Sources',
    caption: 'Connected data sources',
    provider: 'Provider',
    label: 'Label',
    connected: 'Connected',
    lastSync: 'Last sync',
    status: 'Status',
    actions: 'Actions',
    empty: 'No connections yet.',
    syncNow: 'Sync now',
    connectWithings: 'Connect Withings',
    connectGarmin: 'Connect Garmin',
    exportJson: 'Export JSON',
    exportCsv: 'Export CSV',
    deleteAll: 'Delete all data',
    lastSyncResult: 'Last sync: {ingested} ingested, {duplicates} duplicates.',
    deletionAccepted: 'Deletion job {id} accepted (status: {status}); completion arrives on the event stream.'
  },
  dialog: {
    deleteTitle: 'Delete all data',
    deleteMessage: 'Delete all stored data and provider tokens? This runs asynchronously and cannot be undone.',
    confirm: 'Confirm',
    cancel: 'Cancel',
    deleteConfirm: 'Delete all data'
  },
  notFound: {
    title: 'Page not found',
    body: 'The page you are looking for does not exist.',
    home: 'Go to Today'
  }
}
