import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MAXIMO_TEXTO_COMUNICACION, ParrafoVista, filasDeTexto, parrafosConMarcas } from '../../../core/utils/notificacion-resolucion';

let consecutivo = 0;

@Component({
  selector: 'app-texto-comunicacion-component',
  templateUrl: './texto-comunicacion-component.html',
  styleUrls: ['../../pages/content/estilos-academicos.css', './texto-comunicacion-component.css']
})
export class TextoComunicacionComponent {
  @Input({ required: true }) etiqueta!: string;
  @Input({ required: true }) texto!: string;
  @Input() valores: Record<string, string> = {};
  @Input() resaltar = true;
  @Input() maximo = MAXIMO_TEXTO_COMUNICACION;
  @Input() error = '';
  @Input() ayuda = '';
  @Input() sugerido = '';
  @Input() tituloVista = 'Así saldrá';
  @Input() conVista = true;
  @Output() textoChange = new EventEmitter<string>();

  readonly id = `texto-comunicacion-${++consecutivo}`;

  get filas(): number {
    return filasDeTexto(this.texto ?? '');
  }

  get vista(): ParrafoVista[] {
    return parrafosConMarcas(this.texto ?? '', this.valores, this.resaltar);
  }

  get puedeRestablecer(): boolean {
    return !!this.sugerido && this.sugerido !== this.texto;
  }

  cambiar(evento: Event): void {
    this.texto = (evento.target as HTMLTextAreaElement).value;
    this.textoChange.emit(this.texto);
  }

  restablecer(): void {
    this.texto = this.sugerido;
    this.textoChange.emit(this.texto);
  }
}
