import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject } from 'rxjs';
import { AuthService } from '../../../services/auth-service';

import { SideBarUsuarioFietComponent } from './side-bar-usuario-fiet-component';

describe('SideBarUsuarioFietComponent', () => {
  let component: SideBarUsuarioFietComponent;
  let fixture: ComponentFixture<SideBarUsuarioFietComponent>;
  let usuario$: BehaviorSubject<any>;

  beforeEach(async () => {
    usuario$ = new BehaviorSubject<any>(null);
    await TestBed.configureTestingModule({
      imports: [SideBarUsuarioFietComponent],
      providers: [{ provide: AuthService, useValue: { usuario$ } }]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SideBarUsuarioFietComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('el Docente ve la opcion Notificaciones ademas de Mi Usuario y Solicitudes', () => {
    usuario$.next({ uuidUsuario: 'doc-1', roles: [{ nombre: 'Docente' }] });
    fixture.detectChanges();
    expect(component.botones.map(b => b.label)).toEqual(['| Mi Usuario', '| Solicitudes', '| Notificaciones']);
    expect(component.botones[2].route).toBe('/docente/notificaciones');
    expect(fixture.nativeElement.textContent).toContain('Notificaciones');
  });

  it('los demas usuarios FIET no ven Notificaciones', () => {
    usuario$.next({ uuidUsuario: 'c-1', roles: [{ nombre: 'Coordinador Pregrado' }] });
    fixture.detectChanges();
    expect(component.botones.map(b => b.label)).toEqual(['| Mi Usuario', '| Solicitudes']);
    expect(fixture.nativeElement.textContent).not.toContain('Notificaciones');
  });
});
