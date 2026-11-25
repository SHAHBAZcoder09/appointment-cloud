<template>
  <div class="adm-app"><div class="n4-horizon-fade"></div>
    <header class="adm-nav-wrap">
      <div class="adm-nav">
        <div class="adm-brand" @click="admin && (tab = 'bookings')">
          <div class="adm-logo">N4</div>
          <span>N4 Admin Console</span>
          
        </div>

        <nav v-if="admin" class="adm-links">
          <button @click="tab = 'bookings'" :class="['adm-link', { active: tab === 'bookings' }]">All Bookings</button>
          <button @click="tab = 'doctors'" :class="['adm-link', { active: tab === 'doctors' }]">Specialists</button>
          <button @click="tab = 'governance'" :class="['adm-link', { active: tab === 'governance' }]">Governance Suite</button>
        </nav>

        <div class="adm-right">
          <template v-if="admin">
            <span class="adm-user">{{ admin.name }} <b class="adm-tag">ADMIN</b></span>
            <button @click="logout" class="adm-logout">Logout</button>
          </template>
          <a href="http://localhost:5173" class="adm-sub" style="text-decoration:none;color:#e4e4e7;">Patient Portal ↗</a>
        </div>
      </div>
    </header>

    <main class="adm-main">
      <!-- Admin Login -->
      <div v-if="!admin" class="login-bento">
        <div class="hero-card">
          <div class="hero-bg"></div>
          <div class="hero-scrim"></div>
          <div class="hero-glow"></div>
          <div class="hero-inner">
            <span class="pill-glass">N4 Clinic Operations</span>
            <h1><span v-for="(w,i) in 'Clinic Operations,'.split(' ')" :key="'h1a'+i" class="hover-word">{{ w }}</span><br /><span class="sub-line"><span v-for="(w,i) in 'centralized control.'.split(' ')" :key="'h1b'+i" class="hover-word">{{ w }}</span></span></h1>
            <p><span v-for="(w,i) in 'Manage all patient appointments, doctor quotas, and live Spring Cloud Alibaba governance rules from one dedicated admin console.'.split(' ')" :key="'p'+i" class="hover-word">{{ w }}</span></p>
          </div>
        </div>

        <div class="login-card">
          <span class="pill-light">Restricted Access</span>
          <h2>Sign in as Clinic Admin</h2>
          <form @submit.prevent="login" class="form-stack">
            <label>Admin Username<input v-model="username" placeholder="Admin username" required /></label>
            <label>Password<input v-model="password" type="password" placeholder="Password" required /></label>
            <button :disabled="loading" type="submit" class="btn-ink">{{ loading ? 'Verifying...' : 'Enter Console' }}</button>
          </form>
          <p v-if="loginErr" class="err-msg">{{ loginErr }}</p>
        </div>
      </div>

      <!-- Tab 1: All Clinic Bookings -->
      <div v-else-if="tab === 'bookings'">
        <div class="hero-banner">
          <div class="hero-bg"></div>
          <div class="hero-scrim"></div>
          <div class="hero-glow"></div>
          <div class="banner-inner">
            <div>
              <span class="pill-glass">Live Clinic Feed · TiDB Cloud</span>
              <h2>All Patient <span>Appointments.</span></h2>
            </div>
            <div class="stats-row">
              <div class="stat-box"><b>{{ bookings.length }}</b><span>Total Records</span></div>
              <div class="stat-box"><b>{{ activeCount }}</b><span>Active Booked</span></div>
            </div>
          </div>
        </div>

        <div style="display:flex;justify-content:flex-end;margin-bottom:14px;">
          <button @click="loadBookings" class="btn-outline">Refresh Feed</button>
        </div>

        <div v-if="bookings.length" class="list-grid">
          <div v-for="b in bookings" :key="b.id" class="row-card">
            <div>
              <div style="display:flex;gap:8px;align-items:center;margin-bottom:6px;">
                <span class="mono-pill">{{ b.appointmentNo }}</span>
                <span :class="['status-pill', b.status === 'BOOKED' ? 'ok' : 'off']">{{ b.status }}</span>
              </div>
              <div style="font-weight:600;font-size:16px;">
                Patient: {{ b.patient?.name || b.patientId }} → {{ b.schedule?.doctorName }} ({{ b.schedule?.department }})
              </div>
              <div style="font-size:13px;color:#71717a;margin-top:4px;">
                {{ b.schedule?.scheduleDate }} · {{ b.schedule?.timeSlot }} · Fee: ¥{{ b.schedule?.fee }}
              </div>
            </div>
            <button v-if="b.status === 'BOOKED'" @click="cancelBooking(b.id)" class="btn-danger">Force Cancel</button>
          </div>
        </div>
        <div v-else class="empty-box">No appointments in TiDB Cloud yet.</div>
      </div>

      <!-- Tab 2: Manage Specialists -->
      <div v-else-if="tab === 'doctors'">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:18px;">
          <h2 style="margin:0;font-size:26px;letter-spacing:-0.02em;">Specialist Schedules</h2>
          <button @click="loadSchedules" class="btn-outline">Refresh</button>
        </div>

        <form @submit.prevent="addDoctor" class="add-grid">
          <input v-model="newDoc.doctorName" placeholder="Doctor Name" required />
          <input v-model="newDoc.department" placeholder="Department" required />
          <input v-model="newDoc.scheduleDate" placeholder="Date" required />
          <input v-model="newDoc.timeSlot" placeholder="Time Slot" required />
          <input v-model.number="newDoc.totalQuota" type="number" placeholder="Slots" required />
          <input v-model.number="newDoc.fee" type="number" placeholder="Fee (¥)" required />
          <button type="submit" class="btn-orange">+ Add Doctor</button>
        </form>
        <p v-if="docMsg" style="color:#059669;font-weight:600;font-size:13px;">{{ docMsg }}</p>

        <div class="doc-grid">
          <div v-for="s in schedules" :key="s.id" class="doc-card">
            <div style="display:flex;justify-content:space-between;margin-bottom:10px;">
              <span class="pill-light">{{ s.department }}</span>
              <b>¥{{ s.fee }}</b>
            </div>
            <h3 style="margin:0 0 4px;">{{ s.doctorName }}</h3>
            <div style="font-size:13px;color:#71717a;margin-bottom:14px;">{{ s.scheduleDate }} · {{ s.timeSlot }}</div>
            <div style="font-size:12px;color:#52525b;margin-bottom:6px;">Slots: <b>{{ s.availableQuota }} / {{ s.totalQuota }}</b></div>
            <div class="track"><div class="fill" :style="{ width: (s.availableQuota / s.totalQuota * 100) + '%' }"></div></div>
          </div>
        </div>
      </div>

      <!-- Tab 3: Governance Suite -->
      <div v-else-if="tab === 'governance'" class="gov-panel">
        <div class="hero-bg"></div>
        <div class="hero-scrim"></div>
        <div class="hero-glow"></div>
        <div style="position:relative;z-index:2;">
          <span class="pill-glass">Spring Cloud Alibaba Live Tests</span>
          <h2 style="margin:10px 0 6px;font-size:30px;letter-spacing:-0.03em;">Governance &amp; Resilience Suite</h2>
          <p style="margin:0 0 22px;font-size:14px;opacity:0.85;">Verify Gateway, LoadBalancer, Sentinel Rate Limiter (429), Sentinel Fallback (503), and Nacos Config.</p>

          <div class="gov-grid">
            <button @click="testLB" class="gov-card"><b>1. LoadBalancer</b><span>Alternate 8071 / 8072</span></button>
            <button @click="testSentinelBurst" class="gov-card"><b>2. Sentinel Burst (429)</b><span>Send 4 rapid calls (limit 2/s)</span></button>
            <button @click="testSentinelFallback" class="gov-card"><b>3. Sentinel Fallback</b><span>Trigger 503 degrade</span></button>
            <button @click="testNacos" class="gov-card"><b>4. Nacos Config</b><span>Read @RefreshScope state</span></button>
            <button @click="testGateway" class="gov-card"><b>5. Gateway Auth</b><span>Verify 401 without JWT</span></button>
          </div>
          <pre class="console">{{ logs.join('\n') || 'Click any card above to run a live governance test...' }}</pre>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'

const admin = ref(null)
const token = ref('')
const tab = ref('bookings')
const username = ref('')
const password = ref('')
const loading = ref(false)
const loginErr = ref('')

const bookings = ref([])
const schedules = ref([])
const docMsg = ref('')
const newDoc = reactive({ doctorName: '', department: '', scheduleDate: '2026-11-26', timeSlot: '09:00-11:00', totalQuota: 6, fee: 50 })
const logs = ref([])

const activeCount = computed(() => bookings.value.filter(b => b.status === 'BOOKED').length)
function log(t) { logs.value.unshift(`[${new Date().toLocaleTimeString()}] ${t}`) }

async function login() {
  loading.value = true; loginErr.value = ''
  try {
    const res = await fetch('http://localhost:7000/patient/login', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: username.value, password: password.value })
    })
    const data = await res.json()
    if (data.code === 200 && data.data.patient.username === 'Denji') {
      admin.value = data.data.patient; token.value = data.data.token
      await Promise.all([loadBookings(), loadSchedules()])
    } else if (data.code === 200) {
      loginErr.value = 'Admin access only. Patients must use port 5173.'
    } else {
      loginErr.value = data.message || 'Invalid credentials'
    }
  } catch (e) { loginErr.value = e.message } finally { loading.value = false }
}

function logout() { admin.value = null; token.value = '' }

async function loadBookings() {
  const res = await fetch('http://localhost:7000/appointment/findAll', { headers: { Authorization: `Bearer ${token.value}` } })
  const data = await res.json()
  bookings.value = data.data || []
}

async function cancelBooking(id) {
  await fetch(`http://localhost:7000/appointment/cancel/${id}`, { method: 'POST', headers: { Authorization: `Bearer ${token.value}` } })
  await Promise.all([loadBookings(), loadSchedules()])
}

async function loadSchedules() {
  const res = await fetch('http://localhost:7000/schedule/findAll')
  const data = await res.json()
  schedules.value = data.data || []
}

async function addDoctor() {
  const res = await fetch('http://localhost:7000/schedule/add', {
    method: 'POST', headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token.value}` },
    body: JSON.stringify(newDoc)
  })
  const data = await res.json()
  if (data.code === 200) {
    docMsg.value = `Added ${newDoc.doctorName}!`; newDoc.doctorName = ''; newDoc.department = ''
    await loadSchedules()
  }
}

async function testLB() {
  const res = await fetch('http://localhost:7000/appointment/lb/port')
  const data = await res.json()
  log(`LoadBalancer → service-patient port ${data.data}`)
}
async function testSentinelBurst() {
  const calls = [1, 2, 3, 4].map(() =>
    fetch('http://localhost:7000/appointment/book/1/999', {
      method: 'POST', headers: { Authorization: `Bearer ${token.value}` }
    }).then(r => r.json())
  )
  const results = await Promise.all(calls)
  results.forEach((d, i) => log(`Sentinel Burst #${i + 1} → code ${d.code}: ${d.message}`))
}
async function testSentinelFallback() {
  const res = await fetch('http://localhost:7000/appointment/degrade/true')
  const data = await res.json()
  log(`Sentinel Fallback → code ${data.code}: ${data.message}`)
}
async function testNacos() {
  const res = await fetch('http://localhost:7000/schedule/config')
  const data = await res.json()
  log(`Nacos Config → open=${data.data.bookingOpen}, notice="${data.data.bookingNotice}"`)
}
async function testGateway() {
  const res = await fetch('http://localhost:7000/appointment/findAll')
  const data = await res.json()
  log(`Gateway (no JWT) → HTTP ${res.status}: ${data.message}`)
}
</script>

<style>
* { box-sizing: border-box; }
.adm-app { min-height: 100vh; background: #fff; color: #18181b; }
.adm-nav-wrap { padding: 18px 24px 0; max-width: 1120px; margin: 0 auto; }
.adm-nav { display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 12px; padding: 10px 16px; border-radius: 999px; background: #18181b; color: #fff; }
.adm-brand { display: flex; align-items: center; gap: 10px; cursor: pointer; font-weight: 600; font-size: 14px; }
.adm-logo { width: 30px; height: 30px; border-radius: 50%; background: linear-gradient(135deg,#e86c2c,#b45309); display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 700; }
.adm-port-badge { font-size: 11px; padding: 2px 8px; border-radius: 999px; background: rgba(255,255,255,0.14); color: #fdba74; }
.adm-links { display: flex; gap: 6px; }
.adm-link { padding: 7px 14px; border-radius: 999px; border: none; background: transparent; color: rgba(255,255,255,0.72); font-size: 13px; cursor: pointer; }
.adm-link.active { background: rgba(255,255,255,0.18); color: #fff; }
.adm-right { display: flex; align-items: center; gap: 10px; font-size: 13px; }
.adm-tag { padding: 2px 7px; border-radius: 999px; background: rgba(232,108,44,0.25); color: #fdba74; font-size: 10px; }
.adm-logout { padding: 7px 16px; border-radius: 999px; border: none; background: #e86c2c; color: #fff; font-size: 12px; font-weight: 600; cursor: pointer; }
.adm-sub { padding: 6px 14px; border-radius: 999px; background: rgba(255,255,255,0.12); font-size: 12px; }
.adm-main { max-width: 1120px; margin: 0 auto; padding: 28px 24px 60px; }

.login-bento { display: grid; grid-template-columns: 1.15fr 1fr; gap: 20px; }
@media (max-width: 820px) { .login-bento { grid-template-columns: 1fr; } }
.hero-card, .hero-banner, .gov-panel { position: relative; overflow: hidden; border-radius: 24px; padding: 38px 34px; color: #fff; background: linear-gradient(155deg,#52555a,#8f7a6d,#d87433); }
.hero-banner { padding: 30px 34px; margin-bottom: 20px; }
.hero-bg { position: absolute; inset: 0; background: url('/hero-bg.jpg') center/cover no-repeat; animation: drift 12s ease-in-out infinite; }
.hero-scrim { position: absolute; inset: 0; background: linear-gradient(155deg, rgba(24,24,27,0.45), rgba(110,85,70,0.24), rgba(190,95,40,0.36)); }
.hero-glow { position: absolute; left:-10%; right:-10%; bottom:-30%; height:65%; background: radial-gradient(ellipse at bottom, rgba(232,108,44,0.8), transparent 70%); }
.hero-inner { position: relative; z-index: 2; }
.hero-inner h1 { font-size: 36px; line-height: 1.15; letter-spacing: -0.03em; margin: 22px 0 12px; }
.hero-inner h1 span, .banner-inner h2 span { color: rgba(255,255,255,0.78); font-weight: 500; }
.banner-inner { position: relative; z-index: 2; display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 16px; }
.banner-inner h2 { margin: 10px 0 0; font-size: 32px; letter-spacing: -0.03em; }
.stats-row { display: flex; gap: 12px; }
.stat-box { padding: 16px 20px; border-radius: 16px; background: rgba(255,255,255,0.16); border: 1px solid rgba(255,255,255,0.22); backdrop-filter: blur(10px); }
.stat-box b { display: block; font-size: 24px; }
.stat-box span { font-size: 12px; opacity: 0.85; }
.pill-glass { display: inline-block; padding: 5px 12px; border-radius: 999px; background: rgba(255,255,255,0.18); font-size: 12px; }
.pill-light { display: inline-block; padding: 4px 10px; border-radius: 999px; background: #fff; border: 1px solid #e4e4e7; font-size: 11px; font-weight: 600; color: #71717a; }

.login-card { border-radius: 24px; padding: 38px 34px; background: #faf9f7; border: 1px solid #eceae4; display: flex; flex-direction: column; justify-content: center; }
.login-card h2 { margin: 10px 0 20px; font-size: 24px; }
.form-stack { display: flex; flex-direction: column; gap: 14px; }
.form-stack label { font-size: 12px; font-weight: 600; color: #52525b; display: flex; flex-direction: column; gap: 6px; }
.form-stack input { padding: 11px 14px; border-radius: 12px; border: 1px solid #e4e4e7; font-size: 14px; }
.btn-ink { padding: 12px; border-radius: 999px; border: none; background: #18181b; color: #fff; font-weight: 600; cursor: pointer; }
.btn-outline { padding: 8px 16px; border-radius: 999px; border: 1px solid #e4e4e7; background: #fff; cursor: pointer; font-weight: 500; }
.btn-orange { padding: 10px 16px; border-radius: 999px; border: none; background: #e86c2c; color: #fff; font-weight: 600; cursor: pointer; }
.btn-danger { padding: 8px 14px; border-radius: 999px; border: none; background: #fef2f2; color: #dc2626; font-weight: 600; cursor: pointer; }
.err-msg { color: #dc2626; font-size: 13px; margin-top: 12px; }

.list-grid { display: grid; gap: 12px; }
.row-card { border-radius: 18px; padding: 18px 22px; background: #faf9f7; border: 1px solid #eceae4; display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 12px; }
.mono-pill { font-size: 12px; font-weight: 700; padding: 3px 10px; border-radius: 999px; background: #fff; border: 1px solid #e4e4e7; }
.status-pill { font-size: 11px; font-weight: 700; padding: 3px 10px; border-radius: 999px; }
.status-pill.ok { background: #ecfdf5; color: #059669; }
.status-pill.off { background: #fef2f2; color: #dc2626; }
.empty-box { padding: 36px; text-align: center; border-radius: 18px; background: #faf9f7; color: #71717a; }

.add-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px,1fr)); gap: 10px; padding: 18px; border-radius: 20px; background: #faf9f7; border: 1px solid #eceae4; margin-bottom: 20px; }
.add-grid input { padding: 9px 12px; border-radius: 10px; border: 1px solid #e4e4e7; font-size: 13px; }
.doc-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px,1fr)); gap: 16px; }
.doc-card { border-radius: 20px; padding: 20px; background: #faf9f7; border: 1px solid #eceae4; }
.track { height: 6px; border-radius: 999px; background: #e4e4e7; overflow: hidden; }
.fill { height: 100%; background: linear-gradient(90deg,#8f7a6d,#e86c2c); }

.gov-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px,1fr)); gap: 12px; margin-bottom: 18px; }
.gov-card { text-align: left; padding: 18px; border-radius: 16px; background: rgba(255,255,255,0.14); border: 1px solid rgba(255,255,255,0.22); color: #fff; cursor: pointer; backdrop-filter: blur(10px); }
.gov-card b { display: block; font-size: 15px; margin-bottom: 4px; }
.gov-card span { font-size: 12px; opacity: 0.82; }
.console { margin: 0; padding: 16px; border-radius: 14px; min-height: 150px; background: rgba(15,15,18,0.78); color: #fdba74; font-size: 13px; white-space: pre-wrap; }
@keyframes drift { 0%,100% { transform: scale(1.03); } 50% { transform: scale(1.11) translate(-12px,-6px); } }
.n4-app, .adm-app { position:relative; overflow-x:hidden; }
.n4-nav-wrap, .adm-nav-wrap, .n4-main, .adm-main { position:relative; z-index:2; }
.n4-horizon-fade { position:fixed; left:0; right:0; bottom:0; height:42vh; pointer-events:none; z-index:1; background:linear-gradient(to top, rgba(220,104,36,0.48) 0%, rgba(215,120,58,0.22) 38%, rgba(165,148,136,0.06) 70%, transparent 100%); animation:horizonBreathe 6.5s ease-in-out infinite; }
@keyframes horizonBreathe { 0%,100% { opacity:0.82; transform:translateY(0px); } 50% { opacity:1; transform:translateY(-6px); } }
.hover-word { display:inline-block; margin-right:0.26em; transition:transform 0.22s cubic-bezier(0.22, 1, 0.36, 1), color 0.18s ease, text-shadow 0.22s ease; cursor:default; }
.hover-word:hover { transform:translateY(-6px) scale(1.06); color:#ffffff; text-shadow:0 8px 20px rgba(232,108,44,0.55); }
.sub-line { color:rgba(255,255,255,0.78); font-weight:500; }
</style>
