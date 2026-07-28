<template>
  <div class="scene-container">
    <canvas ref="canvasRef"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as THREE from 'three'

const canvasRef = ref(null)
let scene, camera, renderer, particles, grid
let animationId

const init = () => {
  const canvas = canvasRef.value
  if (!canvas) return

  // 场景
  scene = new THREE.Scene()

  // 相机
  camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 1000)
  camera.position.z = 30

  // 渲染器
  renderer = new THREE.WebGLRenderer({ canvas, alpha: true, antialias: true })
  renderer.setSize(window.innerWidth, window.innerHeight)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))

  // 粒子系统 - 星场背景
  const particlesGeo = new THREE.BufferGeometry()
  const particlesCount = 800
  const posArray = new Float32Array(particlesCount * 3)
  const colorArray = new Float32Array(particlesCount * 3)

  for (let i = 0; i < particlesCount * 3; i += 3) {
    posArray[i] = (Math.random() - 0.5) * 80
    posArray[i + 1] = (Math.random() - 0.5) * 50
    posArray[i + 2] = (Math.random() - 0.5) * 40

    // 随机分配青色或紫色
    const isCyan = Math.random() > 0.5
    colorArray[i] = isCyan ? 0.0 : 0.7
    colorArray[i + 1] = isCyan ? 0.94 : 0.28
    colorArray[i + 2] = isCyan ? 1.0 : 0.92
  }

  particlesGeo.setAttribute('position', new THREE.BufferAttribute(posArray, 3))
  particlesGeo.setAttribute('color', new THREE.BufferAttribute(colorArray, 3))

  const particlesMat = new THREE.PointsMaterial({
    size: 0.08,
    vertexColors: true,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
    transparent: true,
    opacity: 0.8
  })

  particles = new THREE.Points(particlesGeo, particlesMat)
  scene.add(particles)

  // 网格地面（赛博朋克风格）
  const gridHelper = new THREE.PolarGridHelper(25, 48, 24, 128, 0x00f0ff, 0x00f0ff)
  gridHelper.position.y = -15
  gridHelper.material.opacity = 0.12
  gridHelper.material.transparent = true
  scene.add(gridHelper)

  // 大网格
  const gridHelper2 = new THREE.PolarGridHelper(40, 32, 32, 128, 0xb347ea, 0xb347ea)
  gridHelper2.position.y = -15.5
  gridHelper2.material.opacity = 0.06
  gridHelper2.material.transparent = true
  scene.add(gridHelper2)

  grid = { h1: gridHelper, h2: gridHelper2 }

  animate()
}

const animate = () => {
  animationId = requestAnimationFrame(animate)

  // 粒子缓慢旋转
  if (particles) {
    particles.rotation.y += 0.0003
    particles.rotation.x += 0.0001
  }

  // 网格缓慢旋转
  if (grid) {
    grid.h1.rotation.z += 0.0002
    grid.h2.rotation.z -= 0.00015
  }

  renderer.render(scene, camera)
}

const onResize = () => {
  if (camera && renderer) {
    camera.aspect = window.innerWidth / window.innerHeight
    camera.updateProjectionMatrix()
    renderer.setSize(window.innerWidth, window.innerHeight)
  }
}

onMounted(() => {
  init()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  cancelAnimationFrame(animationId)
  window.removeEventListener('resize', onResize)
  if (renderer) renderer.dispose()
})
</script>

<style scoped>
.scene-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
}
canvas {
  display: block;
}
</style>
