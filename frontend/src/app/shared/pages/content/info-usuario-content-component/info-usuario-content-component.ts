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
import { FuncionarioAcademicoService } from '../../../../core/services/funcionario-academico-service';
import { FuncionarioAcademicoDTORespuesta } from '../../../../core/models/FuncionarioAcademico/DTOResponse/FuncionarioAcademicoDTORespuesta';
import { ROLES_ACADEMICOS } from '../../../../core/constantes/procesos-academicos';

@Component({
  selector: 'app-info-usuario-content-component',
  templateUrl: './info-usuario-content-component.html',
  styleUrl: './info-usuario-content-component.css',
  imports: [CommonModule, ChangePasswordComponent]
})
export class InfoUsuarioContentComponent implements OnInit {
  /** Información del usuario actualmente autenticado */
  usuario: any;

  funcionarioAcademico: FuncionarioAcademicoDTORespuesta | null = null;

  constructor(
    private authService: AuthService,
    private funcionarioAcademicoService: FuncionarioAcademicoService
  ) {}

  /** Inicializa el componente y obtiene la información del usuario */
  ngOnInit(): void {
    this.authService.usuario$.subscribe(user => {
      this.usuario = user;
      this.cargarFuncionarioAcademico(user);
    });
  }

  get iniciales(): string {
    return [this.usuario?.nombres, this.usuario?.apellidos]
      .map((parte: string | undefined) => (parte ?? '').trim().charAt(0).toUpperCase())
      .join('');
  }

  get nombresRoles(): string {
    return (this.usuario?.roles ?? []).map((rol: { nombre: string }) => rol.nombre).join(', ');
  }

  get cargo(): string {
    return this.funcionarioAcademico?.cargo ?? '';
  }

  get procesosAsignados(): string[] {
    return (this.funcionarioAcademico?.tiposSolicitud ?? []).map(tipo => tipo.nombre);
  }

  private cargarFuncionarioAcademico(user: any): void {
    const esFuncionario = (user?.roles ?? []).some((rol: { nombre: string }) => rol.nombre === ROLES_ACADEMICOS.FUNCIONARIO_ACADEMICO);
    if (!esFuncionario || !user?.uuidUsuario) {
      this.funcionarioAcademico = null;
      return;
    }
    if (this.funcionarioAcademico?.uuidUsuario === user.uuidUsuario) return;
    this.funcionarioAcademicoService.getFuncionarioAcademico(user.uuidUsuario).subscribe({
      next: funcionario => this.funcionarioAcademico = funcionario,
      error: () => this.funcionarioAcademico = null
    });
  }
}
