import Vue from 'vue'
import VueI18n from 'vue-i18n'
import en from './locales/en/index'
import zh from './locales/zh/index'

Vue.use(VueI18n)

// 默认语言
const loadLanguage = 'zh'
const messages = {
  'en': en,
  'zh': zh
}

function getLanguage() {
  localStorage.getItem('lang') ? null : localStorage.setItem('lang', loadLanguage)
  let locale = localStorage.getItem('lang')
  if (!(locale in messages)) locale = loadLanguage
  return locale
}

const i18n = new VueI18n({
  locale: getLanguage(),
  messages,
  silentTranslationWarn: true // 用于去掉警告
})

Vue.prototype._i18n = i18n
export default i18n
