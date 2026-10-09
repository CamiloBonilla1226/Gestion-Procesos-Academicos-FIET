import { Component, Input } from '@angular/core';
import { InputTextComponent } from '../../inputs/input-text-component/input-text-component';
import { InputSelectComponent } from '../../inputs/input-select-component/input-select-component';
import { InputPasswordComponent } from '../../inputs/input-password-component/input-password-component';
import { TIPOS_DOCUMENTO } from '../../../core/constantes/constantes';
import { DatosUsuarioAcademico } from '../../../core/utils/usuarios-academicos';

@Component({
  selector: 'app-datos-usuario-academico-component',
  imports: [InputTextComponent, InputSelectComponent, InputPasswordComponent],
  templateUrl: './datos-usuario-academico-component.html'
})
export class DatosUsuarioAcademicoComponent {
  @Input({ required: true }) datos!: DatosUsuarioAcademico;
  @Input() errores: Record<string, string> = {};

  readonly tiposDocumento = TIPOS_DOCUMENTO;
}
