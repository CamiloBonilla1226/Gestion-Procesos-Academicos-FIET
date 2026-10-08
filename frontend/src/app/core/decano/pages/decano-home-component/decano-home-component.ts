import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarDecanoComponent } from '../../components/side-bar-decano-component/side-bar-decano-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { InfoUsuarioComponent } from '../../../../shared/pages/info-usuario-component/info-usuario-component';

@Component({
  selector: 'app-decano-home-component',
  imports: [PageComponent],
  templateUrl: './decano-home-component.html'
})
export class DecanoHomeComponent {
  sidebar = SideBarDecanoComponent;

  header = HeaderMainComponent;

  breadcrumb = BreadcrumbComponent;

  main = InfoUsuarioComponent;
}
