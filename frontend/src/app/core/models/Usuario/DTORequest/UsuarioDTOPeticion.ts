import { RolUsuarioDTOPeticion } from "./RolUsuarioDTOPeticion";
import { TipoUsuarioDTOPeticion } from "./TipoUsuarioDTOPeticion";
import { UsuarioLivianoDTOPeticion } from "./UsuarioLivianoDTOPeticion";

export class UsuarioDTOPeticion extends UsuarioLivianoDTOPeticion {
    tipoDocumento!: string;
    numeroDocumento!: string;
    telefono!: string;
    correoElectronico!: string;
    username!: string;
    password!: string;
    objTipoUsuario!: TipoUsuarioDTOPeticion;
    roles!: RolUsuarioDTOPeticion[];
}
