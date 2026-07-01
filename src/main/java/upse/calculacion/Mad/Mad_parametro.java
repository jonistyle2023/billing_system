package upse.calculacion.Mad;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import upse.calculacion.general.Mod_DB;

public class Mad_parametro {

    private final Mod_DB bd;

    public Mad_parametro() {
        this(new Mod_DB());
    }

    public Mad_parametro(Mod_DB bd) {
        this.bd = bd;
    }

    /**
     * Busca un parámetro activo por su clave en dbo.ParametroGeneral.
     * Si no existe o la conexión falla, devuelve valorPorDefecto.
     */
    public float obtenerValorNumerico(String clave, float valorPorDefecto) {
        String sql = "select valornumerico from dbo.ParametroGeneral "
                + "where clave = ? and estado = 1";

        if (!bd.conectarBD()) {
            return valorPorDefecto;
        }

        try (PreparedStatement sentencia = bd.getConexion().prepareStatement(sql)) {
            sentencia.setString(1, clave);
            try (ResultSet rs = sentencia.executeQuery()) {
                if (rs.next()) {
                    return rs.getFloat("valornumerico");
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR al leer el parámetro '" + clave + "': " + e.getMessage());
        } finally {
            bd.desconectarBD();
        }
        return valorPorDefecto;
    }
}
