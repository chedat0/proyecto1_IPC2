/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servicios;

import daos.*;
import modelo.*;
import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jeffm
 */
public class CargaServicio {
    
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final UsuarioDAO    usuarioDAO    = new UsuarioDAO();
    private final DestinoDAO    destinoDAO    = new DestinoDAO();
    private final ProveedorDAO  proveedorDAO  = new ProveedorDAO();
    private final PaqueteDAO    paqueteDAO    = new PaqueteDAO();
    private final ServicioPaqueteDAO servicioDAO = new ServicioPaqueteDAO();
    private final ClienteDAO    clienteDAO    = new ClienteDAO();
    private final ReservacionServicio reservacionServicio = new ReservacionServicio();
    private final PagoDAO       pagoDAO       = new PagoDAO();
    private final ReservacionDAO reservacionDAO = new ReservacionDAO();

    public CargaResultado procesar(InputStream is, int idAdmin) throws Exception {
        List<String> errores = new ArrayList<>();
        int procesados = 0, exitosos = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String linea;
            int numLinea = 0;
            while ((linea = br.readLine()) != null) {
                numLinea++;
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) continue;
                procesados++;
                try {
                    procesarLinea(linea);
                    exitosos++;
                } catch (Exception e) {
                    errores.add("Línea " + numLinea + " [" + linea.substring(0, Math.min(40, linea.length())) + "...]: " + e.getMessage());
                }
            }
        }

        CargaResultado res = new CargaResultado();
        res.setRegistrosProcesados(procesados);
        res.setRegistrosExitosos(exitosos);
        res.setRegistrosError(errores.size());
        res.setErrores(errores);
        return res;
    }

    private void procesarLinea(String linea) throws Exception {
        
        String lineaMayus = linea.toUpperCase();
        
        if (lineaMayus.startsWith("USUARIO("))         procesarUsuario(extraerArgs(linea));
        else if (lineaMayus.startsWith("DESTINO("))    procesarDestino(extraerArgs(linea));
        else if (lineaMayus.startsWith("PROVEEDOR("))  procesarProveedor(extraerArgs(linea));
        else if (lineaMayus.startsWith("PAQUETE("))    procesarPaquete(extraerArgs(linea));
        else if (lineaMayus.startsWith("SERVICIO_PAQUETE(")) procesarServicio(extraerArgs(linea));
        else if (lineaMayus.startsWith("CLIENTE("))    procesarCliente(extraerArgs(linea));
        else if (lineaMayus.startsWith("RESERVACION(")) procesarReservacion(extraerArgs(linea));
        else if (lineaMayus.startsWith("PAGO("))       procesarPago(extraerArgs(linea));
        else throw new Exception("Instrucción desconocida.");
    }

    private String[] extraerArgs(String linea) throws Exception {
        int ini = linea.indexOf('(');
        int fin = linea.lastIndexOf(')');
        if (ini < 0 || fin < 0) throw new Exception("Formato de instrucción inválido.");
        String contenido = linea.substring(ini + 1, fin);
        // Separar por comas que NO estén dentro de comillas
        List<String> args = new ArrayList<>();
        boolean inQuote = false;
        StringBuilder cur = new StringBuilder();
        for (char ch : contenido.toCharArray()) {
            if (ch == '"') { inQuote = !inQuote; continue; }
            if (ch == ',' && !inQuote) { args.add(cur.toString().trim()); cur.setLength(0); }
            else cur.append(ch);
        }
        args.add(cur.toString().trim());
        return args.toArray(new String[0]);
    }

    private void procesarUsuario(String[] a) throws Exception {
        requireLen(a, 3, "USUARIO");
        String usuario = a[0], contra = a[1];
        int tipo = parseInt(a[2], "TIPO");
        if (contra.length() < 6) throw new Exception("Contraseña muy corta (mínimo 6 caracteres).");
        if (tipo < 1 || tipo > 3)  throw new Exception("TIPO inválido (1, 2 o 3).");
        if (usuarioDAO.existeUsername(usuario)) throw new Exception("Usuario '" + usuario + "' ya existe.");
        Usuario u = new Usuario(usuario, BCrypt.hashpw(contra, BCrypt.gensalt(10)), usuario, tipo);
        usuarioDAO.ingresar(u);
    }

    private void procesarDestino(String[] a) throws Exception {
        requireLen(a, 3, "DESTINO");
        if (destinoDAO.obtenerPorNombre(a[0]) != null) 
            throw new Exception("Destino '" + a[0] + "' ya existe.");
        
        Destino d = new Destino();
        d.setNombre(a[0]); d.setPais(a[1]); d.setDescripcion(a[2]); d.setActivo(true);
        destinoDAO.ingresar(d);
    }

    private void procesarProveedor(String[] a) throws Exception {
        requireLen(a, 3, "PROVEEDOR");
        int tipo = parseInt(a[1], "TIPO");
        if (tipo < 1 || tipo > 5) throw new Exception("TIPO inválido (1-5).");
        if (proveedorDAO.obtenerPorNombre(a[0]) != null) 
            throw new Exception("Proveedor '" + a[0] + "' ya existe.");
        
        Proveedor p = new Proveedor();
        p.setNombre(a[0]); p.setTipoServicio(tipo); p.setPaisOperacion(a[2]); p.setActivo(true);
        proveedorDAO.ingresar(p);
    }

    private void procesarPaquete(String[] a) throws Exception {
        requireLen(a, 5, "PAQUETE");
        Destino destino = destinoDAO.obtenerPorNombre(a[1]);
        if (destino == null) 
            throw new Exception("Destino '" + a[1] + "' no existe.");
        if (paqueteDAO.obtenerPorNombre(a[0]) != null) 
            throw new Exception("Paquete '" + a[0] + "' ya existe.");
        int duracion = parseInt(a[2], "DURACION");
        double precio = parseDouble(a[3], "PRECIO");
        int cap = parseInt(a[4], "CAPACIDAD");
        if (duracion <= 0) throw new Exception("DURACION debe ser positivo.");
        if (precio <= 0) throw new Exception("PRECIO debe ser mayor a cero.");
        if (cap <= 0) throw new Exception("CAPACIDAD debe ser positivo.");
        Paquete p = new Paquete();
        p.setNombre(a[0]); p.setIdDestino(destino.getIdDestino());
        p.setDuracionDias(duracion); p.setPrecioVenta(precio); p.setCapacidadMaxima(cap); p.setActivo(true);
        paqueteDAO.ingresar(p);
    }

    private void procesarServicio(String[] a) throws Exception {
        requireLen(a, 4, "SERVICIO_PAQUETE");
        Paquete paquete = paqueteDAO.obtenerPorNombre(a[0]);
        if (paquete == null) 
            throw new Exception("Paquete '" + a[0] + "' no existe.");
        Proveedor proveedor = proveedorDAO.obtenerPorNombre(a[1]);
        if (proveedor == null)
            throw new Exception("Proveedor '" + a[1] + "' no existe.");
        
        double costo = parseDouble(a[3], "COSTO");
        if (costo < 0) throw new Exception("COSTO no puede ser negativo.");
        ServicioPaquete s = new ServicioPaquete();
        s.setIdPaquete(paquete.getIdPaquete()); s.setIdProveedor(proveedor.getIdProveedor());
        s.setDescripcion(a[2]); s.setCostoProveedor(costo);
        servicioDAO.ingresar(s);
    }

    private void procesarCliente(String[] a) throws Exception {
        requireLen(a, 6, "CLIENTE");
        if (clienteDAO.obtenerPorDPI(a[0]) != null) 
            throw new Exception("Cliente DPI '" + a[0] + "' ya existe.");
        
        LocalDate fnac;
        try { fnac = LocalDate.parse(a[2], FMT); }
        catch (Exception e) { throw new Exception("FECHA_NAC inválida (dd/mm/yyyy)."); }
        Cliente cl = new Cliente();
        cl.setDpiPasaporte(a[0]); cl.setNombreCompleto(a[1]); cl.setFechaNacimiento(fnac);
        cl.setTelefono(a[3]); cl.setEmail(a[4]); cl.setNacionalidad(a[5]);
        clienteDAO.ingresar(cl);
    }

    private void procesarReservacion(String[] a) throws Exception {
        requireLen(a, 4, "RESERVACION");
        Paquete paquete = paqueteDAO.obtenerPorNombre(a[0]); 
        if (paquete == null)
            throw new Exception("Paquete '" + a[0] + "' no existe.");
        
        Usuario agente  = usuarioDAO.obtenerPorUsuario(a[1]);
        if (agente == null)
            throw new Exception("Usuario '" + a[1] + "' no existe.");
        
        LocalDate fv;
        try { fv = LocalDate.parse(a[2], FMT); }
        catch (Exception e) { throw new Exception("FECHA_VIAJE inválida (dd/mm/yyyy)."); }
        
        String[] dpis = a[3].split("\\|");
        List<Integer> idsPasajeros = new ArrayList<>();
        for (String dpi : dpis) {
            Cliente cl = clienteDAO.obtenerPorDPI(dpi.trim());
            if (cl == null)
                throw new Exception("Pasajero con el DPI '" + dpi.trim() + "' no existe.");
            idsPasajeros.add(cl.getIdCliente());
        }
        reservacionServicio.crearReservacion(paquete.getIdPaquete(), agente.getIdUsuario(), fv.toString(), idsPasajeros);
    }

    private void procesarPago(String[] a) throws Exception {
        requireLen(a, 4, "PAGO");
        Reservacion res = reservacionDAO.obtenerPorNumero(a[0]);
        if (res == null)
            throw new Exception("Reservación '" + a[0] + "' no existe.");
        
        double monto = parseDouble(a[1], "MONTO");
        if (monto <= 0)
            throw new Exception("MONTO debe ser mayor a cero");
        
        int metodo = parseInt(a[2], "METODO");
        if (metodo < 1 || metodo > 3) throw new Exception("METODO inválido debe ser 1 (Efectivo), 2 (Tarjeta) o 3 (Transferencia).");
        reservacionServicio.registrarPago(res.getIdReservacion(), monto, metodo);
    }
   
    private void requireLen(String[] a, int n, String inst) throws Exception {
        if (a.length < n) throw new Exception(inst + " requiere " + n + " argumentos, se recibieron " + a.length + ".");
    }
    
    private int parseInt(String s, String campo) throws Exception {
        try { 
            return Integer.parseInt(s.trim()); 
        } catch (NumberFormatException e) { 
            throw new Exception(campo + " debe ser un número entero."); 
        }
    }
    
    private double parseDouble(String s, String campo) throws Exception {
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            throw new Exception(campo + "debe ser un numero decimal válido");
        }
    }
    
    public static class CargaResultado {
        private int registrosProcesados;
        private int registrosExitosos;
        private int registrosError;
        private List<String> errores;

        public int getRegistrosProcesados()           { return registrosProcesados; }
        public void setRegistrosProcesados(int v)     { registrosProcesados = v; }
        public int getRegistrosExitosos()             { return registrosExitosos; }
        public void setRegistrosExitosos(int v)       { registrosExitosos = v; }
        public int getRegistrosError()                { return registrosError; }
        public void setRegistrosError(int v)          { registrosError = v; }
        public List<String> getErrores()              { return errores; }
        public void setErrores(List<String> v)        { errores = v; }
    }
}
