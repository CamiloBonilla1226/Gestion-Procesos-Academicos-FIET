import { Routes } from '@angular/router';
import { AuthGuard } from './core/auth/guards/auth-guard';
import { RoleGuard } from './core/auth/guards/role-guard';

export const routes: Routes = [
    // Rutas autenticación
    {
        path: 'login',
        loadComponent: () => import('./core/auth/pages/login-component/login-component').then(m => m.LoginComponent)
    },
    // Rutas publicas
    {
        path: 'usuario-publico/solicitudes',
        loadComponent: () => import('./core/usuario publico/pages/usuario-publico-solicitudes-component/usuario-publico-solicitudes-component').then(m => m.UsuarioPublicoSolicitudesComponent)
    },
    // Rutas home
    {
        path: 'sec-general',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['Secretario General']},
        loadComponent: () => import('./core/secretario-general/pages/secretario-general-home-component/secretario-general-home-component').then(m => m.SecretarioGeneralHomeComponent)
    },
    {
        path: 'usuario-fiet',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['Coordinador Pregrado', 'Coordinador Posgrados', 'Jefe de Departamento', 'Decano', 'Docente']},
        loadComponent: () => import('./core/usuario fiet/pages/usuario-fiet-home-component/usuario-fiet-home-component').then(m => m.UsuarioFietHomeComponent)
    },
    {
        path: 'funcionario',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['Funcionario']},
        loadComponent: () => import('./core/funcionario/pages/funcionario-home-component/funcionario-home-component').then(m => m.FuncionarioHomeComponent)
    },
    {
        path: 'sec-fiet',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['Secretaria Decanatura FIET']},
        loadComponent: () => import('./core/secretariaFiet/pages/secretaria-fiet-home-component/secretaria-fiet-home-component').then(m => m.SecretariaFietHomeComponent)
    },
    // Vistas
    {
        path: 'sec-general/roles',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['Secretario General']},
        loadComponent: () => import('./core/secretario-general/pages/secretario-general-roles-component/secretario-general-roles-component').then(m => m.SecretarioGeneralRolesComponent)
    },
    {
        path: 'sec-general/usuarios',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['Secretario General']},
        loadComponent: () => import('./core/secretario-general/pages/secretario-general-usuarios-component/secretario-general-usuarios-component').then(m => m.SecretarioGeneralUsuariosComponent)
    },
    {
        path: 'sec-general/logs',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['Secretario General']},
        loadComponent: () => import('./core/secretario-general/pages/secretario-general-log-component/secretario-general-log-component').then(m => m.SecretarioGeneralLogComponent)
    },
    {
        path: 'sec-general/tipos/solicitudes',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['Secretario General']},
        loadComponent: () => import('./core/secretario-general/pages/secretario-general-tipo-solicitudes-component/secretario-general-tipo-solicitudes-component').then(m => m.SecretarioGeneralTipoSolicitudesComponent)
    },
    {
        path: 'sec-general/solicitudes',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['Secretario General']},
        loadComponent: () => import('./core/secretario-general/pages/secretario-general-solicitudes/secretario-general-solicitudes').then(m => m.SecretarioGeneralSolicitudes)
    },
    {
        path: 'funcionario/solicitudes',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['Funcionario']},
        loadComponent: () => import('./core/funcionario/pages/funcionario-solicitudes/funcionario-solicitudes').then(m => m.FuncionarioSolicitudes)
    },
    {
        path: 'sec-general/orden-del-dia',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['Secretario General']},
        loadComponent: () => import('./core/secretario-general/pages/secretario-general-ordel-del-dia/secretario-general-ordel-del-dia').then(m => m.SecretarioGeneralOrdelDelDia)
    },
    {
        path: 'sec-general/respuestas',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['Secretario General']},
        loadComponent: () => import('./core/secretario-general/pages/secretario-general-respuestas-component/secretario-general-respuestas-component') .then(m => m.SecretarioGeneralRespuestasComponent)
    },
    {
        path: 'funcionario/respuestas',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['Funcionario']},
        loadComponent: () => import('./core/funcionario/pages/funcionario-respuestas-component/funcionario-respuestas-component') .then(m => m.FuncionarioRespuestasComponent)
    },
    {
        path: 'sec-fiet/respuestas',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['Secretaria Decanatura FIET']},
        loadComponent: () => import('./core/secretariaFiet/pages/secretaria-fiet-respuestas-component/secretaria-fiet-respuestas-component') .then(m => m.SecretariaFietRespuestasComponent)
    },
    {
        path: 'usuario-fiet/solicitudes',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['Coordinador Pregrado', 'Coordinador Posgrados', 'Jefe de Departamento', 'Decano', 'Docente']},
        loadComponent: () => import('./core/usuario fiet/pages/usuariofiet-solicitudes-component/usuariofiet-solicitudes-component').then(m => m.UsuariofietSolicitudesComponent)
    },
    {
        path: '',
        redirectTo: 'usuario-publico/solicitudes',
        pathMatch: 'full'
    }
];
