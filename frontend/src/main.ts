// Punto de entrada del frontend de "Gestión de Procesos Académicos FIET".
// Base heredada de https://github.com/jdacamacho/front-fiet-sc (trabajo
// de grado de Julián David Camacho Erazo). Sobre esta base se construyen,
// como trabajo de grado, los módulos de Cancelación de Matrícula,
// Cancelación de Asignatura y Examen Supletorio para la Decanatura FIET.
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
