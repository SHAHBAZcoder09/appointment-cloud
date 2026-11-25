<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:22px;">
      <div>
        <div class="pill-label">Patient Records</div>
        <h2 class="sec-title">Your upcoming <span>&amp; past bookings.</span></h2>
      </div>
      <button @click="loadAppointments" class="btn-light-pill">Refresh</button>
    </div>

    <p v-if="msg" class="info-banner">{{ msg }}</p>
    <p v-if="loading" style="color:#71717a;">Loading appointments...</p>

    <div v-if="appointments.length" class="apt-list">
      <div v-for="a in appointments" :key="a.id" class="apt-card">
        <div>
          <div style="display:flex;align-items:center;gap:10px;margin-bottom:8px;flex-wrap:wrap;">
            <span class="apt-no">{{ a.appointmentNo }}</span>
            <span :class="['status-pill', a.status === 'BOOKED' ? 'booked' : 'cancelled']">{{ a.status }}</span>
          </div>
          <div class="apt-doc">{{ a.schedule?.doctorName }} <span>· {{ a.schedule?.department }}</span></div>
          <div class="apt-meta">{{ a.schedule?.scheduleDate }} ({{ a.schedule?.timeSlot }}) · Fee: ¥{{ a.schedule?.fee }}</div>
        </div>
        <div style="display:flex;gap:8px;">
          <button @click="receipt = a" class="btn-light-pill">View Slip</button>
          <button v-if="a.status === 'BOOKED'" @click="cancel(a.id)" class="btn-cancel-pill">Cancel</button>
        </div>
      </div>
    </div>
    <div v-else-if="!loading" class="empty-card">No appointments yet. Visit Specialists to book your first visit.</div>

    <!-- Frosted Receipt Modal -->
    <div v-if="receipt" class="modal-backdrop" @click.self="receipt = null">
      <div class="modal-card">
        <div class="pill-label" style="margin-bottom:10px;">N4 Clinic — Confirmation Slip</div>
        <h3 style="margin:0 0 16px;font-size:22px;letter-spacing:-0.02em;">{{ receipt.appointmentNo }}</h3>
        <div class="slip-rows">
          <div><span>Patient</span><b>{{ receipt.patient?.name }} ({{ receipt.patient?.phone }})</b></div>
          <div><span>Specialist</span><b>{{ receipt.schedule?.doctorName }}</b></div>
          <div><span>Department</span><b>{{ receipt.schedule?.department }}</b></div>
          <div><span>Schedule</span><b>{{ receipt.schedule?.scheduleDate }} · {{ receipt.schedule?.timeSlot }}</b></div>
          <div><span>Consultation Fee</span><b>¥{{ receipt.schedule?.fee }}</b></div>
          <div><span>Status</span><b>{{ receipt.status }}</b></div>
        </div>
        <button @click="receipt = null" class="btn-ink-full">Close Slip</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps(['patient', 'token'])
const emit = defineEmits(['cancelled'])

const appointments = ref([])
const loading = ref(false)
const msg = ref('')
const receipt = ref(null)

async function loadAppointments() {
  loading.value = true
  try {
    const res = await fetch(`http://localhost:7000/appointment/list/${props.patient.id}`, {
      headers: { Authorization: `Bearer ${props.token}` }
    })
    const data = await res.json()
    appointments.value = data.data || []
  } finally { loading.value = false }
}

async function cancel(id) {
  const res = await fetch(`http://localhost:7000/appointment/cancel/${id}`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${props.token}` }
  })
  const data = await res.json()
  msg.value = data.code === 200 ? 'Appointment cancelled and quota released.' : data.message
  await loadAppointments(); emit('cancelled')
}

onMounted(loadAppointments)
</script>

<style scoped>
.pill-label { display: inline-block; padding: 5px 12px; border-radius: 999px; background: #faf9f7; border: 1px solid #e4e4e7; font-size: 11px; font-weight: 600; color: #71717a; margin-bottom: 8px; }
.sec-title { margin: 0; font-size: 28px; letter-spacing: -0.03em; font-weight: 600; }
.sec-title span { color: #8e8e93; font-weight: 500; }
.btn-light-pill { padding: 8px 16px; border-radius: 999px; border: 1px solid #e4e4e7; background: #fff; color: #18181b; font-size: 13px; font-weight: 500; cursor: pointer; }
.btn-cancel-pill { padding: 8px 16px; border-radius: 999px; border: none; background: #fef2f2; color: #dc2626; font-size: 13px; font-weight: 600; cursor: pointer; }
.info-banner { padding: 12px 16px; border-radius: 14px; background: #faf9f7; border: 1px solid #eceae4; font-size: 13px; font-weight: 600; margin-bottom: 16px; }
.apt-list { display: grid; gap: 14px; }
.apt-card {
  border-radius: 20px; padding: 22px 24px; background: #faf9f7; border: 1px solid #eceae4;
  display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 14px;
}
.apt-no { font-size: 12px; font-weight: 700; padding: 4px 10px; border-radius: 999px; background: #fff; border: 1px solid #e4e4e7; }
.status-pill { font-size: 11px; font-weight: 700; padding: 4px 10px; border-radius: 999px; }
.status-pill.booked { background: #ecfdf5; color: #059669; }
.status-pill.cancelled { background: #fef2f2; color: #dc2626; }
.apt-doc { font-size: 18px; font-weight: 600; letter-spacing: -0.02em; }
.apt-doc span { color: #71717a; font-weight: 500; font-size: 15px; }
.apt-meta { font-size: 13px; color: #71717a; margin-top: 4px; }
.empty-card { border-radius: 20px; padding: 40px; text-align: center; background: #faf9f7; border: 1px solid #eceae4; color: #71717a; }
.modal-backdrop { position: fixed; inset: 0; background: rgba(24,24,27,0.45); backdrop-filter: blur(6px); display: flex; align-items: center; justify-content: center; z-index: 50; }
.modal-card { background: #fff; padding: 28px; border-radius: 24px; max-width: 400px; width: 92%; box-shadow: 0 24px 60px rgba(0,0,0,0.2); }
.slip-rows { display: grid; gap: 10px; margin-bottom: 20px; padding: 14px; border-radius: 16px; background: #faf9f7; border: 1px solid #eceae4; }
.slip-rows div { display: flex; justify-content: space-between; font-size: 13px; }
.slip-rows span { color: #71717a; }
.btn-ink-full { width: 100%; padding: 11px; border-radius: 999px; border: none; background: #18181b; color: #fff; font-weight: 600; cursor: pointer; }
</style>
