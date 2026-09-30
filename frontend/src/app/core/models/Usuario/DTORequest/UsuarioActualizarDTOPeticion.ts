import { UsuarioLivianoDTOPeticion } from "./UsuarioLivianoDTOPeticion";
import { RolUsuarioDTOPeticion } from "./RolUsuarioDTOPeticion";
import { TipoUsuarioDTOPeticion } from "./TipoUsuarioDTOPeticion";

export class UsuarioActualizarDTOPeticion extends UsuarioLivianoDTOPeticion {
    tipoDocumento!: string;
    numeroDocumento!: string;
    telefono!: string;
    correoElectronico!: string;
    username!: string;
    objTipoUsuario!: TipoUsuarioDTOPeticion;
    roles!: RolUsuarioDTOPeticion[];
}
