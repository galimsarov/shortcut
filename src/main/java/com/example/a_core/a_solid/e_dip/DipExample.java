package com.example.a_core.a_solid.e_dip;

/**
 * DIP: зависимости направляем на абстракции.
 */
public class DipExample {

    // ❌ Нарушение DIP: высокоуровневый сервис зависит от конкретного класса MySqlDatabase.
    static class MySqlDatabase {
        void save(String data) {
            System.out.println("Save in MySQL: " + data);
        }
    }

    static class BadReportService {
        private final MySqlDatabase database = new MySqlDatabase();

        void createReport(String data) {
            database.save(data);
        }
    }

    interface DataStore {
        void save(String data);
    }

    static class MySqlDataStore implements DataStore {
        @Override
        public void save(String data) {
            System.out.println("Save in MySQL: " + data);
        }
    }

    static class InMemoryDataStore implements DataStore {
        @Override
        public void save(String data) {
            System.out.println("Save in memory: " + data);
        }
    }

    // ✅ DIP: высокоуровневый сервис зависит от интерфейса DataStore.
    static class ReportService {
        private final DataStore dataStore;

        ReportService(DataStore dataStore) {
            this.dataStore = dataStore;
        }

        void createReport(String data) {
            dataStore.save(data);
        }
    }

    public static void demo() {
        ReportService mysqlReportService = new ReportService(new MySqlDataStore());
        mysqlReportService.createReport("mysql-report");

        ReportService inMemoryReportService = new ReportService(new InMemoryDataStore());
        inMemoryReportService.createReport("memory-report");
    }
}
