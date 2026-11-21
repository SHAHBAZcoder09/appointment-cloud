<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
      <h2 style="margin:0;">Available Doctor Schedules</h2>
      <button @click="loadSchedules" style="padding:6px 12px;cursor:pointer;">Refresh</button>
    </div>
    <p v-if="loading">Loading schedules...</p>
    <p v-if="statusMsg" :style="{ color: isError ? '#dc2626' : '#16a34a', fontWeight: 'bold' }">{{ statusMsg }}</p>
    <table v-if="schedules.length" style="width:100%;border-collapse:collapse;background:#fff;border-radius:8px;overflow:hidden;">
      <thead>
        <tr style="background:#e2e8f0;text-align:left;">
          <th style="padding:10px;">Doctor</th>
          <th style="padding:10px;">Department</th>
          <th style="padding:10px;">Date & Time</th>
          <th style="padding:10px;">Fee</th>
          <th style="padding:10px;">Slots Left</th>
          <th style="padding:10px;">Action</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="s in schedules" :key="s.id" style="border-top:1px solid #e5e7eb;">
          <td style="padding:10px;">{{ s.doctorName }}</td>
          <td style="padding:10px;">{{ s.department }}</td>
          <td style="padding:10px;">{{ s.scheduleDate }} ({{ s.timeSlot }})</td>
          <td style="padding:10px;">¥{{ s.fee }}</td>
          <td style="padding:10px;">{{ s.availableQuota }} / {{ s.totalQuota }}</td>
          <td style="padding:10px;">
            <button :disabled="s.availableQuota <= 0 || bookingId === s.id" @click="book(s.id)"
              style="padding:6px 12px;background:#16a34a;color:#fff;border:none;border-radius:5px;cursor:pointer;">
              {{ bookingId === s.id ? 'Booking...' : 'Book' }}
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps(['patient', 'token'])
const emit = defineEmits(['booked'])

const schedules = ref([])
const loading = ref(false)
const bookingId = ref(null)
const statusMsg = ref('')
const isError = ref(false)

async function loadSchedules() {
  loading.value = true
  try {
    const res = await fetch('http://localhost:7000/schedule/findAll')
    const data = await res.json()
    schedules.value = data.data || []
  } catch (e) {
    statusMsg.value = 'Failed to load schedules: ' + e.message
    isError.value = true
  } finally {
    loading.value = false
  }
}

async function book(scheduleId) {
  bookingId.value = scheduleId
  statusMsg.value = ''
  try {
    const res = await fetch(`http://localhost:7000/appointment/book/${props.patient.id}/${scheduleId}`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${props.token}` }
    })
    const data = await res.json()
    if (data.code === 200) {
      statusMsg.value = `Booked! Appointment No: ${data.data.appointmentNo}`
      isError.value = false
      await loadSchedules()
      emit('booked')
    } else {
      statusMsg.value = data.message || 'Booking failed'
      isError.value = true
    }
  } catch (e) {
    statusMsg.value = 'Error: ' + e.message
    isError.value = true
  } finally {
    bookingId.value = null
  }
}

onMounted(loadSchedules)
</script>
