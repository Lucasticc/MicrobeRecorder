package com.microbe.recorder.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile RecordDao _recordDao;

  private volatile SampleTypeDao _sampleTypeDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `microbe_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `experimentNumber` TEXT NOT NULL, `sampleName` TEXT NOT NULL, `cultureTime` TEXT NOT NULL, `observationResult` TEXT NOT NULL, `notes` TEXT NOT NULL, `description` TEXT NOT NULL, `photoPaths` TEXT NOT NULL, `audioPath` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sample_types` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `category` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '448ce6f4bdf4f2056960e69626a07ff0')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `microbe_records`");
        db.execSQL("DROP TABLE IF EXISTS `sample_types`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsMicrobeRecords = new HashMap<String, TableInfo.Column>(11);
        _columnsMicrobeRecords.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMicrobeRecords.put("experimentNumber", new TableInfo.Column("experimentNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMicrobeRecords.put("sampleName", new TableInfo.Column("sampleName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMicrobeRecords.put("cultureTime", new TableInfo.Column("cultureTime", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMicrobeRecords.put("observationResult", new TableInfo.Column("observationResult", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMicrobeRecords.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMicrobeRecords.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMicrobeRecords.put("photoPaths", new TableInfo.Column("photoPaths", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMicrobeRecords.put("audioPath", new TableInfo.Column("audioPath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMicrobeRecords.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMicrobeRecords.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMicrobeRecords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMicrobeRecords = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMicrobeRecords = new TableInfo("microbe_records", _columnsMicrobeRecords, _foreignKeysMicrobeRecords, _indicesMicrobeRecords);
        final TableInfo _existingMicrobeRecords = TableInfo.read(db, "microbe_records");
        if (!_infoMicrobeRecords.equals(_existingMicrobeRecords)) {
          return new RoomOpenHelper.ValidationResult(false, "microbe_records(com.microbe.recorder.database.RecordEntity).\n"
                  + " Expected:\n" + _infoMicrobeRecords + "\n"
                  + " Found:\n" + _existingMicrobeRecords);
        }
        final HashMap<String, TableInfo.Column> _columnsSampleTypes = new HashMap<String, TableInfo.Column>(4);
        _columnsSampleTypes.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSampleTypes.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSampleTypes.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSampleTypes.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSampleTypes = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSampleTypes = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSampleTypes = new TableInfo("sample_types", _columnsSampleTypes, _foreignKeysSampleTypes, _indicesSampleTypes);
        final TableInfo _existingSampleTypes = TableInfo.read(db, "sample_types");
        if (!_infoSampleTypes.equals(_existingSampleTypes)) {
          return new RoomOpenHelper.ValidationResult(false, "sample_types(com.microbe.recorder.database.SampleTypeEntity).\n"
                  + " Expected:\n" + _infoSampleTypes + "\n"
                  + " Found:\n" + _existingSampleTypes);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "448ce6f4bdf4f2056960e69626a07ff0", "0fd38b6e15f86e520203bf6016cfce9e");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "microbe_records","sample_types");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `microbe_records`");
      _db.execSQL("DELETE FROM `sample_types`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(RecordDao.class, RecordDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SampleTypeDao.class, SampleTypeDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public RecordDao recordDao() {
    if (_recordDao != null) {
      return _recordDao;
    } else {
      synchronized(this) {
        if(_recordDao == null) {
          _recordDao = new RecordDao_Impl(this);
        }
        return _recordDao;
      }
    }
  }

  @Override
  public SampleTypeDao sampleTypeDao() {
    if (_sampleTypeDao != null) {
      return _sampleTypeDao;
    } else {
      synchronized(this) {
        if(_sampleTypeDao == null) {
          _sampleTypeDao = new SampleTypeDao_Impl(this);
        }
        return _sampleTypeDao;
      }
    }
  }
}
