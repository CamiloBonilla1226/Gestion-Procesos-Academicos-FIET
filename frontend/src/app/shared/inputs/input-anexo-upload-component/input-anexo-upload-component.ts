import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FileUploadModule, FileUpload } from 'primeng/fileupload';
import { ButtonModule } from 'primeng/button';
import { TipoAnexoDTORespuesta } from '../../../core/models/TipoSolicitud/DTOResponse/TipoAnexoDTORespuesta';

/**
 * Componente para subir archivos asociados a un tipo de anexo.
 * Permite seleccionar un archivo, validar su formato y eliminarlo.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-input-anexo-upload-component',
  imports: [
    CommonModule,
    FileUploadModule,
    ButtonModule
  ],
  templateUrl: './input-anexo-upload-component.html',
  styleUrl: './input-anexo-upload-component.css'
})
export class InputAnexoUploadComponent {
  /**
   * Tipo de anexo que define el formato permitido y obligatoriedad.
   */
  @Input() tipoAnexo!: TipoAnexoDTORespuesta;

  /**
   * Archivo actualmente cargado en el componente.
   */
  @Input() uploadedFile: File | null = null;

  /**
   * Evento que se emite cuando se selecciona un archivo válido.
   */
  @Output() fileSelected = new EventEmitter<File>();

  /**
   * Evento que se emite cuando se elimina un archivo.
   */
  @Output() fileRemoved = new EventEmitter<void>();

  /**
   * Referencia al componente FileUpload de PrimeNG.
   */
  @ViewChild(FileUpload) fileUpload!: FileUpload;

  /**
   * Indica si hubo un error de formato al seleccionar un archivo.
   */
  errorFormato = false;

  /**
   * Maneja la selección de un archivo.
   * Valida el formato según el tipo de anexo y emite `fileSelected` si es válido.
   * 
   * @param event Evento de selección de archivo del componente FileUpload.
   */
  onFileSelect(event: any): void {
    const file: File = event.files[0];
    if (!file) return;

    if (!this.validarFormato(file)) {
      this.errorFormato = true;
      this.fileUpload.clear();
      return;
    }

    this.errorFormato = false;
    this.uploadedFile = file;
    this.fileSelected.emit(file);
  }

  /**
   * Elimina el archivo actualmente cargado y emite el evento `fileRemoved`.
   */
  removeFile(): void {
    this.uploadedFile = null;
    this.fileUpload.clear();
    this.fileRemoved.emit();
  }

  /**
   * Valida el formato del archivo según las extensiones permitidas en el tipo de anexo.
   * 
   * @param file Archivo a validar.
   * @returns `true` si el formato es válido o no hay restricción; `false` si es inválido.
   */
  private validarFormato(file: File): boolean {
    if (!this.tipoAnexo.formato) return true;

    const extensiones = this.tipoAnexo.formato
      .split(',')
      .map(f => f.trim().toLowerCase());

    const extensionArchivo = file.name.split('.').pop()?.toLowerCase();
    return extensionArchivo ? extensiones.includes(extensionArchivo) : false;
  }

  /**
   * Determina si el archivo es inválido según la obligatoriedad del tipo de anexo.
   * 
   * @returns `true` si el archivo es obligatorio y no hay ningún archivo cargado; `false` en caso contrario.
   */
  esInvalido(): boolean {
    return this.tipoAnexo.obligatoriedad && !this.uploadedFile;
  }

  /**
   * Reinicia el estado del componente, eliminando el archivo cargado y los errores de formato.
   */
  reset(): void {
    this.uploadedFile = null;
    this.errorFormato = false;

    if (this.fileUpload) {
      this.fileUpload.clear();
    }
  }
}
