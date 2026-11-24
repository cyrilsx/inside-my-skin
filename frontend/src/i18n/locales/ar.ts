export default {
  localeName: 'العربية',
  nav: {
    today: 'اليوم',
    trends: 'الاتجاهات',
    activities: 'الأنشطة',
    mental: 'الصحة النفسية',
    sources: 'المصادر'
  },
  app: {
    tagline: 'بيانات صحتك ورفاهيتك، عبر واجهة برمجية واحدة موحّدة.',
    footer: 'لوحة تحكم خاصة — تبقى البيانات على خادمك الخاص. قيم وقت الشاشة تقديرية.',
    language: 'اللغة'
  },
  errors: {
    loadFailed: 'تعذّر تحميل البيانات. حاول مرة أخرى.',
    exportFailed: 'فشل التصدير.',
    deletionFailed: 'فشل طلب الحذف.',
    saveFailed: 'فشل الحفظ.',
    deleteFailed: 'فشل الحذف.',
    renderError: 'حدث خطأ أثناء عرض هذه الصفحة.'
  },
  common: {
    loading: 'جارٍ التحميل…',
    skipToMain: 'تخطٍ إلى المحتوى الرئيسي',
    none: '—',
    minutes: '{count} د',
    hours: '{count} س',
    kilometers: '{count} كم',
    kilograms: '{count} كجم'
  },
  today: {
    title: 'اليوم',
    steps: 'خطوة',
    sleep: 'النوم',
    restingHr: 'معدل ضربات القلب أثناء الراحة (نبضة/د)',
    weight: 'الوزن',
    screenTime: 'وقت الشاشة (تقدير)',
    sources: 'المصادر',
    noData: 'لا توجد بيانات بعد لهذا اليوم {date}.'
  },
  trends: {
    title: 'الاتجاهات',
    metric: 'المقياس'
  },
  activities: {
    title: 'الأنشطة',
    caption: 'الأنشطة خلال آخر 30 يومًا',
    started: 'البداية',
    type: 'النوع',
    activityTitle: 'العنوان',
    duration: 'المدة',
    distance: 'المسافة',
    source: 'المصدر',
    empty: 'لا توجد أنشطة خلال آخر 30 يومًا.'
  },
  mental: {
    title: 'الصحة النفسية',
    screenTime: 'وقت الشاشة',
    screenTimeChart: 'وقت الشاشة يوميًا (بالدقائق، تقدير)',
    sessions: 'الجلسات',
    sessionsEmpty: 'لا توجد جلسات خلال آخر 30 يومًا.',
    mood: 'تسجيلات المزاج',
    moodChart: 'قيمة المزاج',
    valence: 'القيمة'
  },
  sources: {
    title: 'المصادر',
    caption: 'مصادر البيانات المتصلة',
    provider: 'المزوّد',
    label: 'التسمية',
    connected: 'متصل',
    lastSync: 'آخر مزامنة',
    status: 'الحالة',
    actions: 'إجراءات',
    empty: 'لا توجد اتصالات بعد.',
    syncNow: 'مزامنة الآن',
    connectWithings: 'ربط Withings',
    connectGarmin: 'ربط Garmin',
    exportJson: 'تصدير JSON',
    exportCsv: 'تصدير CSV',
    deleteAll: 'حذف كل البيانات',
    lastSyncResult: 'آخر مزامنة: {ingested} مسجّلة، {duplicates} مكررة.',
    deletionAccepted: 'قُبل طلب الحذف {id} (الحالة: {status})؛ سيصل الإكمال عبر دفق الأحداث.'
  },
  credentials: {
    title: 'بيانات اعتماد المزوّد',
    hint: 'أضف بيانات اعتمادك الخاصة بالمزوّد. تُخزن مشفرة على الخادم ولا تُعرض مجددًا.',
    provider: 'المزوّد',
    configured: 'مهيأ ({keys})',
    notConfigured: 'لا توجد بيانات اعتماد مخزنة بعد.',
    save: 'حفظ بيانات الاعتماد',
    delete: 'حذف بيانات الاعتماد',
    saved: 'تم حفظ بيانات الاعتماد.',
    deleted: 'تم حذف بيانات الاعتماد.'
  },
  dialog: {
    deleteTitle: 'حذف كل البيانات',
    deleteMessage: 'حذف كل البيانات المخزنة ورموز المزوّدين؟ هذا الإجراء غير متزامن ولا يمكن التراجع عنه.',
    confirm: 'تأكيد',
    cancel: 'إلغاء',
    deleteConfirm: 'حذف كل البيانات'
  },
  notFound: {
    title: 'الصفحة غير موجودة',
    body: 'الصفحة التي تبحث عنها غير موجودة.',
    home: 'الذهاب إلى اليوم'
  }
}
