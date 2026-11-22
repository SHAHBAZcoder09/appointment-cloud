<template>
  <div style="max-width:960px;margin:0 auto;padding:20px;">
    <header style="display:flex;flex-wrap:wrap;justify-content:space-between;align-items:center;margin-bottom:20px;border-bottom:2px solid #e2e8f0;padding-bottom:12px;">
      <h1 style="margin:0;font-size:22px;">N4 Clinic Appointment System</h1>
      <div v-if="patient" style="display:flex;flex-wrap:wrap;gap:6px;align-items:center;">
        <span style="margin-right:8px;">
          Welcome, <b>{{ patient.name }}</b>
          <span v-if="isAdmin" style="margin-left:4px;padding:2px 6px;background:#fef3c7;color:#92400e;border-radius:4px;font-size:11px;font-weight:bold;">ADMIN</span>
        </span>
        <button @click="tab = 'schedules'" style="padding:6px 12px;cursor:pointer;">Doctors</button>
        <button @click="tab = 'appointments'" style="padding:6px 12px;cursor:pointer;">My Appointments</button>
        <button v-if="isAdmin" @click="tab = 'demo'" style="padding:6px 12px;cursor:pointer;">Governance Demo</button>
        <button @click="logout" style="padding:6px 12px;cursor:pointer;">Logout</button>
      </div>
    </header>

    <LoginView v-if="!patient" @logged-in="onLoggedIn" />

    <div v-else>
      <ScheduleListView v-if="tab === 'schedules'" :patient="patient" :token="token" :isAdmin="isAdmin" @booked="tab = 'appointments'" />
      <MyAppointmentsView v-if="tab === 'appointments'" :patient="patient" :token="token" />
      <GovernanceDemoView v-if="tab === 'demo' && isAdmin" />
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
