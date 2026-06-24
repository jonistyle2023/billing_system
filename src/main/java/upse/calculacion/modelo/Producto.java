package upse.calculacion.modelo;

public class Producto {
    private int id;
    private String codigo;
    private String nombre;
    private float precioCompra;
    private float pvpMenor;
    private float pvpMayor;
    private float stock;
    private boolean aplicaIva;
    private String imagen;
    private String estado;

    public Producto() {
    }

    public Producto(int id, String codigo, String nombre, float precioCompra, float pvpMenor, float pvpMayor, float stock, boolean aplicaIva, String imagen, String estado) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.precioCompra = precioCompra;
        this.pvpMenor = pvpMenor;
        this.pvpMayor = pvpMayor;
        this.stock = stock;
        this.aplicaIva = aplicaIva;
        this.imagen = imagen;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public float getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(float precioCompra) {
        this.precioCompra = precioCompra;
    }

    public float getPvpMenor() {
        return pvpMenor;
    }

    public void setPvpMenor(float pvpMenor) {
        this.pvpMenor = pvpMenor;
    }

    public float getPvpMayor() {
        return pvpMayor;
    }

    public void setPvpMayor(float pvpMayor) {
        this.pvpMayor = pvpMayor;
    }

    public float getStock() {
        return stock;
    }

    public void setStock(float stock) {
        this.stock = stock;
    }

    public boolean isAplicaIva() {
        return aplicaIva;
    }

    public void setAplicaIva(boolean aplicaIva) {
        this.aplicaIva = aplicaIva;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}
