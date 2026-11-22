<template>
  <div style="max-width:380px;margin:40px auto;background:#fff;padding:24px;border-radius:10px;border:1px solid #ddd;">
    <h2 style="margin-top:0;">{{ isRegister ? 'Create Patient Account' : 'N4 Clinic Login' }}</h2>

    <form v-if="!isRegister" @submit.prevent="login">
      <div style="margin-bottom:12px;">
        <label>Username</label>
        <input v-model="username" required style="width:100%;padding:8px;margin-top:4px;box-sizing:border-box;" />
      </div>
      <div style="margin-bottom:16px;">
        <label>Password</label>
        <input v-model="password" type="password" required style="width:100%;padding:8px;margin-top:4px;box-sizing:border-box;" />
      </div>
      <button :disabled="loading" type="submit" style="width:100%;padding:10px;background:#2563eb;color:#fff;border:none;border-radius:6px;cursor:pointer;">
        {{ loading ? 'Logging in...' : 'Login' }}
      </button>
    </form>

    <form v-else @submit.prevent="register">
      <div style="margin-bottom:10px;">
        <label>Full Name</label>
        <input v-model="reg.name" required style="width:100%;padding:8px;margin-top:4px;box-sizing:border-box;" />
      </div>
      <div style="margin-bottom:10px;">
        <label>Username</label>
        <input v-model="reg.username" required style="width:100%;padding:8px;margin-top:4px;box-sizing:border-box;" />
      </div>
      <div style="margin-bottom:10px;">
        <label>Password</label>
        <input v-model="reg.password" type="password" required style="width:100%;padding:8px;margin-top:4px;box-sizing:border-box;" />
      </div>
      <div style="display:flex;gap:8px;margin-bottom:16px;">
        <div style="flex:2;">
          <label>Phone</label>
          <input v-model="reg.phone" style="width:100%;padding:8px;margin-top:4px;box-sizing:border-box;" />
        </div>
        <div style="flex:1;">
          <label>Age</label>
          <input v-model.number="reg.age" type="number" style="width:100%;padding:8px;margin-top:4px;box-sizing:border-box;" />
        </div>
      </div>
      <button :disabled="loading" type="submit" style="width:100%;padding:10px;background:#16a34a;color:#fff;border:none;border-radius:6px;cursor:pointer;">
        {{ loading ? 'Creating...' : 'Register' }}
      </button>
    </form>

    <p v-if="msg" :style="{ color: isError ? '#dc2626' : '#16a34a', marginTop: '12px' }">{{ msg }}</p>
    <p style="margin-top:16px;font-size:14px;text-align:center;">
      <a href="#" @click.prevent="isRegister = !isRegister; msg = ''">
        {{ isRegister ? 'Already have an account? Login' : 'New patient? Create an account' }}
      </a>
    </p>
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
    else { msg.value = data.message || 'Login failed'; isError.value = true }
  } catch (e) {
    msg.value = 'Network error: ' + e.message; isError.value = true
  } finally { loading.value = false }
}

async function register() {
  loading.value = true; msg.value = ''
  try {
    const res = await fetch('http://localhost:7000/patient/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(reg)
    })
    const data = await res.json()
    if (data.code === 200) {
      msg.value = 'Account created! You can now log in.'
      isError.value = false
      username.value = reg.username
      isRegister.value = false
    } else {
      msg.value = data.message || 'Registration failed'; isError.value = true
    }
  } catch (e) {
    msg.value = 'Network error: ' + e.message; isError.value = true
  } finally { loading.value = false }
}
</script>
