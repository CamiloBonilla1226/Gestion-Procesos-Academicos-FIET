import { Component } from '@angular/core';
import { AdminAsignaturasContentComponent } from '../admin-asignaturas-content-component/admin-asignaturas-content-component';
import { AdminEstudiantesContentComponent } from '../admin-estudiantes-content-component/admin-estudiantes-content-component';
import { AdminFuncionariosContentComponent } from '../admin-funcionarios-content-component/admin-funcionarios-content-component';
import { AdminResponsablesContentComponent } from '../admin-responsables-content-component/admin-responsables-content-component';

type SeccionAdministracion = 'asignaturas' | 'estudiantes' | 'funcionarios' | 'responsables';

@Component({
  selector: 'app-admin-academica-content-component',
  imports: [
    AdminAsignaturasContentComponent,
    AdminEstudiantesContentComponent,
    AdminFuncionariosContentComponent,
    AdminResponsablesContentComponent
  ],
  templateUrl: './admin-academica-content-component.html'
})
export class AdminAcademicaContentComponent {
  readonly secciones: { codigo: SeccionAdministracion; titulo: string }[] = [
    { codigo: 'asignaturas', titulo: 'Asignaturas' },
    { codigo: 'estudiantes', titulo: 'Estudiantes' },
    { codigo: 'funcionarios', titulo: 'Funcionarios académicos' },
    { codigo: 'responsables', titulo: 'Responsable por proceso' }
  ];

  seccion: SeccionAdministracion = 'asignaturas';

  elegir(seccion: SeccionAdministracion, evento: Event): void {
    evento.preventDefault();
    this.seccion = seccion;
  }
}
