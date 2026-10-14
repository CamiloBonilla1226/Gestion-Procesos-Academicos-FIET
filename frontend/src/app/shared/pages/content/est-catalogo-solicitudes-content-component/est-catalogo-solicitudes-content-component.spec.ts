import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { Component } from '@angular/core';
import { EstCatalogoSolicitudesContentComponent } from './est-catalogo-solicitudes-content-component';
import { VIGENCIA_PAZ_Y_SALVOS } from '../../../../core/constantes/textos-cancelacion-matricula';

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

  it('la ficha del Examen Supletorio tiene Descargar formato al final de sus dos lineas de anexos', async () => {
    const harness = await abrir();
    const lineas: HTMLElement[] = Array.from(harness.routeNativeElement!.querySelectorAll('[data-tramite="/estudiante/examen-supletorio"] [data-lista="anexos"] li'));
    expect(lineas.length).toBe(2);
    const enlaces = lineas.map(li => li.querySelector('[data-descargar-formato]') as HTMLAnchorElement);
    expect(enlaces.map(a => a.textContent!.trim())).toEqual(['Descargar formato', 'Descargar formato']);
    expect(enlaces.map(a => a.getAttribute('href'))).toEqual(['formatos/formato-firma-docente.doc', 'formatos/formato-firma-docente-cruce.doc']);
    expect(enlaces.map(a => a.getAttribute('download'))).toEqual(['formato-firma-docente.doc', 'formato-firma-docente-cruce.doc']);
    expect(lineas[0].textContent).toContain('Formato firmado por el docente que orienta la asignatura');
    expect(lineas[1].textContent).toContain('docente de la asignatura con la que se presenta el cruce');
    expect(lineas.every(li => li.lastElementChild?.tagName.toLowerCase() === 'app-enlace-formato-component')).toBeTrue();
    expect(enlaces.every(a => a.getAttribute('target') === null)).toBeTrue();
    const otras = harness.routeNativeElement!.querySelectorAll('[data-tramite="/estudiante/cancelacion-matricula"] [data-descargar-formato], [data-tramite="/estudiante/cancelacion-asignatura"] [data-descargar-formato]');
    expect(otras.length).toBe(0);
  });

  it('ninguna ficha muestra los textos informativos de Cancelacion de Matricula', async () => {
    const harness = await abrir();
    const raiz = harness.routeNativeElement!;
    expect(raiz.querySelector('[data-nota]')).toBeNull();
    expect(raiz.textContent).not.toContain(VIGENCIA_PAZ_Y_SALVOS);
    expect(raiz.textContent).not.toContain('PAZ Y SALVOS');
    expect(raiz.textContent).not.toContain('SIMCA');
    expect(raiz.textContent).not.toContain('Normativa aplicable');
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
