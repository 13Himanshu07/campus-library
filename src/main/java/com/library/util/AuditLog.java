package com.library.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;

/** Best-effort audit recorder; only identifiers and action metadata are stored. */
public final class AuditLog {
    private static final Logger LOG=LoggerFactory.getLogger(AuditLog.class);
    private AuditLog() { }
    public static void insert(Connection c,Long actor,String action,String entity,String entityId,boolean success) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("INSERT INTO audit_logs(actor_user_id,action,entity_type,entity_id,outcome) VALUES(?,?,?,?,?)")) {
            if(actor==null)p.setNull(1,java.sql.Types.BIGINT);else p.setLong(1,actor);
            p.setString(2,action);p.setString(3,entity);p.setString(4,entityId);p.setString(5,success?"SUCCESS":"FAILURE");p.executeUpdate();
        }
    }
    /** Records failed attempts independently because the corresponding business transaction is rolled back. */
    public static void record(Long actor,String action,String entity,String entityId,boolean success) {
        try(var c=DBConnection.getConnection()) {
            insert(c,actor,action,entity,entityId,success);
        } catch(Exception e) { LOG.error("audit_record_failed action={} entity={} exception={}",action,entity,e.getClass().getSimpleName()); }
    }
}
