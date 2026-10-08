import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarFuncionarioAcademicoComponent } from '../../components/side-bar-funcionario-academico-component/side-bar-funcionario-academico-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { FunAcadDetalleSolicitudComponent } from '../../../../shared/pages/fun-acad-detalle-solicitud-component/fun-acad-detalle-solicitud-component';

@Component({
  selector: 'app-funcionario-academico-detalle-solicitud-component',
  imports: [PageComponent],
  templateUrl: './funcionario-academico-detalle-solicitud-component.html'
})
export class FuncionarioAcademicoDetalleSolicitudComponent {
  sidebar = SideBarFuncionarioAcademicoComponent;

  header = HeaderMainComponent;

  breadcrumb = BreadcrumbComponent;

  main = FunAcadDetalleSolicitudComponent;
}
