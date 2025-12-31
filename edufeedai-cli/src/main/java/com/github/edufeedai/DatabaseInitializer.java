/*
 * EduFeedAi is an automated system for retrieving, evaluating, and generating AI-based feedback for student submissions in vocational education.
 *
 * Copyright (C) 2026 Arturo Candela
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.edufeedai;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initializeDatabase(File dbFile) throws SQLException {
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                try (Statement stmt = conn.createStatement()) {
                    // Tabla mínima para entregas
                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS entregas (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, fecha TEXT)");
                }
            }
        }
    }
}
