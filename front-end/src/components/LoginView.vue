<template>
  <div class="auth-bento">
    <div class="auth-hero">
      <div class="hero-bg-layer"></div>
      <div class="hero-scrim"></div>
      <div class="hero-content">
        <div class="auth-badge">N4 Clinic Appointment System</div>
        <h1 class="auth-hero-title">
          Personalized care,<br />
          <span>without the wait.</span>
        </h1>
        <p class="auth-hero-sub">
          Book board-certified specialists in seconds. Powered by microservices, live slot validation, and instant confirmation.
        </p>
        <div class="auth-glass-stats">
          <div class="auth-stat">
            <b>99.9%</b>
            <span>Gateway Uptime</span>
          </div>
          <div class="auth-stat">
            <b>3 Services</b>
            <span>Nacos Orchestrated</span>
          </div>
        </div>
      </div>
    </div>

    <div class="auth-form-card">
      <div class="auth-badge-dark">{{ isRegister ? 'New Patient' : 'Welcome Back' }}</div>
      <h2 class="auth-form-title">{{ isRegister ? 'Create your account' : 'Sign in to N4 Clinic' }}</h2>

      <form v-if="!isRegister" @submit.prevent="login" class="auth-form">
        <div class="field">
          <label>Username</label>
          <input v-model="username" placeholder="Enter your username" required />
        </div>
        <div class="field">
          <label>Password</label>
          <input v-model="password" type="password" placeholder="Enter your password" required />
        </div>
        <button :disabled="loading" type="submit" class="btn-ink">
          {{ loading ? 'Signing in...' : 'Sign In' }}
        </button>
      </form>

      <form v-else @submit.prevent="register" class="auth-form">
        <div class="field">
          <label>Full Name</label>
          <input v-model="reg.name" placeholder="e.g. Sara Ali" required />
        </div>
        <div class="field">
          <label>Username</label>
          <input v-model="reg.username" placeholder="Choose a username" required />
        </div>
        <div class="field">
          <label>Password</label>
          <input v-model="reg.password" type="password" placeholder="Create a password" required />
        </div>
        <div style="display:flex;gap:12px;">
          <div class="field" style="flex:2;">
            <label>Phone (10 digits)</label>
            <input v-model="reg.phone" @input="reg.phone = reg.phone.replace(/\D/g, '').slice(0, 10)" inputmode="numeric" required maxlength="10" pattern="\d{10}" placeholder="9876543210" />
          </div>
          <div class="field" style="flex:1;">
            <label>Age</label>
            <input v-model.number="reg.age" type="number" min="1" max="120" required />
          </div>
        </div>
        <button :disabled="loading" type="submit" class="btn-orange-full">
          {{ loading ? 'Creating account...' : 'Create Account' }}
        </button>
      </form>

      <p v-if="msg" :class="['auth-msg', isError ? 'err' : 'ok']">{{ msg }}</p>
      <div class="auth-switch">
        <a href="#" @click.prevent="isRegister = !isRegister; msg = ''">
          {{ isRegister ? 'Already have an account? Sign in' : 'New patient? Start your profile' }}
        </a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const emit = defineEmits(['logged-in'])
const isRegister = ref(false)
const username = ref('')
const password = ref('')
const reg = reactive({ name: '', username: '', password: '', phone: '', age: 25 })
const loading = ref(false)
const msg = ref('')
const isError = ref(false)

async function login() {
  loading.value = true; msg.value = ''
  try {
    const res = await fetch('http://localhost:7000/patient/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: username.value, password: password.value })
    })
    const data = await res.json()
    if (data.code === 200) emit('logged-in', data.data)
    else { msg.value = data.message || 'Invalid credentials'; isError.value = true }
  } catch (e) {
    msg.value = 'Network error: ' + e.message; isError.value = true
  } finally { loading.value = false }
}

async function register() {
  if (!/^\d{10}$/.test(reg.phone)) {
    msg.value = 'Phone number must be exactly 10 digits.'; isError.value = true; return
  }
  loading.value = true; msg.value = ''
  try {
    const res = await fetch('http://localhost:7000/patient/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(reg)
    })
    const data = await res.json()
    if (data.code === 200) {
      msg.value = 'Account created! You can now sign in.'; isError.value = false
      username.value = reg.username; isRegister.value = false
    } else {
      msg.value = data.message || 'Registration failed'; isError.value = true
    }
  } catch (e) {
    msg.value = 'Network error: ' + e.message; isError.value = true
  } finally { loading.value = false }
}
</script>

<style scoped>
.auth-bento { display: grid; grid-template-columns: 1.15fr 1fr; gap: 20px; align-items: stretch; margin-top: 12px; }
@media (max-width: 820px) { .auth-bento { grid-template-columns: 1fr; } }
.auth-hero {
  position: relative; overflow: hidden; border-radius: 24px; padding: 44px 36px; color: #fff;
  background: linear-gradient(155deg, #52555a 0%, #8f7a6d 42%, #d87433 100%);
  box-shadow: 0 20px 50px rgba(24,24,27,0.10);
}
.hero-bg-layer {
  position: absolute; inset: 0; background: url('/hero-bg.jpg') center/cover no-repeat;
  animation: heroDrift 18s ease-in-out infinite; z-index: 0;
}
.hero-scrim {
  position: absolute; inset: 0; z-index: 1;
  background: linear-gradient(155deg, rgba(35,34,36,0.72) 0%, rgba(110,85,70,0.58) 50%, rgba(190,95,40,0.68) 100%);
}
.hero-content { position: relative; z-index: 2; display: flex; flex-direction: column; justify-content: space-between; height: 100%; animation: fadeUp 0.32s cubic-bezier(0.22, 1, 0.36, 1) both; }
.auth-badge { display: inline-block; align-self: flex-start; padding: 6px 14px; border-radius: 999px; background: rgba(255,255,255,0.18); backdrop-filter: blur(8px); font-size: 12px; font-weight: 500; }
.auth-hero-title { font-size: 38px; line-height: 1.12; letter-spacing: -0.03em; font-weight: 600; margin: 28px 0 14px; }
.auth-hero-title span { color: rgba(255,255,255,0.78); font-weight: 500; }
.auth-hero-sub { font-size: 14px; line-height: 1.6; color: rgba(255,255,255,0.88); max-width: 380px; margin: 0 0 28px; }
.auth-glass-stats { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.auth-stat { padding: 16px; border-radius: 16px; background: rgba(255,255,255,0.14); border: 1px solid rgba(255,255,255,0.22); backdrop-filter: blur(12px); }
.auth-stat b { display: block; font-size: 20px; font-weight: 600; }
.auth-stat span { font-size: 12px; color: rgba(255,255,255,0.82); }

.auth-form-card { border-radius: 24px; padding: 40px 36px; background: #faf9f7; border: 1px solid #eceae4; display: flex; flex-direction: column; justify-content: center; animation: fadeUp 0.35s cubic-bezier(0.22, 1, 0.36, 1) both; }
.auth-badge-dark { display: inline-block; align-self: flex-start; padding: 5px 12px; border-radius: 999px; background: #ffffff; border: 1px solid #e4e4e7; font-size: 11px; font-weight: 600; color: #71717a; margin-bottom: 12px; }
.auth-form-title { margin: 0 0 22px; font-size: 24px; letter-spacing: -0.02em; font-weight: 600; }
.auth-form { display: flex; flex-direction: column; gap: 14px; }
.field label { display: block; font-size: 12px; font-weight: 600; color: #52525b; margin-bottom: 6px; }
.field input { width: 100%; padding: 11px 14px; border-radius: 12px; border: 1px solid #e4e4e7; background: #ffffff; font-size: 14px; outline: none; transition: border-color 0.18s ease, box-shadow 0.18s ease; }
.field input:focus { border-color: #d87433; box-shadow: 0 0 0 3px rgba(216,116,51,0.14); }
.btn-ink { margin-top: 6px; padding: 12px; border-radius: 999px; border: none; background: #18181b; color: #fff; font-size: 14px; font-weight: 600; cursor: pointer; }
.btn-ink:hover { background: #27272a; transform: translateY(-1px); box-shadow: 0 8px 20px rgba(24,24,27,0.16); }
.btn-orange-full { margin-top: 6px; padding: 12px; border-radius: 999px; border: none; background: #e86c2c; color: #fff; font-size: 14px; font-weight: 600; cursor: pointer; }
.btn-orange-full:hover { background: #d45d20; transform: translateY(-1px); box-shadow: 0 8px 20px rgba(232,108,44,0.28); }
.auth-msg { margin-top: 14px; font-size: 13px; padding: 10px 14px; border-radius: 12px; }
.auth-msg.err { background: #fef2f2; color: #dc2626; }
.auth-msg.ok { background: #ecfdf5; color: #059669; }
.auth-switch { margin-top: 18px; text-align: center; font-size: 13px; }
.auth-switch a { color: #52525b; text-decoration: none; font-weight: 500; transition: color 0.15s; }
.auth-switch a:hover { color: #e86c2c; }
</style>
