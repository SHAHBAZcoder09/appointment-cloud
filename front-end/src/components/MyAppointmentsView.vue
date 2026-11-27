<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:22px;">
      <div>
        <div class="pill-label">Patient Records</div>
        <h2 class="sec-title">Your upcoming <span>&amp; past bookings.</span></h2>
      </div>
      <button @click="loadAppointments" class="btn-light-pill">Refresh</button>
    </div>

    <div class="kpi-row">
      <div class="kpi-card"><span>Total Bookings</span><b>{{ appointments.length }}</b></div>
      <div class="kpi-card"><span>Active</span><b>{{ appointments.filter(a => a.status === 'BOOKED').length }}</b></div>
      <div class="kpi-card"><span>Cancelled</span><b>{{ appointments.filter(a => a.status === 'CANCELLED').length }}</b></div>
      <div class="kpi-card"><span>Active Fees</span><b>¥{{ appointments.filter(a => a.status === 'BOOKED').reduce((s, a) => s + (Number(a.schedule?.fee) || 0), 0) }}</b></div>
    </div>

    <p v-if="msg" class="info-banner">{{ msg }}</p>
    <p v-if="loading" style="color:rgba(255,255,255,0.72);">Loading appointments...</p>

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

    <!-- SLIP 2: Thermal Paper Receipt -->
    <div v-if="receipt" class="modal-backdrop" @click.self="receipt = null">
      <div class="paper-receipt">
        <div :class="['stamp', receipt.status === 'BOOKED' ? 'ok' : 'void']">{{ receipt.status }}</div>
        <div style="text-align:center;border-bottom:1px dashed #9ca3af;padding-bottom:12px;margin-bottom:14px;">
          <div style="font-size:17px;font-weight:800;letter-spacing:1px;">N4 CLINIC MEDICAL CENTER</div>
          <div style="font-size:11px;color:#52525b;">OUTPATIENT REGISTRATION RECEIPT</div>
          <div style="font-size:12px;margin-top:4px;font-weight:700;">NO: {{ receipt.appointmentNo }}</div>
        </div>

        <div class="paper-rows">
          <div><span>PATIENT</span><b>{{ receipt.patient?.name }}</b></div>
          <div><span>CONTACT</span><b>{{ receipt.patient?.phone }}</b></div>
          <div><span>PHYSICIAN</span><b>{{ receipt.schedule?.doctorName }}</b></div>
          <div><span>DEPT</span><b>{{ receipt.schedule?.department }}</b></div>
          <div><span>VISIT DATE</span><b>{{ receipt.schedule?.scheduleDate }}</b></div>
          <div><span>TIME</span><b>{{ receipt.schedule?.timeSlot }}</b></div>
        </div>

        <div style="border-top:1px dashed #9ca3af;border-bottom:1px dashed #9ca3af;padding:10px 0;margin:14px 0;display:flex;justify-content:space-between;font-size:15px;font-weight:800;">
          <span>TOTAL FEE</span>
          <span>¥{{ receipt.schedule?.fee }}</span>
        </div>
        <div style="text-align:center;font-size:10px;color:#71717a;margin-bottom:14px;">*** PLEASE ARRIVE 10 MINS EARLY ***</div>

        <div style="display:flex;gap:8px;">
          <button @click="printSlip" style="flex:1;padding:9px;background:#e86c2c;color:#fff;border:none;border-radius:6px;font-weight:700;cursor:pointer;">Print Receipt</button>
          <button @click="receipt = null" style="flex:1;padding:9px;background:#18181b;color:#fff;border:none;border-radius:6px;font-weight:700;cursor:pointer;">Close</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
const props = defineProps(['patient', 'token'])
const emit = defineEmits(['cancelled'])
const appointments = ref([]); const loading = ref(false); const msg = ref(''); const receipt = ref(null)
function printSlip() { window.print() }
async function loadAppointments() {
  loading.value = true
  try {
    const res = await fetch(`http://localhost:7000/appointment/list/${props.patient.id}`, { headers: { Authorization: `Bearer ${props.token}` } })
    const data = await res.json(); appointments.value = data.data || []
  } finally { loading.value = false }
}
async function cancel(id) {
  const res = await fetch(`http://localhost:7000/appointment/cancel/${id}`, { method: 'POST', headers: { Authorization: `Bearer ${props.token}` } })
  const data = await res.json(); msg.value = data.code === 200 ? 'Appointment cancelled and quota released.' : data.message
  await loadAppointments(); emit('cancelled')
}
onMounted(loadAppointments)
</script>

<style scoped>
.pill-label { display:inline-block; padding:5px 12px; border-radius:999px; font-size:11px; font-weight:600; margin-bottom:8px; }
.sec-title { margin:0; font-size:28px; letter-spacing:-0.03em; font-weight:600; }
.btn-light-pill { padding:8px 16px; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; }
.btn-cancel-pill { padding:8px 16px; border-radius:999px; border:none; background:#fef2f2; color:#dc2626; font-size:13px; font-weight:600; cursor:pointer; }
.info-banner { padding:12px 16px; border-radius:14px; background:rgba(24,22,20,0.55); border:1px solid rgba(251,191,36,0.35); color:#fdba74; backdrop-filter:blur(16px); font-size:13px; font-weight:600; margin-bottom:16px; }
.kpi-row { display:grid; grid-template-columns:repeat(auto-fit, minmax(160px,1fr)); gap:12px; margin-bottom:18px; }
.kpi-card { border-radius:18px; padding:16px 18px; background:rgba(24,22,20,0.46); backdrop-filter:blur(20px); border:1px solid rgba(255,255,255,0.22); color:#fff; }
.kpi-card span { display:block; font-size:12px; color:rgba(255,255,255,0.72); margin-bottom:4px; }
.kpi-card b { font-size:22px; font-weight:600; }
.apt-list { display:grid; gap:14px; }
.apt-card { border-radius:20px; padding:22px 24px; display:flex; flex-wrap:wrap; justify-content:space-between; align-items:center; gap:14px; }
.apt-no { font-size:12px; font-weight:700; padding:4px 10px; border-radius:999px; }
.status-pill { font-size:11px; font-weight:700; padding:4px 10px; border-radius:999px; }
.status-pill.booked { background:#ecfdf5; color:#059669; }
.status-pill.cancelled { background:#fef2f2; color:#dc2626; }
.apt-doc { font-size:18px; font-weight:600; }
.apt-doc span { font-weight:500; font-size:15px; opacity:0.75; }
.apt-meta { font-size:13px; margin-top:4px; }
.empty-card { border-radius:20px; padding:40px; text-align:center; }
.modal-backdrop { position:fixed; inset:0; background:rgba(10,10,12,0.65); backdrop-filter:blur(8px); display:flex; align-items:center; justify-content:center; z-index:50; }
.paper-receipt { position:relative; width:92%; max-width:360px; padding:26px 24px; background:#fffdf7; color:#18181b; font-family:'Courier New',monospace; box-shadow:0 24px 60px rgba(0,0,0,0.45); border-top:6px solid #e86c2c; }
.paper-rows { display:grid; gap:8px; font-size:12px; }
.paper-rows div { display:flex; justify-content:space-between; }
.paper-rows span { color:#52525b; }
.stamp { position:absolute; top:56px; right:22px; padding:4px 10px; border:3px solid; border-radius:6px; font-weight:900; font-size:14px; transform:rotate(-14deg); opacity:0.85; pointer-events:none; }
.stamp.ok { border-color:#16a34a; color:#16a34a; }
.stamp.void { border-color:#dc2626; color:#dc2626; }
</style>
