<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
      <h2 style="margin:0;">My Appointments</h2>
      <button @click="loadAppointments" style="padding:6px 12px;cursor:pointer;">Refresh</button>
    </div>
    <p v-if="loading">Loading appointments...</p>
    <p v-if="msg" style="color:#2563eb;font-weight:bold;">{{ msg }}</p>
    <table v-if="appointments.length" style="width:100%;border-collapse:collapse;background:#fff;border-radius:8px;overflow:hidden;">
      <thead>
        <tr style="background:#e2e8f0;text-align:left;">
          <th style="padding:10px;">Appointment No</th>
          <th style="padding:10px;">Patient</th>
          <th style="padding:10px;">Doctor & Dept</th>
          <th style="padding:10px;">Time</th>
          <th style="padding:10px;">Status</th>
          <th style="padding:10px;">Action</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="a in appointments" :key="a.id" style="border-top:1px solid #e5e7eb;">
          <td style="padding:10px;">{{ a.appointmentNo }}</td>
          <td style="padding:10px;">{{ a.patient?.name || a.patientId }}</td>
          <td style="padding:10px;">{{ a.schedule?.doctorName }} ({{ a.schedule?.department }})</td>
          <td style="padding:10px;">{{ a.schedule?.scheduleDate }} {{ a.schedule?.timeSlot }}</td>
          <td style="padding:10px;"><b>{{ a.status }}</b></td>
          <td style="padding:10px;">
            <button v-if="a.status === 'BOOKED'" @click="cancel(a.id)"
              style="padding:5px 10px;background:#dc2626;color:#fff;border:none;border-radius:5px;cursor:pointer;">
              Cancel
            </button>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else-if="!loading" style="color:#666;">No appointments yet.</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps(['patient', 'token'])
const emit = defineEmits(['cancelled'])

const appointments = ref([])
const loading = ref(false)
const msg = ref('')

async function loadAppointments() {
  loading.value = true
  try {
    const res = await fetch(`http://localhost:7000/appointment/list/${props.patient.id}`, {
      headers: { Authorization: `Bearer ${props.token}` }
    })
    const data = await res.json()
    appointments.value = data.data || []
  } finally {
    loading.value = false
  }
}

async function cancel(id) {
  const res = await fetch(`http://localhost:7000/appointment/cancel/${id}`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${props.token}` }
  })
  const data = await res.json()
  msg.value = data.code === 200 ? 'Appointment cancelled and slot released!' : data.message
  await loadAppointments()
  emit('cancelled')
}

defineExpose({ loadAppointments })
onMounted(loadAppointments)
</script>
