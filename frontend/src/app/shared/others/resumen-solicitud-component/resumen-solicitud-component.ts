import { Component, Input } from '@angular/core';
import { SolicitudAcademicaDetalleDTORespuesta } from '../../../core/models/SolicitudAcademica/DTOResponse/SolicitudAcademicaDetalleDTORespuesta';

interface FilaResumen {
  etiqueta: string;
  valor: string;
}

@Component({
  selector: 'app-resumen-solicitud-component',
  templateUrl: './resumen-solicitud-component.html',
  styleUrl: './resumen-solicitud-component.css'
})
export class ResumenSolicitudComponent {
  @Input({ required: true }) solicitud!: SolicitudAcademicaDetalleDTORespuesta;

  get filas(): FilaResumen[] {
    const estudiante = this.solicitud?.estudiante;
    const nombre = `${estudiante?.nombres ?? ''} ${estudiante?.apellidos ?? ''}`.trim();
    return [
      { etiqueta: 'Estudiante', valor: nombre },
      { etiqueta: 'Código estudiantil', valor: estudiante?.codigoEstudiantil ?? '' },
      { etiqueta: 'Tipo de proceso', valor: this.solicitud?.tipoSolicitud ?? '' },
      { etiqueta: 'Número de radicado', valor: this.solicitud?.radicado ?? '' }
    ].filter(fila => fila.valor.trim() !== '');
  }
}
