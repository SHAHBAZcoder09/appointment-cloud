<template>
  <div>
    <!-- Live Nacos Notice -->
    <div v-if="clinicConfig" style="padding:10px 14px;border-radius:8px;margin-bottom:14px;background:#eef2ff;border:1px solid #c7d2fe;display:flex;justify-content:space-between;">
      <span><b>Clinic Notice (Nacos):</b> {{ clinicConfig.bookingNotice }}</span>
      <b>{{ clinicConfig.bookingOpen ? 'OPEN' : 'PAUSED' }}</b>
    </div>

    <!-- Top Controls -->
    <div style="display:flex;flex-wrap:wrap;gap:8px;justify-content:space-between;align-items:center;margin-bottom:12px;">
      <h2 style="margin:0;">Doctor Schedules</h2>
      <div style="display:flex;gap:8px;">
        <input v-model="search" placeholder="Search doctor or dept..." style="padding:6px 10px;" />
        <button v-if="isAdmin" @click="showAdd = !showAdd" style="padding:6px 12px;cursor:pointer;">{{ showAdd ? 'Close Form' : '+ Add Doctor' }}</button>
        <button @click="loadAll" style="padding:6px 12px;cursor:pointer;">Refresh</button>
      </div>
    </div>

    <!-- Add Doctor Form -->
    <form v-if="showAdd" @submit.prevent="addDoctor" style="background:#fff;padding:16px;border:1px solid #ddd;border-radius:8px;margin-bottom:16px;display:grid;grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:10px;">
      <input v-model="newDoc.doctorName" placeholder="Doctor Name (e.g. Dr. Ali)" required style="padding:7px;" />
      <input v-model="newDoc.department" placeholder="Department" required style="padding:7px;" />
      <input v-model="newDoc.scheduleDate" placeholder="Date (2026-11-25)" required style="padding:7px;" />
      <input v-model="newDoc.timeSlot" placeholder="Time (09:00-11:00)" required style="padding:7px;" />
      <input v-model.number="newDoc.totalQuota" type="number" placeholder="Slots" required style="padding:7px;" />
      <input v-model.number="newDoc.fee" type="number" placeholder="Fee (¥)" required style="padding:7px;" />
      <button type="submit" style="padding:8px;background:#2563eb;color:#fff;border:none;border-radius:5px;cursor:pointer;">Save Schedule</button>
    </form>

    <p v-if="statusMsg" :style="{ color: isError ? '#dc2626' : '#16a34a', fontWeight: 'bold' }">{{ statusMsg }}</p>
    <p v-if="loading">Loading schedules...</p>

    <table v-if="filtered.length" style="width:100%;border-collapse:collapse;background:#fff;border:1px solid #ddd;">
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
        <tr v-for="s in filtered" :key="s.id" style="border-top:1px solid #e5e7eb;">
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
import { ref, reactive, computed, onMounted } from 'vue'

const props = defineProps(['patient', 'token', 'isAdmin'])
const emit = defineEmits(['booked'])

const schedules = ref([])
const clinicConfig = ref(null)
const search = ref('')
const showAdd = ref(false)
const newDoc = reactive({ doctorName: '', department: '', scheduleDate: '2026-11-25', timeSlot: '09:00-11:00', totalQuota: 5, fee: 45 })
const loading = ref(false)
const bookingId = ref(null)
const statusMsg = ref('')
const isError = ref(false)

const filtered = computed(() =>
  schedules.value.filter(s =>
    s.doctorName.toLowerCase().includes(search.value.toLowerCase()) ||
    s.department.toLowerCase().includes(search.value.toLowerCase())
  )
)

async function loadAll() {
  loading.value = true
  try {
    const [sRes, cRes] = await Promise.all([
      fetch('http://localhost:7000/schedule/findAll'),
      fetch('http://localhost:7000/schedule/config')
    ])
    const sData = await sRes.json()
    schedules.value = sData.data || []
    if (cRes.ok) { const cData = await cRes.json(); clinicConfig.value = cData.data }
  } catch (e) {
    statusMsg.value = 'Failed to load: ' + e.message; isError.value = true
  } finally { loading.value = false }
}

async function addDoctor() {
  const res = await fetch('http://localhost:7000/schedule/add', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${props.token}` },
    body: JSON.stringify(newDoc)
  })
  const data = await res.json()
  if (data.code === 200) {
    statusMsg.value = `Added ${newDoc.doctorName}!`; isError.value = false
    showAdd.value = false
    newDoc.doctorName = ''; newDoc.department = ''
    await loadAll()
  } else {
    statusMsg.value = data.message || 'Failed to add'; isError.value = true
  }
}

async function book(scheduleId) {
  bookingId.value = scheduleId; statusMsg.value = ''
  try {
    const res = await fetch(`http://localhost:7000/appointment/book/${props.patient.id}/${scheduleId}`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${props.token}` }
    })
    const data = await res.json()
    if (data.code === 200) {
      statusMsg.value = `Booked! Appointment No: ${data.data.appointmentNo}`; isError.value = false
      await loadAll()
      emit('booked')
    } else {
      statusMsg.value = data.message || 'Booking failed'; isError.value = true
    }
  } catch (e) {
    statusMsg.value = 'Error: ' + e.message; isError.value = true
  } finally { bookingId.value = null }
}

onMounted(loadAll)
</script>
