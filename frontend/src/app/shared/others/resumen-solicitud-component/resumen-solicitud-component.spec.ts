import { TestBed } from '@angular/core/testing';
import { ResumenSolicitudComponent } from './resumen-solicitud-component';

describe('ResumenSolicitudComponent', () => {
  function crear(codigo: string, numeroDocumento: string | null = null) {
    const fixture = TestBed.createComponent(ResumenSolicitudComponent);
    fixture.componentRef.setInput('solicitud', {
      uuidSolicitudAcademica: 's', radicado: '2026-CA-0003', uuidTipoSolicitudAcademica: 't', tipoSolicitud: 'Cancelación de Asignatura',
      fechaCreacion: '2026-10-08T10:00:00', etiqueta: 'Pendiente', etapaCodigo: 'RADICADA', numeroDocumento,
      estudiante: { uuidUsuario: 'e', nombres: 'Laura', apellidos: 'Muñoz', codigoEstudiantil: codigo, programaAcademico: 'P', semestre: '1', correoElectronico: 'l@u.co' },
      anexos: [], tieneResolucion: false, puedeDescargarResolucion: false, accionesDisponibles: []
    });
    fixture.detectChanges();
    return fixture;
  }

  function textos(fixture: ReturnType<typeof crear>, selector: string): string[] {
    return Array.from(fixture.nativeElement.querySelectorAll(selector)).map((el: any) => el.textContent.trim());
  }

  it('resume estudiante, documento, codigo, tipo y radicado', () => {
    const fixture = crear('104622010148', '1061789234');
    expect(textos(fixture, 'dt')).toEqual(['Estudiante', 'Número de CC', 'Código estudiantil', 'Tipo de proceso', 'Número de radicado']);
    expect(textos(fixture, 'dd')).toEqual(['Laura Muñoz', '1061789234', '104622010148', 'Cancelación de Asignatura', '2026-CA-0003']);
  });

  it('no muestra los campos que no llegan', () => {
    const filas = textos(crear(''), 'dt');
    expect(filas).not.toContain('Código estudiantil');
    expect(filas).not.toContain('Número de CC');
  });
});
