import { Component, Input } from '@angular/core';
import { rutaFormato } from '../../../core/constantes/formatos-supletorio';

@Component({
  selector: 'app-enlace-formato-component',
  template: `<a class="enlace-formato" [attr.href]="ruta" [attr.download]="archivo" data-descargar-formato>Descargar formato</a>`,
  styles: [`
    .enlace-formato {
      color: var(--link);
      font-size: 12.5px;
      font-weight: 600;
      text-decoration: none;
      white-space: nowrap;
    }

    .enlace-formato:hover {
      text-decoration: underline;
    }

    .enlace-formato:focus-visible {
      outline: 2px solid #B9C7EE;
      outline-offset: 2px;
    }
  `]
})
export class EnlaceFormatoComponent {
  @Input({ required: true }) archivo!: string;

  get ruta(): string {
    return rutaFormato(this.archivo);
  }
}
