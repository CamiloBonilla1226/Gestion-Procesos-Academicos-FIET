import { Component, DestroyRef, Input, NgZone, OnChanges, OnInit, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { SolicitudAcademicaService } from '../../../core/services/solicitud-academica-service';

const INTERVALO_MS = 15000;

@Component({
  selector: 'app-aviso-correos-pendientes-component',
  templateUrl: './aviso-correos-pendientes-component.html',
  styleUrls: ['../../pages/content/estilos-academicos.css']
})
export class AvisoCorreosPendientesComponent implements OnInit, OnChanges {
  @Input() actualizar: unknown;

  cantidad = 0;

  private destroyRef = inject(DestroyRef);
  private zona = inject(NgZone);
  private esNavegador = isPlatformBrowser(inject(PLATFORM_ID));

  constructor(private solicitudAcademicaService: SolicitudAcademicaService) {}

  ngOnInit(): void {
    if (!this.esNavegador) return;
    const temporizador = this.zona.runOutsideAngular(() => setInterval(() => this.zona.run(() => this.consultar()), INTERVALO_MS));
    this.destroyRef.onDestroy(() => clearInterval(temporizador));
  }

  ngOnChanges(): void {
    if (this.esNavegador) this.consultar();
  }

  private consultar(): void {
    this.solicitudAcademicaService.getCorreosPendientes().subscribe({
      next: respuesta => (this.cantidad = respuesta.cantidad),
      error: () => (this.cantidad = 0)
    });
  }
}
