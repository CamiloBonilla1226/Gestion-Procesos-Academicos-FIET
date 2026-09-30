import { Component } from '@angular/core';
import { HeaderComponent } from '../../shared/headers/header-component/header-component';
import { FooterComponent } from '../../shared/footers/footer-component/footer-component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-layout-simple-component',
  imports: [CommonModule, HeaderComponent, FooterComponent],
  templateUrl: './layout-simple-component.html',
  styleUrl: './layout-simple-component.css'
})
export class LayoutSimpleComponent {

}
