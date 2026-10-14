import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { SideBarFuncionarioAcademicoComponent } from './side-bar-funcionario-academico-component';

describe('SideBarFuncionarioAcademicoComponent', () => {
  function crear(url: string) {
    const router = { url, navigate: jasmine.createSpy('navigate') };
    TestBed.configureTestingModule({
      imports: [SideBarFuncionarioAcademicoComponent],
      providers: [{ provide: Router, useValue: router }]
    });
    const fixture = TestBed.createComponent(SideBarFuncionarioAcademicoComponent);
    fixture.detectChanges();
    return { fixture, router };
  }

  function activos(elemento: HTMLElement): string[] {
    return Array.from(elemento.querySelectorAll('.nav-item.is-active')).map((b: any) => b.querySelector('.nav-texto').textContent.trim());
  }

  function subitems(elemento: HTMLElement): string[] {
    return Array.from(elemento.querySelectorAll('.nav-subitem .nav-texto')).map((e: any) => e.textContent.trim());
  }

  it('agrupa Solicitudes y debajo Respuestas, y marca Solicitudes en su ruta', () => {
    const { fixture } = crear('/funcionario-academico/solicitudes');
    expect(subitems(fixture.nativeElement)).toEqual(['Solicitudes', 'Respuestas']);
    expect(activos(fixture.nativeElement)).toEqual(['Solicitudes']);
  });

  it('marca Respuestas en su ruta', () => {
    const { fixture } = crear('/funcionario-academico/respuestas');
    expect(activos(fixture.nativeElement)).toEqual(['Respuestas']);
  });

  it('Mi Usuario queda activo solo en el inicio', () => {
    const { fixture } = crear('/funcionario-academico');
    expect(activos(fixture.nativeElement)).toEqual(['Mi Usuario']);
  });

  it('el subitem Respuestas navega a su ruta', () => {
    const { fixture, router } = crear('/funcionario-academico/solicitudes');
    (fixture.nativeElement.querySelectorAll('.nav-subitem')[1] as HTMLButtonElement).click();
    expect(router.navigate).toHaveBeenCalledWith(['/funcionario-academico/respuestas']);
  });
});
