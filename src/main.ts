import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import "./assets/global.css"
import {createPinia} from 'pinia'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faBars, faBook, faCheck, faChevronLeft, faCode, faDatabase, faDesktop, faDiagnoses, faDiagramProject, faLaptopCode, faNewspaper, faPlus, faRightFromBracket, faScrewdriverWrench, faTerminal, faTriangleExclamation, faUser, faXmark } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

const pinia = createPinia();

library.add(
  faDesktop, 
  faUser, 
  faDiagramProject, 
  faScrewdriverWrench,
  faRightFromBracket,
  faPlus,
  faChevronLeft,
  faDiagnoses,
  faBook,
  faDatabase,
  faNewspaper,
  faBars,
  faCheck,
  faTriangleExclamation,
  faXmark,
  faBook,
  faCode,
  faTerminal,
  faLaptopCode,
)

createApp(App)
  .use(router)
  .use(pinia)
  .component('font-awesome-icon', FontAwesomeIcon)
  .mount('#app')
