import { routes } from './app.routes';
import { serverRoutes } from './app.routes.server';
import { AuthGuard } from './core/auth/guards/auth-guard';
import { RoleGuard } from './core/auth/guards/role-guard';
import { RenderMode } from '@angular/ssr';

describe('Rutas de los procesos academicos', () => {
  const esperadas: [string, string][] = [
    ['estudiante', 'Estudiante'],
    ['estudiante/solicitudes', 'Estudiante'],
    ['estudiante/solicitudes/:uuid', 'Estudiante'],
    ['funcionario-academico', 'Funcionario Académico'],
    ['funcionario-academico/solicitudes', 'Funcionario Académico'],
    ['funcionario-academico/solicitudes/:uuid', 'Funcionario Académico'],
    ['decano', 'Decano'],
    ['decano/solicitudes', 'Decano'],
    ['decano/solicitudes/:uuid', 'Decano']
  ];

  for (const [ruta, rol] of esperadas) {
    it(`${ruta} exige AuthGuard y RoleGuard con el rol ${rol}`, async () => {
      const coincidencias = routes.filter(r => r.path === ruta);
      expect(coincidencias.length).toBe(1);
      const definicion = coincidencias[0];
      expect(definicion.canActivate).toEqual([AuthGuard, RoleGuard]);
      expect(definicion.data?.['roles']).toEqual([rol]);
      expect(definicion.loadComponent).toBeDefined();
      const componente = await definicion.loadComponent!();
      expect(componente).toBeTruthy();
    });
  }

  it('las rutas con uuid se renderizan en el cliente y van antes del comodin', () => {
    const indiceComodin = serverRoutes.findIndex(r => r.path === '**');
    for (const [ruta] of esperadas.filter(([r]) => r.endsWith(':uuid'))) {
      const indice = serverRoutes.findIndex(r => r.path === ruta);
      expect(indice).toBeGreaterThanOrEqual(0);
      expect(indice).toBeLessThan(indiceComodin);
      expect(serverRoutes[indice].renderMode).toBe(RenderMode.Client);
    }
  });

  it('el Decano conserva el acceso a las solicitudes del Consejo', () => {
    const consejo = routes.find(r => r.path === 'usuario-fiet/solicitudes');
    expect(consejo?.data?.['roles']).toContain('Decano');
  });
});
