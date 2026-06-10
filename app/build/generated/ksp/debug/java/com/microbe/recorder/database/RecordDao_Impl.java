package com.microbe.recorder.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class RecordDao_Impl implements RecordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RecordEntity> __insertionAdapterOfRecordEntity;

  private final EntityDeletionOrUpdateAdapter<RecordEntity> __deletionAdapterOfRecordEntity;

  private final EntityDeletionOrUpdateAdapter<RecordEntity> __updateAdapterOfRecordEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfUpdateTreatmentGroup;

  public RecordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRecordEntity = new EntityInsertionAdapter<RecordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `microbe_records` (`id`,`plantingDate`,`treatmentGroup`,`observationResult`,`notes`,`photoPaths`,`audioPath`,`createdAt`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RecordEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getPlantingDate());
        statement.bindString(3, entity.getTreatmentGroup());
        statement.bindString(4, entity.getObservationResult());
        statement.bindString(5, entity.getNotes());
        statement.bindString(6, entity.getPhotoPaths());
        statement.bindString(7, entity.getAudioPath());
        statement.bindLong(8, entity.getCreatedAt());
        statement.bindLong(9, entity.getUpdatedAt());
      }
    };
    this.__deletionAdapterOfRecordEntity = new EntityDeletionOrUpdateAdapter<RecordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `microbe_records` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RecordEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfRecordEntity = new EntityDeletionOrUpdateAdapter<RecordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `microbe_records` SET `id` = ?,`plantingDate` = ?,`treatmentGroup` = ?,`observationResult` = ?,`notes` = ?,`photoPaths` = ?,`audioPath` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RecordEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getPlantingDate());
        statement.bindString(3, entity.getTreatmentGroup());
        statement.bindString(4, entity.getObservationResult());
        statement.bindString(5, entity.getNotes());
        statement.bindString(6, entity.getPhotoPaths());
        statement.bindString(7, entity.getAudioPath());
        statement.bindLong(8, entity.getCreatedAt());
        statement.bindLong(9, entity.getUpdatedAt());
        statement.bindLong(10, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM microbe_records WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateTreatmentGroup = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE microbe_records SET treatmentGroup = ? WHERE treatmentGroup = ? AND plantingDate = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final RecordEntity record, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfRecordEntity.insertAndReturnId(record);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final RecordEntity record, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfRecordEntity.handle(record);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final RecordEntity record, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfRecordEntity.handle(record);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTreatmentGroup(final String oldName, final String newName,
      final String plantingDate, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateTreatmentGroup.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, newName);
        _argIndex = 2;
        _stmt.bindString(_argIndex, oldName);
        _argIndex = 3;
        _stmt.bindString(_argIndex, plantingDate);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateTreatmentGroup.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllRecords(final Continuation<? super List<RecordEntity>> $completion) {
    final String _sql = "SELECT * FROM microbe_records ORDER BY createdAt DESC, id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RecordEntity>>() {
      @Override
      @NonNull
      public List<RecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPlantingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "plantingDate");
          final int _cursorIndexOfTreatmentGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "treatmentGroup");
          final int _cursorIndexOfObservationResult = CursorUtil.getColumnIndexOrThrow(_cursor, "observationResult");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfPhotoPaths = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPaths");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<RecordEntity> _result = new ArrayList<RecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecordEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPlantingDate;
            _tmpPlantingDate = _cursor.getString(_cursorIndexOfPlantingDate);
            final String _tmpTreatmentGroup;
            _tmpTreatmentGroup = _cursor.getString(_cursorIndexOfTreatmentGroup);
            final String _tmpObservationResult;
            _tmpObservationResult = _cursor.getString(_cursorIndexOfObservationResult);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpPhotoPaths;
            _tmpPhotoPaths = _cursor.getString(_cursorIndexOfPhotoPaths);
            final String _tmpAudioPath;
            _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new RecordEntity(_tmpId,_tmpPlantingDate,_tmpTreatmentGroup,_tmpObservationResult,_tmpNotes,_tmpPhotoPaths,_tmpAudioPath,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecordById(final long id, final Continuation<? super RecordEntity> $completion) {
    final String _sql = "SELECT * FROM microbe_records WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RecordEntity>() {
      @Override
      @Nullable
      public RecordEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPlantingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "plantingDate");
          final int _cursorIndexOfTreatmentGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "treatmentGroup");
          final int _cursorIndexOfObservationResult = CursorUtil.getColumnIndexOrThrow(_cursor, "observationResult");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfPhotoPaths = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPaths");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final RecordEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPlantingDate;
            _tmpPlantingDate = _cursor.getString(_cursorIndexOfPlantingDate);
            final String _tmpTreatmentGroup;
            _tmpTreatmentGroup = _cursor.getString(_cursorIndexOfTreatmentGroup);
            final String _tmpObservationResult;
            _tmpObservationResult = _cursor.getString(_cursorIndexOfObservationResult);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpPhotoPaths;
            _tmpPhotoPaths = _cursor.getString(_cursorIndexOfPhotoPaths);
            final String _tmpAudioPath;
            _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new RecordEntity(_tmpId,_tmpPlantingDate,_tmpTreatmentGroup,_tmpObservationResult,_tmpNotes,_tmpPhotoPaths,_tmpAudioPath,_tmpCreatedAt,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecordsByPlantingDate(final String plantingDate,
      final Continuation<? super List<RecordEntity>> $completion) {
    final String _sql = "SELECT * FROM microbe_records WHERE plantingDate = ? ORDER BY createdAt DESC, id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, plantingDate);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RecordEntity>>() {
      @Override
      @NonNull
      public List<RecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPlantingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "plantingDate");
          final int _cursorIndexOfTreatmentGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "treatmentGroup");
          final int _cursorIndexOfObservationResult = CursorUtil.getColumnIndexOrThrow(_cursor, "observationResult");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfPhotoPaths = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPaths");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<RecordEntity> _result = new ArrayList<RecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecordEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPlantingDate;
            _tmpPlantingDate = _cursor.getString(_cursorIndexOfPlantingDate);
            final String _tmpTreatmentGroup;
            _tmpTreatmentGroup = _cursor.getString(_cursorIndexOfTreatmentGroup);
            final String _tmpObservationResult;
            _tmpObservationResult = _cursor.getString(_cursorIndexOfObservationResult);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpPhotoPaths;
            _tmpPhotoPaths = _cursor.getString(_cursorIndexOfPhotoPaths);
            final String _tmpAudioPath;
            _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new RecordEntity(_tmpId,_tmpPlantingDate,_tmpTreatmentGroup,_tmpObservationResult,_tmpNotes,_tmpPhotoPaths,_tmpAudioPath,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecordsByTreatmentGroup(final String group,
      final Continuation<? super List<RecordEntity>> $completion) {
    final String _sql = "SELECT * FROM microbe_records WHERE treatmentGroup = ? ORDER BY createdAt DESC, id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, group);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RecordEntity>>() {
      @Override
      @NonNull
      public List<RecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPlantingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "plantingDate");
          final int _cursorIndexOfTreatmentGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "treatmentGroup");
          final int _cursorIndexOfObservationResult = CursorUtil.getColumnIndexOrThrow(_cursor, "observationResult");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfPhotoPaths = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPaths");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<RecordEntity> _result = new ArrayList<RecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecordEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPlantingDate;
            _tmpPlantingDate = _cursor.getString(_cursorIndexOfPlantingDate);
            final String _tmpTreatmentGroup;
            _tmpTreatmentGroup = _cursor.getString(_cursorIndexOfTreatmentGroup);
            final String _tmpObservationResult;
            _tmpObservationResult = _cursor.getString(_cursorIndexOfObservationResult);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpPhotoPaths;
            _tmpPhotoPaths = _cursor.getString(_cursorIndexOfPhotoPaths);
            final String _tmpAudioPath;
            _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new RecordEntity(_tmpId,_tmpPlantingDate,_tmpTreatmentGroup,_tmpObservationResult,_tmpNotes,_tmpPhotoPaths,_tmpAudioPath,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecordsByPlantingAndGroup(final String plantingDate, final String group,
      final Continuation<? super List<RecordEntity>> $completion) {
    final String _sql = "SELECT * FROM microbe_records WHERE plantingDate = ? AND treatmentGroup = ? ORDER BY createdAt DESC, id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, plantingDate);
    _argIndex = 2;
    _statement.bindString(_argIndex, group);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RecordEntity>>() {
      @Override
      @NonNull
      public List<RecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPlantingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "plantingDate");
          final int _cursorIndexOfTreatmentGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "treatmentGroup");
          final int _cursorIndexOfObservationResult = CursorUtil.getColumnIndexOrThrow(_cursor, "observationResult");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfPhotoPaths = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPaths");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<RecordEntity> _result = new ArrayList<RecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecordEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPlantingDate;
            _tmpPlantingDate = _cursor.getString(_cursorIndexOfPlantingDate);
            final String _tmpTreatmentGroup;
            _tmpTreatmentGroup = _cursor.getString(_cursorIndexOfTreatmentGroup);
            final String _tmpObservationResult;
            _tmpObservationResult = _cursor.getString(_cursorIndexOfObservationResult);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpPhotoPaths;
            _tmpPhotoPaths = _cursor.getString(_cursorIndexOfPhotoPaths);
            final String _tmpAudioPath;
            _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new RecordEntity(_tmpId,_tmpPlantingDate,_tmpTreatmentGroup,_tmpObservationResult,_tmpNotes,_tmpPhotoPaths,_tmpAudioPath,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllPlantingDates(final Continuation<? super List<String>> $completion) {
    final String _sql = "SELECT DISTINCT plantingDate FROM microbe_records ORDER BY plantingDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<String>>() {
      @Override
      @NonNull
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item;
            _item = _cursor.getString(0);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllTreatmentGroups(final Continuation<? super List<String>> $completion) {
    final String _sql = "SELECT DISTINCT treatmentGroup FROM microbe_records ORDER BY treatmentGroup ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<String>>() {
      @Override
      @NonNull
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item;
            _item = _cursor.getString(0);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getLatestByPlantingDate(final String plantingDate,
      final Continuation<? super RecordEntity> $completion) {
    final String _sql = "SELECT * FROM microbe_records WHERE plantingDate = ? ORDER BY createdAt DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, plantingDate);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RecordEntity>() {
      @Override
      @Nullable
      public RecordEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPlantingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "plantingDate");
          final int _cursorIndexOfTreatmentGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "treatmentGroup");
          final int _cursorIndexOfObservationResult = CursorUtil.getColumnIndexOrThrow(_cursor, "observationResult");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfPhotoPaths = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPaths");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final RecordEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPlantingDate;
            _tmpPlantingDate = _cursor.getString(_cursorIndexOfPlantingDate);
            final String _tmpTreatmentGroup;
            _tmpTreatmentGroup = _cursor.getString(_cursorIndexOfTreatmentGroup);
            final String _tmpObservationResult;
            _tmpObservationResult = _cursor.getString(_cursorIndexOfObservationResult);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpPhotoPaths;
            _tmpPhotoPaths = _cursor.getString(_cursorIndexOfPhotoPaths);
            final String _tmpAudioPath;
            _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new RecordEntity(_tmpId,_tmpPlantingDate,_tmpTreatmentGroup,_tmpObservationResult,_tmpNotes,_tmpPhotoPaths,_tmpAudioPath,_tmpCreatedAt,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getLatestByTreatmentGroupBefore(final String group, final long beforeTime,
      final Continuation<? super RecordEntity> $completion) {
    final String _sql = "SELECT * FROM microbe_records WHERE treatmentGroup = ? AND createdAt < ? ORDER BY createdAt DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, group);
    _argIndex = 2;
    _statement.bindLong(_argIndex, beforeTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RecordEntity>() {
      @Override
      @Nullable
      public RecordEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPlantingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "plantingDate");
          final int _cursorIndexOfTreatmentGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "treatmentGroup");
          final int _cursorIndexOfObservationResult = CursorUtil.getColumnIndexOrThrow(_cursor, "observationResult");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfPhotoPaths = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPaths");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final RecordEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPlantingDate;
            _tmpPlantingDate = _cursor.getString(_cursorIndexOfPlantingDate);
            final String _tmpTreatmentGroup;
            _tmpTreatmentGroup = _cursor.getString(_cursorIndexOfTreatmentGroup);
            final String _tmpObservationResult;
            _tmpObservationResult = _cursor.getString(_cursorIndexOfObservationResult);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpPhotoPaths;
            _tmpPhotoPaths = _cursor.getString(_cursorIndexOfPhotoPaths);
            final String _tmpAudioPath;
            _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new RecordEntity(_tmpId,_tmpPlantingDate,_tmpTreatmentGroup,_tmpObservationResult,_tmpNotes,_tmpPhotoPaths,_tmpAudioPath,_tmpCreatedAt,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getLatestByPlantingAndGroupBefore(final String plantingDate, final String group,
      final long beforeTime, final long excludeId,
      final Continuation<? super RecordEntity> $completion) {
    final String _sql = "SELECT * FROM microbe_records WHERE plantingDate = ? AND treatmentGroup = ? AND createdAt < ? AND id != ? ORDER BY createdAt DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 4);
    int _argIndex = 1;
    _statement.bindString(_argIndex, plantingDate);
    _argIndex = 2;
    _statement.bindString(_argIndex, group);
    _argIndex = 3;
    _statement.bindLong(_argIndex, beforeTime);
    _argIndex = 4;
    _statement.bindLong(_argIndex, excludeId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RecordEntity>() {
      @Override
      @Nullable
      public RecordEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPlantingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "plantingDate");
          final int _cursorIndexOfTreatmentGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "treatmentGroup");
          final int _cursorIndexOfObservationResult = CursorUtil.getColumnIndexOrThrow(_cursor, "observationResult");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfPhotoPaths = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPaths");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final RecordEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPlantingDate;
            _tmpPlantingDate = _cursor.getString(_cursorIndexOfPlantingDate);
            final String _tmpTreatmentGroup;
            _tmpTreatmentGroup = _cursor.getString(_cursorIndexOfTreatmentGroup);
            final String _tmpObservationResult;
            _tmpObservationResult = _cursor.getString(_cursorIndexOfObservationResult);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpPhotoPaths;
            _tmpPhotoPaths = _cursor.getString(_cursorIndexOfPhotoPaths);
            final String _tmpAudioPath;
            _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new RecordEntity(_tmpId,_tmpPlantingDate,_tmpTreatmentGroup,_tmpObservationResult,_tmpNotes,_tmpPhotoPaths,_tmpAudioPath,_tmpCreatedAt,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
