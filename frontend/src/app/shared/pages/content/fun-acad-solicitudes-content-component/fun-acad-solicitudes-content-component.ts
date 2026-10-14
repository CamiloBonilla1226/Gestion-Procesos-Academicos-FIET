import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Data } from '@angular/router';
import { EMPTY, map } from 'rxjs';
import { BandejaSolicitudesAcademicasComponent, ModoBandeja } from '../bandeja-solicitudes-academicas-component/bandeja-solicitudes-academicas-component';

function modoDe(data: Data | null | undefined): ModoBandeja {
  return data?.['modo'] === 'respuestas' ? 'respuestas' : 'solicitudes';
}

@Component({
  selector: 'app-fun-acad-solicitudes-content-component',
  imports: [BandejaSolicitudesAcademicasComponent],
  templateUrl: './fun-acad-solicitudes-content-component.html'
})
export class FunAcadSolicitudesContentComponent {
  private readonly ruta = inject(ActivatedRoute, { optional: true });

  readonly modo = toSignal((this.ruta?.data ?? EMPTY).pipe(map(modoDe)), { initialValue: modoDe(this.ruta?.snapshot?.data) });
}
