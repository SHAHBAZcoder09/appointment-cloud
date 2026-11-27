<template>
  <div class="n4-app"><div class="full-hero-bg"></div><div class="full-hero-scrim"></div><div class="n4-horizon-fade"></div>
    <header class="n4-nav-wrap">
      <div class="n4-nav">
        <div class="n4-brand" @click="goHome" :style="{ cursor: patient ? 'pointer' : 'default' }">
          
          <span class="n4-brand-text">N4 Clinic</span>
        </div>

        <nav v-if="patient" class="n4-nav-links">
          <button @click="tab = 'schedules'" :class="['n4-nav-btn', { active: tab === 'schedules' }]">Specialists</button>
          <button @click="tab = 'appointments'" :class="['n4-nav-btn', { active: tab === 'appointments' }]">My Bookings</button>
          <button v-if="isAdmin" @click="tab = 'demo'" :class="['n4-nav-btn', { active: tab === 'demo' }]">Governance</button>
        </nav>

        <div class="n4-nav-right">
          <template v-if="patient">
            <span class="n4-user-tag">
              {{ patient.name }}
              <span v-if="isAdmin" class="crown-glow" title="Admin"><svg width="16" height="16" viewBox="0 0 24 24" fill="#fbbf24"><path d="M3 18h18v2H3v-2zm0-2l2-9 4.5 4L12 5l2.5 6L19 7l2 9H3z"/></svg></span>
            </span>
            <button @click="logout" class="n4-pill-orange">Logout</button>
          </template>
          <a href="http://localhost:5174" class="n4-pill-subtle" style="text-decoration:none;">Admin Console ↗</a>
        </div>
      </div>
    </header>

    <main class="n4-main">
      <Transition name="fade-slide" mode="out-in">
        <LoginView v-if="!patient" key="login" @logged-in="onLoggedIn" />
        <ScheduleListView v-else-if="tab === 'schedules'" key="schedules" :patient="patient" :token="token" :isAdmin="isAdmin" @booked="tab = 'appointments'" />
        <MyAppointmentsView v-else-if="tab === 'appointments'" key="appointments" :patient="patient" :token="token" />
        <GovernanceDemoView v-else-if="tab === 'demo' && isAdmin" key="demo" />
      </Transition>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import LoginView from './components/LoginView.vue'
import ScheduleListView from './components/ScheduleListView.vue'
import MyAppointmentsView from './components/MyAppointmentsView.vue'
import GovernanceDemoView from './components/GovernanceDemoView.vue'

const patient = ref(null)
const token = ref('')
const tab = ref('schedules')
const isAdmin = computed(() => patient.value?.username === 'Denji')

function goHome() {
  if (patient.value) tab.value = 'schedules'
}
function onLoggedIn(data) {
  patient.value = data.patient
  token.value = data.token
  tab.value = 'schedules'
}
function logout() {
  patient.value = null
  token.value = ''
}
</script>

<style>
* { box-sizing: border-box; }
.n4-app { min-height: 100vh; background: #ffffff; color: #18181b; }
.n4-nav-wrap { padding: 18px 24px 0; width: min(92vw, 1520px); max-width: 1520px; margin: 0 auto; }
.n4-nav {
  display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 12px;
  padding: 10px 16px; border-radius: 999px;
  background: rgba(24, 24, 27, 0.92); color: #ffffff;
  backdrop-filter: blur(12px);
  box-shadow: 0 12px 32px rgba(24, 24, 27, 0.10);
  animation: slideDown 0.35s cubic-bezier(0.22, 1, 0.36, 1) both;
}
.n4-brand { display: flex; align-items: center; gap: 10px; transition: opacity 0.15s; }
.n4-brand:hover { opacity: 0.85; }
.n4-brand-icon {
  width: 30px; height: 30px; border-radius: 50%;
  background: linear-gradient(135deg, #e86c2c, #b45309);
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 700; color: #fff;
}
.n4-brand-text { font-weight: 600; font-size: 14px; letter-spacing: -0.02em; }
.n4-nav-links { display: flex; gap: 6px; }
.n4-nav-btn {
  padding: 7px 14px; border-radius: 999px; border: none; background: transparent;
  color: rgba(255,255,255,0.72); font-size: 13px; font-weight: 500; cursor: pointer;
  transition: background 0.18s ease, color 0.18s ease, transform 0.14s ease;
}
.n4-nav-btn:hover { color: #fff; transform: translateY(-1px); }
.n4-nav-btn:active { transform: scale(0.96); }
.n4-nav-btn.active { background: rgba(255,255,255,0.18); color: #fff; }
.n4-nav-right { display: flex; align-items: center; gap: 10px; }
.n4-user-tag { font-size: 13px; color: rgba(255,255,255,0.85); display: flex; align-items: center; gap: 6px; }
.n4-admin-dot { padding: 2px 8px; border-radius: 999px; background: rgba(232,108,44,0.25); color: #fdba74; font-size: 10px; font-weight: 700; }
.n4-pill-orange {
  padding: 7px 16px; border-radius: 999px; border: none;
  background: #e86c2c; color: #fff; font-size: 12px; font-weight: 600; cursor: pointer;
  transition: background 0.18s ease, transform 0.14s ease, box-shadow 0.18s ease;
}
.n4-pill-orange:hover { background: #d45d20; transform: translateY(-1px); box-shadow: 0 6px 16px rgba(232,108,44,0.3); }
.n4-pill-orange:active { transform: scale(0.96); }
.n4-pill-subtle { padding: 6px 14px; border-radius: 999px; background: rgba(255,255,255,0.12); font-size: 12px; color: #e4e4e7; }
.n4-main { width: min(92vw, 1520px); max-width: 1520px; margin: 0 auto; padding: 28px 24px 60px; }

button { transition: transform 0.14s cubic-bezier(0.22, 1, 0.36, 1), background 0.18s ease, box-shadow 0.18s ease, opacity 0.18s ease; }
button:active:not(:disabled) { transform: scale(0.97); }

.fade-slide-enter-active, .fade-slide-leave-active { transition: opacity 0.2s cubic-bezier(0.22, 1, 0.36, 1), transform 0.2s cubic-bezier(0.22, 1, 0.36, 1); }
.fade-slide-enter-from { opacity: 0; transform: translateY(8px); }
.fade-slide-leave-to { opacity: 0; transform: translateY(-6px); }

@keyframes slideDown { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes heroDrift {
  0% { transform: scale(1.03) translate(0px, 0px); }
  50% { transform: scale(1.12) translate(-14px, -8px); }
  100% { transform: scale(1.03) translate(0px, 0px); }
}
@keyframes orangeGlow {
  0%, 100% { opacity: 0.72; transform: translateY(0px) scale(1); }
  50% { opacity: 1; transform: translateY(-8px) scale(1.06); }
}
@keyframes fadeUp { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
.n4-app, .adm-app { position:relative; overflow-x:hidden; }
.n4-nav-wrap, .adm-nav-wrap, .n4-main, .adm-main { position:relative; z-index:2; }
.n4-horizon-fade { position:fixed; left:0; right:0; bottom:0; height:42vh; pointer-events:none; z-index:1; background:linear-gradient(to top, rgba(220,104,36,0.48) 0%, rgba(215,120,58,0.22) 38%, rgba(165,148,136,0.06) 70%, transparent 100%); animation:horizonBreathe 6.5s ease-in-out infinite; }
@keyframes horizonBreathe { 0%,100% { opacity:0.82; transform:translateY(0px); } 50% { opacity:1; transform:translateY(-6px); } }
.n4-app, .adm-app { position:relative; min-height:100vh; background:transparent !important; color:#ffffff !important; overflow-x:hidden; }
.n4-nav-wrap, .adm-nav-wrap, .n4-main, .adm-main { position:relative; z-index:2; }
.full-hero-bg { position:fixed; inset:-12px; pointer-events:none; z-index:0; background:url("/hero-bg.jpg") center/cover no-repeat; animation:heroDrift 14s ease-in-out infinite; }
.full-hero-scrim { position:fixed; inset:0; pointer-events:none; z-index:1; background:linear-gradient(155deg, rgba(20,18,17,0.42) 0%, rgba(55,42,35,0.28) 55%, rgba(190,90,34,0.42) 100%); }
.n4-nav, .adm-nav { background:rgba(24,22,20,0.46) !important; backdrop-filter:blur(20px) saturate(140%); -webkit-backdrop-filter:blur(20px) saturate(140%); border:1px solid rgba(255,255,255,0.22); box-shadow:0 16px 40px rgba(0,0,0,0.25); }
/* Empty glass popout cards */
.hero-bg-layer, .hero-scrim, .hero-orange-glow, .hero-bg, .hero-glow { display:none !important; }
.auth-hero, .auth-form-card, .hero-panel, .doc-card, .apt-card, .add-card, .empty-card, .modal-card, .ai-bar, .gov-bento, .hero-card, .hero-banner, .gov-panel, .login-card, .row-card, .add-grid, .empty-box { background:rgba(24,22,20,0.46) !important; backdrop-filter:blur(20px) saturate(140%); -webkit-backdrop-filter:blur(20px) saturate(140%); border:1px solid rgba(255,255,255,0.22) !important; box-shadow:0 20px 50px rgba(0,0,0,0.28) !important; color:#ffffff !important; }
.doc-card:hover, .apt-card:hover, .row-card:hover { transform:translateY(-4px); border-color:rgba(255,255,255,0.38) !important; box-shadow:0 24px 56px rgba(0,0,0,0.36) !important; }
.quota-box, .slip-rows { background:rgba(255,255,255,0.08) !important; border:1px solid rgba(255,255,255,0.16) !important; }
.auth-form-title, .doc-name, .fee-tag, .apt-doc, .sec-title, .quota-labels b, .field label, .form-stack label, .login-card h2, .slip-rows b { color:#ffffff !important; }
.doc-meta, .apt-meta, .quota-labels, .auth-switch a, .slip-rows span, .sec-title span { color:rgba(255,255,255,0.74) !important; }
.auth-badge-dark, .dept-tag, .apt-no, .pill-label, .pill-light, .mono-pill, .btn-light-pill, .dept-pill, .btn-outline { background:rgba(255,255,255,0.14) !important; border:1px solid rgba(255,255,255,0.24) !important; color:#ffffff !important; }
.dept-pill.active, .btn-ink, .book-btn:not(.disabled), .btn-ink-full, .btn-dark-pill { background:#ffffff !important; color:#18181b !important; }
.btn-ink:hover, .book-btn:not(.disabled):hover { background:#e86c2c !important; color:#ffffff !important; }
input, .search-input, .ai-input { background:rgba(255,255,255,0.10) !important; border:1px solid rgba(255,255,255,0.22) !important; color:#ffffff !important; }
input::placeholder { color:rgba(255,255,255,0.52) !important; }
@keyframes heroDrift { 0%,100% { transform:scale(1.03) translate(0,0); } 50% { transform:scale(1.12) translate(-14px,-8px); } }
.hover-word { display:inline-block; margin-right:0.26em; transition:transform 0.22s cubic-bezier(0.22,1,0.36,1), color 0.18s ease, text-shadow 0.22s ease; cursor:default; }
.hover-word:hover { transform:translateY(-6px) scale(1.06); color:#ffffff; text-shadow:0 8px 20px rgba(232,108,44,0.65); }
.crown-glow { display:inline-flex; align-items:center; filter:drop-shadow(0 0 6px rgba(251,191,36,0.9)) drop-shadow(0 0 12px rgba(232,108,44,0.7)); animation:crownPulse 2.4s ease-in-out infinite; }
@keyframes crownPulse { 0%,100% { transform:scale(1); filter:drop-shadow(0 0 6px rgba(251,191,36,0.85)); } 50% { transform:scale(1.12); filter:drop-shadow(0 0 12px rgba(251,191,36,1)); } }
</style>
