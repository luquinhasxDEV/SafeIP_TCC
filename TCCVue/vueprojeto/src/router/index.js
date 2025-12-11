import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/login' },

  { path: '/login', name: 'Login', component: () => import('@/views/shared/login.vue') },

  { path: '/acesso-negado', name: 'AcessoNegado', component: () => import('@/views/shared/acessoNegado.vue') },

  { 
    path: '/funcionario/produtos', 
    name: 'ProdutosFuncionario', 
    component: () => import('@/views/shared/produtos.vue'),
    meta: { requiresAuth: true, roles: ['funcionario', 'gerente'] }
  },
  { 
    path: '/funcionario/registro', 
    name: 'RegistroFuncionario', 
    component: () => import('@/views/shared/registro.vue'),
    meta: { requiresAuth: true, roles: ['funcionario', 'gerente'] }
  },
  { 
    path: '/funcionario/lista', 
    name: 'ListaFuncionario', 
    component: () => import('@/views/shared/lista.vue'),
    meta: { requiresAuth: true, roles: ['funcionario', 'gerente'] }
  },
  { 
    path: '/funcionario/consulta/:id', 
    name: 'ConsultaFuncionario', 
    component: () => import('@/views/shared/consulta.vue'),
    meta: { requiresAuth: true, roles: ['funcionario', 'gerente'] }
  },
    { 
    path: '/gerente/produtos', 
    name: 'ProdutosGerente', 
    component: () => import('@/views/shared/produtos.vue'), 
    meta: { requiresAuth: true, roles: ['gerente'] }
  },
  { 
    path: '/gerente/registro', 
    name: 'RegistroGerente', 
    component: () => import('@/views/shared/registro.vue'),
    meta: { requiresAuth: true, roles: ['gerente'] }
  },
  { 
    path: '/gerente/lista', 
    name: 'ListaGerente', 
    component: () => import('@/views/shared/lista.vue'),
    meta: { requiresAuth: true, roles: ['gerente'] }
  },
  { 
    path: '/gerente/consulta/:id', 
    name: 'ConsultaGerente', 
    component: () => import('@/views/shared/consulta.vue'),
    meta: { requiresAuth: true, roles: ['gerente'] }
  },
  { path: '/logout', name: 'LogoutRedirect', redirect: '/login' },

  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

if (localStorage.getItem('role')) {
    console.log('[Router Init] Sessão anterior detetada. Forçando logout para iniciar no Login.');
    localStorage.removeItem('role');
    localStorage.removeItem('cpfLogado');
    localStorage.removeItem('isIpBlocked');
}

router.beforeEach((to, from, next) => {
  const role = localStorage.getItem('role')
  const isIpBlocked = localStorage.getItem('isIpBlocked') === 'true'; 
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const allowedRoles = to.meta.roles;

  if (to.path === '/logout') {
    localStorage.removeItem('role');
    localStorage.removeItem('cpfLogado');
    localStorage.removeItem('isIpBlocked'); 
    return next({ name: 'Login' });
  }

  if (isIpBlocked) {
    if (to.name !== 'AcessoNegado' && to.name !== 'Login') {
      return next({ name: 'AcessoNegado' });
    }
    return next();
  } else if (to.name === 'AcessoNegado' && !isIpBlocked && role) {
    return next({ name: role === 'gerente' ? 'ProdutosGerente' : 'ProdutosFuncionario' });
  }

  if (to.name === 'Login') {
    if (role) {
      return next({ name: role === 'gerente' ? 'ProdutosGerente' : 'ProdutosFuncionario' });
    }
    return next();
  }
  
  if (requiresAuth) {
    if (!role) {
      return next({ name: 'Login' });
    }
    
    if (allowedRoles && !allowedRoles.includes(role)) {
      console.error('Acesso negado: Você não tem permissão para aceder a esta área.');
      
      return next({ name: 'ProdutosFuncionario' }); 
    }
  }

  next();

  
})

export default router