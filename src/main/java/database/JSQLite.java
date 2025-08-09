package database;

import model.Celda;
import model.Laberinto;
import model.LaberintoInfo;
import model.Partida;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JSQLite {
    private final String url = "jdbc:sqlite:laberintos.db";

    public JSQLite() {
        crearDatabases();
    }

    private void crearDatabases() {
        String sqlPartidas = "CREATE TABLE IF NOT EXISTS partidas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre_jugador TEXT NOT NULL," +
                "modo_juego TEXT NOT NULL," +
                "tamano_laberinto INTEGER NOT NULL," +
                "movimientos INTEGER NOT NULL," +
                "tiempo_restante INTEGER," +
                "fecha TEXT NOT NULL," +
                "completado BOOLEAN NOT NULL)";
        String sqlLaberintos = """
                    CREATE TABLE IF NOT EXISTS laberintos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nombre_jugador TEXT NOT NULL,
                        filas INTEGER NOT NULL,
                        columnas INTEGER NOT NULL,
                        fecha TEXT NOT NULL,
                        racha REAL DEFAULT 0.0,
                        estado TEXT DEFAULT 'PENDIENTE'
                    )
                """;
        String sqlCeldas = """
                    CREATE TABLE IF NOT EXISTS celdas (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        id_laberinto INTEGER NOT NULL,
                        fila INTEGER NOT NULL,
                        columna INTEGER NOT NULL,
                        muroNorte INTEGER NOT NULL,
                        muroSur INTEGER NOT NULL,
                        muroEste INTEGER NOT NULL,
                        muroOeste INTEGER NOT NULL,
                        visitado INTEGER NOT NULL,
                        isBorde INTEGER NOT NULL,
                        pasos INTEGER NOT NULL,
                        FOREIGN KEY (id_laberinto) REFERENCES laberintos(id) ON DELETE CASCADE
                    )
                """;


        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement()) {

            statement.execute(sqlPartidas);
            statement.execute(sqlLaberintos);
            statement.execute(sqlCeldas);

        } catch (SQLException e) {
            System.out.println("Error, hubo un problema al ccrear la base de datos.");
        }
    }

    public void insertarPartida(Partida partida) {
        String sql = "INSERT INTO partidas(nombre_jugador, modo_juego, tamano_laberinto, " +
                "movimientos, tiempo_restante, fecha, completado) VALUES(?,?,?,?,?,?,?)";
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            asignarParametros(preparedStatement, partida);

        } catch (SQLException e) {
            System.err.println("Error al insertar partida: " + e.getMessage());
        }
    }

    private void asignarParametros(PreparedStatement preparedStatement, Partida partida) throws SQLException {
        preparedStatement.setString(1, partida.getJugador().getNombre());
        preparedStatement.setString(2, partida.getModo().toString());
        preparedStatement.setInt(3, partida.getTamanoLaberinto());
        preparedStatement.setInt(4, partida.getMovimientos());
        preparedStatement.setInt(5, partida.getTiempoRestante());
        preparedStatement.setString(6, partida.getFecha().toString());
        preparedStatement.setBoolean(7, partida.isCompletada());
    }

    public int guardarLaberinto(Laberinto laberinto) {
        String sqlLaberinto = "INSERT INTO laberintos(nombre_jugador, filas, columnas, fecha) VALUES (?, ?, ?, datetime('now'))";
        int idLaberinto = -1;

        try (Connection connection = DriverManager.getConnection(url)) {
            connection.setAutoCommit(false);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlLaberinto, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, "nombreJugador");
                preparedStatement.setInt(2, laberinto.getLaberinto().length);
                preparedStatement.setInt(3, laberinto.getLaberinto()[0].length);
                preparedStatement.executeUpdate();

                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) idLaberinto = resultSet.getInt(1);
                }
            }
            String sqlCelda = """
                        INSERT INTO celdas(id_laberinto, fila, columna, muroNorte, muroSur, muroEste, muroOeste,
                                            visitado, isBorde, pasos)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlCelda)) {
                for (Celda[] fila : laberinto.getLaberinto()) {
                    for (Celda cel : fila) {
                        preparedStatement.setInt(1, idLaberinto);
                        preparedStatement.setInt(2, cel.getFila());
                        preparedStatement.setInt(3, cel.getColumna());
                        preparedStatement.setInt(4, cel.isMuroNorte() ? 1 : 0);
                        preparedStatement.setInt(5, cel.isMuroSur() ? 1 : 0);
                        preparedStatement.setInt(6, cel.isMuroEste() ? 1 : 0);
                        preparedStatement.setInt(7, cel.isMuroOeste() ? 1 : 0);
                        preparedStatement.setInt(8, cel.isVisitado() ? 1 : 0);
                        preparedStatement.setInt(9, cel.isIsBorde() ? 1 : 0);
                        preparedStatement.setInt(10, cel.getPasos());
                        preparedStatement.addBatch();
                    }
                }
                preparedStatement.executeBatch();
            }
            connection.commit();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return idLaberinto;
    }

    public Laberinto cargarLaberinto(int id) {
        String sqlLaberinto = "SELECT filas, columnas FROM laberintos WHERE id = ?";
        String sqlCeldas = "SELECT * FROM celdas WHERE id_laberinto = ?";

        Laberinto laberinto = null;
        try (Connection conn = DriverManager.getConnection(url)) {
            // Leer datos del laberinto
            int filas, columnas;
            try (PreparedStatement preparedStatement = conn.prepareStatement(sqlLaberinto)) {
                preparedStatement.setInt(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (!resultSet.next()) return null;
                    filas = resultSet.getInt("filas");
                    columnas = resultSet.getInt("columnas");
                    laberinto = new Laberinto(filas - 2, columnas - 2);
                    laberinto.crearLaberinto();
                }
            }

            // Leer celdas
            try (PreparedStatement preparedStatement = conn.prepareStatement(sqlCeldas)) {
                preparedStatement.setInt(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Celda cel = laberinto.getLaberinto()[resultSet.getInt("fila")][resultSet.getInt("columna")];
                        cel.setMuroNorte(resultSet.getInt("muroNorte") == 1);
                        cel.setMuroSur(resultSet.getInt("muroSur") == 1);
                        cel.setMuroEste(resultSet.getInt("muroEste") == 1);
                        cel.setMuroOeste(resultSet.getInt("muroOeste") == 1);
                        cel.setVisitado(resultSet.getInt("visitado") == 1);
                        cel.setEsBorde(resultSet.getInt("isBorde") == 1);
                        cel.setPasos(resultSet.getInt("pasos"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar laberinto: " + e.getMessage());
        }
        return laberinto;
    }

    public List<LaberintoInfo> listarLaberintosInfo() {
        String sql = "SELECT id, nombre_jugador, fecha, racha, estado FROM laberintos ORDER BY id DESC";
        List<LaberintoInfo> lista = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new LaberintoInfo(
                        rs.getInt("id"),
                        rs.getString("nombre_jugador"),
                        rs.getString("fecha"),
                        rs.getDouble("racha"),
                        rs.getString("estado")

                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar laberintos: " + e.getMessage());
        }
        return lista;
    }

}
