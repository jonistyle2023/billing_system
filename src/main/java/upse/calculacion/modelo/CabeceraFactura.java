package upse.calculacion.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CabeceraFactura {

    private String numFac;       // cab_numFac  — PK, formato 001-001-XXXXXXXXX
    private LocalDate fecha;     // cab_fechaFac
    private String cliId;        // cli_id (cédula/RUC del cliente)
    private float subtotal;      // cab_subtotal  (base 15%)
    private float baseCero;      // cab_basecero  (base 0%)
    private float iva;           // cab_iva
    private float total;         // cab_total
    private String estado;       // cab_estado: 'A'=activa, 'E'=anulada
    private int usrId;           // usr_id (usuario que emitió)
    private List<DetFactura> detalles = new ArrayList<>();

    public CabeceraFactura() {
    }

    public String getNumFac() { return numFac; }
    public void setNumFac(String numFac) { this.numFac = numFac; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getCliId() { return cliId; }
    public void setCliId(String cliId) { this.cliId = cliId; }

    public float getSubtotal() { return subtotal; }
    public void setSubtotal(float subtotal) { this.subtotal = subtotal; }

    public float getBaseCero() { return baseCero; }
    public void setBaseCero(float baseCero) { this.baseCero = baseCero; }

    public float getIva() { return iva; }
    public void setIva(float iva) { this.iva = iva; }

    public float getTotal() { return total; }
    public void setTotal(float total) { this.total = total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getUsrId() { return usrId; }
    public void setUsrId(int usrId) { this.usrId = usrId; }

    public List<DetFactura> getDetalles() { return detalles; }
    public void setDetalles(List<DetFactura> detalles) { this.detalles = detalles; }
}
