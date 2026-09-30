import { Component, Input, Output, EventEmitter, ViewChild } from '@angular/core';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';
import { SimpleButtonComponent } from '../buttons/simple-button-component/simple-button-component';
import { UploadComponent } from '../buttons/upload-component/upload-component';

/**
 * Componente genérico para mostrar un diálogo de subida de archivo.
 * Permite seleccionar, remover y subir un único archivo.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-generic-dialog-upload-file-component',
  imports: [CommonModule, DialogModule, ButtonModule, SimpleButtonComponent, UploadComponent],
  templateUrl: './generic-dialog-upload-file-component.html',
  styleUrl: './generic-dialog-upload-file-component.css'
})
export class GenericDialogUploadFileComponent {
  /** Indica si el diálogo es visible */
  @Input() visible: boolean = false;

  /** Título del diálogo */
  @Input() title: string = '';

  /** Archivo actualmente seleccionado */
  @Input() uploadedFile: File | null = null;

  /** Emite cambios de visibilidad */
  @Output() visibleChange = new EventEmitter<boolean>();

  /** Emite cuando se selecciona un archivo */
  @Output() fileSelected = new EventEmitter<File>();

  /** Emite cuando se remueve un archivo */
  @Output() fileRemoved = new EventEmitter<void>();

  /** Emite cuando se confirma la subida del archivo */
  @Output() upload = new EventEmitter<File>();

  /** Referencia al componente de carga de archivo */
  @ViewChild(UploadComponent) uploadComponent!: UploadComponent;

  /**
   * Cierra el diálogo y reinicia el componente de subida
   */
  close() {
    this.uploadComponent.reset(); 
    this.visibleChange.emit(false);
  }

  /**
   * Maneja la selección de un archivo
   * @param file Archivo seleccionado
   */
  onFileSelected(file: File) {
    this.uploadedFile = file;
    this.fileSelected.emit(file);
  }

  /**
   * Maneja la remoción del archivo seleccionado
   */
  onFileRemoved() {
    this.uploadedFile = null;
    this.fileRemoved.emit();
  }

  /**
   * Emite el evento de subida si hay un archivo seleccionado y cierra el diálogo
   */
  onUpload() {
    if (this.uploadedFile) {
      this.upload.emit(this.uploadedFile);
      this.close();
    }
  }
}
