import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarDecanoComponent } from '../../components/side-bar-decano-component/side-bar-decano-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { DecDetalleSolicitudComponent } from '../../../../shared/pages/dec-detalle-solicitud-component/dec-detalle-solicitud-component';

@Component({
  selector: 'app-decano-detalle-solicitud-component',
  imports: [PageComponent],
  templateUrl: './decano-detalle-solicitud-component.html'
})
export class DecanoDetalleSolicitudComponent {
  sidebar = SideBarDecanoComponent;

  header = HeaderMainComponent;

  breadcrumb = BreadcrumbComponent;

  main = DecDetalleSolicitudComponent;
}
