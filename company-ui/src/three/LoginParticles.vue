<template>
  <div class="login-scene">
    <canvas ref="canvasRef"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as THREE from 'three'

const canvasRef = ref(null)
let scene, camera, renderer, particleSystem, ringSystem
let animationId, clock

const init = () => {
  const canvas = canvasRef.value
  if (!canvas) return

  clock = new THREE.Clock()
  scene = new THREE.Scene()

  camera = new THREE.PerspectiveCamera(50, window.innerWidth / window.innerHeight, 0.1, 100)
  camera.position.z = 8
  camera.position.y = -0.5

  renderer = new THREE.WebGLRenderer({ canvas, alpha: true, antialias: true })
  renderer.setSize(window.innerWidth, window.innerHeight)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))

  // 主粒子环
  const ringGeo = new THREE.BufferGeometry()
  const ringCount = 600
  const ringPos = new Float32Array(ringCount * 3)
  const ringColor = new Float32Array(ringCount * 3)

  for (let i = 0; i < ringCount; i++) {
    const angle = (i / ringCount) * Math.PI * 2
    const radius = 3.5 + Math.random() * 1.5
    const height = (Math.random() - 0.5) * 2.5

    ringPos[i * 3] = Math.cos(angle) * radius
    ringPos[i * 3 + 1] = height
    ringPos[i * 3 + 2] = Math.sin(angle) * radius - 2

    const mix = Math.random()
    ringColor[i * 3] = mix * 0.0 + (1 - mix) * 0.7     // R: cyan=0, purple=0.7
    ringColor[i * 3 + 1] = mix * 0.94 + (1 - mix) * 0.28 // G: cyan=0.94, purple=0.28
    ringColor[i * 3 + 2] = mix * 1.0 + (1 - mix) * 0.92  // B: cyan=1.0, purple=0.92
  }

  ringGeo.setAttribute('position', new THREE.BufferAttribute(ringPos, 3))
  ringGeo.setAttribute('color', new THREE.BufferAttribute(ringColor, 3))

  const ringMat = new THREE.PointsMaterial({
    size: 0.04,
    vertexColors: true,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
    transparent: true,
    opacity: 0.8
  })

  ringSystem = new THREE.Points(ringGeo, ringMat)
  scene.add(ringSystem)

  // 中心光柱粒子
  const pillarGeo = new THREE.BufferGeometry()
  const pillarCount = 200
  const pillarPos = new Float32Array(pillarCount * 3)

  for (let i = 0; i < pillarCount; i++) {
    const angle = Math.random() * Math.PI * 2
    const radius = 0.6 + Math.random() * 0.8
    const y = (Math.random() - 0.5) * 8
    pillarPos[i * 3] = Math.cos(angle) * radius
    pillarPos[i * 3 + 1] = y
    pillarPos[i * 3 + 2] = Math.sin(angle) * radius - 2
  }

  pillarGeo.setAttribute('position', new THREE.BufferAttribute(pillarPos, 3))

  const pillarMat = new THREE.PointsMaterial({
    size: 0.03,
    color: 0x00f0ff,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
    transparent: true,
    opacity: 0.6
  })

  particleSystem = new THREE.Points(pillarGeo, pillarMat)
  scene.add(particleSystem)

  // 加入一个发光环
  const torusGeo = new THREE.TorusGeometry(2.5, 0.02, 16, 100)
  const torusMat = new THREE.MeshBasicMaterial({
    color: 0x00f0ff,
    transparent: true,
    opacity: 0.3
  })
  const torus = new THREE.Mesh(torusGeo, torusMat)
  torus.position.z = -2
  torus.rotation.x = Math.PI / 3
  scene.add(torus)
  torus.name = 'torus1'

  const torus2Geo = new THREE.TorusGeometry(3.2, 0.015, 16, 100)
  const torus2Mat = new THREE.MeshBasicMaterial({
    color: 0xb347ea,
    transparent: true,
    opacity: 0.2
  })
  const torus2 = new THREE.Mesh(torus2Geo, torus2Mat)
  torus2.position.z = -2
  torus2.rotation.x = -Math.PI / 4
  torus2.rotation.y = Math.PI / 6
  scene.add(torus2)
  torus2.name = 'torus2'

  animate()
}

const animate = () => {
  animationId = requestAnimationFrame(animate)
  const elapsed = clock.getElapsedTime()

  if (ringSystem) {
    ringSystem.rotation.y += 0.002
    ringSystem.rotation.x = Math.sin(elapsed * 0.3) * 0.05
  }
  if (particleSystem) {
    particleSystem.rotation.y -= 0.003
  }

  // 旋转光环
  const torus1 = scene.getObjectByName('torus1')
  const torus2 = scene.getObjectByName('torus2')
  if (torus1) torus1.rotation.z += 0.003
  if (torus2) torus2.rotation.z -= 0.002

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
.login-scene {
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
