import { TestBed } from '@angular/core/testing';
import { ComunicacionRespuestaComponent } from './comunicacion-respuesta-component';
import { ComunicacionEnEdicion } from '../../../core/utils/notificacion-resolucion';

describe('ComunicacionRespuestaComponent', () => {
  function crear(comunicacion: ComunicacionEnEdicion) {
    TestBed.configureTestingModule({ imports: [ComunicacionRespuestaComponent] });
    const fixture = TestBed.createComponent(ComunicacionRespuestaComponent);
    fixture.componentRef.setInput('comunicacion', comunicacion);
    fixture.componentRef.setInput('sufijo', '/178');
    fixture.detectChanges();
    return fixture;
  }

  it('la casilla viene marcada con el mensaje y al desmarcarla oculta el cuadro', () => {
    const comunicacion = { enviarMensaje: true, mensaje: 'Buen día', programacion: { fecha: '', numero: '', anio: '2026', decision: 'aprueba la' } };
    const fixture = crear(comunicacion);
    const casilla = fixture.nativeElement.querySelector('[data-campo="enviar-mensaje"]') as HTMLInputElement;
    expect(casilla.checked).toBeTrue();
    expect(fixture.nativeElement.querySelector('[data-campo-mensaje] textarea')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('[data-contador-mensaje]').textContent).toContain('8/2000');
    casilla.click();
    fixture.detectChanges();
    expect(comunicacion.enviarMensaje).toBeFalse();
    expect(fixture.nativeElement.querySelector('[data-campo-mensaje]')).toBeNull();
    expect(fixture.nativeElement.querySelector('[data-programacion]')).toBeTruthy();
  });

  it('muestra los errores de la programacion bajo cada campo', () => {
    const fixture = crear({ enviarMensaje: false, mensaje: '', programacion: { fecha: '', numero: '', anio: '2026', decision: '' } });
    fixture.componentRef.setInput('errores', { fecha: 'Elige la fecha', numero: 'Falta el número', mensaje: 'x' });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-error="fecha"]').textContent).toContain('Elige la fecha');
    expect(fixture.nativeElement.querySelector('[data-error="numero"]').textContent).toContain('Falta el número');
    expect(fixture.nativeElement.querySelector('[data-vista-previa]').textContent).toContain('Resolución No. ___/178 de 2026');
  });
});
