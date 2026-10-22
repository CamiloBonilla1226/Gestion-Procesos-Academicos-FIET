import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ComunicacionRespuestaComponent } from './comunicacion-respuesta-component';
import { ComunicacionEnEdicion } from '../../../core/utils/notificacion-resolucion';

@Component({
  imports: [ComunicacionRespuestaComponent],
  template: `
    <app-comunicacion-respuesta-component [comunicacion]="comunicacion" sufijo="/178" nombreEstudiante="Ana Muñoz" [errores]="errores"
      [sugeridoComunicacion]="sugeridoComunicacion" [sugeridoDarca]="sugeridoDarca">
      <div data-proyectado>Resolución</div>
    </app-comunicacion-respuesta-component>
  `
})
class AnfitrionComponent {
  comunicacion: ComunicacionEnEdicion = {
    mensaje: 'Buen día',
    textoComunicacion: 'Resolución No. {numero}/178 de {anio} para {nombre}: se {decision}',
    textoDarca: 'Popayán, Cauca, {fecha}\n\nResumen',
    programacion: { fecha: '2026-10-25T09:00', numero: '45', anio: '2026', decision: 'aprueba la' }
  };
  errores: Record<string, string> = {};
  sugeridoComunicacion = 'Sugerido {numero}';
  sugeridoDarca = 'Popayán, {fecha}';
}

describe('ComunicacionRespuestaComponent', () => {
  function crear() {
    TestBed.configureTestingModule({ imports: [AnfitrionComponent] });
    const fixture = TestBed.createComponent(AnfitrionComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('muestra en orden el mensaje inmediato sin casilla, lo proyectado y la comunicacion programada', () => {
    const fixture = crear();
    const elemento: HTMLElement = fixture.nativeElement;
    expect(elemento.querySelector('[data-campo="enviar-mensaje"]')).toBeNull();
    const orden = Array.from(elemento.querySelectorAll('[data-bloque="mensaje"], [data-proyectado], [data-bloque="programacion"]'))
      .map(nodo => nodo.getAttribute('data-bloque') ?? 'proyectado');
    expect(orden).toEqual(['mensaje', 'proyectado', 'programacion']);
    const mensaje = elemento.querySelector('[data-campo-mensaje] textarea') as HTMLTextAreaElement;
    expect(mensaje.value).toBe('Buen día');
    expect(elemento.querySelector('[data-campo-mensaje] [data-vista-texto]')).toBeNull();
  });

  it('los textos del estudiante y de DARCA se ven tal como saldran y se editan ahi mismo', () => {
    const fixture = crear();
    const elemento: HTMLElement = fixture.nativeElement;
    const vistaEstudiante = elemento.querySelector('[data-campo-texto-estudiante] [data-vista-texto]') as HTMLElement;
    expect(vistaEstudiante.textContent).toContain('Resolución No. 45/178 de 2026 para Ana Muñoz: se aprueba la');
    expect(vistaEstudiante.querySelectorAll('strong').length).toBe(4);
    const vistaDarca = elemento.querySelector('[data-campo-texto-darca] [data-vista-texto]') as HTMLElement;
    expect(vistaDarca.textContent).toContain('Popayán, Cauca, 25 de octubre de 2026');
    expect(vistaDarca.querySelectorAll('strong').length).toBe(0);

    const darca = elemento.querySelector('[data-campo-texto-darca] textarea') as HTMLTextAreaElement;
    darca.value = 'Texto nuevo {fecha}';
    darca.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    expect(fixture.componentInstance.comunicacion.textoDarca).toBe('Texto nuevo {fecha}');
    expect((elemento.querySelector('[data-campo-texto-darca] [data-vista-texto]') as HTMLElement).textContent).toContain('Texto nuevo 25 de octubre');
  });

  it('cambiar el numero de la Resolucion actualiza la vista del estudiante y muestra los errores de cada texto', () => {
    const fixture = crear();
    fixture.componentInstance.comunicacion.programacion.numero = '99';
    fixture.componentInstance.errores = { mensaje: 'Falta el mensaje', textoDarca: 'Falta DARCA' };
    fixture.detectChanges();
    const elemento: HTMLElement = fixture.nativeElement;
    expect(elemento.querySelector('[data-campo-texto-estudiante] [data-vista-texto]')?.textContent).toContain('99/178');
    expect(elemento.querySelector('[data-campo-mensaje] [data-error-texto]')?.textContent).toContain('Falta el mensaje');
    expect(elemento.querySelector('[data-campo-texto-darca] [data-error-texto]')?.textContent).toContain('Falta DARCA');
  });
});
