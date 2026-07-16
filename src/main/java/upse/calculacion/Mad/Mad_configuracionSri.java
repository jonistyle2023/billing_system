package upse.calculacion.Mad;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import upse.calculacion.general.Mod_DB;
import upse.calculacion.modelo.ConfiguracionSri;

public class Mad_configuracionSri {

    private final Mod_DB bd;

    public Mad_configuracionSri() {
        this(new Mod_DB());
    }

    public Mad_configuracionSri(Mod_DB bd) {
        this.bd = bd;
    }

    public ConfiguracionSri obtener() throws SQLException {
        String sql = "select ambiente, establecimiento, puntoEmision, tipoEmision "
                + "from dbo.ConfiguracionSri where id = 1";

        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement sentencia = bd.getConexion().prepareStatement(sql);
                ResultSet rs = sentencia.executeQuery()) {
            if (rs.next()) {
                ConfiguracionSri config = new ConfiguracionSri();
                config.setAmbiente(rs.getString("ambiente"));
                config.setEstablecimiento(rs.getString("establecimiento"));
                config.setPuntoEmision(rs.getString("puntoEmision"));
                config.setTipoEmision(rs.getString("tipoEmision"));
                return config;
            }
        } finally {
            bd.desconectarBD();
        }
        throw new IllegalStateException(
                "No existe configuración SRI (dbo.ConfiguracionSri, id = 1). Revise databaseScript.sql.");
    }
}
