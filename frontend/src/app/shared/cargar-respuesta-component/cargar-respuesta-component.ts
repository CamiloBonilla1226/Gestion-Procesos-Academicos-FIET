import { Component, EventEmitter, Output, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FileUpload, FileUploadModule } from 'primeng/fileupload';

/**
 * Componente para cargar un archivo de respuesta.
 * Permite seleccionar archivos, validar su formato y emitir el archivo seleccionado.
 * Soporta limpiar la selección y manejar errores de formato.
 * 
 * Formatos permitidos: PDF y DOCX.
 * 
 * @author Julian David Camacho Erazo
 * {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-cargar-respuesta-component',
  standalone: true,
  imports: [CommonModule, FileUploadModule],
  templateUrl: './cargar-respuesta-component.html',
  styleUrl: './cargar-respuesta-component.css'
})
export class CargarRespuestaComponent {

  /** Evento que emite el archivo seleccionado */
  @Output() archivoSeleccionado = new EventEmitter<File>();

  /** Referencia al componente FileUpload de PrimeNG */
  @ViewChild(FileUpload) fileUpload!: FileUpload;

  /** Archivo actualmente seleccionado */
  archivo: File | null = null;

  /** Indica si ocurrió un error por formato no permitido */
  errorFormato = false;

  /** Lista de formatos permitidos para el archivo */
  formatosPermitidos = ['pdf', 'docx'];

  /**
   * Maneja la selección de un archivo.
   * Valida el formato antes de asignarlo.
   * @param event Evento disparado por el componente FileUpload
   */
  onFileSelect(event: any): void {
    const file: File = event.files?.[0];
    if (!file) return;

    if (!this.validarFormato(file)) {
      this.errorFormato = true;
      this.fileUpload.clear();
      return;
    }

    this.errorFormato = false;
    this.archivo = file;
  }

  /**
   * Confirma la carga del archivo seleccionado y emite el evento.
   */
  confirmarCarga(): void {
    if (this.archivo) {
      this.archivoSeleccionado.emit(this.archivo);
    }
  }

  /**
   * Limpia la selección actual del archivo y resetea errores.
   */
  limpiar(): void {
    this.archivo = null;
    this.errorFormato = false;
    this.fileUpload.clear();
  }

  /**
   * Valida si el archivo tiene un formato permitido.
   * @param file Archivo a validar
   * @returns `true` si el formato es permitido, `false` en caso contrario
   */
  private validarFormato(file: File): boolean {
    const extension = file.name.split('.').pop()?.toLowerCase();
    return !!extension && this.formatosPermitidos.includes(extension);
  }
}
