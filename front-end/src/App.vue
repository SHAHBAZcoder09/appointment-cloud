<template>
  <div class="n4-app">
    <header class="n4-nav-wrap">
      <div class="n4-nav">
        <div class="n4-brand">
          <div class="n4-brand-icon">N4</div>
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
              <span v-if="isAdmin" class="n4-admin-dot">ADMIN</span>
            </span>
            <button @click="logout" class="n4-pill-orange">Logout</button>
          </template>
          <span v-else class="n4-pill-subtle">Patient Portal</span>
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
.n4-nav-wrap { padding: 18px 24px 0; max-width: 1120px; margin: 0 auto; }
.n4-nav {
  display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 12px;
  padding: 10px 16px; border-radius: 999px;
  background: rgba(24, 24, 27, 0.92); color: #ffffff;
  backdrop-filter: blur(12px);
  box-shadow: 0 12px 32px rgba(24, 24, 27, 0.10);
  animation: slideDown 0.35s cubic-bezier(0.22, 1, 0.36, 1) both;
}
.n4-brand { display: flex; align-items: center; gap: 10px; }
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
.n4-main { max-width: 1120px; margin: 0 auto; padding: 28px 24px 60px; }

/* Global smooth & fast transitions */
button { transition: transform 0.14s cubic-bezier(0.22, 1, 0.36, 1), background 0.18s ease, box-shadow 0.18s ease, opacity 0.18s ease; }
button:active:not(:disabled) { transform: scale(0.97); }

.fade-slide-enter-active, .fade-slide-leave-active { transition: opacity 0.2s cubic-bezier(0.22, 1, 0.36, 1), transform 0.2s cubic-bezier(0.22, 1, 0.36, 1); }
.fade-slide-enter-from { opacity: 0; transform: translateY(8px); }
.fade-slide-leave-to { opacity: 0; transform: translateY(-6px); }

@keyframes slideDown { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes heroDrift {
  0% { transform: scale(1.02) translate(0px, 0px); }
  50% { transform: scale(1.10) translate(-12px, -6px); }
  100% { transform: scale(1.02) translate(0px, 0px); }
}
@keyframes fadeUp { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>
