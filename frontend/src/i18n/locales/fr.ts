export default {
  localeName: 'Français',
  nav: {
    today: 'Aujourd’hui',
    trends: 'Tendances',
    activities: 'Activités',
    mental: 'Bien-être',
    sources: 'Sources'
  },
  app: {
    tagline: 'Vos données de santé et de bien-être, via une seule API canonique.',
    footer: 'Tableau de bord privé — les données restent sur votre propre serveur. Les durées d’écran sont des estimations.',
    language: 'Langue'
  },
  errors: {
    loadFailed: 'Impossible de charger les données. Veuillez réessayer.',
    exportFailed: 'L’export a échoué.',
    deletionFailed: 'La demande de suppression a échoué.',
    renderError: 'Une erreur est survenue lors de l’affichage de cette page.'
  },
  common: {
    loading: 'Chargement…',
    skipToMain: 'Aller au contenu principal',
    none: '—',
    minutes: '{count} min',
    hours: '{count} h',
    kilometers: '{count} km',
    kilograms: '{count} kg'
  },
  today: {
    title: 'Aujourd’hui',
    steps: 'pas',
    sleep: 'sommeil',
    restingHr: 'FC au repos (bpm)',
    weight: 'poids',
    screenTime: 'temps d’écran (estimation)',
    sources: 'sources',
    noData: 'Pas encore de données pour le {date}.'
  },
  trends: {
    title: 'Tendances',
    metric: 'Métrique'
  },
  activities: {
    title: 'Activités',
    caption: 'Activités des 30 derniers jours',
    started: 'Début',
    type: 'Type',
    activityTitle: 'Titre',
    duration: 'Durée',
    distance: 'Distance',
    source: 'Source',
    empty: 'Aucune activité ces 30 derniers jours.'
  },
  mental: {
    title: 'Bien-être',
    screenTime: 'Temps d’écran',
    screenTimeChart: 'Temps d’écran par jour (minutes, estimation)',
    sessions: 'Sessions',
    sessionsEmpty: 'Aucune session ces 30 derniers jours.',
    mood: 'Bilans d’humeur',
    moodChart: 'Valence de l’humeur',
    valence: 'valence'
  },
  sources: {
    title: 'Sources',
    caption: 'Sources de données connectées',
    provider: 'Fournisseur',
    label: 'Libellé',
    connected: 'Connecté',
    lastSync: 'Dernière synchro',
    status: 'Statut',
    actions: 'Actions',
    empty: 'Aucune connexion pour le moment.',
    syncNow: 'Synchroniser',
    connectWithings: 'Connecter Withings',
    connectGarmin: 'Connecter Garmin',
    exportJson: 'Exporter en JSON',
    exportCsv: 'Exporter en CSV',
    deleteAll: 'Supprimer toutes les données',
    lastSyncResult: 'Dernière synchro : {ingested} ingérées, {duplicates} doublons.',
    deletionAccepted: 'Job de suppression {id} accepté (statut : {status}) ; la confirmation arrivera sur le flux d’événements.'
  },
  dialog: {
    deleteTitle: 'Supprimer toutes les données',
    deleteMessage: 'Supprimer toutes les données stockées et les jetons des fournisseurs ? Cette action est asynchrone et irréversible.',
    confirm: 'Confirmer',
    cancel: 'Annuler',
    deleteConfirm: 'Supprimer toutes les données'
  },
  notFound: {
    title: 'Page introuvable',
    body: 'La page que vous cherchez n’existe pas.',
    home: 'Aller à Aujourd’hui'
  }
}
