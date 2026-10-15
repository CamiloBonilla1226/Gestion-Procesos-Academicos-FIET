import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: 'estudiante/solicitudes/:uuid',
    renderMode: RenderMode.Client
  },
  {
    path: 'funcionario-academico/solicitudes/:uuid',
    renderMode: RenderMode.Client
  },
  {
    path: 'decano/solicitudes/:uuid',
    renderMode: RenderMode.Client
  },
  {
    path: 'docente/notificaciones/:uuid',
    renderMode: RenderMode.Client
  },
  {
    path: '',
    renderMode: RenderMode.Client
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
