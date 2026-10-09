import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarSecretarioGeneralComponent } from '../../components/side-bar-secretario-general-component/side-bar-secretario-general-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { AdminAcademicaComponent } from '../../../../shared/pages/admin-academica-component/admin-academica-component';

@Component({
  selector: 'app-secretario-general-administracion-academica-component',
  imports: [PageComponent],
  templateUrl: './secretario-general-administracion-academica-component.html'
})
export class SecretarioGeneralAdministracionAcademicaComponent {
  sidebar = SideBarSecretarioGeneralComponent;

  header = HeaderMainComponent;

  breadcrumb = BreadcrumbComponent;

  main = AdminAcademicaComponent;
}
