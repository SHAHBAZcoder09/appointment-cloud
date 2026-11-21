<template>
  <div style="max-width:360px;margin:40px auto;background:#fff;padding:24px;border-radius:10px;box-shadow:0 2px 10px rgba(0,0,0,0.08);">
    <h2 style="margin-top:0;">Patient Login</h2>
    <form @submit.prevent="submit">
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
    <p v-if="error" style="color:#dc2626;margin-top:12px;">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['logged-in'])
const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

async function submit() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetch('http://localhost:7000/patient/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: username.value, password: password.value })
    })
    const data = await res.json()
    if (data.code === 200) {
      emit('logged-in', data.data)
    } else {
      error.value = data.message || 'Login failed'
    }
  } catch (e) {
    error.value = 'Network error: ' + e.message
  } finally {
    loading.value = false
  }
}
</script>
