import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarDecanoComponent } from '../../components/side-bar-decano-component/side-bar-decano-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { DecSolicitudesComponent } from '../../../../shared/pages/dec-solicitudes-component/dec-solicitudes-component';

@Component({
  selector: 'app-decano-solicitudes-component',
  imports: [PageComponent],
  templateUrl: './decano-solicitudes-component.html'
})
export class DecanoSolicitudesComponent {
  sidebar = SideBarDecanoComponent;

  header = HeaderMainComponent;

  breadcrumb = BreadcrumbComponent;

  main = DecSolicitudesComponent;
}
