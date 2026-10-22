import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ActivatedRoute, ParamMap, convertToParamMap } from '@angular/router';
import { BehaviorSubject, Subject, of, throwError } from 'rxjs';
import { CancelacionAsignaturaProcesoComponent } from './cancelacion-asignatura-proceso-component';
import { CancelacionAsignaturaService } from '../../../../core/services/cancelacion-asignatura-service';
import { SolicitudAcademicaService } from '../../../../core/services/solicitud-academica-service';
import { CatalogoAcademicoService } from '../../../../core/services/catalogo-academico-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { SolicitudAcademicaDetalleDTORespuesta } from '../../../../core/models/SolicitudAcademica/DTOResponse/SolicitudAcademicaDetalleDTORespuesta';
import { CancelacionAsignaturaDetalleDTORespuesta } from '../../../../core/models/CancelacionAsignatura/DTOResponse/CancelacionAsignaturaDetalleDTORespuesta';
import { AsignaturaSolicitadaDTORespuesta } from '../../../../core/models/CancelacionAsignatura/DTOResponse/AsignaturaSolicitadaDTORespuesta';
import { InputAnexoUploadComponent } from '../../../inputs/input-anexo-upload-component/input-anexo-upload-component';
import { environment } from '../../../../../enviroments/environment';
import { NOTA_SIMCA } from '../../../../core/constantes/textos-cancelacion-matricula';
import { PlantillaNotificacionResolucionDTORespuesta } from '../../../../core/models/CancelacionAsignatura/DTOResponse/PlantillaNotificacionResolucionDTORespuesta';

const PLANTILLA: PlantillaNotificacionResolucionDTORespuesta = {
  mensajeInicial: 'Buen día, estudiante,\n\nAcérquese a la Decanatura.\n\nCordialmente,\n\nCRISTIAN ESTIBEN ILLERA OSPITIA',
  maximoMensaje: 2000,
  decisionPropuesta: 'aprueba parcialmente la',
  anioResolucion: 2026,
  sufijoResolucion: '/178',
  textoComunicacion: 'RESOLUCIÓN No. {numero}/178 de {anio}\n\nSe comunica al estudiante, {nombre}, que se {decision} cancelación.',
  textoDarca: 'Popayán, Cauca, {fecha}\n\nResumen para DARCA.',
  maximoTexto: 4000,
  nombreEstudiante: 'María Pérez'
};

const CUERPO_COMUNICACION = {
  enviarMensaje: true,
  mensaje: PLANTILLA.mensajeInicial,
  textoComunicacion: PLANTILLA.textoComunicacion,
  textoDarca: PLANTILLA.textoDarca,
  programacion: { fechaProgramada: '2099-01-01T08:00', numeroResolucion: '123-26', anioResolucion: 2026, decision: 'aprueba parcialmente la' }
};

function programar(componente: CancelacionAsignaturaProcesoComponent): void {
  componente.comunicacion!.programacion.fecha = '2099-01-01T08:00';
  componente.comunicacion!.programacion.numero = '123-26';
}

function solicitud(acciones: string[], etapaCodigo = 'RADICADA', tieneResolucion = false, etiqueta = 'Pendiente'): SolicitudAcademicaDetalleDTORespuesta {
  return {
    uuidSolicitudAcademica: 's-1', radicado: '2026-CA-0001', uuidTipoSolicitudAcademica: 't',
    tipoSolicitud: 'Cancelación de Asignatura', fechaCreacion: '2026-10-08T10:00:00', etiqueta, etapaCodigo,
    estudiante: {
      uuidUsuario: 'e', nombres: 'María', apellidos: 'Pérez', codigoEstudiantil: '1', programaAcademico: 'P',
      semestre: '1', correoElectronico: 'm@u.co'
    },
    anexos: [], tieneResolucion, puedeDescargarResolucion: tieneResolucion, accionesDisponibles: acciones
  };
}

function soloCodigos(codigo: string, nombre: string, uuid: string): AsignaturaSolicitadaDTORespuesta {
  return {
    uuidAsignaturaSolicitud: uuid, codigoAsignatura: codigo, nombreAsignatura: nombre, numeroFaltas: null, nota: null,
    situacionMatricula: null, cumpleCondiciones: null, observacionEvaluacion: null, aprobadaPorDecano: null,
    observacionDecision: null, situacionCancelar: null
  };
}

function detalle(asignaturas: AsignaturaSolicitadaDTORespuesta[]): CancelacionAsignaturaDetalleDTORespuesta {
  return { solicitud: solicitud([]), motivoCancelacion: 'Cruce laboral', asignaturas };
}

const EVALUADAS: AsignaturaSolicitadaDTORespuesta[] = [
  {
    ...soloCodigos('A1', 'Cálculo', 'as-1'), numeroFaltas: '1', nota: '3.8',
    situacionMatricula: { uuidSituacionAcademica: 'r0', codigo: 'R1', nombre: 'Cursada por primera vez' },
    cumpleCondiciones: true, observacionEvaluacion: null
  },
  {
    ...soloCodigos('A2', 'Física', 'as-2'), numeroFaltas: '6', nota: '2.4',
    situacionMatricula: { uuidSituacionAcademica: 'r2', codigo: 'R2', nombre: 'Cursada por segunda vez' },
    cumpleCondiciones: false, observacionEvaluacion: 'Nota menor a 3.0'
  }
];

describe('CancelacionAsignaturaProcesoComponent', () => {
  let cancelaciones: jasmine.SpyObj<CancelacionAsignaturaService>;
  let solicitudes: jasmine.SpyObj<SolicitudAcademicaService>;
  let catalogo: jasmine.SpyObj<CatalogoAcademicoService>;
  let errores: jasmine.SpyObj<ErrorHandlerService>;
  let recargar: jasmine.Spy;
  const entrada = { reset: jasmine.createSpy('reset') } as unknown as InputAnexoUploadComponent;

  beforeEach(() => {
    cancelaciones = jasmine.createSpyObj<CancelacionAsignaturaService>('CancelacionAsignaturaService', [
      'getDetalle', 'rechazarPorFuncionario', 'remitirADecano', 'enviarRespuesta', 'aprobarPorDecano', 'rechazarPorDecano',
      'getPlantillaNotificacion', 'getNotificacion'
    ]);
    cancelaciones.getPlantillaNotificacion.and.returnValue(of(PLANTILLA));
    cancelaciones.getNotificacion.and.returnValue(of(null));
    solicitudes = jasmine.createSpyObj<SolicitudAcademicaService>('SolicitudAcademicaService', ['adjuntarResolucion', 'adjuntarAnexo']);
    catalogo = jasmine.createSpyObj<CatalogoAcademicoService>('CatalogoAcademicoService', ['getSituaciones', 'getTiposAnexoPorTipo']);
    errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    recargar = jasmine.createSpy('recargar');
    catalogo.getSituaciones.and.returnValue(of([
      { uuidSituacionAcademica: 'r0', codigo: 'R1', nombre: 'Cursada por primera vez', ambito: 'MATRICULA' },
      { uuidSituacionAcademica: 'r3', codigo: 'C1', nombre: 'No vista', ambito: 'CANCELACION' }
    ]));
    TestBed.configureTestingModule({
      imports: [CancelacionAsignaturaProcesoComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        { provide: CancelacionAsignaturaService, useValue: cancelaciones },
        { provide: SolicitudAcademicaService, useValue: solicitudes },
        { provide: CatalogoAcademicoService, useValue: catalogo },
        { provide: ErrorHandlerService, useValue: errores },
        { provide: ToastService, useValue: jasmine.createSpyObj('ToastService', ['showSuccess', 'showError']) }
      ]
    });
  });

  function crear(
    datos: SolicitudAcademicaDetalleDTORespuesta,
    asignaturas: AsignaturaSolicitadaDTORespuesta[],
    rol = 'FUNCIONARIO'
  ): ComponentFixture<CancelacionAsignaturaProcesoComponent> {
    cancelaciones.getDetalle.and.returnValue(of(detalle(asignaturas)));
    const fixture = TestBed.createComponent(CancelacionAsignaturaProcesoComponent);
    fixture.componentRef.setInput('uuidSolicitud', 's-1');
    fixture.componentRef.setInput('solicitud', datos);
    fixture.componentRef.setInput('rol', rol);
    fixture.componentRef.setInput('recargar', recargar);
    fixture.detectChanges();
    return fixture;
  }

  function encabezados(fixture: ComponentFixture<CancelacionAsignaturaProcesoComponent>): string[] {
    return Array.from(fixture.nativeElement.querySelectorAll('thead th')).map((th: any) => th.textContent.trim());
  }

  function botones(fixture: ComponentFixture<CancelacionAsignaturaProcesoComponent>): string[] {
    return Array.from(fixture.nativeElement.querySelectorAll('[data-accion]')).map((b: any) => b.getAttribute('data-accion'));
  }

  describe('Estudiante', () => {
    it('antes de la etapa final solo ve codigo y nombre de cada asignatura', () => {
      const fixture = crear(solicitud([], 'EN_REVISION_DECANO', false, 'En trámite'), [soloCodigos('A1', 'Cálculo', 'as-1'), soloCodigos('A2', 'Física', 'as-2')], 'ESTUDIANTE');
      expect(encabezados(fixture)).toEqual(['Código', 'Asignatura']);
      const texto = fixture.nativeElement.textContent;
      expect(texto).toContain('Cálculo');
      expect(texto).not.toContain('null');
      expect(texto).not.toContain('Sin registrar');
      expect(fixture.nativeElement.querySelector('[data-acciones]')).toBeNull();
    });

    it('aunque el backend enviara la evaluacion, no la muestra', () => {
      const fixture = crear(solicitud([], 'APROBADA', false, 'Aprobada'), EVALUADAS, 'ESTUDIANTE');
      const columnas = encabezados(fixture);
      for (const prohibida of ['Faltas', 'Nota', 'Situación en la matrícula', 'Cumple condiciones', 'Observación de la evaluación', 'Situación al cancelar']) {
        expect(columnas).not.toContain(prohibida);
      }
      expect(fixture.nativeElement.textContent).not.toContain('Nota menor a 3.0');
    });

    it('en la etapa final ve la decision del Decano por asignatura pero nunca su observacion', () => {
      const asignaturas = [
        { ...soloCodigos('A1', 'Cálculo', 'as-1'), aprobadaPorDecano: true },
        { ...soloCodigos('A2', 'Física', 'as-2'), aprobadaPorDecano: false, observacionDecision: 'No cumple las condiciones' }
      ];
      const fixture = crear(solicitud([], 'APROBADA', false, 'Aprobada'), asignaturas, 'ESTUDIANTE');
      expect(encabezados(fixture)).toEqual(['Código', 'Asignatura', 'Decisión del Decano', 'Situación académica al cancelar']);
      const filas = fixture.nativeElement.querySelectorAll('tbody tr');
      expect(filas[0].textContent).toContain('Aprobada');
      expect(filas[0].querySelectorAll('td')[3].textContent?.trim()).toBe('—');
      expect(filas[1].textContent).toContain('No aprobada');
      expect(fixture.nativeElement.textContent).not.toContain('No cumple las condiciones');
    });

    it('en la etapa final ve la situacion al cancelar de las aprobadas', () => {
      const asignaturas = [
        { ...soloCodigos('A1', 'Cálculo', 'as-1'), aprobadaPorDecano: true, situacionCancelar: { uuidSituacionAcademica: 'r1', codigo: 'C2', nombre: 'Perdida' } },
        { ...soloCodigos('A2', 'Física', 'as-2'), aprobadaPorDecano: false }
      ];
      const fixture = crear(solicitud([], 'APROBADA', false, 'Aprobada'), asignaturas, 'ESTUDIANTE');
      expect(encabezados(fixture)).toEqual(['Código', 'Asignatura', 'Decisión del Decano', 'Situación académica al cancelar']);
      const filas = fixture.nativeElement.querySelectorAll('tbody tr');
      expect(filas[0].textContent).toContain('Perdida');
      expect(filas[0].textContent).not.toContain('C2');
      expect(filas[1].textContent).toContain('—');
    });

    it('decide que la etapa es final por la etiqueta y no por etapaCodigo', () => {
      const asignaturas = [{ ...soloCodigos('A1', 'Cálculo', 'as-1'), aprobadaPorDecano: true }];
      expect(encabezados(crear(solicitud([], 'APROBADA', false, 'En trámite'), asignaturas, 'ESTUDIANTE'))).toEqual(['Código', 'Asignatura']);
      expect(encabezados(crear(solicitud([], 'EN_REVISION_DECANO', false, 'Rechazada'), asignaturas, 'ESTUDIANTE'))).toEqual([
        'Código', 'Asignatura', 'Decisión del Decano', 'Situación académica al cancelar'
      ]);
    });

    it('en la etapa final sin decisiones no pinta columnas vacias', () => {
      const fixture = crear(solicitud([], 'RECHAZADA', false, 'Rechazada'), [soloCodigos('A1', 'Cálculo', 'as-1')], 'ESTUDIANTE');
      expect(encabezados(fixture)).toEqual(['Código', 'Asignatura']);
      expect(fixture.nativeElement.textContent).not.toContain('null');
    });
  });

  it('el Funcionario y el Decano ven guion en los datos de la Facultad que no se registraron, sin ocultar columnas', () => {
    const sinDatos = EVALUADAS.map(a => ({ ...a, numeroFaltas: null, nota: null, situacionMatricula: null }));
    const fixture = crear(solicitud([], 'EN_REVISION_DECANO'), sinDatos, 'DECANO');
    expect(fixture.componentInstance.columnas.map(c => c.titulo)).toEqual([
      'Código', 'Asignatura', 'Faltas', 'Nota', 'Situación en la matrícula', 'Cumple condiciones'
    ]);
    const celdas = Array.from(fixture.nativeElement.querySelectorAll('tbody tr')[0].querySelectorAll('td')) as HTMLElement[];
    expect(celdas.slice(2, 5).map(c => c.textContent?.trim())).toEqual(['—', '—', '—']);
  });

  it('el Funcionario y el Decano ven la evaluacion sin la observacion del Funcionario en la tabla', () => {
    const fixture = crear(solicitud([], 'EN_REVISION_DECANO'), EVALUADAS, 'DECANO');
    expect(fixture.componentInstance.columnas.map(c => c.titulo)).toEqual([
      'Código', 'Asignatura', 'Faltas', 'Nota', 'Situación en la matrícula', 'Cumple condiciones'
    ]);
    expect(encabezados(fixture)).toEqual([
      'Código', 'Asignatura', 'Espacio a diligenciar por la Facultad a la que pertenece el estudiante', 'Evaluación del Funcionario',
      'No. Faltas', 'Nota', 'Situación Académica de la(s) Asignatura(s)', 'Cumple condiciones', 'En la Matrícula'
    ]);
    expect(fixture.nativeElement.querySelectorAll('tbody tr')[1].textContent).toContain('2.4');
    expect(fixture.nativeElement.textContent).not.toContain('Nota menor a 3.0');
    expect(fixture.nativeElement.querySelector('[data-tabla-asignaturas] input, [data-tabla-asignaturas] select')).toBeNull();
  });

  it('el Funcionario y el Decano ven la nota de SIMCA justo encima de la tabla de asignaturas', () => {
    for (const rol of ['FUNCIONARIO', 'DECANO']) {
      const fixture = crear(solicitud([], 'EN_REVISION_DECANO'), EVALUADAS, rol);
      const nota = fixture.nativeElement.querySelector('[data-nota="simca"]') as HTMLElement;
      expect(nota.textContent!.trim()).toBe(NOTA_SIMCA);
      expect(nota.textContent!.trim()).toBe('NOTA: La información de faltas y notas corresponde a la registrada en el Sistema Integrado de Matrícula y Control Académico – SIMCA a la fecha en que se recibe la solicitud de cancelación.');
      expect(nota.classList).toContain('nota-aviso');
      const tabla = fixture.nativeElement.querySelector('[data-tabla-asignaturas]') as HTMLElement;
      expect(nota.nextElementSibling).toBe(tabla.parentElement);
      expect(fixture.nativeElement.textContent).not.toContain('Normativa aplicable');
      fixture.destroy();
    }
  });

  it('el Estudiante no ve la nota de SIMCA en el detalle', () => {
    const fixture = crear(solicitud([], 'EN_REVISION_DECANO'), EVALUADAS, 'ESTUDIANTE');
    expect(fixture.nativeElement.querySelector('[data-tabla-asignaturas]')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('[data-nota="simca"]')).toBeNull();
    expect(fixture.nativeElement.textContent).not.toContain('SIMCA');
  });

  it('solo pinta los botones que vienen en accionesDisponibles', () => {
    expect(botones(crear(solicitud(['RECHAZAR_FUNCIONARIO', 'REMITIR_DECANO']), EVALUADAS))).toEqual(['RECHAZAR_FUNCIONARIO', 'REMITIR_DECANO']);
    expect(botones(crear(solicitud(['APROBAR_DECANO', 'RECHAZAR_DECANO'], 'EN_REVISION_DECANO'), EVALUADAS, 'DECANO'))).toEqual([
      'ENVIAR_DECISION'
    ]);
    expect(botones(crear(solicitud(['ENVIAR_RECIBO']), EVALUADAS))).toEqual([]);
  });

  it('el boton abre el dialogo de confirmacion con su titulo', () => {
    const fixture = crear(solicitud(['REMITIR_DECANO']), [soloCodigos('A1', 'Cálculo', 'as-1')]);
    fixture.componentInstance.evaluaciones['as-1'] = { faltas: '0', nota: '4.0', situacion: 'r0', cancelar: null, cumple: true };
    (fixture.nativeElement.querySelector('[data-accion="REMITIR_DECANO"] button') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(fixture.componentInstance.dialogoVisible).toBeTrue();
    expect(fixture.componentInstance.tituloDialogo).toBe('Remitir la solicitud al Decano');
    expect(document.querySelector('[data-nota-remitir]')).toBeTruthy();
    expect(document.querySelector('[data-dialogo] [data-evaluacion]')).toBeNull();
  });

  describe('remitir al Decano', () => {
    function abrirRemision() {
      return crear(solicitud(['REMITIR_DECANO']), [soloCodigos('A1', 'Cálculo', 'as-1'), soloCodigos('A2', 'Física', 'as-2')]);
    }

    it('con nota numerica menor a 3.0 no se puede marcar que cumple, tambien con coma', () => {
      const componente = abrirRemision().componentInstance;
      componente.evaluaciones['as-1'] = { faltas: '0', nota: '2.9', situacion: 'r0', cancelar: null, cumple: true };
      componente.evaluaciones['as-2'] = { faltas: '0', nota: '2,5', situacion: 'r0', cancelar: null, cumple: true };
      componente.abrir('REMITIR_DECANO');
      componente.confirmar();
      expect(componente.errores['cumple-as-1']).toContain('menor a 3.0');
      expect(componente.errores['cumple-as-2']).toContain('menor a 3.0');
      expect(cancelaciones.remitirADecano).not.toHaveBeenCalled();
    });

    it('faltas y nota aceptan texto y con nota de texto no aplica la regla numerica', () => {
      cancelaciones.remitirADecano.and.returnValue(of(detalle(EVALUADAS)));
      const componente = abrirRemision().componentInstance;
      componente.evaluaciones['as-1'] = { faltas: 'Sin registro', nota: 'Sin nota', situacion: 'r0', cancelar: null, cumple: true };
      componente.evaluaciones['as-2'] = { faltas: ' 3 ', nota: ' 4,5 ', situacion: 'r0', cancelar: null, cumple: true };
      componente.abrir('REMITIR_DECANO');
      componente.confirmar();
      expect(componente.errores).toEqual({});
      const cuerpo = cancelaciones.remitirADecano.calls.mostRecent().args[1];
      expect(cuerpo.evaluaciones.map(e => [e.numeroFaltas, e.nota])).toEqual([['Sin registro', 'Sin nota'], ['3', '4,5']]);
    });

    it('rechaza faltas o nota de mas de 20 caracteres y numeros fuera de rango', () => {
      const componente = abrirRemision().componentInstance;
      componente.evaluaciones['as-1'] = { faltas: 'x'.repeat(21), nota: 'y'.repeat(21), situacion: 'r0', cancelar: null, cumple: true };
      componente.evaluaciones['as-2'] = { faltas: '-1', nota: '5.5', situacion: 'r0', cancelar: null, cumple: true };
      componente.abrir('REMITIR_DECANO');
      componente.confirmar();
      expect(componente.errores['faltas-as-1']).toContain('20');
      expect(componente.errores['nota-as-1']).toContain('20');
      expect(componente.errores['faltas-as-2']).toContain('entero mayor o igual a 0');
      expect(componente.errores['nota-as-2']).toContain('entre 0.0 y 5.0');
      expect(componente.errores['cumple-as-2']).toBeUndefined();
      expect(cancelaciones.remitirADecano).not.toHaveBeenCalled();
    });

    it('muestra un solo campo de observaciones para el Decano, opcional', () => {
      const fixture = abrirRemision();
      fixture.componentInstance.evaluaciones['as-1'] = { faltas: '0', nota: '4.0', situacion: 'r0', cancelar: null, cumple: true };
      fixture.componentInstance.evaluaciones['as-2'] = { faltas: '0', nota: '4.0', situacion: 'r0', cancelar: null, cumple: true };
      fixture.componentInstance.abrir('REMITIR_DECANO');
      fixture.detectChanges();
      const campos = document.querySelectorAll('[data-campo-observacion]');
      expect(campos.length).toBe(1);
      expect(campos[0].textContent).toContain('Observaciones para el Decano');
      expect(campos[0].textContent).not.toContain('*');
      expect(document.querySelectorAll('[data-dialogo] textarea').length).toBe(1);
      expect(fixture.nativeElement.querySelector('[data-tabla-asignaturas]')?.parentElement?.parentElement?.querySelector('textarea')).toBeNull();
    });

    it('aunque alguna no cumpla la observacion sigue siendo opcional y no se copia a las asignaturas', () => {
      cancelaciones.remitirADecano.and.returnValue(of(detalle(EVALUADAS)));
      const fixture = abrirRemision();
      const componente = fixture.componentInstance;
      componente.evaluaciones['as-1'] = { faltas: '0', nota: '4.0', situacion: 'r0', cancelar: null, cumple: true };
      componente.evaluaciones['as-2'] = { faltas: '0', nota: '2.0', situacion: 'r0', cancelar: null, cumple: false };
      componente.abrir('REMITIR_DECANO');
      fixture.detectChanges();
      expect(componente.observacionObligatoria).toBeFalse();
      expect(document.querySelector('[data-campo-observacion] [data-contador]')?.textContent?.trim()).toBe('0/500');
      componente.confirmar();
      expect(componente.errores).toEqual({});
      const cuerpo = cancelaciones.remitirADecano.calls.mostRecent().args[1];
      expect(cuerpo.observacion).toBeNull();
      expect(cuerpo.evaluaciones.map(e => e.observacionEvaluacion)).toEqual([null, null]);
    });

    it('si ninguna cumple igual remite al Decano, sin aviso de rechazo ni Resolucion', () => {
      cancelaciones.remitirADecano.and.returnValue(of(detalle(EVALUADAS)));
      const fixture = abrirRemision();
      const componente = fixture.componentInstance;
      componente.evaluaciones['as-1'] = { faltas: '0', nota: '2.0', situacion: 'r0', cancelar: null, cumple: false };
      componente.evaluaciones['as-2'] = { faltas: '0', nota: '2.0', situacion: 'r0', cancelar: null, cumple: false };
      fixture.detectChanges();
      expect(fixture.nativeElement.querySelector('[data-error="ninguna"]')).toBeNull();
      (fixture.nativeElement.querySelector('[data-accion="REMITIR_DECANO"] button') as HTMLButtonElement).click();
      fixture.detectChanges();
      expect(componente.dialogoVisible).toBeTrue();
      expect(fixture.nativeElement.querySelector('[data-error="tabla"]')).toBeNull();
      expect(document.querySelector('[data-campo-resolucion]')).toBeNull();
      componente.confirmar();
      expect(componente.errores).toEqual({});
      expect(solicitudes.adjuntarResolucion).not.toHaveBeenCalled();
      const cuerpo = cancelaciones.remitirADecano.calls.mostRecent().args[1];
      expect(cuerpo.evaluaciones.map(e => e.cumpleCondiciones)).toEqual([false, false]);
      expect(recargar).toHaveBeenCalled();
    });

    it('envia el cuerpo exacto de la remision', () => {
      cancelaciones.remitirADecano.and.returnValue(of(detalle(EVALUADAS)));
      const componente = abrirRemision().componentInstance;
      componente.evaluaciones['as-1'] = { faltas: '2', nota: '3,5', situacion: 'r0', cancelar: 'r3', cumple: true };
      componente.evaluaciones['as-2'] = { faltas: '7', nota: '2.4', situacion: 'r3', cancelar: null, cumple: false };
      componente.abrir('REMITIR_DECANO');
      componente.observacion = ' Repitente ';
      componente.confirmar();
      expect(cancelaciones.remitirADecano).toHaveBeenCalledWith('s-1', {
        observacion: 'Repitente',
        evaluaciones: [
          { asignaturaSolicitudUuid: 'as-1', numeroFaltas: '2', nota: '3,5', situacionMatriculaUuid: 'r0', situacionCancelarUuid: 'r3', cumpleCondiciones: true, observacionEvaluacion: null },
          { asignaturaSolicitudUuid: 'as-2', numeroFaltas: '7', nota: '2.4', situacionMatriculaUuid: 'r3', situacionCancelarUuid: null, cumpleCondiciones: false, observacionEvaluacion: null }
        ]
      });
      expect(recargar).toHaveBeenCalled();
    });

    it('sin faltas, nota ni situacion remite igual y envia esos campos como nulos', () => {
      cancelaciones.remitirADecano.and.returnValue(of(detalle(EVALUADAS)));
      const componente = abrirRemision().componentInstance;
      componente.evaluaciones['as-1'] = { faltas: '', nota: ' ', situacion: null, cancelar: null, cumple: true };
      componente.evaluaciones['as-2'] = { faltas: '', nota: '', situacion: null, cancelar: null, cumple: false };
      componente.abrir('REMITIR_DECANO');
      expect(componente.dialogoVisible).toBeTrue();
      componente.confirmar();
      expect(componente.errores).toEqual({});
      expect(cancelaciones.remitirADecano.calls.mostRecent().args[1].evaluaciones[0]).toEqual({
        asignaturaSolicitudUuid: 'as-1', numeroFaltas: null, nota: null, situacionMatriculaUuid: null, situacionCancelarUuid: null,
        cumpleCondiciones: true, observacionEvaluacion: null
      });
    });

    it('la observacion general es opcional y de hasta 500 caracteres', () => {
      const componente = abrirRemision().componentInstance;
      componente.evaluaciones['as-1'] = { faltas: '0', nota: '4.0', situacion: 'r0', cancelar: null, cumple: true };
      componente.evaluaciones['as-2'] = { faltas: '0', nota: '4.0', situacion: 'r0', cancelar: null, cumple: true };
      componente.abrir('REMITIR_DECANO');
      componente.observacion = 'a'.repeat(501);
      componente.confirmar();
      expect(componente.errores['observacion']).toContain('500');
    });
  });

  describe('tabla en linea', () => {
    it('el Funcionario que puede remitir diligencia faltas, nota, situacion y evaluacion en cada fila', () => {
      const fixture = crear(solicitud(['RECHAZAR_FUNCIONARIO', 'REMITIR_DECANO']), [soloCodigos('A1', 'Cálculo', 'as-1')]);
      const tabla = fixture.nativeElement.querySelector('[data-tabla-asignaturas]') as HTMLElement;
      expect(tabla.getAttribute('data-editable')).toBe('si');
      expect(encabezados(fixture)).toEqual([
        'Código', 'Asignatura', 'Espacio a diligenciar por la Facultad a la que pertenece el estudiante', 'Evaluación del Funcionario',
        'No. Faltas', 'Nota', 'Situación Académica de la(s) Asignatura(s)', 'Cumple condiciones', 'En la Matrícula', 'Al Cancelar'
      ]);
      const fila = tabla.querySelector('[data-fila="as-1"]') as HTMLElement;
      expect(Array.from(fila.querySelectorAll('[data-campo]')).map(c => c.getAttribute('data-campo'))).toEqual([
        'faltas', 'nota', 'situacion', 'cancelar', 'cumple'
      ]);
      expect(fixture.nativeElement.querySelector('textarea')).toBeNull();
      expect(fila.querySelector('[data-campo="observacion"]')).toBeNull();

      const nota = fila.querySelector('[data-campo="nota"]') as HTMLInputElement;
      nota.value = '2.5';
      nota.dispatchEvent(new Event('input'));
      const cumple = fila.querySelector('[data-campo="cumple"]') as HTMLSelectElement;
      cumple.value = 'si';
      cumple.dispatchEvent(new Event('change'));
      expect(fixture.componentInstance.evaluaciones['as-1'].nota).toBe('2.5');
      expect(fixture.componentInstance.evaluaciones['as-1'].cumple).toBeTrue();
    });

    it('al remitir con errores resalta las celdas con los mismos mensajes y no abre el dialogo', () => {
      const fixture = crear(solicitud(['REMITIR_DECANO']), [soloCodigos('A1', 'Cálculo', 'as-1')]);
      const componente = fixture.componentInstance;
      componente.evaluaciones['as-1'] = { faltas: '0', nota: '2.9', situacion: 'r0', cancelar: null, cumple: true };
      (fixture.nativeElement.querySelector('[data-accion="REMITIR_DECANO"] button') as HTMLButtonElement).click();
      fixture.detectChanges();
      expect(componente.dialogoVisible).toBeFalse();
      const fila = fixture.nativeElement.querySelector('[data-fila="as-1"]') as HTMLElement;
      expect(fila.querySelector('[data-error-celda="cumple"]')?.textContent).toContain('menor a 3.0');
      expect(fila.querySelector('[data-campo="cumple"]')?.getAttribute('aria-invalid')).toBe('true');
      expect(fixture.nativeElement.querySelector('[data-error="tabla"]')).toBeTruthy();
    });
  });

  describe('decision del Decano', () => {
    function decano(acciones = ['APROBAR_DECANO', 'RECHAZAR_DECANO']) {
      return crear(solicitud(acciones, 'EN_REVISION_DECANO'), EVALUADAS, 'DECANO');
    }

    function selector(fixture: ComponentFixture<CancelacionAsignaturaProcesoComponent>, uuid: string): HTMLSelectElement {
      return fixture.nativeElement.querySelector(`[data-fila="${uuid}"] [data-campo="decision"]`);
    }

    function decidir(fixture: ComponentFixture<CancelacionAsignaturaProcesoComponent>, uuid: string, valor: string): void {
      const campo = selector(fixture, uuid);
      campo.value = valor;
      campo.dispatchEvent(new Event('change'));
      fixture.detectChanges();
    }

    function boton(fixture: ComponentFixture<CancelacionAsignaturaProcesoComponent>, accion: string): HTMLButtonElement {
      return fixture.nativeElement.querySelector(`[data-accion="${accion}"] button`);
    }

    it('cada asignatura muestra la evaluacion del Funcionario y una decision sin preseleccionar', () => {
      const fixture = decano();
      expect(encabezados(fixture)).toContain('Cumple condiciones');
      expect(encabezados(fixture)).not.toContain('Observación de la evaluación');
      expect(encabezados(fixture)).toContain('Decisión del Decano');
      const fila2 = fixture.nativeElement.querySelector('[data-fila="as-2"]') as HTMLElement;
      expect(fila2.textContent).toContain('No');
      expect(fila2.textContent).not.toContain('Nota menor a 3.0');
      expect(selector(fixture, 'as-1').value).toBe('');
      expect(selector(fixture, 'as-2').value).toBe('');
      expect(Array.from(selector(fixture, 'as-1').options).map(o => o.textContent?.trim())).toEqual(['—', 'Aprobar', 'Rechazar']);
      expect(fixture.componentInstance.decisiones).toEqual({ 'as-1': null, 'as-2': null });
      expect(fixture.nativeElement.querySelector('[data-cambio-decision]')).toBeNull();
    });

    it('tiene un solo boton Enviar decision, sin Rechazar todo ni Aceptar tal como esta', () => {
      const fixture = decano();
      expect(botones(fixture)).toEqual(['ENVIAR_DECISION']);
      expect(boton(fixture, 'ENVIAR_DECISION').textContent?.trim()).toBe('Enviar decisión');
      expect(fixture.nativeElement.textContent).not.toContain('Aceptar tal como está');
      expect(fixture.nativeElement.textContent).not.toContain('Rechazar solicitud completa');
      expect(fixture.nativeElement.querySelector('[data-ayuda-decision]')?.textContent).toContain('Enviar decisión');
    });

    it('el boton queda deshabilitado hasta que todas las asignaturas tengan decision', () => {
      const fixture = decano();
      expect(boton(fixture, 'ENVIAR_DECISION').disabled).toBeTrue();
      fixture.componentInstance.abrirDecision();
      expect(fixture.componentInstance.dialogoVisible).toBeFalse();
      decidir(fixture, 'as-1', 'aprobar');
      expect(boton(fixture, 'ENVIAR_DECISION').disabled).toBeTrue();
      decidir(fixture, 'as-2', 'rechazar');
      expect(boton(fixture, 'ENVIAR_DECISION').disabled).toBeFalse();
      decidir(fixture, 'as-2', '');
      expect(boton(fixture, 'ENVIAR_DECISION').disabled).toBeTrue();
      expect(cancelaciones.aprobarPorDecano).not.toHaveBeenCalled();
      expect(cancelaciones.rechazarPorDecano).not.toHaveBeenCalled();
    });

    it('el Decano no ve ni llena situacion al cancelar ni observacion por asignatura', () => {
      const conSituacion = EVALUADAS.map(a => ({ ...a, situacionCancelar: { uuidSituacionAcademica: 'r3', codigo: 'C1', nombre: 'No vista' } }));
      const fixture = crear(solicitud(['APROBAR_DECANO', 'RECHAZAR_DECANO'], 'EN_REVISION_DECANO'), conSituacion, 'DECANO');
      expect(encabezados(fixture)).not.toContain('Al Cancelar');
      expect(encabezados(fixture)).not.toContain('Observación de la decisión');
      expect(fixture.nativeElement.textContent).not.toContain('No vista');
      decidir(fixture, 'as-1', 'aprobar');
      decidir(fixture, 'as-2', 'rechazar');
      fixture.componentInstance.abrirDecision();
      fixture.detectChanges();
      expect(document.querySelectorAll('[data-dialogo] select').length).toBe(0);
      expect(document.querySelectorAll('[data-dialogo] textarea').length).toBe(1);
    });

    it('aprobacion parcial: envia la decision de cada asignatura por decano/aprobar', () => {
      cancelaciones.aprobarPorDecano.and.returnValue(of(detalle(EVALUADAS)));
      const fixture = decano();
      decidir(fixture, 'as-1', 'rechazar');
      decidir(fixture, 'as-2', 'aprobar');
      expect(fixture.nativeElement.querySelector('[data-fila="as-1"] [data-cambio-decision]')).toBeTruthy();
      expect(fixture.nativeElement.querySelector('[data-fila="as-2"] [data-cambio-decision]')).toBeTruthy();
      boton(fixture, 'ENVIAR_DECISION').click();
      fixture.detectChanges();
      const componente = fixture.componentInstance;
      expect(componente.accionActiva).toBe('APROBAR_DECANO');
      expect(componente.tituloDialogo).toBe('Enviar la decisión del Decano');
      expect((document.querySelector('[data-boton-dialogo] button')?.textContent ?? '').trim()).toBe('Enviar decisión');
      expect(document.querySelector('[data-decision="as-1"]')?.textContent).toContain('Rechazar');
      expect(document.querySelector('[data-decision="as-2"]')?.textContent).toContain('Aprobar');
      componente.observacion = ' Solo la segunda ';
      componente.confirmar();
      expect(cancelaciones.aprobarPorDecano).toHaveBeenCalledWith('s-1', {
        decisiones: [
          { asignaturaSolicitudUuid: 'as-1', aprobada: false },
          { asignaturaSolicitudUuid: 'as-2', aprobada: true }
        ],
        observacion: 'Solo la segunda'
      });
      expect(cancelaciones.aprobarPorDecano.calls.mostRecent().args[1].aceptarEvaluacion).toBeUndefined();
      expect(recargar).toHaveBeenCalled();
    });

    it('aunque coincida con la evaluacion envia las decisiones, nunca aceptarEvaluacion', () => {
      cancelaciones.aprobarPorDecano.and.returnValue(of(detalle(EVALUADAS)));
      const fixture = decano();
      decidir(fixture, 'as-1', 'aprobar');
      decidir(fixture, 'as-2', 'rechazar');
      expect(fixture.nativeElement.querySelector('[data-cambio-decision]')).toBeNull();
      boton(fixture, 'ENVIAR_DECISION').click();
      fixture.componentInstance.confirmar();
      expect(cancelaciones.aprobarPorDecano).toHaveBeenCalledWith('s-1', {
        decisiones: [
          { asignaturaSolicitudUuid: 'as-1', aprobada: true },
          { asignaturaSolicitudUuid: 'as-2', aprobada: false }
        ],
        observacion: null
      });
    });

    it('si todas quedan en Rechazar el boton unico usa decano/rechazar', () => {
      cancelaciones.rechazarPorDecano.and.returnValue(of(detalle(EVALUADAS)));
      const fixture = decano();
      decidir(fixture, 'as-1', 'rechazar');
      decidir(fixture, 'as-2', 'rechazar');
      expect(fixture.nativeElement.querySelector('[data-aviso-todas-rechazadas]')).toBeTruthy();
      boton(fixture, 'ENVIAR_DECISION').click();
      fixture.detectChanges();
      const componente = fixture.componentInstance;
      expect(componente.accionActiva).toBe('RECHAZAR_DECANO');
      expect((document.querySelector('[data-boton-dialogo] button')?.textContent ?? '').trim()).toBe('Enviar decisión');
      expect(document.querySelectorAll('[data-resumen-decisiones] li').length).toBe(2);
      componente.confirmar();
      expect(cancelaciones.rechazarPorDecano).toHaveBeenCalledWith('s-1', { observacion: null });
      expect(cancelaciones.aprobarPorDecano).not.toHaveBeenCalled();
    });

    it('con todas en No cumple el Decano puede aprobar alguna o rechazar la solicitud', () => {
      const sinCumplir = EVALUADAS.map(a => ({ ...a, cumpleCondiciones: false }));
      cancelaciones.aprobarPorDecano.and.returnValue(of(detalle(sinCumplir)));
      const fixture = crear(solicitud(['APROBAR_DECANO', 'RECHAZAR_DECANO'], 'EN_REVISION_DECANO'), sinCumplir, 'DECANO');
      expect(boton(fixture, 'ENVIAR_DECISION').disabled).toBeTrue();
      decidir(fixture, 'as-1', 'aprobar');
      decidir(fixture, 'as-2', 'rechazar');
      boton(fixture, 'ENVIAR_DECISION').click();
      fixture.componentInstance.confirmar();
      expect(cancelaciones.aprobarPorDecano.calls.mostRecent().args[1].decisiones).toEqual([
        { asignaturaSolicitudUuid: 'as-1', aprobada: true },
        { asignaturaSolicitudUuid: 'as-2', aprobada: false }
      ]);
    });

    it('al recargar la solicitud las decisiones vuelven a quedar sin elegir', () => {
      const fixture = decano();
      decidir(fixture, 'as-1', 'aprobar');
      decidir(fixture, 'as-2', 'aprobar');
      fixture.componentRef.setInput('solicitud', solicitud(['APROBAR_DECANO', 'RECHAZAR_DECANO'], 'EN_REVISION_DECANO'));
      fixture.detectChanges();
      expect(fixture.componentInstance.decisiones).toEqual({ 'as-1': null, 'as-2': null });
      expect(boton(fixture, 'ENVIAR_DECISION').disabled).toBeTrue();
    });

    it('la observacion general es opcional y solo la ve el Funcionario', () => {
      const fixture = decano();
      decidir(fixture, 'as-1', 'aprobar');
      decidir(fixture, 'as-2', 'aprobar');
      fixture.componentInstance.abrirDecision();
      fixture.detectChanges();
      const campo = document.querySelector('[data-campo-observacion]') as HTMLElement;
      expect(campo.textContent).toContain('Observación (opcional)');
      expect(campo.textContent).not.toContain('*');
      expect(campo.querySelector('[data-aviso-observacion]')?.textContent?.trim()).toBe('Esta observación solo será visible para el Funcionario Académico.');
    });

    it('el rechazo completo no exige observacion, no pinta asterisco y avisa quien la ve', () => {
      cancelaciones.rechazarPorDecano.and.returnValue(of(detalle(EVALUADAS)));
      const fixture = decano(['RECHAZAR_DECANO']);
      const componente = fixture.componentInstance;
      componente.abrir('RECHAZAR_DECANO');
      fixture.detectChanges();
      const campo = document.querySelector('[data-campo-observacion]') as HTMLElement;
      expect(campo.textContent).not.toContain('*');
      expect(campo.querySelector('[data-aviso-observacion]')?.textContent?.trim()).toBe('Esta observación solo será visible para el Funcionario Académico.');
      componente.observacion = 'a'.repeat(501);
      componente.confirmar();
      expect(componente.errores['observacion']).toContain('500');
      componente.observacion = '';
      componente.confirmar();
      expect(cancelaciones.rechazarPorDecano).toHaveBeenCalledWith('s-1', { observacion: null });
    });
  });

  it('rechazar en el Funcionario no pide Resolucion y exige la observacion para el estudiante', () => {
    const orden: string[] = [];
    cancelaciones.rechazarPorFuncionario.and.callFake(() => { orden.push('rechazar'); return of(detalle(EVALUADAS)); });
    const fixture = crear(solicitud(['RECHAZAR_FUNCIONARIO']), EVALUADAS);
    const componente = fixture.componentInstance;
    componente.abrir('RECHAZAR_FUNCIONARIO');
    fixture.detectChanges();
    expect(document.querySelector('[data-campo-observacion]')?.textContent).toContain('Observación solo para el estudiante (se entrega como respuesta)');
    expect(document.querySelector('[data-campo-resolucion]')).toBeNull();
    expect(document.querySelector('[data-nota-rechazo-directo]')?.textContent).toContain('no lleva Resolución');
    componente.confirmar();
    expect(componente.errores['observacion']).toBeTruthy();
    expect(componente.errores['resolucion']).toBeUndefined();
    componente.observacion = 'Ninguna cumple';
    componente.confirmar();
    expect(orden).toEqual(['rechazar']);
    expect(solicitudes.adjuntarResolucion).not.toHaveBeenCalled();
    expect(recargar).toHaveBeenCalled();
  });

  it('rechazar y responder ya no ofrecen ni suben el documento adicional', () => {
    cancelaciones.rechazarPorFuncionario.and.returnValue(of(detalle(EVALUADAS)));
    const fixture = crear(solicitud(['RECHAZAR_FUNCIONARIO']), EVALUADAS);
    const componente = fixture.componentInstance;
    componente.abrir('RECHAZAR_FUNCIONARIO');
    fixture.detectChanges();
    expect(document.querySelector('[data-campo-documento-adicional]')).toBeNull();
    componente.observacion = 'Ninguna cumple';
    componente.confirmar();
    expect(cancelaciones.rechazarPorFuncionario).toHaveBeenCalled();
    expect(catalogo.getTiposAnexoPorTipo).not.toHaveBeenCalled();
    expect(solicitudes.adjuntarAnexo).not.toHaveBeenCalled();
  });

  describe('respuesta final del Funcionario', () => {
    const SITUACION_R3 = { uuidSituacionAcademica: 'r3', codigo: 'C1', nombre: 'No vista' };
    const DECIDIDAS: AsignaturaSolicitadaDTORespuesta[] = [
      { ...EVALUADAS[0], aprobadaPorDecano: true, decisionCambiadaPorDecano: false, situacionCancelar: SITUACION_R3 },
      { ...EVALUADAS[1], aprobadaPorDecano: true, decisionCambiadaPorDecano: true },
      { ...soloCodigos('A3', 'Química', 'as-3'), cumpleCondiciones: true, aprobadaPorDecano: false, decisionCambiadaPorDecano: true },
      { ...soloCodigos('A4', 'Dibujo', 'as-4'), cumpleCondiciones: false, aprobadaPorDecano: false, decisionCambiadaPorDecano: false },
      { ...soloCodigos('A5', 'Inglés', 'as-5'), cumpleCondiciones: true, aprobadaPorDecano: true, decisionCambiadaPorDecano: false }
    ];

    function responder(asignaturas = DECIDIDAS, etapa = 'APROBADA_POR_DECANO') {
      const fixture = crear(solicitud(['ENVIAR_RESPUESTA'], etapa, true), asignaturas);
      fixture.componentInstance.abrir('ENVIAR_RESPUESTA');
      programar(fixture.componentInstance);
      fixture.detectChanges();
      return fixture;
    }

    it('el detalle marca las asignaturas cambiadas por el Decano', () => {
      const fixture = crear(solicitud([], 'APROBADA_POR_DECANO'), DECIDIDAS);
      const marcadas = Array.from(fixture.nativeElement.querySelectorAll('[data-cambiada-decano]'))
        .map((e: any) => e.closest('tr').getAttribute('data-fila'));
      expect(marcadas).toEqual(['as-2', 'as-3']);
      expect(fixture.nativeElement.querySelector('[data-cambiada-decano]').textContent.trim()).toBe('Cambiada por el Decano');
    });

    it('el Decano no ve la marca de cambiada', () => {
      const fixture = crear(solicitud([], 'APROBADA_POR_DECANO'), DECIDIDAS, 'DECANO');
      expect(fixture.nativeElement.querySelector('[data-cambiada-decano]')).toBeNull();
      expect(fixture.nativeElement.querySelector('[data-aviso-cambiadas]')).toBeNull();
    });

    it('el Funcionario ve un aviso cuando el Decano cambio alguna decision', () => {
      const fixture = crear(solicitud(['ENVIAR_RESPUESTA'], 'APROBADA_POR_DECANO'), DECIDIDAS);
      expect(fixture.nativeElement.querySelector('[data-aviso-cambiadas]')?.textContent).toContain('2 asignaturas');
      const sinCambios = crear(solicitud(['ENVIAR_RESPUESTA'], 'APROBADA_POR_DECANO'), [DECIDIDAS[0]]);
      expect(sinCambios.nativeElement.querySelector('[data-aviso-cambiadas]')).toBeNull();
    });

    it('el aviso de cambios del Decano solo sale en APROBADA_POR_DECANO y RECHAZADA_POR_DECANO, y la marca por fila se mantiene', () => {
      for (const etapa of ['APROBADA_POR_DECANO', 'RECHAZADA_POR_DECANO']) {
        const fixture = crear(solicitud([], etapa), DECIDIDAS);
        expect(fixture.nativeElement.querySelector('[data-aviso-cambiadas]')).withContext(etapa).toBeTruthy();
      }
      for (const etapa of ['APROBADA', 'RECHAZADA']) {
        const fixture = crear(solicitud([], etapa, true, 'Respondida'), DECIDIDAS);
        expect(fixture.nativeElement.querySelector('[data-aviso-cambiadas]')).withContext(etapa).toBeNull();
        const marcadas = Array.from(fixture.nativeElement.querySelectorAll('[data-cambiada-decano]'))
          .map((e: any) => e.closest('tr').getAttribute('data-fila'));
        expect(marcadas).withContext(etapa).toEqual(['as-2', 'as-3']);
      }
    });

    it('solo lista las aprobadas sin situacion o cambiadas, con el selector de situacion, y ninguna rechazada', () => {
      responder();
      const listadas = Array.from(document.querySelectorAll('[data-ajuste]')).map(e => e.getAttribute('data-ajuste'));
      expect(listadas).toEqual(['as-2', 'as-5']);
      expect(document.querySelector('[data-ajuste="as-2"] select')).toBeTruthy();
      expect(document.querySelector('[data-ajuste="as-5"] select')).toBeTruthy();
      expect(document.querySelector('[data-ajuste="as-2"]')?.textContent).toContain('Cambiada por el Decano');
      expect(document.querySelectorAll('[data-ajustes] textarea').length).toBe(0);
    });

    it('la situacion de las aprobadas es opcional y la observacion general sigue obligatoria si hay rechazos', () => {
      const fixture = responder();
      const componente = fixture.componentInstance;
      expect(document.querySelector('[data-campo-observacion]')?.textContent).toContain('Observación solo para el estudiante (se entrega como respuesta)');
      expect(document.querySelector('[data-ajuste="as-2"]')?.textContent).toContain('Situación al cancelar (opcional)');
      expect(componente.observacionObligatoria).toBeTrue();
      componente.confirmar();
      expect(componente.errores['ajuste-cancelar-as-2']).toBeUndefined();
      expect(componente.errores['ajuste-cancelar-as-5']).toBeUndefined();
      expect(componente.errores['ajuste-observacion-as-3']).toBeUndefined();
      expect(componente.errores['observacion']).toContain('obligatoria');
      expect(cancelaciones.enviarRespuesta).not.toHaveBeenCalled();
    });

    it('envia solo los ajustes de las aprobadas, sin observacionDecision de las rechazadas', () => {
      cancelaciones.enviarRespuesta.and.returnValue(of(detalle(DECIDIDAS)));
      const componente = responder().componentInstance;
      componente.ajustes['as-2'].situacion = 'r3';
      componente.ajustes['as-5'].situacion = 'r0';
      componente.observacion = ' Se cancelan tres asignaturas ';
      componente.confirmar();
      expect(cancelaciones.enviarRespuesta).toHaveBeenCalledWith('s-1', {
        ...CUERPO_COMUNICACION,
        observacion: 'Se cancelan tres asignaturas',
        ajustes: [
          { asignaturaSolicitudUuid: 'as-2', situacionCancelarUuid: 'r3', observacionDecision: null },
          { asignaturaSolicitudUuid: 'as-5', situacionCancelarUuid: 'r0', observacionDecision: null }
        ]
      });
      expect(recargar).toHaveBeenCalled();
    });

    it('con rechazos y la observacion envia la respuesta aunque las aprobadas queden sin situacion', () => {
      cancelaciones.enviarRespuesta.and.returnValue(of(detalle(DECIDIDAS)));
      const componente = responder().componentInstance;
      componente.observacion = 'Se cancelan las aprobadas';
      componente.confirmar();
      expect(componente.errores).toEqual({});
      expect(cancelaciones.enviarRespuesta).toHaveBeenCalledWith('s-1', {
        ...CUERPO_COMUNICACION,
        observacion: 'Se cancelan las aprobadas',
        ajustes: [
          { asignaturaSolicitudUuid: 'as-2', situacionCancelarUuid: null, observacionDecision: null },
          { asignaturaSolicitudUuid: 'as-5', situacionCancelarUuid: null, observacionDecision: null }
        ]
      });
    });

    it('sin rechazos ni ajustes la observacion es opcional y no envia ajustes', () => {
      cancelaciones.enviarRespuesta.and.returnValue(of(detalle(EVALUADAS)));
      const aprobadas = [{ ...EVALUADAS[0], aprobadaPorDecano: true, decisionCambiadaPorDecano: false, situacionCancelar: SITUACION_R3 }];
      const fixture = responder(aprobadas);
      const componente = fixture.componentInstance;
      expect(document.querySelector('[data-ajustes]')).toBeNull();
      expect(componente.observacionObligatoria).toBeFalse();
      componente.observacion = 'm'.repeat(501);
      componente.confirmar();
      expect(componente.errores['observacion']).toContain('500');
      componente.observacion = '';
      componente.confirmar();
      expect(cancelaciones.enviarRespuesta).toHaveBeenCalledWith('s-1', { ...CUERPO_COMUNICACION, observacion: null });
    });

    it('si el Decano rechazo la solicitud exige la observacion y no envia ajustes', () => {
      cancelaciones.enviarRespuesta.and.returnValue(of(detalle(EVALUADAS)));
      const componente = responder(EVALUADAS, 'RECHAZADA_POR_DECANO').componentInstance;
      expect(document.querySelector('[data-ajustes]')).toBeNull();
      componente.confirmar();
      expect(componente.errores['observacion']).toContain('obligatoria');
      componente.observacion = 'El Decano negó la cancelación';
      componente.confirmar();
      expect(cancelaciones.enviarRespuesta).toHaveBeenCalledWith('s-1', { ...CUERPO_COMUNICACION, observacion: 'El Decano negó la cancelación' });
    });

    it('pide la Resolucion sin firmar y la sube antes de responder, sin documento adicional', () => {
      const orden: string[] = [];
      solicitudes.adjuntarResolucion.and.callFake(() => { orden.push('resolucion'); return of({ uuidSolicitudAcademica: 's-1', nombreArchivo: 'r.pdf', fechaSubida: '' }); });
      cancelaciones.enviarRespuesta.and.callFake(() => { orden.push('responder'); return of(detalle(EVALUADAS)); });
      const aprobadas = [{ ...EVALUADAS[0], aprobadaPorDecano: true, decisionCambiadaPorDecano: false, situacionCancelar: SITUACION_R3 }];
      const fixture = crear(solicitud(['ENVIAR_RESPUESTA'], 'APROBADA_POR_DECANO'), aprobadas);
      const componente = fixture.componentInstance;
      componente.abrir('ENVIAR_RESPUESTA');
      programar(componente);
      fixture.detectChanges();
      expect(document.querySelector('[data-nota-resolucion-sin-firmar]')).toBeTruthy();
      expect(document.querySelector('[data-campo-documento-adicional]')).toBeNull();
      componente.confirmar();
      expect(componente.errores['resolucion']).toBe('Debes subir la Resolución sin firmar por el estudiante (PDF).');
      componente.alSeleccionarResolucion(new File(['%PDF-'], 'resolucion.pdf'), entrada);
      componente.confirmar();
      expect(orden).toEqual(['resolucion', 'responder']);
      expect(solicitudes.adjuntarAnexo).not.toHaveBeenCalled();
    });

    it('al abrir pide la plantilla y carga el mensaje sin casilla, los dos textos, el anio y la decision propuesta', () => {
      const fixture = crear(solicitud(['ENVIAR_RESPUESTA'], 'APROBADA_POR_DECANO', true), [DECIDIDAS[0]]);
      const componente = fixture.componentInstance;
      componente.abrir('ENVIAR_RESPUESTA');
      fixture.detectChanges();
      expect(cancelaciones.getPlantillaNotificacion).toHaveBeenCalledWith('s-1');
      expect(componente.dialogoVisible).toBeTrue();
      expect(componente.comunicacion).toEqual({
        mensaje: PLANTILLA.mensajeInicial,
        textoComunicacion: PLANTILLA.textoComunicacion,
        textoDarca: PLANTILLA.textoDarca,
        programacion: { fecha: '', numero: '', anio: '2026', decision: 'aprueba parcialmente la' }
      });
      expect(document.querySelector('[data-campo="enviar-mensaje"]')).toBeNull();
      expect(document.querySelector('[data-campo-mensaje] textarea')).toBeTruthy();
      expect(document.querySelector('[data-campo-texto-estudiante] [data-vista-texto]')?.textContent).toContain('María Pérez');
      expect(document.querySelector('[data-campo-texto-darca] textarea')).toBeTruthy();
      expect(document.querySelector('[data-programacion] input[type="datetime-local"]')).toBeTruthy();
      expect(document.querySelector('[data-vista-previa]')?.textContent).toContain('/178 de 2026');
      expect(document.querySelector('[data-nota-retencion]')?.textContent).toContain('en trámite');
    });

    it('si la plantilla no carga muestra el error y no abre el dialogo', () => {
      cancelaciones.getPlantillaNotificacion.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
      const componente = crear(solicitud(['ENVIAR_RESPUESTA'], 'APROBADA_POR_DECANO', true), [DECIDIDAS[0]]).componentInstance;
      componente.abrir('ENVIAR_RESPUESTA');
      expect(componente.dialogoVisible).toBeFalse();
      expect(componente.accionActiva).toBeNull();
      expect(errores.handleError).toHaveBeenCalledWith(jasmine.anything(), 'Error', 'No se pudo cargar la plantilla de la comunicación');
    });

    it('sin programar la fecha y el numero no envia la respuesta', () => {
      const componente = crear(solicitud(['ENVIAR_RESPUESTA'], 'APROBADA_POR_DECANO', true), [DECIDIDAS[0]]).componentInstance;
      componente.abrir('ENVIAR_RESPUESTA');
      componente.confirmar();
      expect(componente.errores['fecha']).toContain('fecha');
      expect(componente.errores['numero']).toContain('obligatorio');
      componente.comunicacion!.programacion.fecha = '2000-01-01T08:00';
      componente.comunicacion!.programacion.numero = '1';
      componente.confirmar();
      expect(componente.errores['fecha']).toContain('posteriores');
      expect(cancelaciones.enviarRespuesta).not.toHaveBeenCalled();
    });

    it('el mensaje inmediato siempre es obligatorio y tiene maximo, y los dos textos no pueden quedar vacios', () => {
      cancelaciones.enviarRespuesta.and.returnValue(of(detalle(EVALUADAS)));
      const aprobada = [{ ...EVALUADAS[0], aprobadaPorDecano: true, decisionCambiadaPorDecano: false, situacionCancelar: SITUACION_R3 }];
      const componente = responder(aprobada).componentInstance;
      componente.comunicacion!.mensaje = '   ';
      componente.comunicacion!.textoComunicacion = '';
      componente.comunicacion!.textoDarca = ' ';
      componente.confirmar();
      expect(componente.errores['mensaje']).toContain('Escribe el mensaje');
      expect(componente.errores['textoComunicacion']).toContain('estudiante');
      expect(componente.errores['textoDarca']).toContain('DARCA');
      componente.comunicacion!.mensaje = 'x'.repeat(2001);
      componente.confirmar();
      expect(componente.errores['mensaje']).toContain('2000');
      expect(cancelaciones.enviarRespuesta).not.toHaveBeenCalled();
    });

    it('el dialogo queda en orden: situaciones, mensaje, Resolucion sin firmar, comunicacion programada y la observacion al final', () => {
      const aprobadas = [{ ...EVALUADAS[0], aprobadaPorDecano: true, decisionCambiadaPorDecano: false, situacionCancelar: null }];
      const fixture = crear(solicitud(['ENVIAR_RESPUESTA'], 'APROBADA_POR_DECANO'), aprobadas);
      fixture.componentInstance.abrir('ENVIAR_RESPUESTA');
      fixture.detectChanges();
      const orden = Array.from(document.querySelectorAll(
        '[data-dialogo] [data-ajustes], [data-dialogo] [data-bloque="mensaje"], [data-dialogo] [data-campo-resolucion], '
        + '[data-dialogo] [data-bloque="programacion"], [data-dialogo] [data-campo-observacion]'))
        .map(nodo => nodo.getAttribute('data-bloque') ?? (nodo.hasAttribute('data-ajustes') ? 'ajustes'
          : nodo.hasAttribute('data-campo-resolucion') ? 'resolucion' : 'observacion'));
      expect(orden).toEqual(['ajustes', 'mensaje', 'resolucion', 'programacion', 'observacion']);
    });

    it('envia los textos editados del estudiante y de DARCA recortados', () => {
      cancelaciones.enviarRespuesta.and.returnValue(of(detalle(EVALUADAS)));
      const aprobada = [{ ...EVALUADAS[0], aprobadaPorDecano: true, decisionCambiadaPorDecano: false, situacionCancelar: SITUACION_R3 }];
      const componente = responder(aprobada).componentInstance;
      componente.comunicacion!.textoComunicacion = ' Hola {nombre} ';
      componente.comunicacion!.textoDarca = ' DARCA {fecha} ';
      componente.confirmar();
      expect(cancelaciones.enviarRespuesta).toHaveBeenCalledWith('s-1', {
        ...CUERPO_COMUNICACION, textoComunicacion: 'Hola {nombre}', textoDarca: 'DARCA {fecha}', observacion: null
      });
    });

    it('envia el texto editado por el funcionario tal como queda', () => {
      cancelaciones.enviarRespuesta.and.returnValue(of(detalle(EVALUADAS)));
      const aprobada = [{ ...EVALUADAS[0], aprobadaPorDecano: true, decisionCambiadaPorDecano: false, situacionCancelar: SITUACION_R3 }];
      const componente = responder(aprobada).componentInstance;
      componente.comunicacion!.mensaje = ' Buen día,\nvenga <pronto> ';
      componente.comunicacion!.programacion.decision = 'aprueba la';
      componente.confirmar();
      expect(cancelaciones.enviarRespuesta).toHaveBeenCalledWith('s-1', {
        ...CUERPO_COMUNICACION,
        mensaje: 'Buen día,\nvenga <pronto>',
        programacion: { ...CUERPO_COMUNICACION.programacion, decision: 'aprueba la' },
        observacion: null
      });
    });
  });

  describe('panel de la notificacion de la Resolucion', () => {
    it('el Funcionario lo ve en la etapa final y pide la notificacion', async () => {
      const fixture = crear(solicitud([], 'APROBADA', true, 'Respondida'), EVALUADAS);
      await fixture.whenStable();
      fixture.detectChanges();
      expect(fixture.componentInstance.muestraPanelNotificacion).toBeTrue();
      expect(cancelaciones.getNotificacion).toHaveBeenCalledWith('s-1');
    });

    it('ni el Estudiante ni el Decano lo ven, y el Funcionario no lo ve antes de responder', async () => {
      for (const [rol, etapa, etiqueta] of [['ESTUDIANTE', 'APROBADA', 'Aprobada'], ['DECANO', 'APROBADA', 'Respondida'], ['FUNCIONARIO', 'APROBADA_POR_DECANO', 'Pendiente Notificar']]) {
        const fixture = crear(solicitud([], etapa, true, etiqueta), EVALUADAS, rol);
        await fixture.whenStable();
        expect(fixture.componentInstance.muestraPanelNotificacion).withContext(rol + etapa).toBeFalse();
      }
      expect(cancelaciones.getNotificacion).not.toHaveBeenCalled();
    });

    it('mientras la respuesta esta retenida el Estudiante ve la solicitud en tramite sin resultados y sin errores', () => {
      const fixture = crear(solicitud([], 'EN_REVISION_DECANO', false, 'En trámite'), [soloCodigos('A1', 'Cálculo', 'as-1')], 'ESTUDIANTE');
      expect(encabezados(fixture)).toEqual(['Código', 'Asignatura']);
      expect(fixture.nativeElement.querySelector('[data-panel-notificacion]')).toBeNull();
      expect(errores.handleError).not.toHaveBeenCalled();
    });
  });

  it('no permite un doble envio y un error con codigoError deja el dialogo abierto', () => {
    const respuesta = new Subject<CancelacionAsignaturaDetalleDTORespuesta>();
    cancelaciones.rechazarPorDecano.and.returnValue(respuesta);
    const componente = crear(solicitud(['RECHAZAR_DECANO'], 'EN_REVISION_DECANO'), EVALUADAS, 'DECANO').componentInstance;
    componente.abrir('RECHAZAR_DECANO');
    componente.observacion = 'No procede';
    componente.confirmar();
    componente.confirmar();
    expect(cancelaciones.rechazarPorDecano).toHaveBeenCalledTimes(1);
    const error = new HttpErrorResponse({ status: 500, error: { codigoError: '4', mensaje: 'La acción no está permitida...' } });
    respuesta.error(error);
    expect(errores.handleError).toHaveBeenCalledWith(error, 'Error', 'No se pudo completar la acción');
    expect(componente.dialogoVisible).toBeTrue();
    expect(componente.enviando).toBeFalse();
    expect(recargar).not.toHaveBeenCalled();
  });

  it('el rechazo del Decano usa Enviar decision en el dialogo', () => {
    const fixture = crear(solicitud(['APROBAR_DECANO', 'RECHAZAR_DECANO'], 'EN_REVISION_DECANO'), EVALUADAS, 'DECANO');
    fixture.componentInstance.abrir('RECHAZAR_DECANO');
    fixture.detectChanges();
    expect((document.querySelector('[data-boton-dialogo] button')?.textContent ?? '').trim()).toBe('Enviar decisión');
  });
});

describe('CancelacionAsignaturaProcesoComponent con el catalogo real', () => {
  const URL_SITUACIONES = `${environment.apiUrl}/catalogos-academicos/situaciones`;
  const PENDIENTE: AsignaturaSolicitadaDTORespuesta[] = [
    { ...EVALUADAS[0], aprobadaPorDecano: true, decisionCambiadaPorDecano: false },
    { ...EVALUADAS[1], aprobadaPorDecano: false, decisionCambiadaPorDecano: false }
  ];
  let http: HttpTestingController;
  let toast: jasmine.SpyObj<ToastService>;
  let errores: ErrorHandlerService;

  beforeEach(() => {
    const cancelaciones = jasmine.createSpyObj<CancelacionAsignaturaService>('CancelacionAsignaturaService', ['getDetalle', 'getPlantillaNotificacion', 'getNotificacion']);
    cancelaciones.getDetalle.and.returnValue(of(detalle(PENDIENTE)));
    cancelaciones.getPlantillaNotificacion.and.returnValue(of(PLANTILLA));
    cancelaciones.getNotificacion.and.returnValue(of(null));
    toast = jasmine.createSpyObj<ToastService>('ToastService', ['showSuccess', 'showError']);
    TestBed.configureTestingModule({
      imports: [CancelacionAsignaturaProcesoComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        { provide: CancelacionAsignaturaService, useValue: cancelaciones },
        { provide: ToastService, useValue: toast }
      ]
    });
    http = TestBed.inject(HttpTestingController);
    errores = TestBed.inject(ErrorHandlerService);
    spyOn(errores, 'handleError').and.callThrough();
    spyOn(console, 'error');
  });

  function clicEnResponder(): ComponentFixture<CancelacionAsignaturaProcesoComponent> {
    const fixture = TestBed.createComponent(CancelacionAsignaturaProcesoComponent);
    fixture.componentRef.setInput('uuidSolicitud', 's-1');
    fixture.componentRef.setInput('solicitud', solicitud(['ENVIAR_RESPUESTA'], 'APROBADA_POR_DECANO', true));
    fixture.componentRef.setInput('rol', 'FUNCIONARIO');
    fixture.componentRef.setInput('recargar', () => undefined);
    fixture.detectChanges();
    (fixture.nativeElement.querySelector('[data-accion="ENVIAR_RESPUESTA"] button') as HTMLButtonElement).click();
    fixture.detectChanges();
    return fixture;
  }

  it('la respuesta con una aprobada sin situacion pide el catalogo y luego abre el dialogo', () => {
    const fixture = clicEnResponder();
    const componente = fixture.componentInstance;
    const peticion = http.expectOne(URL_SITUACIONES);
    expect(peticion.request.method).toBe('GET');
    expect(componente.dialogoVisible).toBeFalse();
    peticion.flush([
      { uuidSituacionAcademica: 's2', codigo: 'C2', nombre: 'Perdida', ambito: 'CANCELACION' },
      { uuidSituacionAcademica: 's0', codigo: 'R0', nombre: 'No cursada', ambito: null },
      { uuidSituacionAcademica: 's3', codigo: 'R3', nombre: 'Cursada por tercera vez', ambito: 'MATRICULA' }
    ]);
    fixture.detectChanges();
    expect(componente.dialogoVisible).toBeTrue();
    const opciones = Array.from(document.querySelectorAll('[data-ajuste="as-1"] option')).map(o => o.textContent?.trim());
    expect(opciones).toContain('Perdida');
    expect(opciones).not.toContain('No cursada');
    expect(opciones).not.toContain('Cursada por tercera vez');
    expect(errores.handleError).not.toHaveBeenCalled();
  });

  it('si el catalogo falla muestra el error y no abre el dialogo', () => {
    const fixture = clicEnResponder();
    http.expectOne(URL_SITUACIONES).flush({ codigoError: '1', mensaje: 'Error interno' }, { status: 500, statusText: 'Error' });
    fixture.detectChanges();
    expect(fixture.componentInstance.dialogoVisible).toBeFalse();
    expect(fixture.componentInstance.accionActiva).toBeNull();
    expect(toast.showError).toHaveBeenCalledWith('Error', 'No se pudieron cargar las situaciones académicas. Error interno');
  });

  it('si el catalogo viene vacio muestra el error y no abre el dialogo', () => {
    const fixture = clicEnResponder();
    http.expectOne(URL_SITUACIONES).flush([]);
    fixture.detectChanges();
    expect(fixture.componentInstance.dialogoVisible).toBeFalse();
    expect(toast.showError).toHaveBeenCalledWith(
      'Error',
      'No se pudieron cargar las situaciones académicas. El catálogo de situaciones académicas está vacío'
    );
  });
});

describe('CancelacionAsignaturaProcesoComponent resaltado por resultado desde Respuestas', () => {
  const RESPONDIDAS: AsignaturaSolicitadaDTORespuesta[] = [
    { ...EVALUADAS[0], aprobadaPorDecano: true, decisionCambiadaPorDecano: false },
    { ...EVALUADAS[1], aprobadaPorDecano: false, decisionCambiadaPorDecano: false },
    { ...soloCodigos('A3', 'Química', 'as-3'), aprobadaPorDecano: null }
  ];
  let parametros: BehaviorSubject<ParamMap>;

  function crearCon(resultado: string | null, etapa = 'APROBADA', rol = 'FUNCIONARIO', etiqueta = 'Respondida'): ComponentFixture<CancelacionAsignaturaProcesoComponent> {
    const mapa = convertToParamMap(resultado ? { resultado } : {});
    parametros = new BehaviorSubject<ParamMap>(mapa);
    const cancelaciones = jasmine.createSpyObj<CancelacionAsignaturaService>('CancelacionAsignaturaService', ['getDetalle', 'getNotificacion']);
    cancelaciones.getDetalle.and.returnValue(of(detalle(RESPONDIDAS)));
    cancelaciones.getNotificacion.and.returnValue(of(null));
    TestBed.configureTestingModule({
      imports: [CancelacionAsignaturaProcesoComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: mapa }, queryParamMap: parametros } },
        { provide: CancelacionAsignaturaService, useValue: cancelaciones },
        { provide: ToastService, useValue: jasmine.createSpyObj('ToastService', ['showSuccess', 'showError']) }
      ]
    });
    const fixture = TestBed.createComponent(CancelacionAsignaturaProcesoComponent);
    fixture.componentRef.setInput('uuidSolicitud', 's-1');
    fixture.componentRef.setInput('solicitud', solicitud([], etapa, true, etiqueta));
    fixture.componentRef.setInput('rol', rol);
    fixture.componentRef.setInput('recargar', () => undefined);
    fixture.detectChanges();
    return fixture;
  }

  function filas(fixture: ComponentFixture<CancelacionAsignaturaProcesoComponent>): (string | null)[] {
    return Array.from(fixture.nativeElement.querySelectorAll('[data-tabla-asignaturas] tbody tr')).map((tr: any) => tr.getAttribute('data-fila'));
  }

  function conClase(fixture: ComponentFixture<CancelacionAsignaturaProcesoComponent>, clase: string): (string | null)[] {
    return Array.from(fixture.nativeElement.querySelectorAll(`[data-tabla-asignaturas] tbody tr.${clase}`)).map((tr: any) => tr.getAttribute('data-fila'));
  }

  function celda(fixture: ComponentFixture<CancelacionAsignaturaProcesoComponent>, titulo: string): string[] {
    const indice = fixture.componentInstance.columnas.findIndex(columna => columna.titulo === titulo);
    return Array.from(fixture.nativeElement.querySelectorAll('[data-tabla-asignaturas] tbody tr'))
      .map((tr: any) => (indice < 0 ? '' : tr.querySelectorAll('td')[indice].textContent.replace(/\s+/g, ' ').trim()));
  }

  function sinDistintivoEnAsignatura(fixture: ComponentFixture<CancelacionAsignaturaProcesoComponent>): boolean {
    return celda(fixture, 'Asignatura').every(texto => !/\b(Aprobada|Rechazada)\b/.test(texto));
  }

  function otras(fixture: ComponentFixture<CancelacionAsignaturaProcesoComponent>): (string | null)[] {
    return Array.from(fixture.nativeElement.querySelectorAll('[data-otra-asignatura]'))
      .map((e: any) => e.closest('tr').getAttribute('data-fila'));
  }

  function aviso(fixture: ComponentFixture<CancelacionAsignaturaProcesoComponent>): string | null {
    return fixture.nativeElement.querySelector('[data-resaltado]')?.textContent.trim() ?? null;
  }

  it('con resultado rechazada muestra todas, las rechazadas primero y resaltadas, y las demas atenuadas', () => {
    const fixture = crearCon('rechazada');
    expect(filas(fixture)).toEqual(['as-2', 'as-3', 'as-1']);
    expect(conClase(fixture, 'fila-resaltada')).toEqual(['as-2', 'as-3']);
    expect(conClase(fixture, 'fila-atenuada')).toEqual(['as-1']);
    expect(otras(fixture)).toEqual(['as-1']);
    expect(fixture.nativeElement.querySelector('[data-otra-asignatura]').textContent.trim()).toBe('Otra asignatura de la solicitud');
    expect(celda(fixture, 'Asignatura')).toEqual(['Física', 'Química', 'Cálculo Otra asignatura de la solicitud']);
    expect(aviso(fixture)).toBe('Se resaltan las asignaturas rechazadas de la solicitud');
    expect(fixture.nativeElement.querySelector('[data-motivo]').textContent).toContain('Cruce laboral');
  });

  it('con resultado aprobada las aprobadas van primero y el resultado sale en Decision del Decano, no en Asignatura', () => {
    const fixture = crearCon('aprobada');
    expect(filas(fixture)).toEqual(['as-1', 'as-2', 'as-3']);
    expect(conClase(fixture, 'fila-resaltada')).toEqual(['as-1']);
    expect(sinDistintivoEnAsignatura(fixture)).toBeTrue();
    const columnas = fixture.componentInstance.columnas.map(columna => columna.titulo);
    expect(columnas.indexOf('Decisión del Decano')).toBeGreaterThan(columnas.indexOf('Asignatura'));
    expect(celda(fixture, 'Decisión del Decano')).toEqual(['Aprobada', 'No aprobada', '—']);
    expect(celda(fixture, 'Código')).toEqual(['A1', 'A2', 'A3']);
    expect(aviso(fixture)).toBe('Se resaltan las asignaturas aprobadas de la solicitud');
  });

  for (const valor of [null, 'otra', 'as-1']) {
    it(`con el parametro ${valor} se ve como desde Solicitudes, sin distintivo en Asignatura`, () => {
      const fixture = crearCon(valor);
      expect(filas(fixture)).toEqual(['as-1', 'as-2', 'as-3']);
      expect(conClase(fixture, 'fila-resaltada')).toEqual([]);
      expect(conClase(fixture, 'fila-atenuada')).toEqual([]);
      expect(aviso(fixture)).toBeNull();
      expect(sinDistintivoEnAsignatura(fixture)).toBeTrue();
    });
  }

  for (const [rol, etiqueta] of [['ESTUDIANTE', 'Aprobada'], ['DECANO', 'Aprobada por el Decano']]) {
    it(`${rol} en una solicitud respondida ve el resultado en Decision del Decano y nada en Asignatura`, () => {
      const fixture = crearCon('aprobada', 'APROBADA', rol, etiqueta);
      expect(sinDistintivoEnAsignatura(fixture)).toBeTrue();
      expect(celda(fixture, 'Decisión del Decano').slice(0, 2)).toEqual(['Aprobada', 'No aprobada']);
      expect(aviso(fixture)).toBeNull();
    });
  }

  it('fuera de la etapa final no hay resaltado', () => {
    const fixture = crearCon('aprobada', 'APROBADA_POR_DECANO');
    expect(filas(fixture)).toEqual(['as-1', 'as-2', 'as-3']);
    expect(sinDistintivoEnAsignatura(fixture)).toBeTrue();
    expect(aviso(fixture)).toBeNull();
  });

  it('si cambia el parametro en la misma instancia cambia el orden y el resaltado', () => {
    const fixture = crearCon('aprobada');
    expect(conClase(fixture, 'fila-resaltada')).toEqual(['as-1']);
    parametros.next(convertToParamMap({ resultado: 'rechazada' }));
    fixture.detectChanges();
    expect(filas(fixture)).toEqual(['as-2', 'as-3', 'as-1']);
    expect(conClase(fixture, 'fila-resaltada')).toEqual(['as-2', 'as-3']);
    parametros.next(convertToParamMap({}));
    fixture.detectChanges();
    expect(filas(fixture)).toEqual(['as-1', 'as-2', 'as-3']);
    expect(aviso(fixture)).toBeNull();
  });
});
