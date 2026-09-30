/**
 * Componente de información de usuario.
 * Muestra la información del usuario actualmente autenticado
 * y permite cambiar la contraseña mediante un componente hijo.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../../../core/services/auth-service';
import { CommonModule } from '@angular/common';
import { ChangePasswordComponent } from '../../../change-password-component/change-password-component';

@Component({
  selector: 'app-info-usuario-content-component',
  templateUrl: './info-usuario-content-component.html',
  styleUrl: './info-usuario-content-component.css',
  imports: [CommonModule, ChangePasswordComponent]
})
export class InfoUsuarioContentComponent implements OnInit {
  /** Información del usuario actualmente autenticado */
  usuario: any;

  constructor(private authService: AuthService) {}

  /** Inicializa el componente y obtiene la información del usuario */
  ngOnInit(): void {
    this.authService.usuario$.subscribe(user => {
      this.usuario = user;
    });
  }
}
