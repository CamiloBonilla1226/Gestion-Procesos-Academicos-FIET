import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { BandejaSolicitudesAcademicasComponent } from './bandeja-solicitudes-academicas-component';
import { SolicitudAcademicaService } from '../../../../core/services/solicitud-academica-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { SolicitudAcademicaResumenDTORespuesta } from '../../../../core/models/SolicitudAcademica/DTOResponse/SolicitudAcademicaResumenDTORespuesta';

function solicitudes(cantidad: number): SolicitudAcademicaResumenDTORespuesta[] {
  return Array.from({ length: cantidad }, (_, i) => ({
    uuidSolicitudAcademica: `uuid-${i + 1}`,
    radicado: `2026-CA-${String(i + 1).padStart(4, '0')}`,
    uuidTipoSolicitudAcademica: 't',
    tipoSolicitud: i % 2 === 0 ? 'Cancelación de Asignatura' : 'Examen Supletorio',
    fechaCreacion: '2026-12-31T23:59:59',
    etiqueta: 'Pendiente',
    etapaCodigo: 'RADICADA',
    nombreEstudiante: `Estudiante ${i + 1}`,
    codigoEstudiantil: `10461${i + 1}`
  }));
}

describe('BandejaSolicitudesAcademicasComponent', () => {
  let servicio: jasmine.SpyObj<SolicitudAcademicaService>;
  let errores: jasmine.SpyObj<ErrorHandlerService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    servicio = jasmine.createSpyObj<SolicitudAcademicaService>('SolicitudAcademicaService', [
      'getSolicitudesEstudiante', 'getSolicitudesFuncionario', 'getSolicitudesDecano'
    ]);
    errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    TestBed.configureTestingModule({
      imports: [BandejaSolicitudesAcademicasComponent],
      providers: [
        { provide: SolicitudAcademicaService, useValue: servicio },
        { provide: ErrorHandlerService, useValue: errores },
        { provide: Router, useValue: router }
      ]
    });
  });

  function crear(rol: string): ComponentFixture<BandejaSolicitudesAcademicasComponent> {
    const fixture = TestBed.createComponent(BandejaSolicitudesAcademicasComponent);
    fixture.componentRef.setInput('rol', rol);
    fixture.detectChanges();
    return fixture;
  }

  function filas(fixture: ComponentFixture<BandejaSolicitudesAcademicasComponent>): HTMLTableRowElement[] {
    return Array.from(fixture.nativeElement.querySelectorAll('tbody tr'));
  }

  function encabezados(fixture: ComponentFixture<BandejaSolicitudesAcademicasComponent>): string[] {
    return Array.from(fixture.nativeElement.querySelectorAll('thead th'))
      .map((th: any) => th.textContent.replace('⇅', '').trim());
  }

  function estados(fixture: ComponentFixture<BandejaSolicitudesAcademicasComponent>): (string | null)[] {
    return Array.from(fixture.nativeElement.querySelectorAll('tbody .estado')).map((e: any) => e.getAttribute('data-categoria'));
  }

  it('el Funcionario ve 10 filas por pagina con las columnas del estudiante', () => {
    servicio.getSolicitudesFuncionario.and.returnValue(of(solicitudes(23)));
    const fixture = crear('FUNCIONARIO');

    expect(servicio.getSolicitudesFuncionario).toHaveBeenCalledTimes(1);
    expect(filas(fixture).length).toBe(10);
    expect(encabezados(fixture)).toEqual([
      'Radicado', 'Estudiante', 'Documento', 'Código', 'Tipo de proceso', 'Fecha de radicación', 'Estado', 'Acciones'
    ]);
    expect(filas(fixture)[0].querySelectorAll('td')[3].textContent.trim()).toBe('104611');
    expect(filas(fixture)[0].textContent).toContain('31/12/2026 23:59');
    expect(filas(fixture)[0].querySelector('.radicado')?.textContent?.trim()).toBe('2026-CA-0001');
    expect(fixture.nativeElement.querySelector('.tarjeta-head h2').textContent.trim()).toBe('Gestión de solicitudes');
    expect(fixture.nativeElement.querySelector('app-barra-busqueda-component')).toBeTruthy();

    fixture.componentInstance.onPageChange(3);
    fixture.detectChanges();
    expect(filas(fixture).length).toBe(3);
  });

  it('Funcionario y Decano ven el documento y lo buscan con la etiqueta de identificacion', () => {
    const lista = solicitudes(3);
    lista[1].numeroDocumento = '1061789234';
    servicio.getSolicitudesFuncionario.and.returnValue(of(lista));
    const fixture = crear('FUNCIONARIO');
    expect(filas(fixture)[1].querySelectorAll('td')[2].textContent.trim()).toBe('1061789234');
    expect(fixture.nativeElement.querySelector('.campo-etiqueta').textContent.trim()).toBe('Número de identificación');
    expect(fixture.nativeElement.querySelector('app-barra-busqueda-component input').getAttribute('placeholder')).toBe('Ej. 1061789234');

    fixture.componentInstance.onBusquedaChange('1061789');
    fixture.detectChanges();
    expect(filas(fixture).length).toBe(1);
    expect(filas(fixture)[0].querySelector('.radicado')?.textContent?.trim()).toBe('2026-CA-0002');
  });

  it('el Funcionario tiene un filtro propio de codigo estudiantil con coincidencia parcial', () => {
    const lista = solicitudes(12);
    servicio.getSolicitudesFuncionario.and.returnValue(of(lista));
    const fixture = crear('FUNCIONARIO');
    const etiquetas = Array.from(fixture.nativeElement.querySelectorAll('.filtros .campo-etiqueta')).map((e: any) => e.textContent.trim());
    expect(etiquetas).toEqual(['Número de identificación', 'Código estudiantil']);
    expect(fixture.nativeElement.querySelectorAll('app-barra-busqueda-component').length).toBe(2);

    fixture.componentInstance.onCodigoChange('6111');
    fixture.detectChanges();
    expect(filas(fixture).map(f => f.querySelector('.radicado')?.textContent?.trim())).toEqual(['2026-CA-0011']);
    expect(fixture.componentInstance.hayFiltros).toBeTrue();

    fixture.componentInstance.limpiarFiltros();
    fixture.detectChanges();
    expect(fixture.componentInstance.codigoFiltro).toBe('');
    expect(filas(fixture).length).toBe(10);
  });

  describe('modos del Funcionario', () => {
    function mezcla(): SolicitudAcademicaResumenDTORespuesta[] {
      const lista = solicitudes(6);
      const etapas = ['RADICADA', 'APROBADA', 'EN_REVISION_DECANO', 'RECHAZADA', 'APROBADA_POR_DECANO', 'APROBADA'];
      const etiquetas = ['Pendiente', 'Respondida', 'En Gestión', 'Respondida', 'Pendiente Notificar', 'Respondida'];
      lista.forEach((s, i) => {
        s.etapaCodigo = etapas[i];
        s.etiqueta = etiquetas[i];
        s.decisionDecano = i === 1 ? 'APROBADA' : null;
      });
      return lista;
    }

    function crearEnModo(modo: string): ComponentFixture<BandejaSolicitudesAcademicasComponent> {
      servicio.getSolicitudesFuncionario.and.returnValue(of(mezcla()));
      const fixture = TestBed.createComponent(BandejaSolicitudesAcademicasComponent);
      fixture.componentRef.setInput('rol', 'FUNCIONARIO');
      fixture.componentRef.setInput('modo', modo);
      fixture.detectChanges();
      return fixture;
    }

    function radicados(fixture: ComponentFixture<BandejaSolicitudesAcademicasComponent>): string[] {
      return filas(fixture).map(f => f.querySelector('.radicado')?.textContent?.trim() ?? '');
    }

    function opcionesEstado(fixture: ComponentFixture<BandejaSolicitudesAcademicasComponent>): string[] {
      return Array.from(fixture.nativeElement.querySelectorAll('#filtro-estado option')).map((o: any) => o.textContent.trim());
    }

    it('Solicitudes solo muestra las no finalizadas y su filtro de estado no ofrece Respondida', () => {
      const fixture = crearEnModo('solicitudes');
      expect(radicados(fixture)).toEqual(['2026-CA-0001', '2026-CA-0003', '2026-CA-0005']);
      expect(opcionesEstado(fixture)).toEqual(['Todos', 'En Gestión', 'Pendiente', 'Pendiente Notificar']);
      expect(fixture.nativeElement.querySelector('.tarjeta-head h2').textContent.trim()).toBe('Gestión de solicitudes');
      expect(encabezados(fixture)).not.toContain('Decisión del Decano');
    });

    it('sin modo la bandeja del Funcionario se comporta como Solicitudes', () => {
      servicio.getSolicitudesFuncionario.and.returnValue(of(mezcla()));
      expect(radicados(crear('FUNCIONARIO'))).toEqual(['2026-CA-0001', '2026-CA-0003', '2026-CA-0005']);
    });

    it('Respuestas solo muestra las finalizadas con estado Aprobada o Rechazada y sus iconos', () => {
      const fixture = crearEnModo('respuestas');
      expect(radicados(fixture)).toEqual(['2026-CA-0002', '2026-CA-0004', '2026-CA-0006']);
      expect(filas(fixture).map(f => f.querySelector('.estado')?.textContent?.trim())).toEqual(['Aprobada', 'Rechazada', 'Aprobada']);
      expect(estados(fixture)).toEqual(['aprobada', 'rechazada', 'aprobada']);
      expect(encabezados(fixture)).toEqual([
        'Radicado', 'Estudiante', 'Documento', 'Código', 'Tipo de proceso', 'Fecha de radicación', 'Estado', 'Acciones'
      ]);
      expect(fixture.nativeElement.querySelector('.tarjeta-head h2').textContent.trim()).toBe('Solicitudes respondidas');
      expect(fixture.nativeElement.querySelector('[data-filtro-codigo]')).toBeTruthy();
    });

    it('el filtro de estado de Respuestas solo ofrece Aprobada y Rechazada y filtra por ellas', () => {
      const fixture = crearEnModo('respuestas');
      expect(opcionesEstado(fixture)).toEqual(['Todos', 'Aprobada', 'Rechazada']);
      fixture.componentInstance.onEstadoChange('Rechazada');
      fixture.detectChanges();
      expect(radicados(fixture)).toEqual(['2026-CA-0004']);
      fixture.componentInstance.onCodigoChange('4614');
      fixture.detectChanges();
      expect(radicados(fixture)).toEqual(['2026-CA-0004']);
      fixture.componentInstance.onCodigoChange('4612');
      fixture.detectChanges();
      expect(radicados(fixture)).toEqual([]);
    });

    it('en Respuestas el ojo abre el detalle igual que en Solicitudes', () => {
      const fixture = crearEnModo('respuestas');
      (filas(fixture)[0].querySelector('.btn-ojo') as HTMLButtonElement).click();
      expect(router.navigate).toHaveBeenCalledWith(['/funcionario-academico/solicitudes', 'uuid-2']);
    });

    it('al cambiar de modo en la misma instancia cambia la lista y se reinicia el filtro de estado', () => {
      const fixture = crearEnModo('solicitudes');
      const instancia = fixture.componentInstance;
      fixture.componentInstance.onEstadoChange('Pendiente');
      fixture.detectChanges();
      expect(radicados(fixture)).toEqual(['2026-CA-0001']);

      fixture.componentRef.setInput('modo', 'respuestas');
      fixture.detectChanges();
      expect(fixture.componentInstance).toBe(instancia);
      expect(instancia.estadoFiltro).toBe(instancia.todos);
      expect(radicados(fixture)).toEqual(['2026-CA-0002', '2026-CA-0004', '2026-CA-0006']);
      expect(opcionesEstado(fixture)).toEqual(['Todos', 'Aprobada', 'Rechazada']);
      expect(fixture.nativeElement.querySelector('.tarjeta-head h2').textContent.trim()).toBe('Solicitudes respondidas');

      instancia.onEstadoChange('Aprobada');
      fixture.componentRef.setInput('modo', 'solicitudes');
      fixture.detectChanges();
      expect(instancia.estadoFiltro).toBe(instancia.todos);
      expect(radicados(fixture)).toEqual(['2026-CA-0001', '2026-CA-0003', '2026-CA-0005']);
      expect(opcionesEstado(fixture)).toEqual(['Todos', 'En Gestión', 'Pendiente', 'Pendiente Notificar']);
      expect(servicio.getSolicitudesFuncionario).toHaveBeenCalledTimes(1);
    });

    describe('una fila por asignatura en Cancelacion de Asignatura', () => {
      function respondidas(): SolicitudAcademicaResumenDTORespuesta[] {
        const lista = solicitudes(5);
        lista[0] = { ...lista[0], tipoSolicitud: 'Cancelación de Asignatura', etapaCodigo: 'APROBADA', etiqueta: 'Respondida',
          resultadosAsignaturas: [{ asignaturaSolicitudUuid: 'as-1', aprobada: true }, { asignaturaSolicitudUuid: 'as-2', aprobada: false }] };
        lista[1] = { ...lista[1], tipoSolicitud: 'Cancelación de Matrícula', etapaCodigo: 'APROBADA', etiqueta: 'Respondida' };
        lista[2] = { ...lista[2], tipoSolicitud: 'Cancelación de Asignatura', etapaCodigo: 'RECHAZADA', etiqueta: 'Respondida',
          resultadosAsignaturas: [{ asignaturaSolicitudUuid: 'as-3', aprobada: false }, { asignaturaSolicitudUuid: 'as-4', aprobada: false }] };
        lista[3] = { ...lista[3], tipoSolicitud: 'Examen Supletorio', etapaCodigo: 'RECHAZADA', etiqueta: 'Respondida' };
        lista[4] = { ...lista[4], tipoSolicitud: 'Cancelación de Asignatura', etapaCodigo: 'EN_REVISION_DECANO', etiqueta: 'En Gestión', resultadosAsignaturas: null };
        return lista;
      }

      function crearCon(modo: string): ComponentFixture<BandejaSolicitudesAcademicasComponent> {
        servicio.getSolicitudesFuncionario.and.returnValue(of(respondidas()));
        const fixture = TestBed.createComponent(BandejaSolicitudesAcademicasComponent);
        fixture.componentRef.setInput('rol', 'FUNCIONARIO');
        fixture.componentRef.setInput('modo', modo);
        fixture.detectChanges();
        return fixture;
      }

      function estadosTexto(fixture: ComponentFixture<BandejaSolicitudesAcademicasComponent>): string[] {
        return filas(fixture).map(f => f.querySelector('.estado')?.textContent?.trim() ?? '');
      }

      function claves(fixture: ComponentFixture<BandejaSolicitudesAcademicasComponent>): string[] {
        return filas(fixture).map(f => f.querySelector('.btn-ojo')?.getAttribute('data-clave') ?? '');
      }

      it('expande cada asignatura en una fila con su resultado y las demas siguen con una fila', () => {
        const fixture = crearCon('respuestas');
        expect(radicados(fixture)).toEqual(['2026-CA-0001', '2026-CA-0001', '2026-CA-0002', '2026-CA-0003', '2026-CA-0003', '2026-CA-0004']);
        expect(estadosTexto(fixture)).toEqual(['Aprobada', 'Rechazada', 'Aprobada', 'Rechazada', 'Rechazada', 'Rechazada']);
        expect(estados(fixture)).toEqual(['aprobada', 'rechazada', 'aprobada', 'rechazada', 'rechazada', 'rechazada']);
        expect(encabezados(fixture)).toEqual([
          'Radicado', 'Estudiante', 'Documento', 'Código', 'Tipo de proceso', 'Fecha de radicación', 'Estado', 'Acciones'
        ]);
        expect(fixture.nativeElement.querySelector('.pie small').textContent.trim()).toBe('4 solicitudes');
      });

      it('cada fila tiene una clave unica de solicitud mas asignatura', () => {
        const fixture = crearCon('respuestas');
        const lista = claves(fixture);
        expect(lista).toEqual(['uuid-1:as-1', 'uuid-1:as-2', 'uuid-2', 'uuid-3:as-3', 'uuid-3:as-4', 'uuid-4']);
        expect(new Set(lista).size).toBe(lista.length);
      });

      it('el filtro de estado se aplica por fila', () => {
        const fixture = crearCon('respuestas');
        fixture.componentInstance.onEstadoChange('Aprobada');
        fixture.detectChanges();
        expect(claves(fixture)).toEqual(['uuid-1:as-1', 'uuid-2']);
        fixture.componentInstance.onEstadoChange('Rechazada');
        fixture.detectChanges();
        expect(claves(fixture)).toEqual(['uuid-1:as-2', 'uuid-3:as-3', 'uuid-3:as-4', 'uuid-4']);
        expect(fixture.nativeElement.querySelector('.pie small').textContent.trim()).toBe('3 solicitudes');
      });

      it('los demas filtros se aplican sobre la solicitud y todas sus filas', () => {
        const fixture = crearCon('respuestas');
        fixture.componentInstance.onCodigoChange('104611');
        fixture.detectChanges();
        expect(claves(fixture)).toEqual(['uuid-1:as-1', 'uuid-1:as-2']);
        fixture.componentInstance.onCodigoChange('');
        fixture.componentInstance.onTipoChange('Cancelación de Asignatura');
        fixture.detectChanges();
        expect(claves(fixture)).toEqual(['uuid-1:as-1', 'uuid-1:as-2', 'uuid-3:as-3', 'uuid-3:as-4']);
      });

      it('la fila de una asignatura abre el detalle con su uuid; las demas, sin parametro', () => {
        const fixture = crearCon('respuestas');
        (filas(fixture)[1].querySelector('.btn-ojo') as HTMLButtonElement).click();
        expect(router.navigate).toHaveBeenCalledWith(['/funcionario-academico/solicitudes', 'uuid-1'], { queryParams: { asignatura: 'as-2' } });
        (filas(fixture)[2].querySelector('.btn-ojo') as HTMLButtonElement).click();
        expect(router.navigate).toHaveBeenCalledWith(['/funcionario-academico/solicitudes', 'uuid-2']);
      });

      it('Solicitudes no cambia: una fila por solicitud y el detalle sin parametro', () => {
        const fixture = crearCon('solicitudes');
        expect(claves(fixture)).toEqual(['uuid-5']);
        (filas(fixture)[0].querySelector('.btn-ojo') as HTMLButtonElement).click();
        expect(router.navigate).toHaveBeenCalledWith(['/funcionario-academico/solicitudes', 'uuid-5']);
      });
    });

    it('separa por etapaCodigo todas las etapas: solo APROBADA y RECHAZADA van a Respuestas', () => {
      const etapas = ['RADICADA', 'EN_REVISION_DECANO', 'APROBADA_POR_DECANO', 'RECHAZADA_POR_DECANO', 'PENDIENTE_PAGO', 'EN_VERIFICACION_PAGO', 'APROBADA', 'RECHAZADA'];
      const lista = solicitudes(etapas.length);
      lista.forEach((s, i) => {
        s.etapaCodigo = etapas[i];
        s.etiqueta = i >= 6 ? 'Respondida' : 'En Gestión';
      });
      servicio.getSolicitudesFuncionario.and.returnValue(of(lista));
      const fixture = TestBed.createComponent(BandejaSolicitudesAcademicasComponent);
      fixture.componentRef.setInput('rol', 'FUNCIONARIO');
      fixture.componentRef.setInput('modo', 'solicitudes');
      fixture.detectChanges();
      const enSolicitudes = fixture.componentInstance.solicitudes.map(s => s.etapaCodigo);
      fixture.componentRef.setInput('modo', 'respuestas');
      fixture.detectChanges();
      const enRespuestas = fixture.componentInstance.solicitudes.map(s => s.etapaCodigo);

      expect(enSolicitudes).toEqual(etapas.slice(0, 6));
      expect(enRespuestas).toEqual(['APROBADA', 'RECHAZADA']);
      expect(enSolicitudes.filter(e => enRespuestas.includes(e))).toEqual([]);
    });

    it('el modo no cambia las bandejas del Decano ni del Estudiante', () => {
      servicio.getSolicitudesDecano.and.returnValue(of(mezcla()));
      const fixture = TestBed.createComponent(BandejaSolicitudesAcademicasComponent);
      fixture.componentRef.setInput('rol', 'DECANO');
      fixture.componentRef.setInput('modo', 'respuestas');
      fixture.detectChanges();
      expect(filas(fixture).length).toBe(6);
    });
  });

  it('el Decano no tiene el filtro de codigo estudiantil', () => {
    servicio.getSolicitudesDecano.and.returnValue(of(solicitudes(2)));
    const fixture = crear('DECANO');
    expect(fixture.nativeElement.querySelector('[data-filtro-codigo]')).toBeNull();
    expect(encabezados(fixture)).not.toContain('Código');
  });

  it('el Estudiante solo ve sus columnas y no tiene filtro por codigo', () => {
    servicio.getSolicitudesEstudiante.and.returnValue(of(solicitudes(2)));
    const fixture = crear('ESTUDIANTE');
    expect(encabezados(fixture)).toEqual(['Radicado', 'Tipo de proceso', 'Fecha de radicación', 'Estado', 'Acciones']);
    expect(fixture.nativeElement.textContent).not.toContain('Número de identificación');
    expect(filas(fixture).length).toBe(2);
    expect(fixture.nativeElement.querySelector('app-barra-busqueda-component')).toBeNull();
    expect(fixture.nativeElement.querySelector('.tarjeta-head h2').textContent.trim()).toBe('Mis trámites radicados');
  });

  it('el Decano ve la columna y el filtro de decision', () => {
    servicio.getSolicitudesDecano.and.returnValue(of(solicitudes(1)));
    const fixture = crear('DECANO');
    expect(encabezados(fixture)).toContain('Decisión');
    expect(fixture.nativeElement.querySelector('label[for="filtro-estado"]').textContent.trim()).toBe('Decisión');
    expect(fixture.nativeElement.querySelector('.tarjeta-head h2').textContent.trim())
      .toBe('Solicitudes remitidas por el Funcionario Académico');
  });

  it('filtrar vuelve a la pagina 1', () => {
    servicio.getSolicitudesDecano.and.returnValue(of(solicitudes(23)));
    const fixture = crear('DECANO');
    const componente = fixture.componentInstance;

    componente.onPageChange(2);
    componente.onTipoChange('Examen Supletorio');
    fixture.detectChanges();

    expect(componente.paginaActual).toBe(1);
    expect(componente.filtradas.length).toBe(11);
    expect(filas(fixture).length).toBe(10);

    componente.onBusquedaChange('104614');
    fixture.detectChanges();
    expect(filas(fixture).length).toBe(1);
    expect(filas(fixture)[0].textContent).toContain('Estudiante 4');
    expect(filas(fixture)[0].textContent).not.toContain('104614');
  });

  it('filtra por estado con las etiquetas presentes y limpia los filtros', () => {
    const lista = solicitudes(4);
    lista[1].etiqueta = 'En Gestión';
    lista[3].etiqueta = 'En Gestión';
    servicio.getSolicitudesFuncionario.and.returnValue(of(lista));
    const fixture = crear('FUNCIONARIO');
    const componente = fixture.componentInstance;
    expect(componente.opcionesEstado).toEqual(['En Gestión', 'Pendiente']);

    const select = fixture.nativeElement.querySelector('#filtro-estado') as HTMLSelectElement;
    select.value = 'En Gestión';
    select.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(filas(fixture).length).toBe(2);

    componente.onBusquedaChange('sin coincidencias');
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-estado="sin-resultados"]')).toBeTruthy();

    (fixture.nativeElement.querySelector('[data-estado="sin-resultados"] .btn-limpiar') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(componente.estadoFiltro).toBe('TODOS');
    expect(componente.textoFiltro).toBe('');
    expect(filas(fixture).length).toBe(4);
  });

  it('ordena por fecha al pulsar el encabezado y alterna la direccion', () => {
    const lista = solicitudes(3);
    lista[0].fechaCreacion = '2026-10-02T10:00:00';
    lista[1].fechaCreacion = '2026-10-01T10:00:00';
    lista[2].fechaCreacion = '2026-10-03T10:00:00';
    servicio.getSolicitudesDecano.and.returnValue(of(lista));
    const fixture = crear('DECANO');
    const radicados = () => filas(fixture).map(f => f.querySelector('.radicado')?.textContent?.trim());

    const boton = fixture.nativeElement.querySelector('button[aria-label="Ordenar por fecha de radicación"]') as HTMLButtonElement;
    boton.click();
    fixture.detectChanges();
    expect(radicados()).toEqual(['2026-CA-0002', '2026-CA-0001', '2026-CA-0003']);

    boton.click();
    fixture.detectChanges();
    expect(radicados()).toEqual(['2026-CA-0003', '2026-CA-0001', '2026-CA-0002']);
  });

  it('el ojo de Acciones navega al detalle del rol', () => {
    servicio.getSolicitudesDecano.and.returnValue(of(solicitudes(1)));
    const fixture = crear('DECANO');
    const ojo = fixture.nativeElement.querySelector('tbody tr .btn-ojo') as HTMLButtonElement;
    expect(ojo.getAttribute('aria-label')).toBe('Ver solicitud 2026-CA-0001');
    ojo.click();
    expect(router.navigate).toHaveBeenCalledWith(['/decano/solicitudes', 'uuid-1']);
  });

  it('el Funcionario ve la decision del Decano en Estado con su icono y sin insignia aparte', () => {
    const lista = solicitudes(3);
    lista[0].etiqueta = 'Aprobada por el Decano';
    lista[1].etiqueta = 'Rechazada por el Decano';
    servicio.getSolicitudesFuncionario.and.returnValue(of(lista));
    const fixture = crear('FUNCIONARIO');
    expect(estados(fixture)).toEqual(['aprobada', 'rechazada', 'pendiente']);
    expect(filas(fixture)[0].querySelector('.estado')?.textContent?.trim()).toBe('Aprobada por el Decano');
    expect(filas(fixture)[1].querySelector('.estado')?.textContent?.trim()).toBe('Rechazada por el Decano');
    expect(fixture.nativeElement.querySelector('[data-decision-decano]')).toBeNull();
    expect(fixture.nativeElement.querySelector('.badge')).toBeNull();
  });

  it('el icono del Estudiante sale solo de la etiqueta, nunca de etapaCodigo', () => {
    const lista = solicitudes(4);
    lista[0].etiqueta = 'En trámite';
    lista[0].etapaCodigo = 'APROBADA';
    lista[1].etiqueta = 'Pendiente de pago';
    lista[1].etapaCodigo = 'RECHAZADA';
    lista[2].etiqueta = 'Aprobada';
    lista[2].etapaCodigo = 'RADICADA';
    lista[3].etiqueta = 'Rechazada';
    lista[3].etapaCodigo = 'RADICADA';
    servicio.getSolicitudesEstudiante.and.returnValue(of(lista));
    const fixture = crear('ESTUDIANTE');
    expect(estados(fixture)).toEqual(['gestion', 'pago', 'aprobada', 'rechazada']);
    expect(fixture.nativeElement.textContent).not.toContain('APROBADA');
    expect(fixture.nativeElement.textContent).not.toContain('RADICADA');
  });

  it('muestra el estado vacio', () => {
    servicio.getSolicitudesEstudiante.and.returnValue(of([]));
    const fixture = crear('ESTUDIANTE');
    const vacio = fixture.nativeElement.querySelector('[data-estado="vacio"]');
    expect(vacio?.textContent.trim()).toBe('No hay solicitudes para mostrar');
    expect(filas(fixture).length).toBe(0);
  });

  it('muestra el error y lo pasa al ErrorHandlerService', () => {
    servicio.getSolicitudesFuncionario.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    const fixture = crear('FUNCIONARIO');
    expect(fixture.nativeElement.querySelector('[data-estado="error"]')).toBeTruthy();
    expect(errores.handleError).toHaveBeenCalledTimes(1);
    expect(filas(fixture).length).toBe(0);
  });
});
