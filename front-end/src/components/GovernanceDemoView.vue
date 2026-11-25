<template>
  <div class="gov-bento">
    <div class="hero-bg-layer"></div>
    <div class="hero-scrim"></div>
    <div class="gov-inner">
      <div class="gov-header">
        <div class="gov-pill">Admin Governance Suite</div>
        <h2 class="gov-title">Built around <span>reliability &amp; speed.</span></h2>
        <p class="gov-sub">Trigger live Spring Cloud Alibaba checks directly from the dashboard.</p>
      </div>

      <div class="gov-cards">
        <button @click="testLB" class="gov-glass-card">
          <b>LoadBalancer</b>
          <span>Switch between 8071 &amp; 8072</span>
        </button>
        <button @click="testSentinel(true)" class="gov-glass-card">
          <b>Sentinel Fallback</b>
          <span>Simulate error → 503 degrade</span>
        </button>
        <button @click="testNacosConfig" class="gov-glass-card">
          <b>Nacos Config</b>
          <span>Read live @RefreshScope state</span>
        </button>
        <button @click="testGatewayBlock" class="gov-glass-card">
          <b>Gateway Auth</b>
          <span>Verify 401 block without JWT</span>
        </button>
      </div>

      <pre class="gov-console">{{ logs.join('\n') || 'Select any governance card above to run a live verification...' }}</pre>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const logs = ref([])
function log(text) { logs.value.unshift(`[${new Date().toLocaleTimeString()}] ${text}`) }

async function testLB() {
  const res = await fetch('http://localhost:7000/appointment/lb/port')
  const data = await res.json()
  log(`LoadBalancer → service-patient responded from port ${data.data}`)
}
async function testSentinel(fail) {
  const res = await fetch(`http://localhost:7000/appointment/degrade/${fail}`)
  const data = await res.json()
  log(`Sentinel (/degrade/${fail}) → code ${data.code}: ${data.message || data.data}`)
}
async function testNacosConfig() {
  const res = await fetch('http://localhost:7000/schedule/config')
  const data = await res.json()
  log(`Nacos Config → open=${data.data.bookingOpen}, notice="${data.data.bookingNotice}"`)
}
async function testGatewayBlock() {
  const res = await fetch('http://localhost:7000/appointment/findAll')
  const data = await res.json()
  log(`Gateway Auth (no token) → HTTP ${res.status}: ${data.message}`)
}
</script>

<style scoped>
.gov-bento {
  position: relative; overflow: hidden; border-radius: 26px; padding: 36px; color: #fff;
  background: linear-gradient(145deg, #4b4744 0%, #6d584b 45%, #a85c32 100%);
}
.hero-bg-layer { position: absolute; inset: 0; background: url('/hero-bg.jpg') center/cover no-repeat; animation: heroDrift 18s ease-in-out infinite; z-index: 0; }
.hero-scrim { position: absolute; inset: 0; z-index: 1; background: linear-gradient(145deg, rgba(35,34,36,0.75) 0%, rgba(100,76,62,0.62) 50%, rgba(168,92,50,0.72) 100%); }
.gov-inner { position: relative; z-index: 2; }
.gov-pill { display: inline-block; padding: 6px 14px; border-radius: 999px; background: rgba(255,255,255,0.18); font-size: 12px; margin-bottom: 14px; }
.gov-title { margin: 0 0 8px; font-size: 32px; letter-spacing: -0.03em; font-weight: 600; }
.gov-title span { color: rgba(255,255,255,0.78); font-weight: 500; }
.gov-sub { margin: 0 0 24px; font-size: 14px; color: rgba(255,255,255,0.84); }
.gov-cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 14px; margin-bottom: 20px; }
.gov-glass-card {
  text-align: left; padding: 20px; border-radius: 18px; cursor: pointer; color: #fff;
  background: rgba(255,255,255,0.13); border: 1px solid rgba(255,255,255,0.22); backdrop-filter: blur(12px);
}
.gov-glass-card:hover { background: rgba(255,255,255,0.24); transform: translateY(-3px); }
.gov-glass-card b { display: block; font-size: 16px; margin-bottom: 4px; }
.gov-glass-card span { font-size: 12px; color: rgba(255,255,255,0.82); }
.gov-console {
  margin: 0; padding: 18px; border-radius: 16px; min-height: 150px;
  background: rgba(15, 15, 18, 0.74); border: 1px solid rgba(255,255,255,0.16);
  color: #fdba74; font-size: 13px; white-space: pre-wrap;
}
</style>
