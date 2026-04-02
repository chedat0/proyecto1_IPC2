/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servicios;

import daos.PagoDAO;
import modelo.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 *
 * @author jeffm
 */
public class PdfServicio {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color AZUL  = new Color(13, 33, 64);
    private static final Color GRIS  = new Color(248, 249, 250);

    // ─── Comprobante de pago ────────────────────────────────
    public byte[] generarComprobantePago(Reservacion reservacion, Pago pago, List<Cliente> pasajeros) throws Exception {
        ByteArrayOutputStream bos;
        try (Document doc = new Document(PageSize.A4, 50, 50, 60, 40)) {
            bos = new ByteArrayOutputStream();
            PdfWriter.getInstance(doc, bos);
            doc.open();
            Font fTitulo  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, AZUL);
            Font fSub     = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, AZUL);
            Font fNormal  = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
            Font fPeq     = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
            // Encabezado 
            Paragraph titulo = new Paragraph("HORIZONTES SIN LÍMITES", fTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(titulo);
            Paragraph sub = new Paragraph("AGENCIA DE VIAJES", FontFactory.getFont(FontFactory.HELVETICA, 11, Color.GRAY));
            sub.setAlignment(Element.ALIGN_CENTER);
            doc.add(sub);
            doc.add(new Paragraph("\n"));
            Paragraph comp = new Paragraph("COMPROBANTE DE PAGO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, AZUL));
            comp.setAlignment(Element.ALIGN_CENTER);
            doc.add(comp);            
            doc.add(new Paragraph("─────────────────────────────────────────────────────\n", fPeq));
            // Datos de Reservación 
            doc.add(new Paragraph("DATOS DE LA RESERVACIÓN", fSub));
            PdfPTable t1 = new PdfPTable(2);
            t1.setWidthPercentage(100);
            t1.setSpacingBefore(8f);
            agregarFila(t1, "Número de Reservación:", reservacion.getNumeroReservacion(), fNormal);
            agregarFila(t1, "Paquete:", reservacion.getNombrePaquete(), fNormal);
            agregarFila(t1, "Destino:", reservacion.getNombreDestino(), fNormal);
            agregarFila(t1, "Fecha de Viaje:", reservacion.getFechaViaje().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), fNormal);
            agregarFila(t1, "Pasajeros:", String.valueOf(reservacion.getCantidadPasajeros()), fNormal);
            agregarFila(t1, "Estado:", reservacion.getEstado(), fNormal);
            doc.add(t1);
            // Pasajeros
            doc.add(new Paragraph("\nPASAJEROS", fSub));
            PdfPTable tPas = new PdfPTable(3);
            tPas.setWidthPercentage(100);
            tPas.setWidths(new float[]{3f, 4f, 3f});
            tPas.setSpacingBefore(8f);
            addHeader(tPas, "DPI/Pasaporte");
            addHeader(tPas, "Nombre");
            addHeader(tPas, "Nacionalidad");
            for (Cliente cl : pasajeros) {
                addCell(tPas, cl.getDpiPasaporte()); addCell(tPas, cl.getNombreCompleto()); addCell(tPas, cl.getNacionalidad());
            }   doc.add(tPas);
            // Datos de Pago
            doc.add(new Paragraph("\nDETALLE DEL PAGO", fSub));
            PdfPTable t2 = new PdfPTable(2);
            t2.setWidthPercentage(100);
            t2.setSpacingBefore(8f);
            agregarFila(t2, "Número de Comprobante:", pago.getNumeroComprobante(), fNormal);
            agregarFila(t2, "Fecha de Pago:", pago.getFechaPago().format(FMT), fNormal);
            agregarFila(t2, "Método de Pago:", pago.getMetodoNombre(), fNormal);
            agregarFila(t2, "Monto Pagado:", String.format("Q. %.2f", pago.getMonto()), fNormal);
            agregarFila(t2, "Costo Total:", String.format("Q. %.2f", reservacion.getCostoTotal()), fNormal);
            double totalPagado = reservacion.getTotalPagado();
            double saldoPendiente = reservacion.getCostoTotal() - totalPagado;
            if (saldoPendiente < 0) saldoPendiente = 0;
            agregarFila(t2, "Saldo Pendiente:", String.format("Q. %.2f", saldoPendiente), fNormal);
            doc.add(t2);
            // Pie
            doc.add(new Paragraph("\n\n"));
            Paragraph pie = new Paragraph("Generado el " + LocalDateTime.now().format(FMT) + " | Horizontes Sin Límites - Agencia de Viajes", fPeq);
            pie.setAlignment(Element.ALIGN_CENTER);
            doc.add(pie);
        }
        return bos.toByteArray();
    }

    private void agregarFila(PdfPTable t, String label, String valor, Font f) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY)));
        c1.setBorder(Rectangle.NO_BORDER); c1.setPaddingBottom(5f);
        PdfPCell c2 = new PdfPCell(new Phrase(valor != null ? valor : "-", f));
        c2.setBorder(Rectangle.NO_BORDER); c2.setPaddingBottom(5f);
        t.addCell(c1); t.addCell(c2);
    }

    private void addHeader(PdfPTable t, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
        c.setBackgroundColor(AZUL); c.setPadding(5f);
        t.addCell(c);
    }

    private void addCell(PdfPTable t, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "-", FontFactory.getFont(FontFactory.HELVETICA, 10)));
        c.setPadding(4f);
        t.addCell(c);
    }
}
