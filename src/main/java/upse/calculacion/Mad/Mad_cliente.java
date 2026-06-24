package upse.calculacion.Mad;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import upse.calculacion.general.Mod_DB;
import upse.calculacion.modelo.Cliente;

public class Mad_cliente {

    private final Mod_DB bd;

    public Mad_cliente() {
        this(new Mod_DB());
    }

    public Mad_cliente(Mod_DB bd) {
        this.bd = bd;
    }

    public ObservableList<Cliente> listarClientes() throws SQLException {
        ObservableList<Cliente> clientes = FXCollections.observableArrayList();
        String sql = "select cli_id, cli_nombres, cli_telefono, cli_correo, cli_direccion "
                + "from dbo.Cliente "
                + "where cli_estado = 'A' or cli_estado is null "
                + "order by cli_nombres";

        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement sentencia = bd.getConexion().prepareStatement(sql);
                ResultSet rs = sentencia.executeQuery()) {
            while (rs.next()) {
                clientes.add(mapearCliente(rs));
            }
        } finally {
            bd.desconectarBD();
        }

        return clientes;
    }

    public ObservableList<Cliente> buscarClientes(String criterio) throws SQLException {
        ObservableList<Cliente> clientes = FXCollections.observableArrayList();
        String sql = "select cli_id, cli_nombres, cli_telefono, cli_correo, cli_direccion "
                + "from dbo.Cliente "
                + "where (cli_estado = 'A' or cli_estado is null) "
                + "and (cli_id like ? or upper(cli_nombres) like upper(?)) "
                + "order by cli_nombres";

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
                    clientes.add(mapearCliente(rs));
                }
            }
        } finally {
            bd.desconectarBD();
        }

        return clientes;
    }

    public Cliente buscarPorCedula(String cedula) throws SQLException {
        String sql = "select cli_id, cli_nombres, cli_telefono, cli_correo, cli_direccion "
                + "from dbo.Cliente "
                + "where cli_id = ? "
                + "and (cli_estado = 'A' or cli_estado is null)";

        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement sentencia = bd.getConexion().prepareStatement(sql)) {
            sentencia.setString(1, cedula);
            try (ResultSet rs = sentencia.executeQuery()) {
                if (rs.next()) {
                    return mapearCliente(rs);
                }
            }
        } finally {
            bd.desconectarBD();
        }
        return null;
    }

    public boolean guardarCliente(Cliente cliente) throws SQLException {
        String existeSql = "select count(1) from dbo.Cliente where cli_id = ?";
        String insertSql = "insert into dbo.Cliente (cli_id, cli_nombres, cli_telefono, cli_correo, cli_direccion, cli_estado) "
                + "values (?, ?, ?, ?, ?, 'A')";
        String updateSql = "update dbo.Cliente set cli_nombres = ?, cli_telefono = ?, cli_correo = ?, cli_direccion = ?, cli_estado = 'A' "
                + "where cli_id = ?";

        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        try {
            boolean existe;
            try (PreparedStatement sentencia = bd.getConexion().prepareStatement(existeSql)) {
                sentencia.setString(1, cliente.getCedula());
                try (ResultSet rs = sentencia.executeQuery()) {
                    existe = rs.next() && rs.getInt(1) > 0;
                }
            }

            try (PreparedStatement sentencia = bd.getConexion().prepareStatement(existe ? updateSql : insertSql)) {
                if (existe) {
                    sentencia.setString(1, cliente.getNombres());
                    sentencia.setString(2, cliente.getTelefono());
                    sentencia.setString(3, cliente.getCorreo());
                    sentencia.setString(4, cliente.getDireccion());
                    sentencia.setString(5, cliente.getCedula());
                } else {
                    sentencia.setString(1, cliente.getCedula());
                    sentencia.setString(2, cliente.getNombres());
                    sentencia.setString(3, cliente.getTelefono());
                    sentencia.setString(4, cliente.getCorreo());
                    sentencia.setString(5, cliente.getDireccion());
                }
                return sentencia.executeUpdate() > 0;
            }
        } finally {
            bd.desconectarBD();
        }
    }

    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setCedula(rs.getString("cli_id"));
        cliente.setNombres(rs.getString("cli_nombres"));
        cliente.setTelefono(rs.getString("cli_telefono"));
        cliente.setCorreo(rs.getString("cli_correo"));
        cliente.setDireccion(rs.getString("cli_direccion"));
        return cliente;
    }
}
