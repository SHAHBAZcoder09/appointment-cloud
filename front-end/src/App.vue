<template>
  <div style="max-width:960px;margin:0 auto;padding:20px;">
    <header style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px;border-bottom:2px solid #e2e8f0;padding-bottom:12px;">
      <h1 style="margin:0;font-size:22px;">Clinic Appointment System</h1>
      <div v-if="patient">
        <span style="margin-right:12px;">Welcome, <b>{{ patient.name }}</b></span>
        <button @click="tab = 'schedules'" style="margin-right:6px;padding:6px 12px;cursor:pointer;">Doctors</button>
        <button @click="tab = 'appointments'" style="margin-right:6px;padding:6px 12px;cursor:pointer;">My Appointments</button>
        <button @click="logout" style="padding:6px 12px;cursor:pointer;">Logout</button>
      </div>
    </header>

    <LoginView v-if="!patient" @logged-in="onLoggedIn" />

    <div v-else>
      <ScheduleListView v-if="tab === 'schedules'" :patient="patient" :token="token" @booked="tab = 'appointments'" />
      <MyAppointmentsView v-if="tab === 'appointments'" :patient="patient" :token="token" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import LoginView from './components/LoginView.vue'
import ScheduleListView from './components/ScheduleListView.vue'
import MyAppointmentsView from './components/MyAppointmentsView.vue'

const patient = ref(null)
const token = ref('')
const tab = ref('schedules')

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
