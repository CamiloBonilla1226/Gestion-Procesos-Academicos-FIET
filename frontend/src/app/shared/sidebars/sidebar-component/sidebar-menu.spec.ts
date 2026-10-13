import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { SidebarComponent } from './sidebar-component';

describe('SidebarComponent menu', () => {
  const botones = [
    { label: '| Mi Usuario', imgUrl: 'buttons/usuario.svg', route: '/decano' },
    {
      label: '| Gestión académica', imgUrl: 'buttons/configuracion.svg', children: [
        { label: '| Solicitudes académicas', route: '/decano/solicitudes' },
        { label: '| Administración académica', route: '/decano/administracion-academica' }
      ]
    },
    { label: '| Solicitudes del Consejo', route: 'decano/solicitudes-consejo' }
  ];

  function crear(url: string) {
    TestBed.configureTestingModule({
      imports: [SidebarComponent],
      providers: [{ provide: Router, useValue: { url } }]
    });
    const fixture = TestBed.createComponent(SidebarComponent);
    fixture.componentRef.setInput('buttons', botones);
    fixture.detectChanges();
    return fixture;
  }

  function activos(fixture: ReturnType<typeof crear>): string[] {
    return Array.from(fixture.nativeElement.querySelectorAll('.nav-item.is-active'))
      .map((b: any) => b.querySelector('.nav-texto').textContent.trim());
  }

  it('muestra el escudo e items con icono, barra y texto sin el separador', () => {
    const fixture = crear('/decano');
    expect(fixture.nativeElement.querySelector('.crest img').getAttribute('src')).toBe('logos/unicauca-big.png');
    const primero = fixture.nativeElement.querySelector('.nav-item');
    expect(primero.querySelector('.nav-icono').getAttribute('src')).toBe('buttons/usuario.svg');
    expect(primero.querySelector('.bar').textContent.trim()).toBe('|');
    expect(primero.querySelector('.nav-texto').textContent.trim()).toBe('Mi Usuario');
  });

  it('resalta solo la ruta mas especifica que coincide', () => {
    expect(activos(crear('/decano'))).toEqual(['Mi Usuario']);
  });

  it('abre el grupo cuyo subitem esta activo y resalta el subitem', () => {
    const fixture = crear('/decano/solicitudes/123e4567-e89b-12d3-a456-426614174000');
    const subitems = fixture.nativeElement.querySelectorAll('.nav-subitem');
    expect(subitems.length).toBe(2);
    expect(subitems[0].querySelector('.sub-marca').textContent.trim()).toBe('>');
    expect(activos(fixture)).toEqual(['Solicitudes académicas']);
    expect(fixture.nativeElement.querySelector('[aria-expanded]').getAttribute('aria-expanded')).toBe('true');
  });

  it('el grupo se despliega y se pliega al pulsarlo y sus subitems emiten la ruta', () => {
    const fixture = crear('/decano');
    const grupo = fixture.nativeElement.querySelector('[aria-expanded]') as HTMLButtonElement;
    expect(fixture.nativeElement.querySelectorAll('.nav-subitem').length).toBe(0);

    grupo.click();
    fixture.detectChanges();
    const rutas: string[] = [];
    fixture.componentInstance.buttonClicked.subscribe(ruta => rutas.push(ruta));
    (fixture.nativeElement.querySelectorAll('.nav-subitem')[1] as HTMLButtonElement).click();
    expect(rutas).toEqual(['/decano/administracion-academica']);

    grupo.click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('.nav-subitem').length).toBe(0);
  });

  it('acepta rutas sin barra inicial', () => {
    expect(activos(crear('/decano/solicitudes-consejo'))).toEqual(['Solicitudes del Consejo']);
  });

  it('resalta el item cuyas rutas relacionadas coinciden en lugar del prefijo mas corto', () => {
    TestBed.configureTestingModule({
      imports: [SidebarComponent],
      providers: [{ provide: Router, useValue: { url: '/estudiante/examen-supletorio' } }]
    });
    const fixture = TestBed.createComponent(SidebarComponent);
    fixture.componentRef.setInput('buttons', [
      { label: '| Mi Usuario', route: '/estudiante' },
      { label: '| Solicitudes', route: '/estudiante/catalogo', activaEn: ['/estudiante/examen-supletorio'] },
      { label: '| Mis solicitudes', route: '/estudiante/solicitudes' }
    ]);
    fixture.detectChanges();
    expect(activos(fixture)).toEqual(['Solicitudes']);
  });
});
