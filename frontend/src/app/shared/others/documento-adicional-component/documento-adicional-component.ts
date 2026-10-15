import { Component, EventEmitter, Input, Output } from '@angular/core';
import { InputAnexoUploadComponent } from '../../inputs/input-anexo-upload-component/input-anexo-upload-component';
import { FORMATOS_RESOLUCION } from '../../../core/constantes/procesos-academicos';
import { comoTipoAnexo, errorDeArchivo } from '../../../core/utils/validaciones-academicas';

@Component({
  selector: 'app-documento-adicional-component',
  imports: [InputAnexoUploadComponent],
  templateUrl: './documento-adicional-component.html',
  styleUrl: '../../pages/content/estilos-academicos.css'
})
export class DocumentoAdicionalComponent {
  @Input() archivo: File | null = null;
  @Input() yaCargado = false;
  @Input() errorServidor = '';
  @Output() archivoChange = new EventEmitter<File | null>();

  readonly tipo = comoTipoAnexo('documento-adicional', 'Documento adicional (opcional)', FORMATOS_RESOLUCION, false);
  error = '';

  alSeleccionar(archivo: File, entrada: InputAnexoUploadComponent): void {
    const error = errorDeArchivo(archivo, FORMATOS_RESOLUCION);
    if (error) {
      this.error = error;
      entrada.reset();
      this.archivoChange.emit(null);
      return;
    }
    this.error = '';
    this.archivoChange.emit(archivo);
  }

  alQuitar(): void {
    this.error = '';
    this.archivoChange.emit(null);
  }
}
