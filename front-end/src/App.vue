<template>
  <div :class="['app-shell', { dark: darkMode }]">
    <div style="max-width:960px;margin:0 auto;padding:20px;">
      <header class="app-header">
        <h1 style="margin:0;font-size:22px;">N4 Clinic Appointment System</h1>
        <div style="display:flex;flex-wrap:wrap;gap:6px;align-items:center;">
          <button @click="toggleDark" class="btn-outline">
            {{ darkMode ? '☀️ Light' : '🌙 Dark' }}
          </button>
          <template v-if="patient">
            <span style="margin:0 8px;">
              Welcome, <b>{{ patient.name }}</b>
              <span v-if="isAdmin" style="margin-left:4px;padding:2px 6px;background:#fef3c7;color:#92400e;border-radius:4px;font-size:11px;font-weight:bold;">ADMIN</span>
            </span>
            <button @click="tab = 'schedules'" class="btn-outline">Doctors</button>
            <button @click="tab = 'appointments'" class="btn-outline">My Appointments</button>
            <button v-if="isAdmin" @click="tab = 'demo'" class="btn-outline">Governance Demo</button>
            <button @click="logout" class="btn-outline">Logout</button>
          </template>
        </div>
      </header>

      <LoginView v-if="!patient" @logged-in="onLoggedIn" />

      <div v-else>
        <ScheduleListView v-if="tab === 'schedules'" :patient="patient" :token="token" :isAdmin="isAdmin" @booked="tab = 'appointments'" />
        <MyAppointmentsView v-if="tab === 'appointments'" :patient="patient" :token="token" />
        <GovernanceDemoView v-if="tab === 'demo' && isAdmin" />
      </div>
    </div>
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
const darkMode = ref(localStorage.getItem('n4_dark') === 'true')
const isAdmin = computed(() => patient.value?.username === 'Denji')

function toggleDark() {
  darkMode.value = !darkMode.value
  localStorage.setItem('n4_dark', darkMode.value)
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
.app-shell {
  min-height: 100vh;
  background: #f5f7fa;
  color: #0f172a;
  transition: background 0.2s, color 0.2s;
}
.app-shell.dark {
  background: #0f172a;
  color: #f1f5f9;
}
.app-header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border-bottom: 2px solid #cbd5e1;
  padding-bottom: 12px;
  gap: 10px;
}
.dark .app-header {
  border-bottom-color: #334155;
}
.card, .dark table {
  background: #ffffff;
  border: 1px solid #cbd5e1;
}
.dark .card, .dark table, .dark form {
  background: #1e293b !important;
  color: #f1f5f9 !important;
  border-color: #334155 !important;
}
.dark thead tr {
  background: #334155 !important;
  color: #f8fafc !important;
}
.dark tbody tr {
  border-top-color: #334155 !important;
}
.input, .dark input {
  width: 100%;
  padding: 8px;
  margin-top: 4px;
  box-sizing: border-box;
  border: 1px solid #cbd5e1;
  border-radius: 5px;
}
.dark input {
  background: #0f172a;
  color: #f8fafc;
  border-color: #475569;
}
.btn-outline {
  padding: 6px 12px;
  cursor: pointer;
  background: #fff;
  color: #0f172a;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
}
.dark .btn-outline {
  background: #1e293b;
  color: #f1f5f9;
  border-color: #475569;
}
</style>
