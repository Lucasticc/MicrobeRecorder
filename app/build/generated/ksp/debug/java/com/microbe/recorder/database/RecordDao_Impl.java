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
import java.lang.Integer;
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

  public RecordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRecordEntity = new EntityInsertionAdapter<RecordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `microbe_records` (`id`,`experimentNumber`,`sampleName`,`cultureTime`,`observationResult`,`notes`,`description`,`photoPaths`,`audioPath`,`createdAt`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RecordEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getExperimentNumber());
        statement.bindString(3, entity.getSampleName());
        statement.bindString(4, entity.getCultureTime());
        statement.bindString(5, entity.getObservationResult());
        statement.bindString(6, entity.getNotes());
        statement.bindString(7, entity.getDescription());
        statement.bindString(8, entity.getPhotoPaths());
        statement.bindString(9, entity.getAudioPath());
        statement.bindLong(10, entity.getCreatedAt());
        statement.bindLong(11, entity.getUpdatedAt());
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
        return "UPDATE OR ABORT `microbe_records` SET `id` = ?,`experimentNumber` = ?,`sampleName` = ?,`cultureTime` = ?,`observationResult` = ?,`notes` = ?,`description` = ?,`photoPaths` = ?,`audioPath` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RecordEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getExperimentNumber());
        statement.bindString(3, entity.getSampleName());
        statement.bindString(4, entity.getCultureTime());
        statement.bindString(5, entity.getObservationResult());
        statement.bindString(6, entity.getNotes());
        statement.bindString(7, entity.getDescription());
        statement.bindString(8, entity.getPhotoPaths());
        statement.bindString(9, entity.getAudioPath());
        statement.bindLong(10, entity.getCreatedAt());
        statement.bindLong(11, entity.getUpdatedAt());
        statement.bindLong(12, entity.getId());
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
  public Object getAllRecords(final Continuation<? super List<RecordEntity>> $completion) {
    final String _sql = "SELECT * FROM microbe_records ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RecordEntity>>() {
      @Override
      @NonNull
      public List<RecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExperimentNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "experimentNumber");
          final int _cursorIndexOfSampleName = CursorUtil.getColumnIndexOrThrow(_cursor, "sampleName");
          final int _cursorIndexOfCultureTime = CursorUtil.getColumnIndexOrThrow(_cursor, "cultureTime");
          final int _cursorIndexOfObservationResult = CursorUtil.getColumnIndexOrThrow(_cursor, "observationResult");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfPhotoPaths = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPaths");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<RecordEntity> _result = new ArrayList<RecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecordEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpExperimentNumber;
            _tmpExperimentNumber = _cursor.getString(_cursorIndexOfExperimentNumber);
            final String _tmpSampleName;
            _tmpSampleName = _cursor.getString(_cursorIndexOfSampleName);
            final String _tmpCultureTime;
            _tmpCultureTime = _cursor.getString(_cursorIndexOfCultureTime);
            final String _tmpObservationResult;
            _tmpObservationResult = _cursor.getString(_cursorIndexOfObservationResult);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpPhotoPaths;
            _tmpPhotoPaths = _cursor.getString(_cursorIndexOfPhotoPaths);
            final String _tmpAudioPath;
            _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new RecordEntity(_tmpId,_tmpExperimentNumber,_tmpSampleName,_tmpCultureTime,_tmpObservationResult,_tmpNotes,_tmpDescription,_tmpPhotoPaths,_tmpAudioPath,_tmpCreatedAt,_tmpUpdatedAt);
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
          final int _cursorIndexOfExperimentNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "experimentNumber");
          final int _cursorIndexOfSampleName = CursorUtil.getColumnIndexOrThrow(_cursor, "sampleName");
          final int _cursorIndexOfCultureTime = CursorUtil.getColumnIndexOrThrow(_cursor, "cultureTime");
          final int _cursorIndexOfObservationResult = CursorUtil.getColumnIndexOrThrow(_cursor, "observationResult");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfPhotoPaths = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPaths");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final RecordEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpExperimentNumber;
            _tmpExperimentNumber = _cursor.getString(_cursorIndexOfExperimentNumber);
            final String _tmpSampleName;
            _tmpSampleName = _cursor.getString(_cursorIndexOfSampleName);
            final String _tmpCultureTime;
            _tmpCultureTime = _cursor.getString(_cursorIndexOfCultureTime);
            final String _tmpObservationResult;
            _tmpObservationResult = _cursor.getString(_cursorIndexOfObservationResult);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpPhotoPaths;
            _tmpPhotoPaths = _cursor.getString(_cursorIndexOfPhotoPaths);
            final String _tmpAudioPath;
            _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new RecordEntity(_tmpId,_tmpExperimentNumber,_tmpSampleName,_tmpCultureTime,_tmpObservationResult,_tmpNotes,_tmpDescription,_tmpPhotoPaths,_tmpAudioPath,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object searchRecords(final String keyword,
      final Continuation<? super List<RecordEntity>> $completion) {
    final String _sql = "SELECT * FROM microbe_records WHERE sampleName LIKE '%' || ? || '%' OR experimentNumber LIKE '%' || ? || '%' ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, keyword);
    _argIndex = 2;
    _statement.bindString(_argIndex, keyword);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RecordEntity>>() {
      @Override
      @NonNull
      public List<RecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExperimentNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "experimentNumber");
          final int _cursorIndexOfSampleName = CursorUtil.getColumnIndexOrThrow(_cursor, "sampleName");
          final int _cursorIndexOfCultureTime = CursorUtil.getColumnIndexOrThrow(_cursor, "cultureTime");
          final int _cursorIndexOfObservationResult = CursorUtil.getColumnIndexOrThrow(_cursor, "observationResult");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfPhotoPaths = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPaths");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<RecordEntity> _result = new ArrayList<RecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecordEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpExperimentNumber;
            _tmpExperimentNumber = _cursor.getString(_cursorIndexOfExperimentNumber);
            final String _tmpSampleName;
            _tmpSampleName = _cursor.getString(_cursorIndexOfSampleName);
            final String _tmpCultureTime;
            _tmpCultureTime = _cursor.getString(_cursorIndexOfCultureTime);
            final String _tmpObservationResult;
            _tmpObservationResult = _cursor.getString(_cursorIndexOfObservationResult);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpPhotoPaths;
            _tmpPhotoPaths = _cursor.getString(_cursorIndexOfPhotoPaths);
            final String _tmpAudioPath;
            _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new RecordEntity(_tmpId,_tmpExperimentNumber,_tmpSampleName,_tmpCultureTime,_tmpObservationResult,_tmpNotes,_tmpDescription,_tmpPhotoPaths,_tmpAudioPath,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getRecordCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM microbe_records";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
  public Object getRecordsBySampleName(final String sampleName,
      final Continuation<? super List<RecordEntity>> $completion) {
    final String _sql = "SELECT * FROM microbe_records WHERE sampleName = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sampleName);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RecordEntity>>() {
      @Override
      @NonNull
      public List<RecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExperimentNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "experimentNumber");
          final int _cursorIndexOfSampleName = CursorUtil.getColumnIndexOrThrow(_cursor, "sampleName");
          final int _cursorIndexOfCultureTime = CursorUtil.getColumnIndexOrThrow(_cursor, "cultureTime");
          final int _cursorIndexOfObservationResult = CursorUtil.getColumnIndexOrThrow(_cursor, "observationResult");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfPhotoPaths = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPaths");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<RecordEntity> _result = new ArrayList<RecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecordEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpExperimentNumber;
            _tmpExperimentNumber = _cursor.getString(_cursorIndexOfExperimentNumber);
            final String _tmpSampleName;
            _tmpSampleName = _cursor.getString(_cursorIndexOfSampleName);
            final String _tmpCultureTime;
            _tmpCultureTime = _cursor.getString(_cursorIndexOfCultureTime);
            final String _tmpObservationResult;
            _tmpObservationResult = _cursor.getString(_cursorIndexOfObservationResult);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpPhotoPaths;
            _tmpPhotoPaths = _cursor.getString(_cursorIndexOfPhotoPaths);
            final String _tmpAudioPath;
            _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new RecordEntity(_tmpId,_tmpExperimentNumber,_tmpSampleName,_tmpCultureTime,_tmpObservationResult,_tmpNotes,_tmpDescription,_tmpPhotoPaths,_tmpAudioPath,_tmpCreatedAt,_tmpUpdatedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
