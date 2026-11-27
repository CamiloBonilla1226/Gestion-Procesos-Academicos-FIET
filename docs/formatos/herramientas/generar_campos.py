import json
import sys
from pathlib import Path


def campo(clave, pagina, x, y, ancho, alto, fase, tipo="texto", tamano=9, alinear="izquierda", minimo=None):
    datos = {"clave": clave, "pagina": pagina, "x": round(x, 1), "y": round(y, 1), "ancho": round(ancho, 1),
             "alto": round(alto, 1), "tamanoFuente": tamano, "tipo": tipo, "alinear": alinear, "fase": fase}
    if minimo is not None:
        datos["tamanoMinimo"] = minimo
    return datos


def linea(clave, x, ancho, base, fase, alto=11):
    return campo(clave, 1, x, base - (alto + 9.9) / 2 + 7.65, ancho, alto, fase)


def tabla(filas, columnas, minimo=None):
    campos = []
    for i, (abajo, arriba) in enumerate(filas):
        for clave, fase, izquierda, derecha in columnas:
            campos.append(campo(clave.format(i=i), 1, izquierda + 1, abajo + 0.5, derecha - izquierda - 2, arriba - abajo - 1, fase,
                                minimo=minimo))
    return campos


def columnas_cancelacion(codigo, nombre, faltas, nota, matricula, cancelar, fin):
    return [("asignatura[{i}].codigo", "estudiante", codigo, nombre),
            ("asignatura[{i}].nombre", "estudiante", nombre, faltas),
            ("facultad[{i}].faltas", "facultad", faltas, nota),
            ("facultad[{i}].nota", "facultad", nota, matricula),
            ("facultad[{i}].matricula", "facultad", matricula, cancelar),
            ("facultad[{i}].cancelar", "facultad", cancelar, fin)]


def margen(derecha, y):
    return campo("radicadoMargen", 0, derecha - 186, y, 186, 10, "estudiante", alinear="derecha")


def for_84():
    campos = [
        linea("fecha.dia", 142, 33, 663.3, "estudiante", 10.5),
        linea("fecha.mes", 203, 42, 663.3, "estudiante", 10.5),
        linea("fecha.anio", 273, 43, 663.3, "estudiante", 10.5),
        linea("nombre", 119, 189, 561.5, "estudiante"),
        linea("codigo", 355, 210, 561.5, "estudiante"),
        linea("programa", 128, 180, 546.7, "estudiante"),
        linea("facultad", 362, 203, 546.7, "estudiante"),
        linea("semestre", 126, 182, 532.0, "estudiante"),
        linea("cedula", 354, 211, 532.0, "estudiante"),
        linea("correo", 176, 389, 517.4, "estudiante"),
        linea("motivo", 200, 365, 502.7, "estudiante"),
        campo("nombreFirma", 1, 64, 217, 200, 11, "estudiante"),
        campo("revisionAcademica", 1, 348, 244.5, 183, 11, "facultad"),
        campo("decano.aprobadoSi", 1, 154.4, 169.8, 11.3, 11.3, "decano", "marca"),
        campo("decano.aprobadoNo", 1, 230.0, 170.0, 10.3, 10.3, "decano", "marca"),
        margen(566.6, 764),
    ]
    campos += tabla([(377.2, 389.2), (365.2, 377.2), (353.2, 365.2), (341.2, 353.2), (329.1, 341.2)],
                    columnas_cancelacion(70.8, 134.4, 368.5, 418.2, 460.7, 510.4, 567.1), minimo=5)
    return {"codigo": "PA-GA-4-FOR-84", "titulo": "Solicitud de Cancelación de Asignaturas", "paginas": 1,
            "ultimaPaginaEstudiante": 1, "filasAsignaturas": 5, "campos": campos}


def for_83():
    campos = [
        linea("fecha.dia", 135, 33, 647.0, "estudiante", 10.5),
        linea("fecha.mes", 196, 43, 647.0, "estudiante", 10.5),
        linea("fecha.anio", 267, 43, 647.0, "estudiante", 10.5),
        linea("nombre", 117, 178, 624.0, "estudiante"),
        linea("codigo", 341, 223, 624.0, "estudiante"),
        linea("programa", 126, 169, 612.0, "estudiante"),
        linea("facultad", 348, 216, 612.0, "estudiante"),
        linea("semestre", 124, 171, 600.0, "estudiante"),
        linea("cedula", 340, 224, 600.0, "estudiante"),
        linea("correo", 174, 390, 588.0, "estudiante"),
        linea("motivo", 198, 366, 575.9, "estudiante"),
        linea("nombreFirma", 107, 220, 150.3, "estudiante"),
        campo("revisionAcademica", 1, 334, 198, 183, 11, "facultad"),
        campo("decano.aprobadoSi", 1, 140.2, 111.7, 11.3, 11.3, "decano", "marca"),
        campo("decano.aprobadoNo", 1, 215.8, 111.9, 10.3, 10.3, "decano", "marca"),
        margen(559.4, 764),
    ]
    campos += tabla([(353.6, 377.2), (330.3, 353.6), (306.8, 330.3), (283.3, 306.8), (259.8, 283.3)],
                    columnas_cancelacion(67.5, 120.3, 347.2, 404.1, 439.4, 496.3, 567.1))
    return {"codigo": "PA-GA-4-FOR-83", "titulo": "Solicitud de Cancelación de Matrícula", "paginas": 2,
            "ultimaPaginaEstudiante": 1, "filasAsignaturas": 5, "campos": campos}


def for_23():
    campos = [
        linea("fecha.dia", 149, 27, 632.0, "estudiante", 10),
        linea("fecha.mes", 202, 22, 632.0, "estudiante", 10),
        linea("fecha.anio", 250, 46, 632.0, "estudiante", 10),
        linea("radicado", 177, 124, 609.7, "estudiante", 10),
        linea("programa", 137, 165, 586.2, "estudiante", 10),
        linea("semestre", 354, 75, 586.2, "estudiante", 10),
        linea("codigo", 472, 85, 585.0, "estudiante", 10),
        linea("estudiante", 141, 161, 574.2, "estudiante", 10),
        linea("cedula", 402, 155, 574.2, "estudiante", 10),
        linea("correo", 119, 183, 563.4, "estudiante", 10),
        linea("telefono", 351, 206, 563.4, "estudiante", 10),
        linea("asignatura", 142, 160, 552.4, "estudiante", 10),
        linea("asignatura.grupo", 340, 75, 552.4, "estudiante", 10),
        linea("asignatura.codigo", 458, 99, 552.4, "estudiante", 10),
        linea("fechaExamen", 236, 321, 530.7, "estudiante", 10),
        campo("documentos", 1, 88, 431.5, 468, 21, "estudiante"),
        linea("cruce.asignatura", 280, 156, 386.7, "estudiante", 10),
        linea("cruce.grupo", 475, 82, 386.7, "estudiante", 10),
        linea("cruce.codigo", 130, 80, 374.8, "estudiante", 10),
        linea("cruce.fecha", 299, 87, 375.9, "estudiante", 10),
        linea("cruce.hora", 418, 139, 375.9, "estudiante", 10),
        campo("decano.aprobado", 1, 147.3, 256.3, 8.0, 8.0, "decano", "marca"),
        campo("decano.negado", 1, 216.3, 256.3, 8.0, 8.0, "decano", "marca"),
        linea("autorizacion.asignatura", 142, 252, 246.2, "decano", 10),
        linea("autorizacion.grupo", 433, 124, 245.1, "decano", 10),
        linea("autorizacion.estudiante", 141, 252, 234.1, "decano", 10),
        linea("autorizacion.codigo", 437, 120, 234.1, "decano", 10),
        margen(560.2, 754),
    ]
    return {"codigo": "PM-FO-4-FOR-23", "titulo": "Solicitud de Examen Supletorio", "paginas": 1,
            "ultimaPaginaEstudiante": 1, "filasAsignaturas": 0, "campos": campos}


def main():
    destino = Path(sys.argv[1])
    for plantilla in (for_84(), for_83(), for_23()):
        ruta = destino / (plantilla["codigo"] + ".campos.json")
        ruta.write_text(json.dumps(plantilla, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(ruta, len(plantilla["campos"]))


if __name__ == "__main__":
    main()
