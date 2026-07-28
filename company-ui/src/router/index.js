import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      redirect: '/admin/login'
    },

    // ======================== 管理员 ========================
    {
      path: '/admin/login',
      name: 'AdminLogin',
      component: () => import('@/views/admin/AdminLogin.vue'),
      meta: { title: '管理员登录', noAuth: true }
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      redirect: '/admin/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/Dashboard.vue'),
          meta: { title: '管理仪表盘', icon: 'Odometer' }
        },
        {
          path: 'dept',
          name: 'DeptManage',
          component: () => import('@/views/admin/DeptManage.vue'),
          meta: { title: '部门管理', icon: 'OfficeBuilding' }
        },
        {
          path: 'post',
          name: 'PostManage',
          component: () => import('@/views/admin/PostManage.vue'),
          meta: { title: '岗位管理', icon: 'Briefcase' }
        },
        {
          path: 'user',
          name: 'UserManage',
          component: () => import('@/views/admin/UserManage.vue'),
          meta: { title: '员工管理', icon: 'User' }
        },
        {
          path: 'checkin',
          name: 'AdminCheckin',
          component: () => import('@/views/admin/CheckinManage.vue'),
          meta: { title: '考勤管理', icon: 'Clock' }
        },
        {
          path: 'salary',
          name: 'AdminSalary',
          component: () => import('@/views/admin/SalaryManage.vue'),
          meta: { title: '薪资管理', icon: 'Money' }
        }
      ]
    },

    // ======================== 员工 ========================
    {
      path: '/employee/login',
      name: 'EmployeeLogin',
      component: () => import('@/views/employee/EmployeeLogin.vue'),
      meta: { title: '员工登录', noAuth: true }
    },
    {
      path: '/employee',
      component: () => import('@/layouts/EmployeeLayout.vue'),
      redirect: '/employee/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'EmployeeDashboard',
          component: () => import('@/views/employee/EmployeeDashboard.vue'),
          meta: { title: '工作台', icon: 'Monitor' }
        },
        {
          path: 'punch',
          name: 'PunchClock',
          component: () => import('@/views/employee/PunchClock.vue'),
          meta: { title: '打卡签到', icon: 'Clock' }
        },
        {
          path: 'checkins',
          name: 'MyCheckins',
          component: () => import('@/views/employee/MyCheckins.vue'),
          meta: { title: '我的考勤', icon: 'List' }
        },
        {
          path: 'salary',
          name: 'MySalary',
          component: () => import('@/views/employee/MySalary.vue'),
          meta: { title: '我的薪资', icon: 'Money' }
        },
        {
          path: 'profile',
          name: 'MyProfile',
          component: () => import('@/views/employee/MyProfile.vue'),
          meta: { title: '个人信息', icon: 'UserFilled' }
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 公司管理系统` : '公司管理系统'

  const token = localStorage.getItem('token')
  const userType = localStorage.getItem('userType')

  // 不需要登录的页面直接放行
  if (to.meta.noAuth) {
    // 已登录跳转到对应主页
    if (token && userType === 'admin' && to.path === '/admin/login') {
      return next('/admin/dashboard')
    }
    if (token && userType === 'employee' && to.path === '/employee/login') {
      return next('/employee/dashboard')
    }
    return next()
  }

  // 未登录
  if (!token) {
    if (to.path.startsWith('/admin')) {
      return next('/admin/login')
    }
    return next('/employee/login')
  }

  // 角色权限检查
  if (to.path.startsWith('/admin') && userType !== 'admin') {
    return next('/employee/dashboard')
  }
  if (to.path.startsWith('/employee') && userType === 'admin') {
    return next('/admin/dashboard')
  }

  next()
})

export default router
