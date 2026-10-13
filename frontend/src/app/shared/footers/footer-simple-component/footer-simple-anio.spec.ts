import { TestBed } from '@angular/core/testing';
import { FooterSimpleComponent } from './footer-simple-component';

describe('FooterSimpleComponent textos', () => {
  it('muestra el año actual y los textos institucionales', () => {
    const fixture = TestBed.createComponent(FooterSimpleComponent);
    fixture.detectChanges();
    const texto = fixture.nativeElement.textContent.replace(/\s+/g, ' ');
    expect(texto).toContain(`${new Date().getFullYear()} | Gestión de Procesos Académicos FIET | Facultad de Ingeniería Electrónica y Telecomunicaciones`);
    expect(texto).toContain('División de Tecnologías de la Información y las Comunicaciones | Versión 1.0.0');
  });
});
