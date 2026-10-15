import email
import itertools
import json
import re
import socketserver
import sys
import threading
import zlib
from email import policy
from pathlib import Path

PUERTO = int(sys.argv[1])
CARPETA = Path(sys.argv[2])
CONTADOR = itertools.count(1)
BLOQUEO = threading.Lock()
FLUJO = re.compile(rb"stream\r?\n(.*?)\r?\nendstream", re.S)
CADENA = re.compile(rb"\(((?:\\.|[^\\)])*)\)\s*Tj", re.S)
OCTAL = re.compile(rb"[0-7]{1,3}")


def cadena_pdf(cruda):
    salida = bytearray()
    i = 0
    while i < len(cruda):
        c = cruda[i]
        if c == 0x5C and i + 1 < len(cruda):
            octal = OCTAL.match(cruda, i + 1, i + 4)
            if octal:
                salida.append(int(octal.group(0), 8) & 0xFF)
                i += 1 + len(octal.group(0))
                continue
            siguiente = cruda[i + 1]
            salida.append({0x6E: 0x0A, 0x72: 0x0D, 0x74: 0x09}.get(siguiente, siguiente))
            i += 2
            continue
        salida.append(c)
        i += 1
    return salida.decode("cp1252", "replace")


def texto_pdf(contenido):
    if not contenido.startswith(b"%PDF"):
        return ""
    textos = []
    for flujo in FLUJO.findall(contenido):
        try:
            plano = zlib.decompress(flujo)
        except zlib.error:
            plano = flujo
        textos.extend(cadena_pdf(cruda) for cruda in CADENA.findall(plano))
    return " ".join(textos)


def adjunto(parte):
    contenido = parte.get_payload(decode=True) or b""
    return {
        "nombre": parte.get_filename(),
        "tipo": parte.get_content_type(),
        "tamanio": len(contenido),
        "esPdf": contenido.startswith(b"%PDF"),
        "textoPdf": texto_pdf(contenido),
    }


def guardar(destinatarios, datos):
    mensaje = email.message_from_bytes(datos, policy=policy.default)
    cuerpo = mensaje.get_body(preferencelist=("plain",))
    adjuntos = [adjunto(parte) for parte in mensaje.iter_attachments()]
    registro = {
        "de": str(mensaje["from"]),
        "para": destinatarios,
        "asunto": str(mensaje["subject"]),
        "texto": cuerpo.get_content() if cuerpo is not None else "",
        "adjuntos": adjuntos,
    }
    with BLOQUEO:
        numero = next(CONTADOR)
    temporal = CARPETA / f"{numero:05d}.tmp"
    temporal.write_text(json.dumps(registro, ensure_ascii=False), encoding="utf-8")
    temporal.replace(CARPETA / f"{numero:05d}.json")


class Sesion(socketserver.StreamRequestHandler):
    def responder(self, texto):
        self.wfile.write((texto + "\r\n").encode("ascii"))

    def leer(self):
        linea = self.rfile.readline()
        return linea.decode("utf-8", "replace").strip() if linea else None

    def handle(self):
        self.responder("220 smtp-prueba")
        destinatarios = []
        while True:
            comando = self.leer()
            if comando is None:
                return
            partes = comando.split()
            verbo = partes[0].upper() if partes else ""
            if verbo == "EHLO":
                self.wfile.write(b"250-smtp-prueba\r\n250-AUTH PLAIN LOGIN\r\n250 8BITMIME\r\n")
            elif verbo == "HELO":
                self.responder("250 smtp-prueba")
            elif verbo == "AUTH":
                mecanismo = partes[1].upper() if len(partes) > 1 else ""
                faltan = (1 if len(partes) == 2 else 0) if mecanismo == "PLAIN" else (2 if len(partes) == 2 else 1)
                for _ in range(faltan):
                    self.responder("334 ")
                    self.leer()
                self.responder("235 2.7.0 autenticado")
            elif verbo == "MAIL":
                destinatarios = []
                self.responder("250 ok")
            elif verbo == "RCPT":
                destinatario = comando.split(":", 1)[1].strip().strip("<>")
                if "smtp-rechaza" in destinatario:
                    self.responder("550 5.1.1 buzon rechazado por la prueba")
                    continue
                destinatarios.append(destinatario)
                self.responder("250 ok")
            elif verbo == "DATA":
                self.responder("354 terminar con punto")
                lineas = []
                while True:
                    linea = self.rfile.readline()
                    if not linea or linea in (b".\r\n", b".\n"):
                        break
                    lineas.append(linea[1:] if linea.startswith(b"..") else linea)
                guardar(destinatarios, b"".join(lineas))
                self.responder("250 recibido")
            elif verbo == "QUIT":
                self.responder("221 adios")
                return
            else:
                self.responder("250 ok")


class Servidor(socketserver.ThreadingTCPServer):
    allow_reuse_address = True
    daemon_threads = True


if __name__ == "__main__":
    CARPETA.mkdir(parents=True, exist_ok=True)
    with Servidor(("127.0.0.1", PUERTO), Sesion) as servidor:
        print(f"smtp-prueba escuchando en 127.0.0.1:{PUERTO}, correos en {CARPETA}", flush=True)
        servidor.serve_forever()
