import { RolUsuarioDTORespuesta } from "./RolUsuarioDTORespuesta";
import { TipoUsuarioDTORespuesta } from "./TipoUsuarioDTORespuesta";
import { UsuarioLivianoDTORespuesta } from "./UsuarioLivianoDTORespuesta";

export class UsuarioDTORespuesta extends UsuarioLivianoDTORespuesta {
    tipoDocumento!: string;
    numeroDocumento!: string;
    telefono!: string;
    correoElectronico!: string;
    username!: string;
    password!: string;
    objTipoUsuario!: TipoUsuarioDTORespuesta;
    roles!: RolUsuarioDTORespuesta[]
}