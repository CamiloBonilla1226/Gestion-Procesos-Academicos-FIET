package com.unicauca.cfiet.solicitudes.dominio.servicios;

import java.time.LocalDate;

public final class RedaccionResolucion {
    private static final String[] MESES = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre",
            "octubre", "noviembre", "diciembre"};
    private static final String[] HASTA_VEINTINUEVE = {"cero", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve",
            "diez", "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve", "veinte",
            "veintiuno", "veintidós", "veintitrés", "veinticuatro", "veinticinco", "veintiséis", "veintisiete", "veintiocho", "veintinueve"};
    private static final String[] DECENAS = {"", "", "", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa"};

    public static final String CONSIDERANDOS_COMUNES = """
            1. El acuerdo N° 055 de 1991 "Por el cual se modifica el Capítulo V del Acuerdo 002 de 1988 sobre Matrícula" artículo 15 establece: "Un estudiante podrá cancelar o adicionar asignaturas, rotaciones o seminarios, previo estudio del Secretario Académico, durante los plazos acordados para ello por la respectiva Facultad."

            2. El Acuerdo 002 de 1988 (por el cual se expide el Reglamento Estudiantil de la Universidad del Cauca) **modificado por el Acuerdo 029 del 13 de septiembre de 2011 Capítulo VIII, artículo 5** señala:

            > "ARTÍCULO 5: El Decano podrá conceder, antes de la terminación del período académico, la cancelación de una o varias asignaturas al estudiante que la solicite por escrito aduciendo los motivos que lo inducen a ello, siempre y cuando no se estén cursando en calidad de repitente, no se violen condiciones de correquisitos y su nota promedio sea igual o superior a tres puntos cero (3.0).

            > PARÁGRAFO: No obstante, a juicio del Consejo de Facultad podrá autorizarse la cancelación de asignaturas que se cursan en calidad de repitente, cuando medien circunstancias de fuerza mayor o de caso fortuito."

            3. El reglamento estudiantil en el capítulo VI artículo 56 numeral 1 y 5, establece:

            > "56. Se incurre en la pérdida definitiva de una asignatura, en cualquiera de los siguientes casos:

            > 1. Cuando habiéndose acumulado en una asignatura un porcentaje de faltas de asistencia mayor al veinte por ciento (20%) de las clases programadas, no se llenen los requisitos contemplados en el literal b. del artículo 47 para la presentación del examen de validación."

            > "56. Se incurre en la pérdida definitiva de una asignatura, en cualquiera de los siguientes casos:

            > 5. Cuando al cancelarla, al tenor del presente Reglamento, el promedio ponderado de las evaluaciones realizadas hasta el momento sea inferior a tres puntos cero (3.0)."

            4. El reglamento estudiantil en el capítulo VI artículo 43 indica: "Se entiende por nota previa el promedio ponderado de las calificaciones de los exámenes parciales, de acuerdo con los criterios establecidos para la evaluación de la asignatura".

            5. Una asignatura se considera aprobada cuando la nota definitiva es igual o superior a 3.0 (artículo 50 Capítulo VI Reglamento estudiantil).""";

    public static final String CONSIDERANDO_MATRICULA = """
            6. El Reglamento estudiantil (acuerdo N° 055 DE 1991) establece: "Artículo 1: La Matrícula, es un acto que vincula al estudiante con la Universidad, por medio del cual, la primera se compromete con todos los recursos a su alcance a proporcionar al segundo una formación integral y éste, a cumplir todas las obligaciones y deberes inherentes a su calidad de estudiante". Antes de matricular el estudiante debe verificar los horarios y demás condiciones para su cumplimiento de acuerdo a sus necesidades. El artículo 2 del Reglamento Estudiantil (acuerdo N° 055 DE 1991) señala: "Quien tenga derecho a ser estudiante de la Universidad debe diligenciar, bajo su responsabilidad y para cada período académico, la matrícula correspondiente de acuerdo con los procedimientos que establezca el Consejo Académico y los reglamentos vigentes".""";

    public static final String PREAMBULO = "El Decano de la Facultad de Ingeniería Electrónica y Telecomunicaciones de la Universidad del Cauca, en uso de "
            + "**su competencia funcional indicado en el Acuerdo N°. 002 de 1988 Modificado por el Acuerdo 029 del 13 de septiembre de 2011 "
            + "Capítulo VIII, artículo 5** y,";

    public static final String PREAMBULO_CONSEJO = "El Consejo de la Facultad de Ingeniería Electrónica y Telecomunicaciones de la Universidad del Cauca, en uso de "
            + "**sus atribuciones reglamentarias, en especial las del parágrafo del artículo 5 del Capítulo VIII del Acuerdo N°. 002 de 1988 "
            + "modificado por el Acuerdo 029 del 13 de septiembre de 2011** y,";
    public static final String CONSEJO = "el Consejo de la Facultad de Ingeniería Electrónica y Telecomunicaciones";

    public static final String ASUNTO_ACEPTA = "Por la cual se acepta la cancelación de una asignatura (s).";
    public static final String ASUNTO_NO_ACEPTA = "Por la cual NO se acepta la cancelación de una asignatura (s).";
    public static final String CARGO_FIRMANTE = "Decano";

    private RedaccionResolucion() {
    }

    public static String fechaCorta(LocalDate fecha) {
        if (fecha == null) return "";
        return fecha.getDayOfMonth() + " de " + MESES[fecha.getMonthValue() - 1] + " de " + fecha.getYear();
    }

    public static String expedicion(LocalDate fecha) {
        if (fecha == null) return "Se expide en Popayán.";
        int dia = fecha.getDayOfMonth();
        String mes = MESES[fecha.getMonthValue() - 1];
        String anio = anioEnLetras(fecha.getYear()) + " (" + fecha.getYear() + ")";
        if (dia == 1)
            return "Se expide en Popayán, el primer (1) día del mes de " + mes + " del año " + anio + ".";
        return "Se expide en Popayán, a los " + diaEnLetras(dia) + " (" + dia + ") días del mes de " + mes + " del año " + anio + ".";
    }

    public static String periodo(LocalDate fecha) {
        if (fecha == null) return "periodo académico en curso";
        return (fecha.getMonthValue() <= 6 ? "primer" : "segundo") + " periodo académico del año " + fecha.getYear();
    }

    public static String diligencia(LocalDate fecha) {
        int anio = fecha == null ? LocalDate.now().getYear() : fecha.getYear();
        return "Al Señor (a) _____________________________________ en la fecha _______________ del " + anio + ", se notificó "
                + "personalmente de la presente resolución. Dado el caso que se proceda a notificar por aviso mediante correo electrónico, "
                + "se le advierte que esta resolución se considerará notificada al finalizar el día siguiente al de la entrega del correo "
                + "donde se le enviará la resolución, seguidamente se continuarán con los trámites administrativos procedentes.";
    }

    private static String diaEnLetras(int dia) {
        if (dia == 21) return "veintiún";
        if (dia == 31) return "treinta y un";
        return numeroEnLetras(dia);
    }

    private static String anioEnLetras(int anio) {
        if (anio < 2000 || anio > 2099) return String.valueOf(anio);
        int resto = anio - 2000;
        return resto == 0 ? "dos mil" : "dos mil " + numeroEnLetras(resto);
    }

    private static String numeroEnLetras(int numero) {
        if (numero < 30) return HASTA_VEINTINUEVE[numero];
        int unidad = numero % 10;
        String decena = DECENAS[numero / 10];
        return unidad == 0 ? decena : decena + " y " + HASTA_VEINTINUEVE[unidad];
    }
}
