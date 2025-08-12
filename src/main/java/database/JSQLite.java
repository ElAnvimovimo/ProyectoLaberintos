package database;

import model.Laberinto;
import model.LaberintoInfo;
import model.ModoJuego;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JSQLite {

    private final String url = "jdbc:sqlite:laberintos.db";

    public JSQLite() {
        crearDatabases();
    }
    private void crearDatabases() {
        String sqlPartidasGuardadas = """
                CREATE TABLE IF NOT EXISTS partidas_guardadas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre_jugador TEXT NOT NULL,
                    laberinto_json TEXT,
                    size_laberinto INTEGER NOT NULL,
                    movimientos INTEGER NOT NULL,
                    jugador_fila INTEGER NOT NULL,
                    jugador_columna INTEGER NOT NULL,
                    meta_row INTEGER NOT NULL,
                    meta_col INTEGER NOT NULL,
                    modo_juego TEXT,
                    fecha TEXT NOT NULL,
                    racha REAL DEFAULT 0.0,
                    estado TEXT DEFAULT 'EN_CURSO',
                    tiempo_restante INTEGER NOT NULL,
                    juegos_terminados INTEGER NOT NULL
                )
                """;

        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement()) {

            statement.execute(sqlPartidasGuardadas);

        } catch (SQLException e) {
            System.err.println("Error al crear la base de datos: " + e.getMessage());
        }
    }

    public int guardarPartidaJSON(LaberintoInfo laberintoInfo) {
        String sql = """
                    INSERT INTO partidas_guardadas(
                        nombre_jugador, laberinto_json, size_laberinto, movimientos,
                        jugador_fila, jugador_columna, meta_row, meta_col,
                        modo_juego, fecha, racha, estado, tiempo_restante, juegos_terminados
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'), ?, ?, ?, ?)
                """;
        int nuevoId = -1;

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, laberintoInfo.getNombreJugador());
            ps.setString(2, laberintoInfo.getLaberintoJson());
            ps.setInt(3, laberintoInfo.getSizeLaberinto());
            ps.setInt(4, laberintoInfo.getMovimientos());
            ps.setInt(5, laberintoInfo.getRowJugador());
            ps.setInt(6, laberintoInfo.getColJugador());
            ps.setInt(7, laberintoInfo.getRowMeta());
            ps.setInt(8, laberintoInfo.getColMeta());
            if (laberintoInfo.getModoJuego() != null) {
                ps.setString(9, laberintoInfo.getModoJuego().toString());
            } else {
                ps.setString(9, "");
            }
            ps.setDouble(10, laberintoInfo.getRacha());
            ps.setString(11, laberintoInfo.getEstado());
            ps.setInt(12, laberintoInfo.getTiempoRestante());
            ps.setInt(13, laberintoInfo.getJuegosTerminados());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                nuevoId = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al guardar partida JSON: " + e.getMessage());
        }
        return nuevoId;
    }

    public List<LaberintoInfo> listarPartidasGuardadas() {
        String sql = "SELECT * FROM partidas_guardadas ORDER BY id DESC";
        List<LaberintoInfo> lista = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                String modoJuegoString = rs.getString("modo_juego");
                ModoJuego modoJuegoEnum = null;

                try {
                    if (modoJuegoString != null && !modoJuegoString.isBlank()) {
                        modoJuegoEnum = ModoJuego.valueOf(modoJuegoString);
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Valor de modo_juego no válido: " + modoJuegoString);
                }
                lista.add(new LaberintoInfo(
                        rs.getInt("id"),
                        rs.getString("nombre_jugador"),
                        rs.getString("fecha"),
                        rs.getDouble("racha"),
                        rs.getString("estado"),
                        rs.getString("laberinto_json"),
                        rs.getInt("size_laberinto"),
                        rs.getInt("movimientos"),
                        rs.getInt("jugador_fila"),
                        rs.getInt("jugador_columna"),
                        rs.getInt("meta_row"),
                        rs.getInt("meta_col"),
                        modoJuegoEnum,
                        rs.getInt("tiempo_restante"),
                        rs.getInt("juegos_terminados")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar partidas guardadas: " + e.getMessage());
        }
        return lista;
    }

    public Laberinto cargarLaberintoDesdeJSON(String json) {
        return LaberintoSerializer.fromJson(json);
    }

    public String convertirLaberintoAJson(Laberinto laberinto) {
        return LaberintoSerializer.toJson(laberinto);
    }

    public int guardarOActualizarPartida(LaberintoInfo laberintoInfo) {
        if (laberintoInfo.getId() > 0) {
            actualizarPartida(laberintoInfo);
            return laberintoInfo.getId();
        } else {
            return guardarPartidaJSON(laberintoInfo);
        }
    }

    public boolean actualizarPartida(LaberintoInfo laberintoInfo) {
        String sql = """
                    UPDATE partidas_guardadas
                    SET
                        laberinto_json = ?,
                        size_laberinto = ?,
                        movimientos = ?,
                        jugador_fila = ?,
                        jugador_columna = ?,
                        meta_row = ?,
                        meta_col = ?,
                        modo_juego = ?,
                        fecha = datetime('now'),
                        racha = ?,
                        estado = ?,
                        tiempo_restante = ?,
                        juegos_terminados = ?
                    WHERE id = ?
                """;
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, laberintoInfo.getLaberintoJson());
            ps.setInt(2, laberintoInfo.getSizeLaberinto());
            ps.setInt(3, laberintoInfo.getMovimientos());
            ps.setInt(4, laberintoInfo.getRowJugador());
            ps.setInt(5, laberintoInfo.getColJugador());
            ps.setInt(6, laberintoInfo.getRowMeta());
            ps.setInt(7, laberintoInfo.getColMeta());
            if (laberintoInfo.getModoJuego() != null) {
                ps.setString(9, laberintoInfo.getModoJuego().toString());
            } else {
                ps.setString(9, "");
            }
            ps.setDouble(9, laberintoInfo.getRacha());
            ps.setString(10, laberintoInfo.getEstado());
            ps.setInt(11, laberintoInfo.getTiempoRestante());
            ps.setInt(12, laberintoInfo.getId());
            ps.setInt(13, laberintoInfo.getJuegosTerminados());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error:" + e.getMessage());
            return false;
        }
    }
}
