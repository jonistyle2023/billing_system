package upse.calculacion.Mad;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import upse.calculacion.general.Mod_DB;
import upse.calculacion.modelo.Producto;

public class Mad_producto {

    private final Mod_DB bd;

    public Mad_producto() {
        this(new Mod_DB());
    }

    public Mad_producto(Mod_DB bd) {
        this.bd = bd;
    }

    public ObservableList<Producto> listarProductos() throws SQLException {
        ObservableList<Producto> productos = FXCollections.observableArrayList();
        String sql = "select prod_id, prod_cod, prod_nombre, prod_precioCompra, prod_pvpxmenor, prod_pvpxmayor, prod_stock, prod_aplicaIva, pod_imagen, prod_estado "
                + "from dbo.Producto "
                + "where prod_estado = 'A' or prod_estado is null "
                + "order by prod_nombre";

        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement sentencia = bd.getConexion().prepareStatement(sql);
                ResultSet rs = sentencia.executeQuery()) {
            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } finally {
            bd.desconectarBD();
        }

        return productos;
    }

    public ObservableList<Producto> buscarProductos(String criterio) throws SQLException {
        ObservableList<Producto> productos = FXCollections.observableArrayList();
        String sql = "select prod_id, prod_cod, prod_nombre, prod_precioCompra, prod_pvpxmenor, prod_pvpxmayor, prod_stock, prod_aplicaIva, pod_imagen, prod_estado "
                + "from dbo.Producto "
                + "where (prod_estado = 'A' or prod_estado is null) "
                + "and (prod_cod like ? or upper(prod_nombre) like upper(?)) "
                + "order by prod_nombre";

        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        String filtro = criterio == null ? "" : criterio.trim();
        String like = "%" + filtro + "%";

        try (PreparedStatement sentencia = bd.getConexion().prepareStatement(sql)) {
            sentencia.setString(1, like);
            sentencia.setString(2, like);
            try (ResultSet rs = sentencia.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapearProducto(rs));
                }
            }
        } finally {
            bd.desconectarBD();
        }

        return productos;
    }

    public Producto buscarPorCodigo(String codigo) throws SQLException {
        String sql = "select prod_id, prod_cod, prod_nombre, prod_precioCompra, prod_pvpxmenor, prod_pvpxmayor, prod_stock, prod_aplicaIva, pod_imagen, prod_estado "
                + "from dbo.Producto where prod_cod = ? and (prod_estado = 'A' or prod_estado is null)";

        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement sentencia = bd.getConexion().prepareStatement(sql)) {
            sentencia.setString(1, codigo);
            try (ResultSet rs = sentencia.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }
        } finally {
            bd.desconectarBD();
        }

        return null;
    }

    public Producto obtenerPorId(int id) throws SQLException {
        String sql = "select prod_id, prod_cod, prod_nombre, prod_precioCompra, prod_pvpxmenor, prod_pvpxmayor, prod_stock, prod_aplicaIva, pod_imagen, prod_estado "
                + "from dbo.Producto where prod_id = ?";

        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement sentencia = bd.getConexion().prepareStatement(sql)) {
            sentencia.setInt(1, id);
            try (ResultSet rs = sentencia.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }
        } finally {
            bd.desconectarBD();
        }

        return null;
    }

    public boolean guardarProducto(Producto producto) throws SQLException {
        String insertSql = "insert into dbo.Producto "
                + "(prod_cod, prod_nombre, prod_precioCompra, prod_pvpxmenor, prod_pvpxmayor, prod_stock, prod_aplicaIva, pod_imagen, prod_estado) "
                + "values (?, ?, ?, ?, ?, ?, ?, ?, 'A')";
        String updateSql = "update dbo.Producto set "
                + "prod_cod = ?, prod_nombre = ?, prod_precioCompra = ?, prod_pvpxmenor = ?, prod_pvpxmayor = ?, "
                + "prod_stock = ?, prod_aplicaIva = ?, pod_imagen = ? "
                + "where prod_id = ?";

        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement sentencia = bd.getConexion().prepareStatement(
                producto.getId() > 0 ? updateSql : insertSql)) {
            sentencia.setString(1, producto.getCodigo());
            sentencia.setString(2, producto.getNombre());
            sentencia.setFloat(3, producto.getPrecioCompra());
            sentencia.setFloat(4, producto.getPvpMenor());
            sentencia.setFloat(5, producto.getPvpMayor());
            sentencia.setFloat(6, producto.getStock());
            sentencia.setInt(7, producto.isAplicaIva() ? 1 : 0);
            sentencia.setString(8, producto.getImagen());
            if (producto.getId() > 0) {
                sentencia.setInt(9, producto.getId());
            }
            return sentencia.executeUpdate() > 0;
        } finally {
            bd.desconectarBD();
        }
    }

    public boolean eliminarProducto(int id) throws SQLException {
        String sql = "update dbo.Producto set prod_estado = 'E' where prod_id = ?";

        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement sentencia = bd.getConexion().prepareStatement(sql)) {
            sentencia.setInt(1, id);
            return sentencia.executeUpdate() > 0;
        } finally {
            bd.desconectarBD();
        }
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setId(rs.getInt("prod_id"));
        producto.setCodigo(rs.getString("prod_cod"));
        producto.setNombre(rs.getString("prod_nombre"));
        producto.setPrecioCompra((float) rs.getDouble("prod_precioCompra"));
        producto.setPvpMenor((float) rs.getDouble("prod_pvpxmenor"));
        producto.setPvpMayor((float) rs.getDouble("prod_pvpxmayor"));
        producto.setStock((float) rs.getDouble("prod_stock"));
        producto.setAplicaIva(rs.getInt("prod_aplicaIva") == 1);
        producto.setImagen(rs.getString("pod_imagen"));
        producto.setEstado(rs.getString("prod_estado"));
        return producto;
    }
}
