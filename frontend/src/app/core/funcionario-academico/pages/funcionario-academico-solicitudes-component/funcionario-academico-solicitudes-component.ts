import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarFuncionarioAcademicoComponent } from '../../components/side-bar-funcionario-academico-component/side-bar-funcionario-academico-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { FunAcadSolicitudesComponent } from '../../../../shared/pages/fun-acad-solicitudes-component/fun-acad-solicitudes-component';

@Component({
  selector: 'app-funcionario-academico-solicitudes-component',
  imports: [PageComponent],
  templateUrl: './funcionario-academico-solicitudes-component.html'
})
export class FuncionarioAcademicoSolicitudesComponent {
  sidebar = SideBarFuncionarioAcademicoComponent;

  header = HeaderMainComponent;

  breadcrumb = BreadcrumbComponent;

  main = FunAcadSolicitudesComponent;
}
