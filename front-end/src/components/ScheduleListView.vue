<template>
  <div>
    <!-- Animated Image Hero + Live Nacos Config -->
    <div class="hero-panel">
      <div class="hero-bg-layer"></div>
      <div class="hero-scrim"></div>
      <div class="hero-orange-glow"></div>
      <div class="hero-inner">
        <div>
          <div class="hero-pill">
            <span :class="['dot', clinicConfig?.bookingOpen ? 'open' : 'paused']"></span>
            <span>{{ clinicConfig?.bookingNotice || 'Clinic booking is OPEN!' }}</span>
          </div>
          <h1 class="hero-title">
            Board-Certified<br />
            <span>Clinic Specialists.</span>
          </h1>
        </div>
        <div class="hero-metrics">
          <div class="metric-glass">
            <b>{{ schedules.length }}</b>
            <span>Doctors Available</span>
          </div>
          <div class="metric-glass">
            <b>{{ totalOpenSlots }}</b>
            <span>Open Slots Today</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Search & Filter Bar -->
    <div class="toolbar">
      <div class="dept-pills">
        <button v-for="dept in departments" :key="dept" @click="selectedDept = dept"
          :class="['dept-pill', { active: selectedDept === dept }]">{{ dept }}</button>
      </div>
      <div class="toolbar-right">
        <input v-model="search" placeholder="Search doctor or specialty..." class="search-input" />
        <button v-if="isAdmin" @click="showAdd = !showAdd" class="btn-dark-pill">
          {{ showAdd ? 'Close' : '+ Add Doctor' }}
        </button>
        <button @click="loadAll" class="btn-light-pill">Refresh</button>
      </div>
    </div>

    <!-- Admin Add Doctor Form -->
    <form v-if="showAdd && isAdmin" @submit.prevent="addDoctor" class="add-card">
      <input v-model="newDoc.doctorName" placeholder="Doctor Name (e.g. Dr. Ali)" required />
      <input v-model="newDoc.department" placeholder="Department" required />
      <input v-model="newDoc.scheduleDate" placeholder="Date (2026-11-25)" required />
      <input v-model="newDoc.timeSlot" placeholder="Time (09:00-11:00)" required />
      <input v-model.number="newDoc.totalQuota" type="number" placeholder="Slots" required />
      <input v-model.number="newDoc.fee" type="number" placeholder="Fee (¥)" required />
      <button type="submit" class="btn-orange-pill">Save Schedule</button>
    </form>

    <p v-if="statusMsg" :class="['feedback', isError ? 'err' : 'ok']">{{ statusMsg }}</p>
    <p v-if="loading" style="color:#71717a;font-size:14px;">Loading specialists...</p>

    <!-- Bento Doctor Cards -->
    <div class="doc-grid">
      <div v-for="(s, idx) in filtered" :key="s.id" class="doc-card" :style="{ animationDelay: (idx * 45) + 'ms' }">
        <div>
          <div class="doc-top">
            <span class="dept-tag">{{ s.department }}</span>
            <span class="fee-tag">¥{{ s.fee }}</span>
          </div>
          <h3 class="doc-name">{{ s.doctorName }}</h3>
          <div class="doc-meta">{{ s.scheduleDate }} · {{ s.timeSlot }}</div>

          <div class="quota-box">
            <div class="quota-labels">
              <span>Available Quota</span>
              <b>{{ s.availableQuota }} / {{ s.totalQuota }}</b>
            </div>
            <div class="quota-track">
              <div class="quota-fill" :style="{ width: (s.availableQuota / s.totalQuota * 100) + '%' }"></div>
            </div>
          </div>
        </div>

        <button
          :disabled="s.availableQuota <= 0 || bookingId === s.id || (clinicConfig && !clinicConfig.bookingOpen)"
          @click="book(s.id)"
          :class="['book-btn', { disabled: s.availableQuota <= 0 || (clinicConfig && !clinicConfig.bookingOpen) }]">
          {{ bookingId === s.id ? 'Booking...' : (s.availableQuota <= 0 ? 'Fully Booked' : 'Book Appointment') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

const props = defineProps(['patient', 'token', 'isAdmin'])
const emit = defineEmits(['booked'])

const schedules = ref([])
const clinicConfig = ref(null)
const selectedDept = ref('All')
const search = ref('')
const showAdd = ref(false)
const newDoc = reactive({ doctorName: '', department: '', scheduleDate: '2026-11-25', timeSlot: '09:00-11:00', totalQuota: 5, fee: 45 })
const loading = ref(false)
const bookingId = ref(null)
const statusMsg = ref('')
const isError = ref(false)

const departments = computed(() => ['All', ...new Set(schedules.value.map(s => s.department))])
const totalOpenSlots = computed(() => schedules.value.reduce((acc, s) => acc + (s.availableQuota || 0), 0))
const filtered = computed(() =>
  schedules.value.filter(s =>
    (selectedDept.value === 'All' || s.department === selectedDept.value) &&
    (s.doctorName.toLowerCase().includes(search.value.toLowerCase()) ||
     s.department.toLowerCase().includes(search.value.toLowerCase()))
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
    showAdd.value = false; newDoc.doctorName = ''; newDoc.department = ''
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
      statusMsg.value = `Confirmed! Appointment No: ${data.data.appointmentNo}`; isError.value = false
      await loadAll(); emit('booked')
    } else {
      statusMsg.value = data.message || 'Booking failed'; isError.value = true
    }
  } catch (e) {
    statusMsg.value = 'Error: ' + e.message; isError.value = true
  } finally { bookingId.value = null }
}

onMounted(loadAll)
</script>

<style scoped>
.hero-panel {
  position: relative; overflow: hidden; border-radius: 24px; padding: 34px 36px; margin-bottom: 24px; color: #fff;
  background: linear-gradient(135deg, #4b4744 0%, #7c685c 45%, #c86d36 100%);
}
.hero-bg-layer { position: absolute; inset: 0; background: url('/hero-bg.jpg') center/cover no-repeat; animation: heroDrift 12s ease-in-out infinite; z-index: 0; }
.hero-orange-glow { position:absolute; left:-10%; right:-10%; bottom:-35%; height:70%; z-index:1; pointer-events:none; background:radial-gradient(ellipse at bottom center, rgba(232,108,44,0.78) 0%, rgba(217,119,54,0.3) 45%, transparent 75%); animation:orangeGlow 5.5s ease-in-out infinite; }
.hero-scrim { position: absolute; inset: 0; z-index: 1; background: linear-gradient(135deg, rgba(35,34,36,0.72) 0%, rgba(105,80,66,0.58) 50%, rgba(185,90,38,0.68) 100%); }
.hero-inner { position: relative; z-index: 2; display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 20px; }
.hero-pill { display: inline-flex; align-items: center; gap: 8px; padding: 6px 14px; border-radius: 999px; background: rgba(255,255,255,0.18); backdrop-filter: blur(8px); font-size: 12px; margin-bottom: 14px; }
.dot { width: 8px; height: 8px; border-radius: 50%; }
.dot.open { background: #4ade80; box-shadow: 0 0 8px #4ade80; }
.dot.paused { background: #f87171; }
.hero-title { margin: 0; font-size: 34px; line-height: 1.15; letter-spacing: -0.03em; font-weight: 600; }
.hero-title span { color: rgba(255,255,255,0.78); font-weight: 500; }
.hero-metrics { display: flex; gap: 12px; }
.metric-glass { padding: 18px 22px; border-radius: 18px; min-width: 140px; background: rgba(255,255,255,0.15); border: 1px solid rgba(255,255,255,0.24); backdrop-filter: blur(12px); }
.metric-glass b { display: block; font-size: 26px; font-weight: 600; }
.metric-glass span { font-size: 12px; color: rgba(255,255,255,0.85); }

.toolbar { display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 12px; margin-bottom: 20px; }
.dept-pills { display: flex; flex-wrap: wrap; gap: 6px; }
.dept-pill { padding: 8px 16px; border-radius: 999px; border: 1px solid #e4e4e7; background: #fff; color: #52525b; font-size: 13px; font-weight: 500; cursor: pointer; }
.dept-pill:hover { border-color: #18181b; transform: translateY(-1px); }
.dept-pill.active { background: #18181b; color: #fff; border-color: #18181b; }
.toolbar-right { display: flex; flex-wrap: wrap; gap: 8px; }
.search-input { padding: 9px 14px; border-radius: 999px; border: 1px solid #e4e4e7; font-size: 13px; outline: none; min-width: 210px; transition: border-color 0.18s; }
.search-input:focus { border-color: #d87433; }
.btn-dark-pill { padding: 9px 16px; border-radius: 999px; border: none; background: #18181b; color: #fff; font-size: 13px; font-weight: 600; cursor: pointer; }
.btn-dark-pill:hover { transform: translateY(-1px); }
.btn-light-pill { padding: 9px 16px; border-radius: 999px; border: 1px solid #e4e4e7; background: #fff; color: #18181b; font-size: 13px; font-weight: 500; cursor: pointer; }
.btn-light-pill:hover { border-color: #18181b; transform: translateY(-1px); }
.btn-orange-pill { padding: 9px 16px; border-radius: 999px; border: none; background: #e86c2c; color: #fff; font-size: 13px; font-weight: 600; cursor: pointer; }

.add-card { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 10px; padding: 18px; border-radius: 20px; background: #faf9f7; border: 1px solid #eceae4; margin-bottom: 20px; animation: fadeUp 0.22s ease both; }
.add-card input { padding: 9px 12px; border-radius: 10px; border: 1px solid #e4e4e7; font-size: 13px; }
.feedback { padding: 12px 16px; border-radius: 14px; font-size: 13px; font-weight: 600; margin-bottom: 18px; animation: fadeUp 0.2s ease both; }
.feedback.ok { background: #ecfdf5; color: #059669; }
.feedback.err { background: #fef2f2; color: #dc2626; }

.doc-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 18px; }
.doc-card {
  border-radius: 22px; padding: 24px; background: #faf9f7; border: 1px solid #eceae4;
  display: flex; flex-direction: column; justify-content: space-between;
  transition: transform 0.2s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.2s ease, border-color 0.2s ease;
  animation: fadeUp 0.28s cubic-bezier(0.22, 1, 0.36, 1) both;
}
.doc-card:hover { transform: translateY(-4px); box-shadow: 0 16px 34px rgba(24,24,27,0.08); border-color: #d6d3cd; }
.doc-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.dept-tag { padding: 5px 12px; border-radius: 999px; background: #fff; border: 1px solid #e4e4e7; font-size: 12px; font-weight: 500; color: #52525b; }
.fee-tag { font-size: 18px; font-weight: 600; color: #18181b; }
.doc-name { margin: 0 0 6px; font-size: 20px; letter-spacing: -0.02em; font-weight: 600; }
.doc-meta { font-size: 13px; color: #71717a; margin-bottom: 18px; }
.quota-box { padding: 12px 14px; border-radius: 14px; background: #fff; border: 1px solid #eceae4; margin-bottom: 18px; }
.quota-labels { display: flex; justify-content: space-between; font-size: 12px; color: #71717a; margin-bottom: 8px; }
.quota-labels b { color: #18181b; }
.quota-track { height: 6px; border-radius: 999px; background: #f4f4f5; overflow: hidden; }
.quota-fill { height: 100%; border-radius: 999px; background: linear-gradient(90deg, #8f7a6d, #e86c2c); transition: width 0.35s cubic-bezier(0.22, 1, 0.36, 1); }
.book-btn { width: 100%; padding: 11px; border-radius: 999px; border: none; background: #18181b; color: #fff; font-size: 13px; font-weight: 600; cursor: pointer; }
.book-btn:hover:not(.disabled) { background: #e86c2c; transform: translateY(-1px); box-shadow: 0 6px 16px rgba(232,108,44,0.28); }
.book-btn.disabled { background: #d4d4d8; cursor: not-allowed; }
</style>
