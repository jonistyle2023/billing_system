package upse.calculacion.modelo;

/** Totales agregados de ventas para un rango de fechas (reporte "Resumen de ventas"). */
public class ResumenVentas {
    private int numFacturasActivas;
    private int numFacturasAnuladas;
    private float subtotal;
    private float subtotalCero;
    private float iva;
    private float total;
    private float totalEfectivo;
    private float totalTarjeta;
    private float totalTransferencia;

    public int getNumFacturasActivas() { return numFacturasActivas; }
    public void setNumFacturasActivas(int numFacturasActivas) { this.numFacturasActivas = numFacturasActivas; }

    public int getNumFacturasAnuladas() { return numFacturasAnuladas; }
    public void setNumFacturasAnuladas(int numFacturasAnuladas) { this.numFacturasAnuladas = numFacturasAnuladas; }

    public float getSubtotal() { return subtotal; }
    public void setSubtotal(float subtotal) { this.subtotal = subtotal; }

    public float getSubtotalCero() { return subtotalCero; }
    public void setSubtotalCero(float subtotalCero) { this.subtotalCero = subtotalCero; }

    public float getIva() { return iva; }
    public void setIva(float iva) { this.iva = iva; }

    public float getTotal() { return total; }
    public void setTotal(float total) { this.total = total; }

    public float getTotalEfectivo() { return totalEfectivo; }
    public void setTotalEfectivo(float totalEfectivo) { this.totalEfectivo = totalEfectivo; }

    public float getTotalTarjeta() { return totalTarjeta; }
    public void setTotalTarjeta(float totalTarjeta) { this.totalTarjeta = totalTarjeta; }

    public float getTotalTransferencia() { return totalTransferencia; }
    public void setTotalTransferencia(float totalTransferencia) { this.totalTransferencia = totalTransferencia; }
}
