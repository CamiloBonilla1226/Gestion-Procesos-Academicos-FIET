import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarDecanoComponent } from '../../components/side-bar-decano-component/side-bar-decano-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { AdminAcademicaComponent } from '../../../../shared/pages/admin-academica-component/admin-academica-component';

@Component({
  selector: 'app-decano-administracion-academica-component',
  imports: [PageComponent],
  templateUrl: './decano-administracion-academica-component.html'
})
export class DecanoAdministracionAcademicaComponent {
  sidebar = SideBarDecanoComponent;

  header = HeaderMainComponent;

  breadcrumb = BreadcrumbComponent;

  main = AdminAcademicaComponent;
}
