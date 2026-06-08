package upse.calculacion.Mad;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import upse.calculacion.general.Mod_DB;
import upse.calculacion.modelo.Usuario;

public class Mad_seguridad {

    private final Mod_DB bd;

    public Mad_seguridad() {
        this(new Mod_DB());
    }

    public Mad_seguridad(Mod_DB bd) {
        this.bd = bd;
    }

    public Usuario login(String usuario, String clave) throws SQLException {
        Usuario usu = null;
        String sql = "select usr_id, per_id, usr_usuario, usr_clave, usr_nombres, usr_estado "
                + "from usuarios "
                + "where usr_usuario = ? "
                + "and usr_clave = ? "
                + "and usr_estado = 'A'";

        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement sentencia = bd.getConexion().prepareStatement(sql)) {
            sentencia.setString(1, usuario);
            sentencia.setString(2, clave);
            try (ResultSet rs = sentencia.executeQuery()) {
                if (rs.next()) {
                    usu = new Usuario();
                    usu.setId(rs.getInt("usr_ide"));
                    usu.setPer_id(rs.getInt("per_id"));
                    usu.setUsuario(rs.getString("usr_usuario"));
                    usu.setClave(rs.getString("usr_clave"));
                    usu.setNombres(rs.getString("usr_nombres"));
                    usu.setEstado(rs.getString("usr_estado"));
                }
            }
        } finally {
            bd.desconectarBD();
        }

        return usu;
    }
}
