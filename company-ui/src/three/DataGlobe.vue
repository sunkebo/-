<template>
  <div class="globe-wrapper" ref="wrapperRef">
    <canvas ref="canvasRef"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as THREE from 'three'

const canvasRef = ref(null)
const wrapperRef = ref(null)
let scene, camera, renderer, globe, dots
let animationId

const init = () => {
  const canvas = canvasRef.value
  const wrapper = wrapperRef.value
  if (!canvas || !wrapper) return

  const rect = wrapper.getBoundingClientRect()
  const w = rect.width
  const h = rect.height

  scene = new THREE.Scene()
  camera = new THREE.PerspectiveCamera(45, w / h, 0.1, 100)
  camera.position.z = 6

  renderer = new THREE.WebGLRenderer({ canvas, alpha: true, antialias: true })
  renderer.setSize(w, h)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))

  // 线框球体
  const globeGeo = new THREE.SphereGeometry(1.8, 48, 36)
  const globeMat = new THREE.MeshBasicMaterial({
    color: 0x00f0ff,
    wireframe: true,
    transparent: true,
    opacity: 0.15
  })
  globe = new THREE.Mesh(globeGeo, globeMat)
  scene.add(globe)

  // 外层线框
  const outerGeo = new THREE.SphereGeometry(1.95, 32, 24)
  const outerMat = new THREE.MeshBasicMaterial({
    color: 0xb347ea,
    wireframe: true,
    transparent: true,
    opacity: 0.08
  })
  const outerGlobe = new THREE.Mesh(outerGeo, outerMat)
  scene.add(outerGlobe)

  // 纬度环
  for (let i = 0; i < 3; i++) {
    const ringGeo = new THREE.TorusGeometry(1.85, 0.008, 16, 100)
    const ringMat = new THREE.MeshBasicMaterial({
      color: 0x00f0ff,
      transparent: true,
      opacity: 0.2
    })
    const ring = new THREE.Mesh(ringGeo, ringMat)
    ring.rotation.x = Math.PI / 2
    ring.position.y = (i - 1) * 0.9
    scene.add(ring)
  }

  // 散布点
  const dotsGeo = new THREE.BufferGeometry()
  const dotsCount = 200
  const dotsPos = new Float32Array(dotsCount * 3)

  for (let i = 0; i < dotsCount; i++) {
    const phi = Math.acos(2 * Math.random() - 1)
    const theta = Math.random() * Math.PI * 2
    const r = 1.82
    dotsPos[i * 3] = r * Math.sin(phi) * Math.cos(theta)
    dotsPos[i * 3 + 1] = r * Math.sin(phi) * Math.sin(theta)
    dotsPos[i * 3 + 2] = r * Math.cos(phi)
  }

  dotsGeo.setAttribute('position', new THREE.BufferAttribute(dotsPos, 3))
  const dotsMat = new THREE.PointsMaterial({
    size: 0.03,
    color: 0x00f0ff,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
    transparent: true,
    opacity: 0.7
  })
  dots = new THREE.Points(dotsGeo, dotsMat)
  scene.add(dots)

  animate()
}

const animate = () => {
  animationId = requestAnimationFrame(animate)

  if (globe) {
    globe.rotation.y += 0.003
    globe.rotation.x += 0.001
    scene.children.forEach(c => {
      if (c !== globe && c !== dots) {
        c.rotation.y += 0.002
      }
    })
  }
  if (dots) {
    dots.rotation.y += 0.004
    dots.rotation.x += 0.0015
  }

  renderer.render(scene, camera)
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
.globe-wrapper {
  width: 100%;
  height: 100%;
  min-height: 280px;
}
canvas {
  display: block;
  width: 100% !important;
  height: 100% !important;
}
</style>
