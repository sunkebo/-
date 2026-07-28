<template>
  <div class="holo-card" :class="{ 'holo-active': active }" @mouseenter="onHover" @mouseleave="onLeave">
    <canvas ref="canvasRef"></canvas>
    <div class="holo-content">
      <slot />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as THREE from 'three'

const props = defineProps({
  active: { type: Boolean, default: false }
})

const canvasRef = ref(null)
let scene, camera, renderer, borderFrame
let animationId

const init = () => {
  const canvas = canvasRef.value
  if (!canvas) return

  const rect = canvas.parentElement.getBoundingClientRect()
  const w = rect.width
  const h = rect.height

  scene = new THREE.Scene()
  camera = new THREE.PerspectiveCamera(45, w / h, 0.1, 100)
  camera.position.z = 5

  renderer = new THREE.WebGLRenderer({ canvas, alpha: true, antialias: true })
  renderer.setSize(w, h)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))

  // 角线效果 - 用 4 条线
  const lineLen = 0.6
  const gap = 2.0
  const lines = []

  const material = new THREE.LineBasicMaterial({
    color: 0x00f0ff,
    transparent: true,
    opacity: 0.5
  })

  // 四个角的小线段 (在 2D 平面)
  const corners = [
    { x: -gap, y: gap, dx: 1, dy: 0 },    // 左上水平
    { x: -gap, y: gap, dx: 0, dy: -1 },    // 左上垂直
    { x: gap, y: gap, dx: -1, dy: 0 },     // 右上水平
    { x: gap, y: gap, dx: 0, dy: -1 },     // 右上垂直
    { x: -gap, y: -gap, dx: 1, dy: 0 },    // 左下水平
    { x: -gap, y: -gap, dx: 0, dy: 1 },    // 左下垂直
    { x: gap, y: -gap, dx: -1, dy: 0 },    // 右下水平
    { x: gap, y: -gap, dx: 0, dy: 1 }      // 右下垂直
  ]

  corners.forEach(c => {
    const geo = new THREE.BufferGeometry()
    const start = { x: c.x, y: c.y }
    const end = { x: c.x + c.dx * lineLen, y: c.y + c.dy * lineLen }
    geo.setAttribute('position', new THREE.Float32BufferAttribute([
      start.x, start.y, 0,
      end.x, end.y, 0
    ], 3))
    const line = new THREE.Line(geo, material)
    scene.add(line)
    lines.push(line)
  })

  borderFrame = { lines }
  animate()
}

const animate = () => {
  animationId = requestAnimationFrame(animate)
  renderer.render(scene, camera)
}

const onHover = () => {
  if (borderFrame) {
    borderFrame.lines.forEach(l => { l.material.opacity = 0.9; l.material.color.set(0x00f0ff) })
  }
}

const onLeave = () => {
  if (borderFrame) {
    borderFrame.lines.forEach(l => { l.material.opacity = 0.5; l.material.color.set(0x00f0ff) })
  }
}

onMounted(() => {
  init()
})

onUnmounted(() => {
  cancelAnimationFrame(animationId)
  if (renderer) renderer.dispose()
})
</script>

<style scoped>
.holo-card {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
}
.holo-card canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100% !important;
  height: 100% !important;
  pointer-events: none;
  z-index: 1;
}
.holo-content {
  position: relative;
  z-index: 2;
}
</style>
