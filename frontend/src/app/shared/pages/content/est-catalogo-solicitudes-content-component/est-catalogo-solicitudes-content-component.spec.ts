import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { Component } from '@angular/core';
import { EstCatalogoSolicitudesContentComponent } from './est-catalogo-solicitudes-content-component';

@Component({ template: '' })
class FormularioFalso {}

describe('EstCatalogoSolicitudesContentComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([
        { path: 'estudiante/catalogo', component: EstCatalogoSolicitudesContentComponent },
        { path: 'estudiante/cancelacion-matricula', component: FormularioFalso },
        { path: 'estudiante/cancelacion-asignatura', component: FormularioFalso },
        { path: 'estudiante/examen-supletorio', component: FormularioFalso }
      ])]
    });
  });

  async function abrir() {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/estudiante/catalogo', EstCatalogoSolicitudesContentComponent);
    return harness;
  }

  it('muestra las tres tarjetas con sus titulos, requisitos y anexos', async () => {
    const harness = await abrir();
    const tarjetas: any[] = Array.from(harness.routeNativeElement!.querySelectorAll('article.tramite'));
    expect(tarjetas.length).toBe(3);
    const titulos = tarjetas.map(t => t.querySelector('h2').textContent.trim());
    expect(titulos).toEqual(['Cancelación de Matrícula', 'Cancelación de Asignatura', 'Examen Supletorio']);
    expect(tarjetas[0].querySelectorAll('[data-lista="requisitos"] li').length).toBe(3);
    expect(tarjetas[1].querySelectorAll('[data-lista="anexos"] li').length).toBe(2);
    expect(tarjetas[2].querySelector('.descripcion').textContent).toContain('examen parcial, final o de habilitación');
    expect(tarjetas[0].querySelector('[data-lista="anexos"] li').textContent.trim())
      .toBe('Paz y salvo de Bibliotecas, Deportes y Recreación, Salud Integral y División Financiera.');
  });

  it('cada boton lleva al formulario existente', async () => {
    const harness = await abrir();
    const rutas = ['/estudiante/cancelacion-matricula', '/estudiante/cancelacion-asignatura', '/estudiante/examen-supletorio'];
    for (const ruta of rutas) {
      await harness.navigateByUrl('/estudiante/catalogo');
      const boton = harness.routeNativeElement!.querySelector(`[data-tramite="${ruta}"] a.iniciar`) as HTMLAnchorElement;
      expect(boton.textContent!.trim()).toBe('Iniciar solicitud →');
      expect(boton.getAttribute('href')).toBe(ruta);
      boton.click();
      await harness.fixture.whenStable();
      expect(TestBed.inject(Router).url).toBe(ruta);
    }
  });
});
