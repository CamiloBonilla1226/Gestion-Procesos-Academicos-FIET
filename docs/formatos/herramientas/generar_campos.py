import json
import sys
from pathlib import Path


def campo(clave, pagina, x, y, ancho, alto, fase, tipo="texto", tamano=9, alinear="izquierda"):
    return {"clave": clave, "pagina": pagina, "x": round(x, 1), "y": round(y, 1), "ancho": round(ancho, 1),
            "alto": round(alto, 1), "tamanoFuente": tamano, "tipo": tipo, "alinear": alinear, "fase": fase}


def tabla(filas, columnas):
    campos = []
    for i, (abajo, arriba) in enumerate(filas):
        for clave, fase, izquierda, derecha in columnas:
            campos.append(campo(clave.format(i=i), 1, izquierda + 1, abajo + 0.5, derecha - izquierda - 2, arriba - abajo - 1, fase))
    return campos


def columnas_cancelacion(codigo, nombre, faltas, nota, matricula, cancelar, fin):
    return [("asignatura[{i}].codigo", "estudiante", codigo, nombre),
            ("asignatura[{i}].nombre", "estudiante", nombre, faltas),
            ("facultad[{i}].faltas", "facultad", faltas, nota),
            ("facultad[{i}].nota", "facultad", nota, matricula),
            ("facultad[{i}].matricula", "facultad", matricula, cancelar),
            ("facultad[{i}].cancelar", "facultad", cancelar, fin)]


def for_9():
    campos = [
        campo("fecha.dia", 1, 150, 646.5, 31, 10.5, "estudiante"),
        campo("fecha.mes", 1, 211, 646.5, 41, 10.5, "estudiante"),
        campo("fecha.anio", 1, 282, 646.5, 41, 10.5, "estudiante"),
        campo("nombre", 1, 119, 544.3, 189, 11, "estudiante"),
        campo("codigo", 1, 355, 544.3, 210, 11, "estudiante"),
        campo("programa", 1, 128, 532.3, 180, 11, "estudiante"),
        campo("facultad", 1, 362, 532.3, 203, 11, "estudiante"),
        campo("semestre", 1, 126, 520.3, 182, 11, "estudiante"),
        campo("cedula", 1, 354, 520.3, 211, 11, "estudiante"),
        campo("correo", 1, 176, 508.3, 389, 11, "estudiante"),
        campo("motivo", 1, 200, 496.2, 365, 11, "estudiante"),
        campo("nombreFirma", 1, 64, 218, 200, 11, "estudiante"),
        campo("revisionAcademica", 1, 348, 245, 183, 11, "facultad"),
        campo("decano.aprobadoSi", 1, 154.4, 169.5, 11.3, 11.3, "decano", "marca"),
        campo("decano.aprobadoNo", 1, 234.0, 169.8, 10.3, 10.3, "decano", "marca"),
        campo("radicadoMargen", 0, 380, 756, 186, 12, "estudiante", alinear="derecha"),
    ]
    campos += tabla([(364.4, 388.0), (340.9, 364.4), (317.4, 340.9)],
                    columnas_cancelacion(71.3, 134.4, 368.5, 418.2, 460.7, 510.4, 567.1))
    return {"codigo": "PA-GA-4.2-FOR-9", "titulo": "Solicitud de Cancelación de Asignaturas", "paginas": 1,
            "ultimaPaginaEstudiante": 1, "filasAsignaturas": 3, "campos": campos}


def for_14():
    campos = [
        campo("fecha.dia", 1, 136, 622.5, 31, 10.5, "estudiante"),
        campo("fecha.mes", 1, 197, 622.5, 41, 10.5, "estudiante"),
        campo("fecha.anio", 1, 267, 622.5, 42, 10.5, "estudiante"),
        campo("nombre", 1, 118, 599.1, 176, 11, "estudiante"),
        campo("codigo", 1, 341, 599.1, 222, 11, "estudiante"),
        campo("programa", 1, 127, 587.2, 167, 11, "estudiante"),
        campo("facultad", 1, 348, 587.2, 215, 11, "estudiante"),
        campo("semestre", 1, 125, 575.3, 169, 11, "estudiante"),
        campo("cedula", 1, 340, 575.3, 223, 11, "estudiante"),
        campo("correo", 1, 175, 563.1, 388, 11, "estudiante"),
        campo("motivo", 1, 198, 551.2, 365, 11, "estudiante"),
        campo("nombreFirma", 1, 116, 149.5, 220, 11, "estudiante"),
        campo("revisionAcademica", 1, 334, 201, 184, 11, "facultad"),
        campo("decano.aprobadoSi", 1, 140.5, 102.3, 11.2, 11.2, "decano", "marca"),
        campo("decano.aprobadoNo", 1, 215.8, 102.3, 10.1, 10.1, "decano", "marca"),
        campo("radicadoMargen", 0, 366, 756, 186, 12, "estudiante", alinear="derecha"),
    ]
    campos += tabla([(344.7, 368.1), (321.3, 344.7), (297.5, 321.3), (274.1, 297.5), (250.3, 274.1)],
                    columnas_cancelacion(67.7, 120.3, 347.3, 403.8, 439.5, 496.0, 563.7))
    return {"codigo": "PA-GA-4.2-FOR-14", "titulo": "Solicitud de Cancelación de Matrícula", "paginas": 2,
            "ultimaPaginaEstudiante": 1, "filasAsignaturas": 5, "campos": campos}


def for_23():
    campos = [
        campo("fecha.dia", 1, 145, 620, 25, 10.5, "estudiante"),
        campo("fecha.mes", 1, 197, 620, 21, 10.5, "estudiante"),
        campo("fecha.anio", 1, 245, 620, 46, 10.5, "estudiante"),
        campo("radicado", 1, 172, 597.3, 124, 10, "estudiante"),
        campo("programa", 1, 133, 574, 163, 10, "estudiante"),
        campo("semestre", 1, 349, 574, 202, 10, "estudiante"),
        campo("estudiante", 1, 137, 562, 159, 10, "estudiante"),
        campo("codigo", 1, 340, 562, 211, 10, "estudiante"),
        campo("correo", 1, 115, 551, 181, 10, "estudiante"),
        campo("telefono", 1, 346, 551, 205, 10, "estudiante"),
        campo("asignatura", 1, 137, 540, 159, 10, "estudiante"),
        campo("asignatura.grupo", 1, 336, 540, 74, 10, "estudiante"),
        campo("asignatura.codigo", 1, 453, 540, 99, 10, "estudiante"),
        campo("fechaExamen", 1, 231, 529.2, 320, 10, "estudiante"),
        campo("documentos", 1, 86, 402, 464, 33, "estudiante"),
        campo("cruce.asignatura", 1, 276, 354.1, 154, 10, "estudiante"),
        campo("cruce.grupo", 1, 470, 354.1, 81, 10, "estudiante"),
        campo("cruce.codigo", 1, 125, 342.1, 79, 10.5, "estudiante"),
        campo("cruce.fecha", 1, 294, 342.1, 86, 10.5, "estudiante"),
        campo("cruce.hora", 1, 413, 342.1, 138, 10.5, "estudiante"),
        campo("decano.aprobado", 1, 141.4, 225.7, 7.9, 7.9, "decano", "marca"),
        campo("decano.negado", 1, 210.6, 225.7, 7.9, 7.9, "decano", "marca"),
        campo("autorizacion.asignatura", 1, 137, 212.3, 250, 10.5, "decano"),
        campo("autorizacion.grupo", 1, 428, 212.3, 123, 10.5, "decano"),
        campo("autorizacion.estudiante", 1, 136, 201.3, 252, 10, "decano"),
        campo("autorizacion.codigo", 1, 432, 201.3, 119, 10, "decano"),
        campo("radicadoMargen", 0, 367, 748, 186, 12, "estudiante", alinear="derecha"),
    ]
    return {"codigo": "PM-FO-4-FOR-23", "titulo": "Solicitud de Examen Supletorio", "paginas": 1,
            "ultimaPaginaEstudiante": 1, "filasAsignaturas": 0, "campos": campos}


def main():
    destino = Path(sys.argv[1])
    for plantilla in (for_9(), for_14(), for_23()):
        ruta = destino / (plantilla["codigo"] + ".campos.json")
        ruta.write_text(json.dumps(plantilla, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(ruta, len(plantilla["campos"]))


if __name__ == "__main__":
    main()
