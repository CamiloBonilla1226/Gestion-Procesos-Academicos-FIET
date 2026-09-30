import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FileUploadModule, FileUpload } from 'primeng/fileupload';
import { ButtonModule } from 'primeng/button';

/**
 * Componente de subida de archivos.
 * Permite seleccionar un archivo, emitir el evento de selección y eliminación,
 * y controlar la referencia interna de PrimeNG FileUpload.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-upload-component',
  imports: [CommonModule, FileUploadModule, ButtonModule],
  templateUrl: './upload-component.html',
  styleUrl: './upload-component.css'
})
export class UploadComponent {

  /**
   * Archivo actualmente cargado.
   */
  @Input() uploadedFile: File | null = null;

  /**
   * Evento emitido cuando se selecciona un archivo.
   */
  @Output() fileSelected = new EventEmitter<File>();

  /**
   * Evento emitido cuando se elimina un archivo.
   */
  @Output() fileRemoved = new EventEmitter<void>();

  /**
   * Referencia al componente FileUpload de PrimeNG.
   */
  @ViewChild(FileUpload) fileUpload!: FileUpload;

  /**
   * Maneja la selección de archivos.
   * Actualiza el archivo cargado y emite el evento fileSelected.
   */
  onFileSelect(event: any): void {
    const file: File = event.files[0];
    if (file) {
      this.uploadedFile = file;
      this.fileSelected.emit(file);
    }
  }

  /**
   * Elimina el archivo seleccionado y emite el evento fileRemoved.
   */
  removeFile(): void {
    this.uploadedFile = null;
    this.fileUpload.clear();
    this.fileRemoved.emit();
  }

  /**
   * Resetea el componente, limpiando el archivo seleccionado.
   */
  reset(): void {
    this.uploadedFile = null;
    this.fileUpload.clear();
  }
}
