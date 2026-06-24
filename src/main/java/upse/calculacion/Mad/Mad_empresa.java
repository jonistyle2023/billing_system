package upse.calculacion.Mad;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import upse.calculacion.general.Mod_DB;
import upse.calculacion.modelo.Empresa;

public class Mad_empresa {

    private final Mod_DB bd;

    public Mad_empresa() {
        this(new Mod_DB());
    }

    public Mad_empresa(Mod_DB bd) {
        this.bd = bd;
    }

    public Empresa obtenerEmpresaActiva() throws SQLException {
        String sql = "select top 1 emp_nombre, emp_direccion, emp_telefono, emp_estado "
                + "from dbo.Empresa "
                + "where emp_estado = 'A' or emp_estado is null "
                + "order by emp_nombre";

        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement sentencia = bd.getConexion().prepareStatement(sql);
                ResultSet rs = sentencia.executeQuery()) {
            if (rs.next()) {
                Empresa empresa = new Empresa();
                empresa.setNombre(rs.getString("emp_nombre"));
                empresa.setDireccion(rs.getString("emp_direccion"));
                empresa.setTelefono(rs.getString("emp_telefono"));
                empresa.setEstado(rs.getString("emp_estado"));
                return empresa;
            }
        } finally {
            bd.desconectarBD();
        }
        return null;
    }
}
