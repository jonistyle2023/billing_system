/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package upse.calculacion.general;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.util.function.Function;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Jaime
 */
public class Mod_DB {

    private Connection conexion;
    private Statement sentenciaSQL;
    private ResultSet resulSet;

    public Mod_DB() {
    }

    public Connection getConexion() {
        return conexion;
    }

    public void setConexion(Connection conexion) {
        this.conexion = conexion;
    }

    public Statement getSentenciaSQL() {
        return sentenciaSQL;
    }

    public void setSentenciaSQL(Statement sentenciaSQL) {
        this.sentenciaSQL = sentenciaSQL;
    }

    public ResultSet getResulSet() {
        return resulSet;
    }

    public void setResulSet(ResultSet resulSet) {
        this.resulSet = resulSet;
    }

    //función para conectar con la base de datos
    public boolean conectarBD() {
        String servidor, basedatos, usuario, clave, classNombre, cadenaConexion;
        if (Mod_general.gestorBD == 1) {// se conecta con mysql
            servidor = Mod_configuracion.get("db.mariadb.servidor", "localhost");
            basedatos = Mod_configuracion.get("db.mariadb.baseDatos", "base20261");
            usuario = Mod_configuracion.get("db.mariadb.usuario", "root");
            clave = Mod_configuracion.get("db.mariadb.clave", "");
            Mod_general.str_nombreBD = "Maria DB";
            //cadena de conección
            classNombre = "org.mariadb.jdbc.Driver";
            cadenaConexion = "jdbc:mariadb://"
                    + servidor + ":3306/"
                    + basedatos + "?"
                    + "user=" + usuario
                    + "&" + "password=" + clave;
        } else {
            //conecta con SQL Server
            servidor = Mod_configuracion.get("db.sqlserver.servidor", "localhost");
            basedatos = Mod_configuracion.get("db.sqlserver.baseDatos", "BD2026_1");
            usuario = Mod_configuracion.get("db.sqlserver.usuario", "sa");
            clave = Mod_configuracion.get("db.sqlserver.clave", "Admin.");
            Mod_general.str_nombreBD = "SQL Server";
            classNombre = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            cadenaConexion = "jdbc:sqlserver://"
                    + servidor + ":1433;"
                    + "databaseName=" + basedatos + ";"
                    + "user=" + usuario + ";"
                    + "password=" + clave + ";"
                    + "encrypt=false;trustServerCertificate=true;loginTimeout=30;";
        }
        try {
            Class.forName(classNombre);
            Connection conexion = DriverManager.getConnection(cadenaConexion);
            this.setConexion(conexion);
            return true;
        } catch (Exception e) {
            Mod_log.error("Error de conexión a la base de datos (" + Mod_general.str_nombreBD + ")", e);
            return false;
        }
    }

    public void desconectarBD() {
        try {
            this.conexion.close();
        } catch (Exception e) {
            Mod_log.warning("Error al desconectar de la base de datos: " + e.getMessage());
        }
    }

    public void iniciarTransaccion() {
        try {
            this.conexion.setAutoCommit(false);
        } catch (Exception e) {
            Mod_log.warning("Error al iniciar transacción: " + e.getMessage());
        }
    }

    public void commit() {
        try {
            this.conexion.commit();
        } catch (Exception e) {
            Mod_log.warning("Error al hacer commit: " + e.getMessage());
        }
    }

    public void rollback() {
        try {
            this.conexion.rollback();
        } catch (Exception e) {
            Mod_log.warning("Error al hacer rollback: " + e.getMessage());
        }
    }

    //crear una función que me permita ejecutar sentencias sql
    // select
    public void ejecutarConsultaSql(String cadenaSQL) {
        try {
            this.sentenciaSQL = this.conexion.createStatement();
            this.resulSet = this.sentenciaSQL.executeQuery(cadenaSQL);
        } catch (Exception e) {
            Mod_log.error("Error al ejecutar consulta SQL: " + cadenaSQL, e);
        }

    }

    //crear una función para Insertar - Update - Delete
    public int ejecutarSQL(String cadenaSQL) {
        int filas = 0;
        try {
            this.sentenciaSQL = this.conexion.createStatement();
            filas = this.sentenciaSQL.executeUpdate(cadenaSQL);
            this.commit();
        } catch (Exception e) {
            Mod_log.error("Error al ejecutar SQL: " + cadenaSQL, e);
        }
        return filas;
    }

    public <T> ObservableList<T> getListaConsulta(String cadenaSQL, Function<ResultSet, T> mapper) {
        ObservableList<T> obsListAux = FXCollections.observableArrayList();
        try {
            conectarBD(); // conectar a la BD
            ejecutarConsultaSql(cadenaSQL); // ejecutar la consulta
            ResultSet rs = getResulSet(); // obtener el resultado
            while (rs.next()) {
                T obj = mapper.apply(rs); // mapear la fila
                obsListAux.add(obj);
            }
            rs.close();
        } catch (Exception e) {
            Mod_log.error("Error en getListaConsulta: " + cadenaSQL, e);
        } finally {
            desconectarBD();
        }
        return obsListAux;
    }

    public boolean fun_ejecutar(String cadenaSQL) {
        try {
            this.conectarBD();
            int filas = this.ejecutarSQL(cadenaSQL);

            if (filas > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        } finally {
            this.desconectarBD();
        }
    }
}

