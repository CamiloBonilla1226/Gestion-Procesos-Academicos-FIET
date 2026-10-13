import { Component, Input } from '@angular/core';

export interface PasoFormulario {
  titulo: string;
  completo: boolean;
}

@Component({
  selector: 'app-stepper-formulario-component',
  templateUrl: './stepper-formulario-component.html',
  styleUrl: './stepper-formulario-component.css'
})
export class StepperFormularioComponent {
  @Input() pasos: PasoFormulario[] = [];

  get activo(): number {
    const pendiente = this.pasos.findIndex(paso => !paso.completo);
    return pendiente === -1 ? this.pasos.length - 1 : pendiente;
  }

  estado(indice: number): 'hecho' | 'activo' | 'pendiente' {
    if (indice === this.activo && !this.pasos[indice]?.completo) return 'activo';
    return this.pasos[indice]?.completo ? 'hecho' : 'pendiente';
  }
}
