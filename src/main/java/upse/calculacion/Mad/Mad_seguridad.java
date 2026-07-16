package upse.calculacion.Mad;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import upse.calculacion.general.Mod_DB;
import upse.calculacion.general.Mod_hash;
import upse.calculacion.general.Mod_log;
import upse.calculacion.modelo.Usuario;

public class Mad_seguridad {

    private final Mod_DB bd;

    public Mad_seguridad() {
        this(new Mod_DB());
    }

    public Mad_seguridad(Mod_DB bd) {
        this.bd = bd;
    }

    /**
     * Valida usuario/clave. La clave se guarda hasheada (PBKDF2), pero si el registro
     * todavía está en texto plano (datos semilla o BD sin migrar) se compara tal cual
     * y, si coincide, se re-guarda hasheada de inmediato — migración perezosa, sin
     * necesitar una herramienta de migración aparte.
     */
    public Usuario login(String usuario, String clave) throws SQLException {
        String sql = "select usr_id, per_id, usr_nombres, usr_usuario, usr_clave, usr_estado "
                + "from dbo.Usuario "
                + "where usr_usuario = ? "
                + "and usr_estado = 'A'";

        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement sentencia = bd.getConexion().prepareStatement(sql)) {
            sentencia.setString(1, usuario);
            try (ResultSet rs = sentencia.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                String valorAlmacenado = rs.getString("usr_clave");
                boolean claveValida = Mod_hash.esFormatoHash(valorAlmacenado)
                        ? Mod_hash.verificar(clave, valorAlmacenado)
                        : clave.equals(valorAlmacenado);

                if (!claveValida) {
                    return null;
                }

                int usrId = rs.getInt("usr_id");
                if (!Mod_hash.esFormatoHash(valorAlmacenado)) {
                    migrarAHash(usrId, clave);
                }

                Usuario usu = new Usuario();
                usu.setId(usrId);
                usu.setPer_id(rs.getInt("per_id"));
                usu.setUsuario(rs.getString("usr_usuario"));
                usu.setClave(valorAlmacenado);
                usu.setNombres(rs.getString("usr_nombres"));
                usu.setEstado(rs.getString("usr_estado"));
                return usu;
            }
        } finally {
            bd.desconectarBD();
        }
    }

    private void migrarAHash(int usrId, String claveEnTextoPlano) {
        String sql = "update dbo.Usuario set usr_clave = ? where usr_id = ?";
        try (PreparedStatement sentencia = bd.getConexion().prepareStatement(sql)) {
            sentencia.setString(1, Mod_hash.hashear(claveEnTextoPlano));
            sentencia.setInt(2, usrId);
            sentencia.executeUpdate();
        } catch (SQLException e) {
            Mod_log.warning("No se pudo migrar la contraseña del usuario " + usrId + " a formato hash: " + e.getMessage());
        }
    }
}
