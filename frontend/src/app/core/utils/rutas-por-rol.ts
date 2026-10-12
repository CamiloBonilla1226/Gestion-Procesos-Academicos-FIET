import { ROLES_FIET } from '../constantes/constantes';
import { ROLES_ACADEMICOS } from '../constantes/procesos-academicos';

export function rutaDeInicio(roles: string[]): string | null {
  if (roles.includes('Secretario General')) return '/sec-general';
  if (roles.includes('Funcionario')) return '/funcionario';
  if (roles.includes('Secretaria Decanatura FIET')) return '/sec-fiet';
  if (roles.includes(ROLES_ACADEMICOS.DECANO)) return '/decano';
  if (roles.includes(ROLES_ACADEMICOS.FUNCIONARIO_ACADEMICO)) return '/funcionario-academico';
  if (roles.includes(ROLES_ACADEMICOS.ESTUDIANTE)) return '/estudiante';
  if (roles.some(r => ROLES_FIET.includes(r))) return '/usuario-fiet';
  return null;
}
