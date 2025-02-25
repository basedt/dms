import { openDB } from "idb";
import { DB_STORES } from "@/constants";

const DATABASE_NAME = "dms";
const DATABASE_VERSION = 1;

export const idbAPI = {
  initDB: async () => {
    return openDB(DATABASE_NAME, DATABASE_VERSION, {
      upgrade(db) {
        if (!db.objectStoreNames.contains(DB_STORES.tableMeta)) {
          const tableStore = db.createObjectStore(DB_STORES.tableMeta, {
            keyPath: ["databaseId", "identifier"],
            autoIncrement: false,
          });
          tableStore.createIndex("idx_databaseId", "databaseId", {
            unique: false,
          });
          tableStore.createIndex("idx_tableName", "tableName", {
            unique: false,
          });
        }
      },
    });
  },

  upsertTable: async (table: DMS.TableMetaInfo) => {
    const db = await idbAPI.initDB();
    return db.put(DB_STORES.tableMeta, table);
  },

  deleteTable: async (databaseId: string, identifier: string) => {
    const db = await idbAPI.initDB();
    return db.delete(DB_STORES.tableMeta, [databaseId, identifier]);
  },

  getTablesByName: async (databaseId: string, tableName: string) => {
    const tables = await idbAPI.getTablesByDbId(databaseId);
    return tables.filter((table: DMS.TableMetaInfo) =>
      table.tableName.includes(tableName)
    );
  },

  getTableByIdentifier: async (databaseId: string, identifier: string) => {
    const db = await idbAPI.initDB();
    return db.get(DB_STORES.tableMeta, [databaseId, identifier]);
  },

  getTablesByDbId: async (databaseId: string) => {
    const db = await idbAPI.initDB();
    return db.getAllFromIndex(
      DB_STORES.tableMeta,
      "idx_databaseId",
      databaseId
    );
  },

  deleteTableByDbId: async (databaseId: string) => {
    const tables = await idbAPI.getTablesByDbId(databaseId);
    tables.map((table) =>
      idbAPI.deleteTable(table.databaseId, table.identifier)
    );
  },

  cleanupOldMetadata: async () => {
    const db = await idbAPI.initDB();
    const tx = db.transaction(DB_STORES.tableMeta, "readwrite");
    const store = tx.objectStore(DB_STORES.tableMeta);
    let cursor = await store.openCursor();
    const now = Date.now();
    const thirtyDaysAgo = now - 30 * 24 * 60 * 60 * 1000;
    while (cursor) {
      if (new Date(cursor.value.createAt).getTime() < thirtyDaysAgo) {
        cursor.delete();
      }
      cursor = await cursor.continue();
    }
    return tx.done;
  },
};
