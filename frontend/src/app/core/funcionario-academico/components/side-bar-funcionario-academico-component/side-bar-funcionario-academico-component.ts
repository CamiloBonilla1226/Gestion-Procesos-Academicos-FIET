import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { SidebarComponent } from '../../../../shared/sidebars/sidebar-component/sidebar-component';

@Component({
  selector: 'app-side-bar-funcionario-academico-component',
  imports: [SidebarComponent],
  templateUrl: './side-bar-funcionario-academico-component.html'
})
export class SideBarFuncionarioAcademicoComponent {
  constructor(private router: Router) {}

  onSidebarButtonClick(route: string) {
    this.router.navigate([route]);
  }
}
