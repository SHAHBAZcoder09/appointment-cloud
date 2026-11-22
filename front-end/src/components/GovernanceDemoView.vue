<template>
  <div>
    <h2 style="margin-top:0;">Architecture Governance Demo</h2>
    <p style="color:#555;">One-click tests for your microservice governance features:</p>
    <div style="display:flex;flex-wrap:wrap;gap:10px;margin-bottom:16px;">
      <button @click="testLB" style="padding:8px 14px;cursor:pointer;">1. Test Load Balancer</button>
      <button @click="testSentinel(true)" style="padding:8px 14px;cursor:pointer;">2. Test Sentinel Fallback (Fail)</button>
      <button @click="testSentinel(false)" style="padding:8px 14px;cursor:pointer;">3. Test Sentinel Healthy</button>
      <button @click="testNacosConfig" style="padding:8px 14px;cursor:pointer;">4. Check Nacos Config</button>
      <button @click="testGatewayBlock" style="padding:8px 14px;cursor:pointer;">5. Test Gateway 401 Block</button>
    </div>
    <pre style="background:#111;color:#0f0;padding:14px;border-radius:8px;min-height:140px;white-space:pre-wrap;">{{ logs.join('\n') || 'Click a button above to run a live test...' }}</pre>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const logs = ref([])
function log(text) {
  logs.value.unshift(`[${new Date().toLocaleTimeString()}] ${text}`)
}

async function testLB() {
  const res = await fetch('http://localhost:7000/appointment/lb/port')
  const data = await res.json()
  log(`LoadBalancer hit service-patient on port: ${data.data}`)
}

async function testSentinel(fail) {
  const res = await fetch(`http://localhost:7000/appointment/degrade/${fail}`)
  const data = await res.json()
  log(`Sentinel (/degrade/${fail}): code=${data.code} | ${data.message || data.data}`)
}

async function testNacosConfig() {
  const res = await fetch('http://localhost:7000/schedule/config')
  const data = await res.json()
  log(`Nacos Config: open=${data.data.bookingOpen}, notice="${data.data.bookingNotice}"`)
}

async function testGatewayBlock() {
  const res = await fetch('http://localhost:7000/appointment/findAll')
  const data = await res.json()
  log(`Gateway (no token): HTTP ${res.status} | ${data.message}`)
}
</script>
