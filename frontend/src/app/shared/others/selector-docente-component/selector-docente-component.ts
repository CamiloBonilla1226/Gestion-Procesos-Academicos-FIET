import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { finalize } from 'rxjs';
import { InputTextComponent } from '../../inputs/input-text-component/input-text-component';
import { SimpleButtonComponent } from '../../buttons/simple-button-component/simple-button-component';
import { Paginator } from '../../paginator/paginator';
import { ExamenSupletorioService } from '../../../core/services/examen-supletorio-service';
import { ErrorHandlerService } from '../../../core/services/error-handler-service';
import { DocenteDTORespuesta } from '../../../core/models/ExamenSupletorio/DTOResponse/DocenteDTORespuesta';

export const TAMANIO_PAGINA_DOCENTES = 5;

@Component({
  selector: 'app-selector-docente-component',
  imports: [InputTextComponent, SimpleButtonComponent, Paginator],
  templateUrl: './selector-docente-component.html',
  styleUrls: ['../../pages/content/estilos-academicos.css', '../../pages/content/tabla-asignaturas.css']
})
export class SelectorDocenteComponent implements OnInit {
  @Input() seleccionado: DocenteDTORespuesta | null = null;
  @Input() deshabilitado = false;
  @Output() seleccionadoChange = new EventEmitter<DocenteDTORespuesta | null>();

  filtro = { nombre: '', numeroDocumento: '', username: '' };
  docentes: DocenteDTORespuesta[] = [];
  paginaActual = 1;
  totalPaginas = 1;
  buscando = false;
  buscado = false;

  constructor(
    private examenSupletorioService: ExamenSupletorioService,
    private errorHandlerService: ErrorHandlerService
  ) {}

  ngOnInit(): void {
    this.cargar();
  }

  buscar(): void {
    this.paginaActual = 1;
    this.cargar();
  }

  cambiarPagina(pagina: number): void {
    this.paginaActual = pagina;
    this.cargar();
  }

  elegir(docente: DocenteDTORespuesta): void {
    if (this.deshabilitado) return;
    this.seleccionado = docente;
    this.seleccionadoChange.emit(docente);
  }

  esElegido(docente: DocenteDTORespuesta): boolean {
    return this.seleccionado?.uuidUsuario === docente.uuidUsuario;
  }

  nombreDe(docente: DocenteDTORespuesta): string {
    return `${docente.nombres} ${docente.apellidos}`.trim();
  }

  private cargar(): void {
    this.buscando = true;
    this.examenSupletorioService
      .buscarDocentes(
        {
          nombre: this.filtro.nombre.trim(),
          numeroDocumento: this.filtro.numeroDocumento.trim(),
          username: this.filtro.username.trim()
        },
        this.paginaActual - 1,
        TAMANIO_PAGINA_DOCENTES
      )
      .pipe(finalize(() => (this.buscando = false)))
      .subscribe({
        next: pagina => {
          this.docentes = pagina.content ?? [];
          this.totalPaginas = Math.max(1, Math.ceil((pagina.totalElements ?? 0) / TAMANIO_PAGINA_DOCENTES));
          this.buscado = true;
        },
        error: err => {
          this.docentes = [];
          this.buscado = true;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudieron buscar los profesores');
        }
      });
  }
}
