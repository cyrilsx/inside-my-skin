export default {
  localeName: 'Русский',
  nav: {
    today: 'Сегодня',
    trends: 'Тренды',
    activities: 'Активности',
    mental: 'Психика',
    sources: 'Источники'
  },
  app: {
    tagline: 'Ваши данные о здоровье и самочувствии — через один канонический API.',
    footer: 'Приватная панель — данные остаются на вашем сервере. Время экрана указано приблизительно.',
    language: 'Язык'
  },
  errors: {
    loadFailed: 'Не удалось загрузить данные. Повторите попытку.',
    exportFailed: 'Экспорт не удался.',
    deletionFailed: 'Запрос на удаление не удался.',
    saveFailed: 'Не удалось сохранить.',
    deleteFailed: 'Не удалось удалить.',
    renderError: 'При отображении этой страницы произошла ошибка.'
  },
  common: {
    loading: 'Загрузка…',
    skipToMain: 'Перейти к основному содержимому',
    none: '—',
    minutes: '{count} мин',
    hours: '{count} ч',
    kilometers: '{count} км',
    kilograms: '{count} кг'
  },
  today: {
    title: 'Сегодня',
    steps: 'шаги',
    sleep: 'сон',
    restingHr: 'пульс в покое (уд/мин)',
    weight: 'вес',
    screenTime: 'экранное время (оценка)',
    sources: 'источники',
    noData: 'За {date} данных пока нет.'
  },
  trends: {
    title: 'Тренды',
    metric: 'Метрика'
  },
  activities: {
    title: 'Активности',
    caption: 'Активности за последние 30 дней',
    started: 'Начало',
    type: 'Тип',
    activityTitle: 'Название',
    duration: 'Длительность',
    distance: 'Дистанция',
    source: 'Источник',
    empty: 'За последние 30 дней активностей нет.'
  },
  mental: {
    title: 'Психика',
    screenTime: 'Экранное время',
    screenTimeChart: 'Экранное время по дням (минуты, оценка)',
    sessions: 'Сеансы',
    sessionsEmpty: 'За последние 30 дней сеансов нет.',
    mood: 'Отметки настроения',
    moodChart: 'Валентность настроения',
    valence: 'валентность'
  },
  sources: {
    title: 'Источники',
    caption: 'Подключённые источники данных',
    provider: 'Провайдер',
    label: 'Метка',
    connected: 'Подключено',
    lastSync: 'Последняя синхронизация',
    status: 'Статус',
    actions: 'Действия',
    empty: 'Подключений пока нет.',
    syncNow: 'Синхронизировать',
    connectWithings: 'Подключить Withings',
    connectGarmin: 'Подключить Garmin',
    exportJson: 'Экспорт JSON',
    exportCsv: 'Экспорт CSV',
    deleteAll: 'Удалить все данные',
    lastSyncResult: 'Последняя синхронизация: загружено {ingested}, дубликатов {duplicates}.',
    deletionAccepted: 'Задача удаления {id} принята (статус: {status}); завершение придёт в потоке событий.'
  },
  credentials: {
    title: 'Учётные данные провайдера',
    hint: 'Укажите свои учётные данные провайдера. Они хранятся на сервере в зашифрованном виде и больше не отображаются.',
    provider: 'Провайдер',
    configured: 'Настроено ({keys})',
    notConfigured: 'Учётные данные пока не сохранены.',
    save: 'Сохранить',
    delete: 'Удалить учётные данные',
    saved: 'Учётные данные сохранены.',
    deleted: 'Учётные данные удалены.'
  },
  dialog: {
    deleteTitle: 'Удалить все данные',
    deleteMessage: 'Удалить все сохранённые данные и токены провайдеров? Действие асинхронное и необратимое.',
    confirm: 'Подтвердить',
    cancel: 'Отмена',
    deleteConfirm: 'Удалить все данные'
  },
  notFound: {
    title: 'Страница не найдена',
    body: 'Страница, которую вы ищете, не существует.',
    home: 'Перейти к «Сегодня»'
  }
}
