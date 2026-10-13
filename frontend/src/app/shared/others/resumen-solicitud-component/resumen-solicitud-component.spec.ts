import { TestBed } from '@angular/core/testing';
import { ResumenSolicitudComponent } from './resumen-solicitud-component';

describe('ResumenSolicitudComponent', () => {
  function crear(codigo: string) {
    const fixture = TestBed.createComponent(ResumenSolicitudComponent);
    fixture.componentRef.setInput('solicitud', {
      uuidSolicitudAcademica: 's', radicado: '2026-CA-0003', uuidTipoSolicitudAcademica: 't', tipoSolicitud: 'Cancelación de Asignatura',
      fechaCreacion: '2026-10-08T10:00:00', etiqueta: 'Pendiente', etapaCodigo: 'RADICADA',
      estudiante: { uuidUsuario: 'e', nombres: 'Laura', apellidos: 'Muñoz', codigoEstudiantil: codigo, programaAcademico: 'P', semestre: '1', correoElectronico: 'l@u.co' },
      anexos: [], tieneResolucion: false, puedeDescargarResolucion: false, accionesDisponibles: []
    });
    fixture.detectChanges();
    return fixture;
  }

  it('resume estudiante, codigo, tipo y radicado', () => {
    const fixture = crear('104622010148');
    const filas = Array.from(fixture.nativeElement.querySelectorAll('dt')).map((dt: any) => dt.textContent.trim());
    const valores = Array.from(fixture.nativeElement.querySelectorAll('dd')).map((dd: any) => dd.textContent.trim());
    expect(filas).toEqual(['Estudiante', 'Código estudiantil', 'Tipo de proceso', 'Número de radicado']);
    expect(valores).toEqual(['Laura Muñoz', '104622010148', 'Cancelación de Asignatura', '2026-CA-0003']);
  });

  it('no muestra los campos que no llegan', () => {
    const filas = Array.from(crear('').nativeElement.querySelectorAll('dt')).map((dt: any) => dt.textContent.trim());
    expect(filas).not.toContain('Código estudiantil');
    expect(filas).not.toContain('Documento');
  });
});
