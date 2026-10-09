import { Routes } from '@angular/router';
import { AuthGuard } from './core/auth/guards/auth-guard';
import { RoleGuard } from './core/auth/guards/role-guard';
import { ROLES_ACADEMICOS } from './core/constantes/procesos-academicos';

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
        path: 'estudiante',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: [ROLES_ACADEMICOS.ESTUDIANTE] },
        loadComponent: () => import('./core/estudiante/pages/estudiante-home-component/estudiante-home-component').then(m => m.EstudianteHomeComponent)
    },
    {
        path: 'estudiante/solicitudes',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: [ROLES_ACADEMICOS.ESTUDIANTE] },
        loadComponent: () => import('./core/estudiante/pages/estudiante-solicitudes-component/estudiante-solicitudes-component').then(m => m.EstudianteSolicitudesComponent)
    },
    {
        path: 'estudiante/solicitudes/:uuid',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: [ROLES_ACADEMICOS.ESTUDIANTE] },
        loadComponent: () => import('./core/estudiante/pages/estudiante-detalle-solicitud-component/estudiante-detalle-solicitud-component').then(m => m.EstudianteDetalleSolicitudComponent)
    },
    {
        path: 'estudiante/cancelacion-matricula',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: [ROLES_ACADEMICOS.ESTUDIANTE] },
        loadComponent: () => import('./core/estudiante/pages/estudiante-cancelacion-matricula-component/estudiante-cancelacion-matricula-component').then(m => m.EstudianteCancelacionMatriculaComponent)
    },
    {
        path: 'estudiante/cancelacion-asignatura',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: [ROLES_ACADEMICOS.ESTUDIANTE] },
        loadComponent: () => import('./core/estudiante/pages/estudiante-cancelacion-asignatura-component/estudiante-cancelacion-asignatura-component').then(m => m.EstudianteCancelacionAsignaturaComponent)
    },
    {
        path: 'estudiante/examen-supletorio',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: [ROLES_ACADEMICOS.ESTUDIANTE] },
        loadComponent: () => import('./core/estudiante/pages/estudiante-examen-supletorio-component/estudiante-examen-supletorio-component').then(m => m.EstudianteExamenSupletorioComponent)
    },
    {
        path: 'funcionario-academico',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: [ROLES_ACADEMICOS.FUNCIONARIO_ACADEMICO] },
        loadComponent: () => import('./core/funcionario-academico/pages/funcionario-academico-home-component/funcionario-academico-home-component').then(m => m.FuncionarioAcademicoHomeComponent)
    },
    {
        path: 'funcionario-academico/solicitudes',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: [ROLES_ACADEMICOS.FUNCIONARIO_ACADEMICO] },
        loadComponent: () => import('./core/funcionario-academico/pages/funcionario-academico-solicitudes-component/funcionario-academico-solicitudes-component').then(m => m.FuncionarioAcademicoSolicitudesComponent)
    },
    {
        path: 'funcionario-academico/solicitudes/:uuid',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: [ROLES_ACADEMICOS.FUNCIONARIO_ACADEMICO] },
        loadComponent: () => import('./core/funcionario-academico/pages/funcionario-academico-detalle-solicitud-component/funcionario-academico-detalle-solicitud-component').then(m => m.FuncionarioAcademicoDetalleSolicitudComponent)
    },
    {
        path: 'decano',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: [ROLES_ACADEMICOS.DECANO] },
        loadComponent: () => import('./core/decano/pages/decano-home-component/decano-home-component').then(m => m.DecanoHomeComponent)
    },
    {
        path: 'decano/solicitudes',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: [ROLES_ACADEMICOS.DECANO] },
        loadComponent: () => import('./core/decano/pages/decano-solicitudes-component/decano-solicitudes-component').then(m => m.DecanoSolicitudesComponent)
    },
    {
        path: 'decano/solicitudes/:uuid',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: [ROLES_ACADEMICOS.DECANO] },
        loadComponent: () => import('./core/decano/pages/decano-detalle-solicitud-component/decano-detalle-solicitud-component').then(m => m.DecanoDetalleSolicitudComponent)
    },
    {
        path: 'decano/solicitudes-consejo',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: [ROLES_ACADEMICOS.DECANO] },
        loadComponent: () => import('./core/decano/pages/decano-solicitudes-consejo-component/decano-solicitudes-consejo-component').then(m => m.DecanoSolicitudesConsejoComponent)
    },
    {
        path: '',
        redirectTo: 'usuario-publico/solicitudes',
        pathMatch: 'full'
    }
];
